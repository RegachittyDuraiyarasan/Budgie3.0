package com.hepl.budgie.service.impl.leavemanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LeaveGranterTableDTO;
import com.hepl.budgie.entity.LeaveCategory;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.leavemanagement.Details;
import com.hepl.budgie.entity.leavemanagement.LeaveBalanceSummary;
import com.hepl.budgie.entity.leavemanagement.LeaveGranter;
import com.hepl.budgie.entity.leavemanagement.LeaveMaster;
import com.hepl.budgie.entity.leavemanagement.LeaveScheme;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.hepl.budgie.entity.userinfo.BasicDetails;
import com.hepl.budgie.entity.userinfo.ProbationDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.leavemanagement.LeaveGranterMapper;
import com.hepl.budgie.repository.leavemanagement.LeaveGranterRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveMasterRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveSchemeRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveTypeCategoryRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.leavemanagement.LeaveGranterService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveGranterServiceImpl implements LeaveGranterService {

	private final LeaveSchemeRepository leaveSchemeRepository;
	private final LeaveGranterMapper leaveGranterMapper;
	private final LeaveMasterRepository leaveMasterRepository;
	private final LeaveTypeCategoryRepository leaveTypeCategoryRepository;
	private final LeaveGranterRepository leaveGranterRepository;
	private final UserInfoRepository userInfoRepository;
	private final MongoTemplate mongoTemplate;
	private final JWTHelper jwtHelper;

	@Override
	public void leaveGranter(FormRequest formRequest) {
		log.info("Leave granter service impl");
//		LeaveGranter leaveGranter = leaveGranterMapper.toEntity(formRequest.getFormFields());
		Map<String, Object> formFields = formRequest.getFormFields();
		String leaveScheme = (String) formFields.get("leaveScheme");
		String periodicity = (String) formFields.get("periodicity");
		String year = (String) formFields.get("year");
		String monthly = (String) formFields.get("monthly");

		Map<String, String> leaveType = (Map<String, String>) formFields.get("leaveType");

		if ("yearly".equalsIgnoreCase(periodicity) && "monthly".equalsIgnoreCase(periodicity)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid periodicity. Must be 'yearly' or 'monthly'.");
		}

		List<UserInfo> users = userInfoRepository.findByLeaveSchemeAndStatus(leaveScheme, Status.ACTIVE.label,
				mongoTemplate);

		if (users == null || users.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"No active users found for the leave scheme: " + leaveScheme);
		}

		List<String> empIds = users.stream().map(UserInfo::getEmpId).collect(Collectors.toList());

		for (String empId : empIds) {
			if ("yearly".equalsIgnoreCase(periodicity)) {
				processYearlyRequest(empId, year, leaveType);
			} else if ("monthly".equalsIgnoreCase(periodicity)) {
				processMonthlyRequest(empId, monthly, leaveType);
			}
		}
	}

	@Override
	public List<String> fetchLeaveScheme() {
		log.info("Fetching Leave Scheme Types ");
		List<LeaveScheme> activeLeaveSchemes = leaveSchemeRepository.findByActiveStatus(jwtHelper.getOrganizationCode(),
				mongoTemplate);
		if (activeLeaveSchemes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
		}
		return activeLeaveSchemes.stream().map(LeaveScheme::getSchemeName).collect(Collectors.toList());
	}

	@Override
	public Map<String, Object> fetchPeriodicity(String leaveScheme) {
		log.info("Fetching Leave Scheme Periodicity .. ");
		List<LeaveTypeCategory> leaveTypes = leaveTypeCategoryRepository.findBySchemeName(leaveScheme,
				jwtHelper.getOrganizationCode(), mongoTemplate);
		int year = LocalDate.now().getYear();

		List<String> periodicity = leaveTypes.stream().map(LeaveTypeCategory::getPeriodicity).distinct().toList();

		List<Map<String, Object>> yearlyLeaveTypes = leaveTypes.stream()
				.filter(leaveType -> "Yearly".equalsIgnoreCase(leaveType.getPeriodicity())).map(leaveTypeCategory -> {
					Map<String, Object> leaveTypeAndDays = new HashMap<>();
					leaveTypeAndDays.put("leaveType", leaveTypeCategory.getLeaveTypeName());
					leaveTypeAndDays.put("periodicityDays", leaveTypeCategory.getPeriodicityDays());
					return leaveTypeAndDays;
				}).toList();

		List<Map<String, Object>> monthlyLeaveTypes = leaveTypes.stream()
				.filter(leaveType -> "Monthly".equalsIgnoreCase(leaveType.getPeriodicity())).map(leaveTypeCategory -> {
					Map<String, Object> leaveTypeAndDays = new HashMap<>();
					leaveTypeAndDays.put("leaveType", leaveTypeCategory.getLeaveTypeName());
					leaveTypeAndDays.put("periodicityDays", leaveTypeCategory.getPeriodicityDays());
					return leaveTypeAndDays;
				}).toList();

		Map<String, Object> result = new HashMap<>();
		result.put("year", year);
		result.put("periodicity", periodicity);
		result.put("yearlyLeaveTypes", yearlyLeaveTypes);
		result.put("monthlyLeaveTypes", monthlyLeaveTypes);

		return result;
	}

	@Override
	public List<LeaveGranterTableDTO> fetchHistory(String processedType) {
		log.info("Fetching Leave Granter for Type ", processedType);
		List<LeaveGranter> leaveGranterList = leaveGranterRepository.findByProcessedType(processedType,
				jwtHelper.getOrganizationCode(), mongoTemplate);
		List<String> empIds = leaveGranterList.stream().map(LeaveGranter::getEmpId).collect(Collectors.toList());
		List<UserInfo> userInfoList = userInfoRepository.findByEmpIdInAndStatus(empIds, Status.ACTIVE.label, mongoTemplate);
		List<LeaveGranterTableDTO> tableData = new ArrayList<>();
		int i = 1;
		for (LeaveGranter leaveGranter : leaveGranterList) {
			UserInfo userInfo = userInfoList.stream().filter(u -> u.getEmpId().equals(leaveGranter.getEmpId()))
					.findFirst().orElse(null);
			if (userInfo != null) {

				LeaveGranterTableDTO dto = new LeaveGranterTableDTO();
				dto.setSNo(i++);
				dto.setEmpId(leaveGranter.getEmpId());
				dto.setEmployeeName(getEmployeeName(userInfo));
				dto.setRoleOfIntake(userInfo.getSections().getWorkingInformation().getRoleOfIntake());
				dto.setDateOfJoin(getDateOfJoin(userInfo));
				dto.setScheme(userInfo.getSections().getHrInformation().getLeaveScheme());
				dto.setPeriodicity(getPeriodicity(userInfo.getSections().getHrInformation().getLeaveScheme()));
				dto.setFromDate(leaveGranter.getFromDate());
				dto.setToDate(leaveGranter.getToDate());
				dto.setPostedOn(leaveGranter.getPostedOn());
				dto.setDetails(leaveGranter.getDetails());
				dto.setConfirmed(EmployeeStatus(userInfo.getSections().getProbationDetails()));
				tableData.add(dto);
			}
		}
		return tableData;
	}

	private String EmployeeStatus(ProbationDetails probationDetails) {
		return probationDetails != null ? (probationDetails.isProbation() ? "Probation" : "Confirmed") : "Trainee";
	}

	private String getEmployeeName(UserInfo userInfo) {
		if (userInfo != null && userInfo.getSections() != null && userInfo.getSections().getBasicDetails() != null) {
			BasicDetails basicDetails = userInfo.getSections().getBasicDetails();
			return basicDetails.getFirstName() + " " + basicDetails.getLastName();
		}
		return null;
	}

	private String getDateOfJoin(UserInfo userInfo) {
		if (userInfo != null && userInfo.getSections() != null
				&& userInfo.getSections().getWorkingInformation() != null) {
			return userInfo.getSections().getWorkingInformation().getDoj().toString();
		}
		return null;
	}

	private String getPeriodicity(String leaveScheme) {
		if (leaveScheme != null) {
			LeaveTypeCategory leaveTypeCategory = leaveTypeCategoryRepository.findByLeaveSchemeName(leaveScheme,
					jwtHelper.getOrganizationCode(), mongoTemplate);
			if (leaveTypeCategory != null) {
				return leaveTypeCategory.getPeriodicity();
			}
		}
		return null;
	}

	private void processYearlyRequest(String empId, String year, Map<String, String> formFields) {
		String org = jwtHelper.getOrganizationCode();

		Optional<LeaveMaster> existingLeaveMaster = leaveMasterRepository.findByEmpIdAndYear(empId, year, org,
				mongoTemplate);
//	    double leaveDays = privilegeLeaveCalculation(empId, numDays);
		if (existingLeaveMaster.isPresent()) {
			log.info("LeaveMaster data already exists for empId: {} and year: {}. No action taken.", empId, year);
			return;
		}
		String startDate = year + "-01-01";
		String endDate = year + "-12-31";

		if (formFields.containsKey(LeaveCategory.PRIVILEGE_LEAVE.label)) {
			int privilegeLeaveDays = Integer.parseInt(formFields.get(LeaveCategory.PRIVILEGE_LEAVE.label));
			int calculatedLeaveDays = privilegeLeaveCalculation(empId, privilegeLeaveDays);
			formFields.put(LeaveCategory.PRIVILEGE_LEAVE.label, String.valueOf(calculatedLeaveDays));
		}
		LeaveMaster newLeaveMaster = createLeaveMaster(empId, year, startDate, endDate, formFields);
		LeaveGranter newLeaveGranter = createLeaveGranter(empId, startDate, endDate, formFields);
		leaveMasterRepository.saveLeaveMaster(newLeaveMaster, org, mongoTemplate);
		leaveGranterRepository.saveLeaveGranter(newLeaveGranter, org, mongoTemplate);

		log.info("Successfully processed yearly request for empId: {} and year: {}", empId, year);
	}

	private void processMonthlyRequest(String empId, String monthly, Map<String, String> formFields) {
		String org = jwtHelper.getOrganizationCode();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
		YearMonth yearMonth = YearMonth.parse(monthly, formatter);
		String year = String.valueOf(yearMonth.getYear());
		String startDate = yearMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Optional<LeaveMaster> leaveMasters = leaveMasterRepository.findByEmpIdYearAndTransactionDateRange(empId, year,
				startDate, endDate, mongoTemplate, org);

		if (leaveMasters.isPresent()) {
			log.info("LeaveMaster data already exists for empId: {} and year: {}. No action taken.", empId, monthly);
			return;
		}

		LeaveMaster newLeaveMaster = leaveMasterRepository.findByEmpIdAndYear(empId, year, org, mongoTemplate)
				.map(existing -> updateLeaveMaster(existing, startDate, endDate, formFields))
				.orElseGet(() -> createLeaveMaster(empId, year, startDate, endDate, formFields));

		LeaveGranter newLeaveGranter = createLeaveGranter(empId, startDate, endDate, formFields);

		leaveMasterRepository.saveLeaveMaster(newLeaveMaster, org, mongoTemplate);
		leaveGranterRepository.saveLeaveGranter(newLeaveGranter, org, mongoTemplate);

		log.info("Successfully processed monthly request for empId: {}, year: {}, and date range: {}-{}", empId, year,
				startDate, endDate);
	}

	private LeaveGranter createLeaveGranter(String empId, String fromDate, String toDate,
			Map<String, String> formFields) {
		LeaveGranter newLeaveGranter = new LeaveGranter();
		newLeaveGranter.setEmpId(empId);
		newLeaveGranter.setProcessedType(LeaveCategory.LEAVE_GRANTER.label);
		newLeaveGranter.setFromDate(fromDate);
		newLeaveGranter.setToDate(toDate);
		newLeaveGranter.setPostedOn(LocalDate.now().toString());
		List<Details> newDetails = formFields.entrySet().stream().map(entry -> {
			Details details = new Details();
			details.setTransaction("Granter");
			details.setType(entry.getKey());
			details.setDays(entry.getValue());
			return details;
		}).collect(Collectors.toList());
		newLeaveGranter.setDetails(newDetails);
		return newLeaveGranter;
	}

	private LeaveMaster createLeaveMaster(String empId, String year, String fromDate, String toDate,
			Map<String, String> leaveTypeMap) {
		LeaveMaster leaveMaster = new LeaveMaster();
		leaveMaster.setEmpId(empId);
		leaveMaster.setYear(year);

		List<LeaveBalanceSummary> leaveSummaries = createLeaveBalanceSummaries(leaveTypeMap);
		leaveMaster.setLeaveBalanceSummary(leaveSummaries);

		List<LeaveTransactions> leaveTransactions = createLeaveTransactions(leaveTypeMap, fromDate, toDate);
		leaveMaster.setLeaveTransactions(leaveTransactions);

		return leaveMaster;
	}

	private LeaveMaster updateLeaveMaster(LeaveMaster existing, String startDate, String endDate,
			Map<String, String> formFields) {
		existing.setLeaveBalanceSummary(createLeaveBalanceSummaries(formFields));
		existing.setLeaveTransactions(createLeaveTransactions(formFields, startDate, endDate));
		return existing;
	}

	private List<LeaveBalanceSummary> createLeaveBalanceSummaries(Map<String, String> leaveTypeMap) {
		return leaveTypeMap.entrySet().stream().map(entry -> {
			LeaveBalanceSummary summary = new LeaveBalanceSummary();

			summary.setLeaveTypeName(entry.getKey());
			summary.setOpeningBalance(0);
			summary.setGranted(Integer.parseInt(entry.getValue()));
			summary.setAvailed(0);
			summary.setLapsed(0);
			summary.setBalance(Integer.parseInt(entry.getValue()));
			summary.setStatus(1);
			return summary;
		}).collect(Collectors.toList());
	}

	private List<LeaveTransactions> createLeaveTransactions(Map<String, String> leaveTypeMap, String fromDate,
			String toDate) {
		String user = jwtHelper.getUserRefDetail().getEmpId();
		AtomicInteger counter = new AtomicInteger(1);

		return leaveTypeMap.entrySet().stream().map(entry -> {
			LeaveTransactions transaction = new LeaveTransactions();
			transaction.setTransactionId("T" + String.format("%03d", counter.getAndIncrement()));
			transaction.setLeaveTypeName(entry.getKey());
			transaction.setTransactionType("Granted");
			transaction.setProcessedBy(jwtHelper.getUserRefDetail().getEmpId());
			transaction.setPostedOn(LocalDate.now().toString());
			transaction.setFromDate(fromDate);
			transaction.setToDate(toDate);
			transaction.setFromSession("Session 1");
			transaction.setToSession("Session 2");
			transaction.setNoOfDays(Integer.parseInt(entry.getValue()));
			transaction.setCreatedByUser(user);
			transaction.setCreatedDate(LocalDateTime.now());
			transaction.setModifiedByUser(user);
			transaction.setLastModifiedDate(LocalDateTime.now());
			return transaction;
		}).collect(Collectors.toList());
	}

	private Integer privilegeLeaveCalculation(String empId, Integer totalLeaves) {

		final int TOTAL_MONTHS_IN_YEAR = 12;

		UserInfo userInfo = userInfoRepository.findByEmpIdAndIsProbation(empId, false, mongoTemplate);

		if (userInfo != null) {
			ZonedDateTime confirmDate = userInfo.getSections().getProbationDetails().getProbationEndDate();
			LocalDate probationDate = confirmDate.toLocalDate();
			int year = probationDate.getYear();
			int yearsSinceProbation = Period.between(probationDate, LocalDate.now()).getYears();
			if (yearsSinceProbation < 1) {

				LocalDate endOfYear = LocalDate.of(year, Month.DECEMBER, 31);
				int monthsInProbationYear = Period.between(probationDate, endOfYear).getMonths() + 1;

				double proRatedLeave = ((double) totalLeaves / TOTAL_MONTHS_IN_YEAR) * monthsInProbationYear;
				int wholeDays = (int) proRatedLeave;
				double fractionalDays = proRatedLeave - wholeDays;
				if (fractionalDays >= 0.5) {
					wholeDays += 1;
				}
				return wholeDays;

			} else {
				return totalLeaves;
			}
		}

		return 0;
	}
}
