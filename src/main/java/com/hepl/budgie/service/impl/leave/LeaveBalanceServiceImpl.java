package com.hepl.budgie.service.impl.leave;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.leave.LeaveBalanceSummaryResponse;
import com.hepl.budgie.entity.LeaveCategory;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leave.LeaveApplyDates;
import com.hepl.budgie.entity.leavemanagement.LeaveBalanceSummary;
import com.hepl.budgie.entity.leavemanagement.LeaveMaster;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.leave.LeaveApplyRepo;
import com.hepl.budgie.repository.leavemanagement.LeaveMasterRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.leave.LeaveApplyService1;
import com.hepl.budgie.service.leave.LeaveBalanceService;
import com.hepl.budgie.service.leavemanagement.LeaveMasterService;
import com.hepl.budgie.service.leavemanagement.LeaveTypeCategoryService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.DateTimeFormatting;
import com.hepl.budgie.utils.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

	private final LeaveMasterRepository leaveMasterRepository;
	private final LeaveTypeCategoryService leaveTypeCategoryService;
	private final LeaveApplyService1 leaveApplyService;
	private final LeaveApplyRepo leaveApplyRepo;
	private final UserInfoRepository userInfoRepository;
	private final LeaveMasterService leaveMasterService;
	private final MongoTemplate mongoTemplate;
	private final JWTHelper jwtHelper;

	@Override
	public List<LeaveBalanceSummaryResponse> fetchLeaveByYear(String year) {

		validateYear(year);

		UserRef user = getAuthenticatedUser();

		List<String> leaveTypes = fetchLeaveTypes(user);

		LeaveMaster leaveDetails = fetchLeaveMasterDetails(user.getEmpId(), year);

//		Map<String, String> leaveTypeCodeMap = leaveTypeCategoryService.fetchLeaveTypeCodeMap();

		return buildLeaveBalanceSummaryResponses(leaveTypes, leaveDetails, user.getEmpId(), year);
	}

	private void validateYear(String year) {
		if (year == null || year.trim().isEmpty()) {
			throw new IllegalArgumentException("Year cannot be null or empty");
		}
	}

	private void validateLeaveType(String leaveType) {
		if (leaveType == null || leaveType.trim().isEmpty()) {
			throw new IllegalArgumentException("Year cannot be null or empty");
		}
	}

	private UserRef getAuthenticatedUser() {
		UserRef user = jwtHelper.getUserRefDetail();
		if (user == null || user.getEmpId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User details not found");
		}
		return user;
	}

	private List<String> fetchLeaveTypes(UserRef user) {
		List<String> leaveTypes = leaveApplyService.fetchLeaveType(user.getEmpId(), user.getActiveRole());
		if (leaveTypes.isEmpty()) {
			log.warn("No leave types found for employee: {}", user.getEmpId());
			return Collections.emptyList();
		}
		return leaveTypes;
	}

	private LeaveMaster fetchLeaveMasterDetails(String empId, String year) {
		return leaveMasterRepository.findByEmpIdAndYear(empId, year, jwtHelper.getOrganizationCode(), mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("%s --> Leave Master not found for Employee ID: %s and Year: %s",
								AppMessages.RESOURCE_NOT_FOUND, empId, year)));
	}

	private List<LeaveBalanceSummaryResponse> buildLeaveBalanceSummaryResponses(List<String> leaveTypes,
			LeaveMaster leaveDetails, String empId, String year) {
		return leaveTypes.stream().sorted().map(
				leaveType -> buildLeaveBalanceSummaryResponse(leaveType, leaveDetails, empId, year))
				.collect(Collectors.toList());
	}

	private LeaveBalanceSummaryResponse buildLeaveBalanceSummaryResponse(String leaveType, LeaveMaster leaveDetails,
			String empId, String year) {
		LeaveBalanceSummaryResponse response = new LeaveBalanceSummaryResponse();
		response.setLeaveTypeName(leaveType);

//		response.setLeaveCode(leaveTypeCodeMap.getOrDefault(leaveType, ""));
		response.setLeaveCode(StringUtil.abbreviate(leaveType));
		
		leaveBalanceDetails(response, leaveDetails, leaveType);

		calculateAppliedLeaveDays(response, empId, leaveType, year);

		return response;
	}

	private void leaveBalanceDetails(LeaveBalanceSummaryResponse response, LeaveMaster leaveDetails, String leaveType) {
		leaveDetails.getLeaveBalanceSummary().stream()
				.filter(summary -> leaveType.equalsIgnoreCase(summary.getLeaveTypeName())).findFirst()
				.ifPresent(summary -> {
					response.setOpeningBalance(summary.getOpeningBalance());
					response.setBalance(summary.getBalance());
					response.setGranted(summary.getGranted());
					response.setAvailed(summary.getAvailed());
					response.setStatus(summary.getStatus());
				});
	}

	private void calculateAppliedLeaveDays(LeaveBalanceSummaryResponse response, String empId, String leaveType,
			String year) {
		LocalDate startDate = LocalDate.of(Integer.parseInt(year), 1, 1);
		LocalDate endDate = LocalDate.of(Integer.parseInt(year), 12, 31);

		List<LeaveApply> leaveApplies = leaveApplyRepo
				.findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndFromToDateListBetween(empId, leaveType,
						LeaveCategory.LEAVE_APPLY.label, Status.PENDING.label, DateTimeFormatting.formatDate(startDate, "yyyy-MM-dd"),
						DateTimeFormatting.formatDate(endDate, "yyyy-MM-dd"), jwtHelper.getOrganizationCode(), mongoTemplate);

		double appliedDays = leaveApplies.stream().mapToDouble(LeaveApply::getNumOfDays).sum();
		response.setApplied(appliedDays);
	}

	@Override
	public List<Integer> fetchLeaveByYear() {

		log.info("fetching year ");

		UserRef user = getAuthenticatedUser();
		UserInfo userData = getUserInfromations(user.getEmpId());
		ZonedDateTime doj = userData.getSections().getWorkingInformation().getDoj();

		int currentYear = ZonedDateTime.now().getYear();

		int dojYear = doj.getYear();

		List<Integer> years = IntStream.rangeClosed(dojYear, currentYear).boxed().collect(Collectors.toList());

		return years;
	}

	private UserInfo getUserInfromations(String empId) {

		UserInfo userInfo = userInfoRepository.findByEmpId(empId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
		return userInfo;
	}

	@Override
	public Object fetchTransactions(String year, String leaveType) {
		log.info("Fetching Leave Transactions ");
		validateYear(year);
		validateLeaveType(leaveType);
		UserRef user = getAuthenticatedUser();

		List<LeaveTransactions> transactions = leaveMasterService.fetchLeaveTransactionByYearAndType(user.getEmpId(),
				year, leaveType);

		int yearValue = Integer.parseInt(year);
		LocalDate startDate = LocalDate.of(yearValue, 1, 1);
		LocalDate endDate = LocalDate.of(yearValue, 12, 31);
		String org = jwtHelper.getOrganizationCode();
		List<LeaveApply> leaveApplies = leaveApplyRepo
				.findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndFromToDateListBetween(user.getEmpId(), leaveType,
						LeaveCategory.LEAVE_APPLY.label, Status.PENDING.label, DateTimeFormatting.formatDate(startDate, "yyyy-MM-dd"),
						DateTimeFormatting.formatDate(endDate, "yyyy-MM-dd"), org, mongoTemplate);

		List<LeaveApply> leaveAppliesRejectedList = leaveApplyRepo
				.findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndFromToDateListBetween(user.getEmpId(), leaveType,
						LeaveCategory.LEAVE_APPLY.label, Status.REJECTED.label, DateTimeFormatting.formatDate(startDate, "yyyy-MM-dd"),
						DateTimeFormatting.formatDate(endDate, "yyyy-MM-dd"), org, mongoTemplate);

		List<LeaveApply> allLeaveApplies = new ArrayList<>(leaveApplies);
		allLeaveApplies.addAll(leaveAppliesRejectedList);

		List<LeaveTransactions> leaveTransactionsFromApply = allLeaveApplies.stream()
				.flatMap(leaveApply -> leaveApply.getLeaveApply().stream()
						.map(leaveApplyDate -> mapToLeaveTransaction(leaveApply, leaveApplyDate)))
				.collect(Collectors.toList());

		Map<String, List<LeaveTransactions>> response = new HashMap<>();
		response.put("transactionsFromLeaveMaster", transactions);
		response.put("transactionsFromLeaveApply", leaveTransactionsFromApply);
		return response;
	}

	@Override
	public Object fetchLeaveCount(String year, String leaveType) {
		validateYear(year);
		validateLeaveType(leaveType);
		return null;
	}

	@Override
	public Object fetchLeaveChart(String year, String leaveType) {
		log.info("Fetching Leave Consumed  chart");
		validateYear(year);
		validateLeaveType(leaveType);
		UserRef user = getAuthenticatedUser();

		LeaveMaster leaveData = fetchLeaveMasterDetails(user.getEmpId(), year);

		double initialBalance = leaveData.getLeaveBalanceSummary().stream()
				.filter(balance -> leaveType.equals(balance.getLeaveTypeName())).findFirst()
				.map(balance -> balance.getGranted() + balance.getOpeningBalance())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"No Leave transaction found for the Leave Type: " + leaveType));

		return generateMonthlyLeaveDetails(user.getEmpId(), leaveType, year, initialBalance);
	}

	private List<Object> generateMonthlyLeaveDetails(String empId, String leaveType, String year,
			double initialBalance) {

		List<LeaveApply> allLeaveApplies = leaveApplyRepo
				.findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndFromToDateListBetween(empId, leaveType,
						LeaveCategory.LEAVE_APPLY.label, Status.APPROVED.label,
						DateTimeFormatting.formatDate(LocalDate.of(Integer.parseInt(year), 1, 1), "yyyy-MM-dd"),
						DateTimeFormatting.formatDate(LocalDate.of(Integer.parseInt(year), 12, 31), "yyyy-MM-dd"),
						jwtHelper.getOrganizationCode(), mongoTemplate);

		Map<YearMonth, List<LeaveApply>> leaveAppliesByMonth = allLeaveApplies.stream().collect(
				Collectors.groupingBy(leaveApply -> YearMonth.from(LocalDate.parse(leaveApply.getFromDate()))));

		double[] balanceHolder = { initialBalance };

		return Stream.iterate(LocalDate.of(Integer.parseInt(year), 1, 1), date -> date.plusMonths(1)).limit(12)
				.map(monthStartDate -> {
					YearMonth yearMonth = YearMonth.from(monthStartDate);
					String monthAbbreviation = yearMonth.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH));

					List<LeaveApply> leaveApplies = leaveAppliesByMonth.getOrDefault(yearMonth,
							Collections.emptyList());

					double consumedDays = leaveApplies.stream().mapToDouble(LeaveApply::getNumOfDays).sum();

					double updatedBalance = balanceHolder[0] - consumedDays;
					balanceHolder[0] = updatedBalance;

					Map<String, Object> monthlyDetails = new LinkedHashMap<>();
					monthlyDetails.put("Month", monthAbbreviation);
					monthlyDetails.put("Balanced", updatedBalance);
					monthlyDetails.put("Consumed", consumedDays);

					return monthlyDetails;
				}).collect(Collectors.toList());
	}

	@Override
	public LeaveBalanceSummaryResponse fetchSummary(String year, String leaveType) {
		log.info("Fetching Leave Balance Summary");
		validateYear(year);
		validateLeaveType(leaveType);
		UserRef user = getAuthenticatedUser();

		LeaveBalanceSummary leaveSummary = leaveMasterService.getLeaveBalanceSummary(user.getEmpId(), year, leaveType)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		List<LeaveApply> allLeaveApplies = leaveApplyRepo
				.findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndFromToDateListBetween(user.getEmpId(), leaveType,
						LeaveCategory.LEAVE_APPLY.label, Status.PENDING.label,
						DateTimeFormatting.formatDate(LocalDate.of(Integer.parseInt(year), 1, 1), "yyyy-MM-dd"),
						DateTimeFormatting.formatDate(LocalDate.of(Integer.parseInt(year), 12, 31), "yyyy-MM-dd"),
						jwtHelper.getOrganizationCode(), mongoTemplate);

		double totalNumOfDays = allLeaveApplies.stream().mapToDouble(LeaveApply::getNumOfDays).sum();
		LeaveBalanceSummaryResponse response = mapToResponse(leaveSummary, totalNumOfDays);
		return response;
	}

	private LeaveBalanceSummaryResponse mapToResponse(LeaveBalanceSummary leaveSummary, double totalNumOfDays) {
		LeaveBalanceSummaryResponse response = new LeaveBalanceSummaryResponse();
		response.setLeaveTypeName(leaveSummary.getLeaveTypeName());
		response.setLeaveCode(StringUtil.abbreviate(leaveSummary.getLeaveTypeName()));
		response.setOpeningBalance(leaveSummary.getOpeningBalance());
		response.setBalance(leaveSummary.getBalance());
		response.setGranted(leaveSummary.getGranted());
		response.setAvailed(leaveSummary.getAvailed());
		response.setLapsed(leaveSummary.getLapsed());
		response.setApplied(totalNumOfDays);
		response.setStatus(leaveSummary.getStatus());
		return response;
	}

	private LeaveTransactions mapToLeaveTransaction(LeaveApply leaveApply, LeaveApplyDates leaveApplyDate) {
		LeaveTransactions transaction = new LeaveTransactions();
		transaction.setTransactionId(leaveApply.getLeaveCode());
		transaction.setLeaveTypeName(leaveApply.getLeaveType());
		transaction.setTransactionType(LeaveCategory.LEAVE_APPLY.label);
		transaction.setProcessedBy(leaveApply.getAppliedTo());
		transaction.setPostedOn(leaveApply.getCreatedDate().toLocalDate().toString());
		transaction.setFromDate(leaveApplyDate.getDate());
		transaction.setToDate(leaveApplyDate.getDate());
		transaction.setFromSession(leaveApplyDate.getFromSession());
		transaction.setToSession(leaveApplyDate.getToSession());
		transaction.setNoOfDays(leaveApplyDate.getCount());

		return transaction;
	}

}