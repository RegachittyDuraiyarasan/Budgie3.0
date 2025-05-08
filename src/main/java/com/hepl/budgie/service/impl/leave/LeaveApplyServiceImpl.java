package com.hepl.budgie.service.impl.leave;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.leave.CheckDate;
import com.hepl.budgie.dto.leave.CompOffDto;
import com.hepl.budgie.dto.leave.EmployeeDTO;
import com.hepl.budgie.dto.leave.HolidayApplyDto;
import com.hepl.budgie.dto.leave.LeaveApplyBalanceDTO;
import com.hepl.budgie.dto.leave.LeaveApplyDTO;
import com.hepl.budgie.dto.leave.LeaveEmpDetailsResponseDTO;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.LeaveCategory;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.attendancemanagement.AttendanceData;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInfo;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInformationHepl;
import com.hepl.budgie.entity.attendancemanagement.AttendancePunchInformation;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;
import com.hepl.budgie.entity.attendancemanagement.WeekEnd;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leave.LeaveApplyDates;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.entity.settings.Holiday;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.leavemanagement.LeaveMapper;
import com.hepl.budgie.repository.attendancemanagement.AttendancePunchInformationRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceWeekendPolicyRepository;
import com.hepl.budgie.repository.leave.LeaveApplyRepo;
import com.hepl.budgie.repository.leavemanagement.LeaveTypeCategoryRepository;
import com.hepl.budgie.repository.master.HolidayRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.HolidaysService;
import com.hepl.budgie.service.leave.LeaveApplyService1;
import com.hepl.budgie.service.leavemanagement.LeaveMasterService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.DateDifference;
import com.hepl.budgie.utils.DateTimeFormatting;
import com.hepl.budgie.utils.IdGenerator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class LeaveApplyServiceImpl implements LeaveApplyService1 {

	private final UserInfoRepository userInfoRepository;
	private final PayrollLockMonthRepository payrollLockMonthRepository;
	private final LeaveTypeCategoryRepository leaveTypeCategoryRepository;
	private final LeaveMasterService leaveMasterService;
	private final LeaveApplyRepo leaveApplyRepository;
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;
	private final HolidayRepository holidayRepository;
	private final HolidaysService holidaysService;
	private final FileService fileService;
	private final JWTHelper jwtHelper;
	private final IdGenerator idGenerator;
	private final AttendanceWeekendPolicyRepository attendanceWeekendPolicyRepository;
	private final LeaveMapper leaveApplyMapper;
	private final AttendancePunchInformationRepository punchRepo;

	@Override
	public LeaveEmpDetailsResponseDTO fetchEmployeeDetails(String id) {
		log.info("fetching Employee details for : {}" + id);

		UserInfo userInfo = userInfoRepository.findByEmpId(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		String contactNo = userInfo.getSections().getContact().getPrimaryContactNumber();

		UserInfo reportingManager = fetchUserByEmpId(
				userInfo.getSections().getHrInformation().getPrimary().getManagerId());
		UserInfo reviewer = fetchUserByEmpId(userInfo.getSections().getHrInformation().getReviewer().getManagerId());

		List<EmployeeDTO> applyingToList = prepareApplyingToList(reportingManager, reviewer);

		List<UserInfo> activeEmpList = userInfoRepository.findByStatus(Status.ACTIVE.label);

		List<EmployeeDTO> activeEmpList1 = activeEmpList.stream().map(this::buildEmployeeDTO)
				.collect(Collectors.toList());

		PayrollLockMonth lockDate = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate,
				jwtHelper.getOrganizationCode(), "IN");

		return buildLeaveEmpDetailsResponse(contactNo, lockDate.getAttendanceEmpLockDate(), lockDate.getFinYear(),
				applyingToList, activeEmpList1);
	}

	private UserInfo fetchUserByEmpId(String empId) {
		return userInfoRepository.findByEmpId(empId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND));
	}

	private List<EmployeeDTO> prepareApplyingToList(UserInfo reportingManager, UserInfo reviewer) {
		EmployeeDTO rmDto = buildEmployeeDTO(reportingManager);
		EmployeeDTO reviewerDto = buildEmployeeDTO(reviewer);

		return List.of(rmDto, reviewerDto).stream().distinct()
				.sorted(Comparator.comparing(dto -> dto.getEmpId().equals(reportingManager.getEmpId()) ? -1 : 1))
				.collect(Collectors.toList());
	}

	private EmployeeDTO buildEmployeeDTO(UserInfo userInfo) {
		EmployeeDTO dto = new EmployeeDTO();
		dto.setEmpId(userInfo.getEmpId());
		dto.setName(userInfo.getSections().getBasicDetails().getFirstName() + " "
				+ userInfo.getSections().getBasicDetails().getLastName());
		dto.setFirstName(userInfo.getSections().getBasicDetails().getFirstName());
		dto.setDepartment(userInfo.getSections().getWorkingInformation().getDepartment());
		dto.setDesignation(userInfo.getSections().getWorkingInformation().getDesignation());
		dto.setWorkLocation(userInfo.getSections().getWorkingInformation().getWorkLocation());
		dto.setDateOfJoining(userInfo.getSections().getWorkingInformation().getDoj().toString());
		return dto;
	}

	private LeaveEmpDetailsResponseDTO buildLeaveEmpDetailsResponse(String contactNo, String lockDates, String year,
			List<EmployeeDTO> applyingToList, List<EmployeeDTO> activeEmpList) {
		LeaveEmpDetailsResponseDTO response = new LeaveEmpDetailsResponseDTO();
		if (year.contains("-")) {
			String[] yearParts = year.split("-");
			response.setStartYear(yearParts[0]);
			response.setEndYear(yearParts[1]);
		}
		    // Process lockDate logic
			int lockDay = Integer.parseInt(lockDates);
			LocalDate today = LocalDate.now();
			int currentDay = today.getDayOfMonth();
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

			LocalDate lockStart = LocalDate.MIN; 
			if (currentDay <= lockDay) {
				LocalDate empLockEnd = LocalDate.of(today.getYear(), today.getMonth(), lockDay);
				response.setEmpLockEndDate(empLockEnd.format(formatter));				
				lockStart = LocalDate.of(today.getYear(), today.getMonth().minus(1), lockDay);
				response.setEmpLockStartDate(lockStart.format(formatter));
			} else {
				LocalDate empLockStart = LocalDate.of(today.getYear(), today.getMonth(), 
                Math.min(lockDay, today.lengthOfMonth()));
				LocalDate nextMonth = today.plusMonths(1);
				LocalDate empLockEnd = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(),
						Math.min(lockDay, nextMonth.lengthOfMonth()));

				response.setEmpLockStartDate(empLockStart.format(formatter));
				response.setEmpLockEndDate(empLockEnd.format(formatter));			
			}
		response.setContactNo(contactNo);
		response.setApplyingTo(applyingToList);
		response.setCc(activeEmpList);
		return response;
	}

	@Override
	public List<String> fetchLeaveType(String empId, String type) {

		try {
			if (type.equalsIgnoreCase("Reporting Manager")) {
				String managerEmpId = empId;
				UserInfo userInfo = userInfoRepository.findByEmpId(empId).orElseThrow(
						() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

				String teamLead = userInfo.getSections().getHrInformation().getPrimary().getManagerId();
				String manager = userInfo.getSections().getHrInformation().getReviewer().getManagerId();

				if (!teamLead.equals(managerEmpId) || manager.equals(managerEmpId)) {
					throw new RuntimeException(empId + " is not under your supervision");
				}
			} else if (!type.equalsIgnoreCase("Admin") && !type.equalsIgnoreCase("Employee")) {
				empId = empId;
			}
			UserInfo userInfo = fetchUserByEmpId(empId);

			String leaveScheme = userInfo.getSections().getHrInformation().getLeaveScheme();
			List<LeaveTypeCategory> leaveCategory = leaveTypeCategoryRepository.findBySchemeName(leaveScheme,
					jwtHelper.getOrganizationCode(), mongoTemplate);

			return leaveCategory.stream().map(LeaveTypeCategory::getLeaveTypeName).collect(Collectors.toList());
		} catch (Exception e) {
			throw new RuntimeException("Error While Fetching leavetype " + e.getMessage());
		}
	}

	@Override
	public LeaveApplyBalanceDTO fetchEmployeeLeaveBalance(String empId, String type, String leaveType) {

		if (!type.equals("Reporting Manager") && !type.equalsIgnoreCase("Admin")) {
			empId = jwtHelper.getUserRefDetail().getEmpId();
		}

		List<String> leaveTypeList = fetchLeaveType(empId, type);
		if (!leaveTypeList.contains(leaveType)) {
			throw new RuntimeException(leaveType + " is not applicable for " + empId);
		}

		LeaveTypeCategory leaveTypeCategory = leaveTypeCategoryRepository.findByLeaveTypeName(leaveType,
				jwtHelper.getOrganizationCode(), mongoTemplate);

		LeaveApplyBalanceDTO leaveBalance = objectMapper.convertValue(leaveTypeCategory, LeaveApplyBalanceDTO.class);
		if (leaveBalance.getBalanceCheck().equals(YesOrNoEnum.YES.label)) {
			String year = String.valueOf(Year.now().getValue());
			double balance = leaveMasterService.employeeLeaveBalance(empId, year, leaveType);
			List<LeaveApply> leaveApplies = leaveApplyRepository.findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatus(empId,
					leaveType, LeaveCategory.LEAVE_APPLY.label, Status.PENDING.label, jwtHelper.getOrganizationCode(), mongoTemplate);
			double numOfDays = leaveApplies.stream().mapToDouble(LeaveApply::getNumOfDays).sum();
			double toSend = balance - numOfDays;
			leaveBalance.setLeaveBalance(toSend);

		}
		return leaveBalance;
	}

	@Override
	public CheckDate checkLeaveApplyDate(String empId, String fromDate, String toDate, String fromSession,
			String toSession) {
		LocalDate startDate = LocalDate.parse(fromDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalDate endDate = LocalDate.parse(toDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		UserInfo userInfo = fetchUserByEmpId(empId);
		String weekOff = userInfo.getSections().getHrInformation().getWeekOff();
		String workingLocation = userInfo.getSections().getWorkingInformation().getWorkLocation();
		String state = userInfo.getSections().getContact().getPresentAddressDetails().getPresentState();
		try {

			Map<String, Boolean> leaveAppliedDates = fromToDateCheckLeave(startDate, endDate, fromSession, toSession,
					empId);

			Map<String, Boolean> checkIsWeekOff = checkIsWeekOff(weekOff, startDate, endDate, fromSession, toSession);

			Map<String, Boolean> holidayDates = holidayToCheck(startDate, endDate, fromSession, toSession, empId,
					workingLocation, state);

			CheckDate resultMap = new CheckDate();
			resultMap.setIsAlreadyApplyDates(leaveAppliedDates);
			resultMap.setHolidayDates(holidayDates);
			resultMap.setWeekEndsDates(checkIsWeekOff);
			return resultMap;

		} catch (Exception e) {
			throw new RuntimeException("Error checking leave apply dates: " + e.getMessage(), e);
		}
	}

	private Map<String, Boolean> checkIsWeekOff(String weekOff, LocalDate startDate, LocalDate endDate,
			String fromSession, String toSession) {
		Map<String, Boolean> weekOffDates = new LinkedHashMap<>();
		long numOfDays = DateDifference.calculateDayDifference(startDate, endDate) + 1;

		boolean usePolicyDB = weekOff.equalsIgnoreCase("Saturday-Alternative Full Day")
				|| weekOff.equals("Saturday and Sunday (alternative full day)");

		final AttendanceWeekendPolicy weekendPolicy;
		if (usePolicyDB) {
			String monthYear = DateTimeFormatting.formatDate(startDate, "MM-yyyy");
			weekendPolicy = attendanceWeekendPolicyRepository.findByMonthYear(monthYear,
					jwtHelper.getOrganizationCode(), mongoTemplate);
		} else {
			weekendPolicy = null;
		}

		Stream.iterate(startDate, date -> date.plusDays(1)).limit(numOfDays).forEach(date -> {
			String formattedDate = DateTimeFormatting.formatDate(date, "yyyy-MM-dd");
			DayOfWeek day = date.getDayOfWeek();
			boolean isWeekOff = false;

			switch (weekOff) {
			case "Saturday-Full OFF Day":
			case "Saturday and Sunday (Full OFF Day)":
				if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
					isWeekOff = true;
				}
				break;

			case "Saturday-Full Day":
				if (day == DayOfWeek.SATURDAY) {
					isWeekOff = false;
				} else if (day == DayOfWeek.SUNDAY) {
					isWeekOff = true;
				}
				break;

			case "Saturday and Sunday (alternative full day)":
				if (weekendPolicy != null && day == DayOfWeek.SATURDAY) {
					isWeekOff = isWeekOffDay(date, weekendPolicy);
				}
				break;

			case "Saturday-Alternative Full Day":
				if (day == DayOfWeek.SUNDAY) {
					isWeekOff = true;
				} else if (weekendPolicy != null && day == DayOfWeek.SATURDAY) {
					isWeekOff = isWeekOffDay(date, weekendPolicy);
				}
				break;
			}

			weekOffDates.put(formattedDate, isWeekOff);

		});

		return weekOffDates;
	}

	private boolean isWeekOffDay(LocalDate date, AttendanceWeekendPolicy weekendPolicy) {
		if (weekendPolicy == null || weekendPolicy.getWeek() == null) {
			return false;
		}

		for (WeekEnd weekEnd : weekendPolicy.getWeek()) {
			if (weekEnd.getSatDate() != null && weekEnd.getSatDate().equals(date.toString())) {
				return "off".equalsIgnoreCase(weekEnd.getSatStatus());
			}
			if (weekEnd.getSunDate() != null && weekEnd.getSunDate().equals(date.toString())) {
				return "off".equalsIgnoreCase(weekEnd.getSunStatus());
			}
		}

		return false;
	}

	private Map<String, Boolean> fromToDateCheckLeave(LocalDate fromDate, LocalDate toDate, String fromSession,
			String toSession, String empId) {
		long numOfDays = DateDifference.calculateDayDifference(fromDate, toDate) + 1;

		return Stream.iterate(fromDate, date -> date.plusDays(1)).limit(numOfDays).sorted().map(date -> {
			String fromDateSession;
			String toDateSession;

			if (fromDate.isEqual(toDate)) {
				fromDateSession = fromSession;
				toDateSession = toSession;
			} else {
				fromDateSession = determineFromSession(date, fromDate, toDate, fromSession, toSession);
				toDateSession = determineToSession(date, fromDate, toDate, fromSession, toSession);
			}

			String convertedDate = DateTimeFormatting.formatDate(date, "yyyy-MM-dd");
			boolean isLeaveApplied = isLeaveAppliedForDate(empId, convertedDate, fromDateSession, toDateSession);

			return Map.entry(convertedDate, isLeaveApplied);
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
				LinkedHashMap::new));
	}

	private boolean isLeaveAppliedForDate(String empId, String date, String fromSession, String toSession) {
		List<LeaveApply> leaveApplies = leaveApplyRepository.findByEmpIdAndLeaveApplyDate(empId, date , jwtHelper.getOrganizationCode(), mongoTemplate);

		return leaveApplies.stream().flatMap(leaveApply -> leaveApply.getLeaveApply().stream()).anyMatch(
				leaveApplyDateDetails -> matchesSessionAndStatus(leaveApplyDateDetails, fromSession, toSession));
	}

	private boolean matchesSessionAndStatus(LeaveApplyDates leaveApplyDateDetails, String fromSession,
			String toSession) {
		return (leaveApplyDateDetails.getFromSession().equals(fromSession)
				|| leaveApplyDateDetails.getToSession().equals(toSession))
				&& (leaveApplyDateDetails.getStatus().equals(Status.PENDING.label)
						|| leaveApplyDateDetails.getStatus().equals(Status.APPROVED.label));
	}

	private Map<String, Boolean> holidayToCheck(LocalDate fromDate, LocalDate toDate, String fromSession,
			String toSession, String empId, String workingLocation, String state) {
		long numOfDays = DateDifference.calculateDayDifference(fromDate, toDate) + 1;

		return Stream.iterate(fromDate, date -> date.plusDays(1)).limit(numOfDays).sorted().map(date -> {
			String convertedDate = DateTimeFormatting.formatDate(date, "yyyy-MM-dd");
			boolean isHoliday = isHoliday(convertedDate, workingLocation, state);
			return Map.entry(convertedDate, isHoliday);
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
				LinkedHashMap::new));
	}

	private boolean isHoliday(String date, String workingLocation, String state) {
		LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
		Optional<Holiday> holidays = holidaysService.findHolidayByDate(localDate, jwtHelper.getOrganizationCode());

		if (holidays.isPresent()) {
			Holiday holiday = holidays.get();
			if (holiday.isAllState() && holiday.isAllLocation()) {
				return true;
			}

			boolean isStateMatch = holiday.isAllState() || holiday.getStateList().contains(state.toLowerCase());

			boolean isLocationMatch = holiday.isAllLocation()
					|| holiday.getLocationList().contains(workingLocation.toLowerCase());

			return isStateMatch && isLocationMatch;
		}
		return false;
	}

	private String determineFromSession(LocalDate date, LocalDate fromDate, LocalDate toDate, String fromSession,
			String toSession) {
		if (date.isEqual(fromDate)) {
			return fromSession;
		} else if (date.isEqual(toDate)) {
			return toSession.equals("Session 2") ? "Session 1" : toSession;
		} else {
			return "Session 1";
		}
	}

	private String determineToSession(LocalDate date, LocalDate fromDate, LocalDate toDate, String fromSession,
			String toSession) {
		if (date.isEqual(fromDate)) {
			return fromSession.equals("Session 1") ? "Session 2" : fromSession;
		} else if (date.isEqual(toDate)) {
			return toSession;
		} else {
			return "Session 2";
		}
	}

	@Override
	public Object leaveApply(@Valid LeaveApplyDTO data) {
		LeaveApply leaveApply = createAndValidateLeaveApply(data);
		leaveApplyRepository.saveLeaveApply(leaveApply, jwtHelper.getOrganizationCode(), mongoTemplate);
		return leaveApply;
	}

	private LeaveApply createAndValidateLeaveApply(LeaveApplyDTO data) {
		LeaveApply leaveApply = buildLeaveApplyFromDTO(data);
		List<LocalDate> leaveDates = validateLeaveApplication(data, leaveApply);
		processLeaveDetails(data, leaveApply, leaveDates);
		processFileUpload(data, leaveApply);
		return leaveApply;
	}

	private LeaveApply buildLeaveApplyFromDTO(LeaveApplyDTO data) {
		LeaveApply leaveApply = new LeaveApply();
		leaveApply.setLeaveCode(idGenerator.generateLeaveCode(jwtHelper.getOrganizationCode()));
		leaveApply.setEmpId(data.getEmpId());
		leaveApply.setAppliedTo(data.getAppliedTo());
		leaveApply.setLeaveType(data.getLeaveType());
		leaveApply.setLeaveCategory(LeaveCategory.LEAVE_APPLY.label);
		leaveApply.setEmpReason(data.getReason());
		leaveApply.setContactNo(data.getContactNo());
		leaveApply.setAppliedCC(data.getAppliedCC());
		leaveApply.setFromSession(data.getFromSession());
		leaveApply.setToSession(data.getToSession());
		leaveApply.setStatus(Status.PENDING.label);
		leaveApply.setLeaveCancel(YesOrNoEnum.NO.label);
		return leaveApply;
	}


	private List<LocalDate> validateLeaveApplication(LeaveApplyDTO data, LeaveApply leaveApply) {

		UserInfo userInfo = fetchUserByEmpId(data.getEmpId());
		String leaveScheme = userInfo.getSections().getHrInformation().getLeaveScheme();
		LeaveTypeCategory leaveTypeCategory = leaveTypeCategoryRepository.findByLeaveSchemeAndLeaveTypeName(leaveScheme,
				data.getLeaveType(), jwtHelper.getOrganizationCode(), mongoTemplate);
		if (leaveTypeCategory == null) {
			throw new RuntimeException(data.getEmpId() + " is not eligible for " + data.getLeaveType());
		}
		List<LocalDate> leaveDates = validateLeaveDateRange(data);
		validateLeaveApplicationCount(data, leaveTypeCategory);
		return leaveDates;
	}

	private List<LocalDate> validateLeaveDateRange(LeaveApplyDTO data) {
		CheckDate dateCheck = checkLeaveApplyDate(data.getEmpId(), data.getFromDate().toString(),
				data.getToDate().toString(), data.getFromSession(), data.getToSession());

		Map<String, Boolean> alreadyAppliedDates = dateCheck.getIsAlreadyApplyDates();
		String alreadyAppliedDate = findFirstTrueDate(alreadyAppliedDates);
		if (alreadyAppliedDate != null) {
			throw new RuntimeException("Leave already applied for the date: " + alreadyAppliedDate);
		}

		List<LocalDate> validLeaveDates = filterValidLeaveDates(data.getFromDate(), data.getToDate(),
				dateCheck.getHolidayDates(), dateCheck.getWeekEndsDates());

		if (validLeaveDates == null) {
			throw new RuntimeException("Leave Applied Dates Are Not Working Days ");
		}
		return validLeaveDates;
	}

	private String findFirstTrueDate(Map<String, Boolean> dateMap) {
		if (dateMap == null) {
			return null;
		}
		for (Map.Entry<String, Boolean> entry : dateMap.entrySet()) {
			if (entry.getValue()) {
				return entry.getKey();
			}
		}
		return null;
	}

	private List<LocalDate> filterValidLeaveDates(LocalDate fromDate, LocalDate toDate,
			Map<String, Boolean> holidayDates, Map<String, Boolean> weekEndsDates) {
		List<LocalDate> validDates = new ArrayList<>();

		long numOfDays = DateDifference.calculateDayDifference(fromDate, toDate) + 1;

		Stream.iterate(fromDate, date -> date.plusDays(1)).limit(numOfDays).forEach(date -> {
			String formattedDate = DateTimeFormatting.formatDate(date, "yyyy-MM-dd");

			boolean isHoliday = holidayDates != null && holidayDates.getOrDefault(formattedDate, false);
			boolean isWeekend = weekEndsDates != null && weekEndsDates.getOrDefault(formattedDate, false);

			if (!isHoliday && !isWeekend) {
				validDates.add(date);
			}
		});

		return validDates;
	}

	private void validateLeaveApplicationCount(LeaveApplyDTO data, LeaveTypeCategory leaveTypeCategory) {
		LocalDate startDate = data.getFromDate().with(TemporalAdjusters.firstDayOfMonth());
		LocalDate endDate = data.getToDate().with(TemporalAdjusters.lastDayOfMonth());
		int leaveApplicationCount = leaveApplyRepository.countByEmpIdAndLeaveTypeAndDateListBetween(data.getEmpId(),
				data.getLeaveType(), startDate.toString(), endDate.toString(), jwtHelper.getOrganizationCode(), mongoTemplate);

		int maxAvailed = Integer.parseInt(leaveTypeCategory.getMaxAvailedLimit());
		if (leaveApplicationCount >= maxAvailed) {
			throw new RuntimeException(
					String.format("Exceeds maximum allowed limit. %s cannot be applied more than %d days in a month.",
							data.getLeaveType(), maxAvailed));
		}
	}

	private void processLeaveDetails(LeaveApplyDTO data, LeaveApply leaveApply, List<LocalDate> leaveDates) {
		List<LeaveApplyDates> dateDetails = leaveApplyDateDetails(data, leaveDates, data.getFromSession(),
				data.getToSession());

		leaveApply.setLeaveApply(dateDetails);
		List<String> dateListString = leaveDates.stream().map(LocalDate::toString).collect(Collectors.toList());
		leaveApply.setDateList(dateListString);
		double numOfDays = calculateTotalLeaveDays(dateDetails);

		LeaveApplyBalanceDTO balance = fetchEmployeeLeaveBalance(data.getEmpId(), data.getType(), data.getLeaveType());
		validateLeaveBalance(data, numOfDays, balance);

		leaveApply.setNumOfDays(numOfDays);
		leaveApply.setBalance((balance.getLeaveBalance() - numOfDays));

		leaveApply.setFromDate(data.getFromDate().toString());
		leaveApply.setToDate(data.getToDate().toString());
	}

	private double calculateTotalLeaveDays(List<LeaveApplyDates> dateDetails) {
		return dateDetails.stream().mapToDouble(LeaveApplyDates::getCount).sum();
	}

	private void validateLeaveBalance(LeaveApplyDTO data, double numOfDays, LeaveApplyBalanceDTO balance) {

		if (numOfDays > Double.parseDouble(balance.getMaxAvailedLimit())) {
			throw new RuntimeException(
					data.getLeaveType() + " cannot be taken more than " + balance.getMaxAvailedLimit() + " days.");
		}
		if (numOfDays < Double.parseDouble(balance.getMinAvailedLimit())) {
			throw new RuntimeException(
					data.getLeaveType() + " must be taken for at least " + balance.getMinAvailedLimit() + " days.");
		}
		if (balance.getBalanceCheck().equals("Yes") && numOfDays > balance.getLeaveBalance()) {
			throw new RuntimeException("Insufficient leave balance. Available balance: " + balance.getLeaveBalance());
		}
	}

	private void processFileUpload(LeaveApplyDTO data, LeaveApply leaveApply) {
		if (data.getFile() == null || data.getFile().isEmpty()) {
			if (data.getLeaveType().equals("Sick Leave") && leaveApply.getNumOfDays() > 2) {
				throw new RuntimeException("File upload is required for Sick Leave exceeding 2 days.");
			}
			return;
		}
		List<String> fileNames = data.getFile().stream().filter(file -> !file.isEmpty()).map(file -> {
			String fileExtension = getFileExtension(file.getOriginalFilename());
			if (!isValidFileType(fileExtension)) {
				throw new RuntimeException("Unsupported file type. Only JPG, JPEG, PNG, and PDF files are allowed.");
			}
			try {
				return fileService.uploadFile(file, FileType.LEAVE_APPLY, fileExtension);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return fileExtension;
		}).collect(Collectors.toList());

		leaveApply.setFileNames(fileNames);
	}

	public List<LeaveApplyDates> leaveApplyDateDetails(LeaveApplyDTO data, List<LocalDate> leaveDates,
			String fromSession, String toSession) {
		return leaveDates.stream().map(date -> {
			String fromDateSession;
			String toDateSession;

			if (leaveDates.size() == 1) {
				fromDateSession = fromSession;
				toDateSession = toSession;
			} else if (date.equals(leaveDates.get(0))) {
				fromDateSession = fromSession;
				toDateSession = "Session 2";
			} else if (date.equals(leaveDates.get(leaveDates.size() - 1))) {
				fromDateSession = "Session 1";
				toDateSession = toSession;
			} else {
				fromDateSession = "Session 1";
				toDateSession = "Session 2";
			}
			String leaveType = fromDateSession.equals(toDateSession) ? "Half Day" : "Full Day";
			double count = fromDateSession.equals(toDateSession) ? 0.5 : 1;

			LeaveApplyDates details = new LeaveApplyDates();
			details.setDate(DateTimeFormatting.formatDate(date, "yyyy-MM-dd"));
			details.setFromSession(fromDateSession);
			details.setToSession(toDateSession);
			details.setLeaveType(leaveType);
			details.setCount((Double) count);
			details.setStatus("Pending");
			details.setIsHalfDay(leaveType.equals("Half Day"));
			details.setApproverId(data.getAppliedTo());
			return details;
		}).collect(Collectors.toList());
	}

	private String getFileExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
	}

	private boolean isValidFileType(String fileExtension) {
		return Set.of("jpg", "jpeg", "png", "pdf").contains(fileExtension);
	}

	@Override
	public String applyRestictedHolidays(List<HolidayApplyDto> applyHoliday) {

		String empId = jwtHelper.getUserRefDetail().getEmpId();
		String org = jwtHelper.getOrganizationCode();
		String collection = leaveApplyRepository.getCollectionName(org);
		Optional<UserInfo> user = userInfoRepository.findByEmpId(empId);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND);
		}
		for (HolidayApplyDto holiday : applyHoliday) {
			String holidayCol = holidayRepository.getCollectionName(org);
			Holiday holiday1 = mongoTemplate.findById(holiday.getHolidayId(), Holiday.class, holidayCol);
			if (holiday1 == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday not found");
			}
			LeaveApply leaveApply = new LeaveApply();
			String leaveCode = idGenerator.generateLeaveCode(jwtHelper.getOrganizationCode());
			leaveApply.setLeaveCode(leaveCode);
			leaveApply.setEmpId(empId);
			leaveApply.setAppliedTo(holiday.getAppliedTo());
			leaveApply.setLeaveType("RestrictedHoliday");
			leaveApply.setLeaveCategory("RestrictedHoliday");
			leaveApply.setEmpReason(holiday.getReason());
			leaveApply.setContactNo(user.get().getSections().getContact().getPrimaryContactNumber());
			List<String> ccList = new ArrayList<>();
			if (holiday.getCc() != null) {
				ccList.addAll(holiday.getCc());
				leaveApply.setAppliedCC(ccList);
			}
			leaveApply.setNumOfDays(1);
			leaveApply.setFromSession("Session 1");
			leaveApply.setToSession("Session 2");
			leaveApply.setStatus("Pending");
			leaveApply.setLeaveCancel("No");
			leaveApply.setFromDate(holiday1.getDate().toString());
			leaveApply.setToDate(holiday1.getDate().toString());
			mongoTemplate.save(leaveApply, collection);
			Holiday holiday2 = mongoTemplate.findById(holiday.getHolidayId(), Holiday.class, holidayCol);
			holiday2.setRestricted(true);
			mongoTemplate.save(holiday2, holidayCol);
		}

		return "Success";
	}

	@Override
	public List<Holiday> getRestictedHolidaysList() {

		String org = jwtHelper.getOrganizationCode();
		String collection = holidayRepository.getCollectionName(org);
		PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, org, "IN");

		if (payrollLock == null) {
			return new ArrayList<>();
		}

		PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
			.filter(PayrollLockMonth.PayrollMonths::getLockMonth)
			.findFirst()
			.orElse(null);

		if (payrollMonth == null) {
			return new ArrayList<>();
		}

		LocalDate startDate = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		Criteria criteria = new Criteria();
		criteria.and("restrictedHoliday").is("yes");
		criteria.and("isRestricted").is(false);
		criteria.and("date").gte(startDate).lte(endDate);

		Query query = new Query(criteria);

		return mongoTemplate.find(query, Holiday.class, collection);
	}

	@Override
	public void applyCompOff(CompOffDto comp) {

		String empId = jwtHelper.getUserRefDetail().getEmpId();
		String collection = leaveApplyRepository.getCollectionName(jwtHelper.getOrganizationCode());
		Optional<UserInfo> user = userInfoRepository.findByEmpId(empId);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND);
		}
		LeaveApplyDates leaveDate = leaveApplyMapper.toLeaveApplyDate(comp);
        List<LeaveApplyDates> datesList = Collections.singletonList(leaveDate);

        LeaveApply leave = leaveApplyMapper.toLeaveApply(comp, empId, jwtHelper.getOrganizationCode(), user.get().getSections().getContact().getPrimaryContactNumber(), datesList);
        leave.setEmpId(empId); 
		leave.setLeaveCode(idGenerator.generateLeaveCode(jwtHelper.getOrganizationCode()));

        mongoTemplate.save(leave, collection);

	}

	@Override
	public List<Map<String, Object>> workedDate() {

		String empId = jwtHelper.getUserRefDetail().getEmpId();
		String org = jwtHelper.getOrganizationCode();
		LocalDate today = LocalDate.now();

		LocalDate firstDateOfPrev2Month = today.minusMonths(1).withDayOfMonth(1);
		LocalDate lastDate = today.minusDays(1); // yesterday

		List<String> monthsToCheck = Arrays.asList(
			firstDateOfPrev2Month.format(DateTimeFormatter.ofPattern("MM-yyyy")),
			lastDate.format(DateTimeFormatter.ofPattern("MM-yyyy"))
		);

		Set<LocalDate> weekendDates = new HashSet<>();
		for (String monthYear : monthsToCheck) {
			AttendanceWeekendPolicy policy = attendanceWeekendPolicyRepository.findByMonthYear(monthYear, org, mongoTemplate);
			if (policy != null && policy.getWeek() != null) {
				for (WeekEnd week : policy.getWeek()) {
					if ("OFF".equalsIgnoreCase(week.getSatStatus())) {
						weekendDates.add(LocalDate.parse(week.getSatDate()));
					}
					if ("OFF".equalsIgnoreCase(week.getSunStatus())) {
						weekendDates.add(LocalDate.parse(week.getSunDate()));
					}
				}
			}
		}

		List<Holiday> holidays = holidayRepository.findByDateBetween(
			org, firstDateOfPrev2Month, lastDate, mongoTemplate
		);
		Set<LocalDate> holidayDates = holidays.stream()
			.map(Holiday::getDate)
			.collect(Collectors.toSet());

		List<Map<String, Object>> workedOnOffDays = new ArrayList<>();
		Set<LocalDate> workedDates = new HashSet<>();

		for (String monthYear : monthsToCheck) {
			
			YearMonth yearMonth = YearMonth.parse(monthYear, DateTimeFormatter.ofPattern("MM-yyyy"));
			String month = yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
			AttendancePunchInformation attendanceRecord = punchRepo.findByEmpIdAndMonthYear(empId, month, org, mongoTemplate);

			if (attendanceRecord != null && attendanceRecord.getAttendanceData() != null) {
				for (AttendanceData info : attendanceRecord.getAttendanceData()) {
					LocalDate date = LocalDate.parse(info.getDate());

					if ((date.isEqual(firstDateOfPrev2Month) || date.isAfter(firstDateOfPrev2Month)) &&
						(date.isEqual(lastDate) || date.isBefore(lastDate)) &&
						(weekendDates.contains(date) || holidayDates.contains(date)) &&
						info.getPunchIn() != null && !info.getPunchIn().isEmpty()) {

						workedDates.add(date);

						Map<String, Object> data = new HashMap<>();
						data.put("date", date.toString());
						data.put("inTime", info.getPunchIn());
						data.put("outTime", info.getPunchOut());
						data.put("type", weekendDates.contains(date) ? "Weekend" : "Holiday");
						workedOnOffDays.add(data);
					}
				}
			}
		}

		List<LeaveApply> compOffLeaves = leaveApplyRepository.findByEmpIdAndLeaveType(empId, "Comp Off",Status.REJECTED.getLabel(), org, mongoTemplate);
		Set<LocalDate> appliedCompOffDates = new HashSet<>();

		for (LeaveApply leave : compOffLeaves) {
			if (leave.getFromDate() != null && !leave.getFromDate().isEmpty()) {
				appliedCompOffDates.add(LocalDate.parse(leave.getFromDate()));
			}
		}

		List<Map<String, Object>> finalResult = workedOnOffDays.stream()
			.filter(entry -> !appliedCompOffDates.contains(LocalDate.parse(entry.get("date").toString())))
			.collect(Collectors.toList());

		return finalResult;
	}



}
