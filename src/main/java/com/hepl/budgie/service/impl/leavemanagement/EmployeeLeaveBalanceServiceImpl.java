package com.hepl.budgie.service.impl.leavemanagement;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LeaveBalanceDTO;
import com.hepl.budgie.dto.leavemanagement.LeaveTransactionReportDTO;
import com.hepl.budgie.dto.leavemanagement.LeaveTypeInfoDTO;
import com.hepl.budgie.dto.leavemanagement.PostLeaveTransactionDTO;
import com.hepl.budgie.entity.LeaveCategory;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leavemanagement.LeaveBalanceSummary;
import com.hepl.budgie.entity.leavemanagement.LeaveMaster;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactionType;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.leave.LeaveApplyRepo;
import com.hepl.budgie.repository.leavemanagement.LeaveMasterRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveTransactionTypeRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveTypeCategoryRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.leavemanagement.EmployeeLeaveBalanceService;
import com.hepl.budgie.service.leavemanagement.LeaveTypeCategoryService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.DateTimeFormatting;
import com.hepl.budgie.utils.ExcelGenerator;
import com.hepl.budgie.utils.IdGenerator;
import com.hepl.budgie.utils.PdfGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeLeaveBalanceServiceImpl implements EmployeeLeaveBalanceService {

	private final UserInfoRepository userInfoRepository;
	private final Translator translator;
	private final LeaveTypeCategoryRepository leaveTypeCategoryRepository;
	private final LeaveTypeCategoryService leaveTypeCategoryService;
	private final MongoTemplate mongoTemplate;
	private final LeaveMasterRepository leaveMasterRepository;
	private final LeaveApplyRepo leaveApplyRepo;
	private final LeaveTransactionTypeRepository leaveTransactionTypeRepository;
	private final IdGenerator idGenerator;
	private final ObjectMapper objectMapper;
	private final JWTHelper jwtHelper;

	private static final String COMPANY_NAME = "Hema's Enterprises Private Limited";
	private static final String COMPANY_ADDRESS = "No.12, Cenotaph Road, Teynampet, Chennai, Tamilnadu-600018";

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

	@Override
	public Object getEmployeeDetails(String empId) {

		UserInfo userInfo = userInfoRepository.findByEmpId(empId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND));

		String leaveScheme = Optional.ofNullable(userInfo.getSections()).map(sections -> sections.getHrInformation())
				.map(hrInfo -> hrInfo.getLeaveScheme()).orElse("");

		List<LeaveTypeCategory> leaveTypeCategory = leaveTypeCategoryRepository.findBySchemeName(leaveScheme,
				jwtHelper.getOrganizationCode(), mongoTemplate);
		List<String> leaveType = leaveTypeCategory.stream().map(LeaveTypeCategory::getLeaveTypeName).toList();
//		List<LeaveTypeInfoDTO> leaveTypes = leaveTypeCategory.stream()
//				.map(leaveTypeCategorys -> new LeaveTypeInfoDTO(leaveTypeCategorys.getLeaveUniqueCode(),
//						leaveTypeCategorys.getLeaveTypeName(), leaveTypeCategorys.getLeaveTypeCode()))
//				.collect(Collectors.toList());
		List<LeaveTransactionType> transactionType = leaveTransactionTypeRepository
				.findByActiveStatus(jwtHelper.getOrganizationCode(), mongoTemplate);
		List<String> transactionTypes = transactionType.stream().map(LeaveTransactionType::getLeaveTransactionType)
				.toList();

		List<Integer> years = getYearListOfEmployee(empId);
		return Map.of("leaveScheme", leaveScheme, "leaveTypes", leaveType, "transactionTypes", transactionTypes,
				"years", years);
	}

	private List<Integer> getYearListOfEmployee(String empId) {

		UserInfo userInfo = userInfoRepository.findByEmpId(empId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND));
		int dateOfJoin = userInfo.getSections().getWorkingInformation().getDoj().getYear();

		int currentYear = ZonedDateTime.now().getYear();

		return IntStream.rangeClosed(dateOfJoin, currentYear).boxed().collect(Collectors.toList());
	}

	@Override
	public List<LeaveBalanceDTO> getEmployeeLeaveBalance(String empId, String year) {

//		UserInfo userInfo = userInfoRepository.findByEmpId(empId)
//				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND));
//
//		String leaveScheme = Optional.ofNullable(userInfo.getSections()).map(sections -> sections.getHrInformation())
//				.map(hrInfo -> hrInfo.getLeaveScheme()).orElse("");
//
//		log.info("Leave scheme for Employee ID : ", empId, leaveScheme);
//
//		List<LeaveTypeInfoDTO> leaveTypes = getLeaveTypes(leaveScheme);

//		return Map.of("schemeName", leaveScheme, "leaveTypes", leaveTypes, "leaveMasterDetails",
//				getLeaveMasterDetails(empId, year));

		return getLeaveMasterDetails(empId, year);
	}

	private List<LeaveTypeInfoDTO> getLeaveTypes(String leaveScheme) {

		List<LeaveTypeCategory> leaveTypeCategories = leaveTypeCategoryRepository.findBySchemeName(leaveScheme,
				jwtHelper.getOrganizationCode(), mongoTemplate);
		List<LeaveTypeInfoDTO> leaveTypes = leaveTypeCategories.stream()
				.map(leaveTypeCategory -> new LeaveTypeInfoDTO(leaveTypeCategory.getLeaveUniqueCode(),
						leaveTypeCategory.getLeaveTypeName(), leaveTypeCategory.getLeaveTypeCode()))
				.collect(Collectors.toList());

		if (leaveTypes.isEmpty()) {
			log.warn("No leave types found for leave scheme: {}", leaveScheme);
		}

		return leaveTypes;
	}

	private List<LeaveBalanceDTO> getLeaveMasterDetails(String empId, String year) {
		log.info("Getting leave Balance for ID: " + empId);

		LeaveMaster leaveMaster = leaveMasterRepository
				.findByEmpIdAndYear(empId, year, jwtHelper.getOrganizationCode(), mongoTemplate)
				.orElse(new LeaveMaster());

		List<String> leaveTypes = Optional.ofNullable(leaveMaster.getLeaveBalanceSummary())
				.orElse(Collections.emptyList()).stream().map(LeaveBalanceSummary::getLeaveTypeName).toList();
		Map<String, String> leaveCodeMap = leaveTypeCategoryService
				.fetchLeaveTypeCodeMap(jwtHelper.getOrganizationCode(), mongoTemplate);

		List<LeaveApply> appliedLeaves = leaveApplyRepo.findByEmpIdAndYearAndLeaveTypes(empId, year, leaveTypes,
				Status.PENDING.label, YesOrNoEnum.NO.label, jwtHelper.getOrganizationCode(), mongoTemplate);

		Map<String, Double> appliedDaysByLeaveType = appliedLeaves.stream().collect(
				Collectors.groupingBy(LeaveApply::getLeaveType, Collectors.summingDouble(LeaveApply::getNumOfDays)));

		return Optional.ofNullable(leaveMaster.getLeaveBalanceSummary()).orElse(Collections.emptyList()).stream()
				.map(summary -> {
					LeaveBalanceDTO dto = new LeaveBalanceDTO();
					dto.setLeaveCode(leaveCodeMap.getOrDefault(summary.getLeaveTypeName(), ""));
					dto.setLeaveType(summary.getLeaveTypeName());
					dto.setOpeningBalance(summary.getOpeningBalance());
					dto.setGranted(summary.getGranted());
					dto.setAvailed(summary.getAvailed());
					dto.setLapsed(summary.getLapsed());
					dto.setBalance(summary.getBalance());
					dto.setStatus(summary.getStatus());
					dto.setApplied(appliedDaysByLeaveType.getOrDefault(summary.getLeaveTypeName(), 0.0));
					return dto;
				}).collect(Collectors.toList());
	}

	@Override
	public Object getEmployeeLeaveMaster(String empId, String year, String leaveType, String transactionType) {
		log.info("Getting leave Balance for ID: " + empId);
		LeaveMaster leaveData = leaveMasterRepository
				.findByEmpIdAndYear(empId, year, jwtHelper.getOrganizationCode(), mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		LeaveBalanceSummary leaveSummary = leaveData.getLeaveBalanceSummary().stream()
				.filter(summary -> leaveType.equalsIgnoreCase(summary.getLeaveTypeName())).findFirst().orElse(null);

		List<LeaveApply> applied = leaveApplyRepo.findByEmpIdAndYearAndLeaveType(empId, year, leaveType,
				Status.PENDING.label, YesOrNoEnum.NO.label, jwtHelper.getOrganizationCode(), mongoTemplate);
		Map<String, String> leaveCodeMap = leaveTypeCategoryService
				.fetchLeaveTypeCodeMap(jwtHelper.getOrganizationCode(), mongoTemplate);

		double totalApplied = applied.stream().mapToDouble(LeaveApply::getNumOfDays).sum();
		LeaveBalanceDTO dto = new LeaveBalanceDTO();
		if (leaveSummary != null) {
			dto.setLeaveCode(leaveCodeMap.getOrDefault(leaveSummary.getLeaveTypeName(), ""));
			dto.setLeaveType(leaveSummary.getLeaveTypeName());
			dto.setOpeningBalance(leaveSummary.getOpeningBalance());
			dto.setBalance(leaveSummary.getBalance());
			dto.setGranted(leaveSummary.getGranted());
			dto.setAvailed(leaveSummary.getAvailed());
			dto.setLapsed(leaveSummary.getLapsed());
			dto.setApplied(totalApplied);
			dto.setStatus(leaveSummary.getStatus());
		}

		List<LeaveTransactions> leaveTransactions = leaveData.getLeaveTransactions().stream()
				.filter(transaction -> leaveType.equalsIgnoreCase(transaction.getLeaveTypeName())
						&& (transactionType == null || transactionType.isEmpty()
								|| transactionType.equalsIgnoreCase(transaction.getTransactionType())))
				.collect(Collectors.toList());

//		leaveData.setLeaveBalanceSummary(leaveSummary);
//		leaveData.setLeaveTransactions(leaveTransactions);

		return Map.of("leaveBalanceSammary", dto, "leaveTransaction", leaveTransactions, "chart",
				getEmployeeChartDetails(empId, year, leaveType));
	}

	@Override
	public void postLeaveTransaction(String empId, FormRequest formRequest) {
		log.info("Processing post leave transaction for employee :{}", empId);

		String currentYear = String.valueOf(LocalDate.now().getYear());
		LeaveMaster leaveData = leaveMasterRepository
				.findByEmpIdAndYear(empId, currentYear, jwtHelper.getOrganizationCode(), mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		PostLeaveTransactionDTO transactionData = objectMapper.convertValue(formRequest.getFormFields(),
				PostLeaveTransactionDTO.class);

		updateLeaveBalanceSummary(leaveData, transactionData);
		updateLeaveTransactions(leaveData, transactionData);

		leaveMasterRepository.saveLeaveMaster(leaveData, jwtHelper.getOrganizationCode(), mongoTemplate);
	}

	private void updateLeaveBalanceSummary(LeaveMaster leaveData, PostLeaveTransactionDTO transactionData) {
		LeaveBalanceSummary leaveBalance = findOrCreateLeaveBalanceSummary(leaveData, transactionData);
		updateLeaveBalance(leaveBalance, transactionData);
	}

	private LeaveBalanceSummary findOrCreateLeaveBalanceSummary(LeaveMaster leaveData,
			PostLeaveTransactionDTO transactionData) {
		return leaveData.getLeaveBalanceSummary().stream()
				.filter(summary -> transactionData.getLeaveTypeName().equalsIgnoreCase(summary.getLeaveTypeName()))
				.findFirst().orElseGet(() -> createNewLeaveBalanceSummary(leaveData, transactionData));
	}

	private LeaveBalanceSummary createNewLeaveBalanceSummary(LeaveMaster leaveData,
			PostLeaveTransactionDTO transactionData) {
		if (!"granted".equalsIgnoreCase(transactionData.getTransactionType())) {
			throw new ResourceNotFoundException(
					"Leave Type not found in Leave Balance Summary for type: " + transactionData.getLeaveTypeName());
		}

		LeaveBalanceSummary newLeaveBalance = new LeaveBalanceSummary();
		newLeaveBalance.setLeaveTypeName(transactionData.getLeaveTypeName());
		newLeaveBalance.setOpeningBalance(0.0);
		newLeaveBalance.setGranted(0.0);
		newLeaveBalance.setAvailed(0.0);
		newLeaveBalance.setLapsed(0.0);

		leaveData.getLeaveBalanceSummary().add(newLeaveBalance);
		return newLeaveBalance;
	}

	private void updateLeaveBalance(LeaveBalanceSummary leaveBalance, PostLeaveTransactionDTO transactionData) {
		double noOfDays = transactionData.getNoOfDays();

		switch (transactionData.getTransactionType().toLowerCase()) {
		case "opening balance":
			leaveBalance.setOpeningBalance(leaveBalance.getOpeningBalance() + noOfDays);
			break;
		case "granted":
			leaveBalance.setGranted(leaveBalance.getGranted() + noOfDays);
			break;
		case "availed":
			leaveBalance.setAvailed(leaveBalance.getAvailed() + noOfDays);
			break;
		case "lapsed":
			leaveBalance.setLapsed(leaveBalance.getLapsed() + noOfDays);
			break;
		default:
			throw new IllegalArgumentException("Invalid transaction type: " + transactionData.getTransactionType());
		}

		leaveBalance.setBalance(leaveBalance.getOpeningBalance() + leaveBalance.getGranted() - leaveBalance.getAvailed()
				- leaveBalance.getLapsed());
	}

	private void updateLeaveTransactions(LeaveMaster leaveData, PostLeaveTransactionDTO transactionData) {
		findExistingTransaction(leaveData, transactionData).ifPresentOrElse(
				transaction -> updateExistingTransaction(transaction, transactionData),
				() -> addNewTransaction(leaveData, transactionData));
	}

	private void updateExistingTransaction(LeaveTransactions transaction, PostLeaveTransactionDTO transactionData) {
		transaction.setNoOfDays(transaction.getNoOfDays() + transactionData.getNoOfDays());
		log.info("Updated existing leave transaction with ID: {}", transaction.getTransactionId());
	}

	private void addNewTransaction(LeaveMaster leaveData, PostLeaveTransactionDTO transactionData) {
		LeaveTransactions newTransaction = createNewTransaction(leaveData, transactionData);
		leaveData.getLeaveTransactions().add(newTransaction);
		log.info("Added new leave transaction with ID: {}", newTransaction.getTransactionId());
	}

	private Optional<LeaveTransactions> findExistingTransaction(LeaveMaster leaveData,
			PostLeaveTransactionDTO transactionData) {
		return leaveData.getLeaveTransactions().stream()
				.filter(tx -> tx.getTransactionType().equalsIgnoreCase(transactionData.getTransactionType())
						&& tx.getLeaveTypeName().equalsIgnoreCase(transactionData.getLeaveTypeName())
						&& areDatesSame(tx.getFromDate(), tx.getToDate(), transactionData.getFromDate(),
								transactionData.getToDate()))
				.findFirst();
	}

	private boolean areDatesSame(String existingFromDateStr, String existingToDateStr, String newFromDateStr,
			String newToDateStr) {
		LocalDate existingFromDate = LocalDate.parse(existingFromDateStr);
		LocalDate existingToDate = LocalDate.parse(existingToDateStr);
		LocalDate newFromDate = LocalDate.parse(newFromDateStr);
		LocalDate newToDate = LocalDate.parse(newToDateStr);

		return existingFromDate.equals(newFromDate) && existingToDate.equals(newToDate);
	}

	private LeaveTransactions createNewTransaction(LeaveMaster leaveData, PostLeaveTransactionDTO transactionData) {
		String user = jwtHelper.getUserRefDetail().getEmpId();
		LeaveTransactions newTransaction = new LeaveTransactions();
		newTransaction.setTransactionId(idGenerator.generateTransactionId(leaveData));
		newTransaction.setLeaveTypeName(transactionData.getLeaveTypeName());
		newTransaction.setTransactionType(transactionData.getTransactionType());
		newTransaction.setProcessedBy(user);
		newTransaction.setPostedOn(LocalDate.now().toString());
		newTransaction.setFromDate(transactionData.getFromDate());
		newTransaction.setToDate(transactionData.getToDate());
		newTransaction.setFromSession(transactionData.getFromSession());
		newTransaction.setToSession(transactionData.getToSession());
		newTransaction.setNoOfDays(transactionData.getNoOfDays());
		return newTransaction;
	}

	@Override
	public List<LeaveTransactions> filterByLeaveTypeAndTransactionType(String empId, String leaveType,
			String transactionType) {
		List<LeaveMaster> leaveDataList = leaveMasterRepository.findAllByEmpId(empId, jwtHelper.getOrganizationCode(),
				mongoTemplate);

		List<LeaveTransactions> filteredTransactions = leaveDataList.stream()
				.flatMap(leaveData -> leaveData.getLeaveTransactions().stream())
				.filter(transaction -> (leaveType == null || leaveType.isEmpty()
						|| leaveType.equalsIgnoreCase(transaction.getLeaveTypeName()))
						&& (transactionType == null || transactionType.isEmpty()
								|| transactionType.equalsIgnoreCase(transaction.getTransactionType())))
				.collect(Collectors.toList());

		return filteredTransactions;
	}

	@Override
	public byte[] exportLeaveData(String empId, FormRequest formRequest) {

		LeaveTransactionReportDTO requestData = objectMapper.convertValue(formRequest.getFormFields(),
				LeaveTransactionReportDTO.class);

		UserInfo userInfo = userInfoRepository.findByEmpIdAndStatus(empId, Status.ACTIVE.label)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND));

		List<LeaveTransactions> filteredTransactions = getFilteredTransactions(empId, requestData);

		if (filteredTransactions.isEmpty()) {
			throw new ResourceNotFoundException("No leave transactions found for the given criteria.");
		}

		Map<String, String> metadata = createMetadata(userInfo, requestData);

		List<String> headers = Arrays.asList("S.No", "Posted On", "From Date", "To Date", "Days", "Leave Type",
				"Transaction Type");

		List<List<Object>> data = createData(filteredTransactions);

		return generateReport(requestData.getGenerateAs(), headers, data, metadata);

	}

	private List<LeaveTransactions> getFilteredTransactions(String empId, LeaveTransactionReportDTO reportCriteria) {
		return leaveMasterRepository.findAllByEmpId(empId, jwtHelper.getOrganizationCode(), mongoTemplate).stream()
				.flatMap(leaveMaster -> leaveMaster.getLeaveTransactions().stream())
				.filter(transaction -> filterByCriteria(transaction, reportCriteria)).toList();
	}

	private boolean filterByCriteria(LeaveTransactions transaction, LeaveTransactionReportDTO criteria) {
		try {
			LocalDate transactionFromDate = LocalDate.parse(transaction.getFromDate(), DATE_FORMATTER);
			LocalDate transactionToDate = LocalDate.parse(transaction.getToDate(), DATE_FORMATTER);

			LocalDate criteriaFromDate = LocalDate.parse(criteria.getFromDate(), DATE_FORMATTER);
			LocalDate criteriaToDate = LocalDate.parse(criteria.getToDate(), DATE_FORMATTER);

			boolean isTransactionTypeMatch = "all".equalsIgnoreCase(criteria.getTransactionType())
					|| transaction.getTransactionType().equalsIgnoreCase(criteria.getTransactionType());

			return transaction.getLeaveTypeName().equalsIgnoreCase(criteria.getLeaveTypeName())
					&& isTransactionTypeMatch && !transactionFromDate.isBefore(criteriaFromDate)
					&& !transactionToDate.isAfter(criteriaToDate);
		} catch (DateTimeParseException e) {
			log.warn("Invalid date format in transaction: {}", e.getMessage());
			return false;
		}
	}

	private Map<String, String> createMetadata(UserInfo userInfo, LeaveTransactionReportDTO requestData) {
		Map<String, String> metadata = new LinkedHashMap<>();
		metadata.put("Company Name", COMPANY_NAME);
		metadata.put("Company Address", COMPANY_ADDRESS);
		metadata.put("Report Period",
				"Leave Transactions From " + LocalDate.parse(requestData.getFromDate()).format(OUTPUT_DATE_FORMATTER)
						+ " To " + LocalDate.parse(requestData.getToDate()).format(OUTPUT_DATE_FORMATTER));
		metadata.put("Employee Name", userInfo.getSections().getBasicDetails().getFirstName());
		metadata.put("Employee ID", userInfo.getEmpId());
		metadata.put("Department", userInfo.getSections().getWorkingInformation().getDepartment());
		metadata.put("Date of Joining",
				userInfo.getSections().getWorkingInformation().getDoj().toLocalDate().format(OUTPUT_DATE_FORMATTER));
		metadata.put("Reporting Manager", userInfo.getSections().getHrInformation().getPrimary().getManagerId());
		metadata.put("Manager Department", userInfo.getSections().getWorkingInformation().getDepartment());
		return metadata;
	}

	private List<List<Object>> createData(List<LeaveTransactions> transactions) {
		List<List<Object>> data = new ArrayList<>();
		int serialNumber = 1;

		for (LeaveTransactions transaction : transactions) {
			String parsedDate = LocalDate.parse(transaction.getPostedOn(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					.toString();
			String formattedPostedOn = formatDate(parsedDate);
			String formattedFromDate = formatDate(transaction.getFromDate());
			String formattedToDate = formatDate(transaction.getToDate());

			data.add(Arrays.asList(serialNumber++, formattedPostedOn, formattedFromDate, formattedToDate,
					transaction.getNoOfDays(), transaction.getLeaveTypeName(), transaction.getTransactionType()));
		}

		return data;
	}

	private byte[] generateReport(String format, List<String> headers, List<List<Object>> data,
			Map<String, String> metadata) {
		switch (format.toLowerCase()) {
		case "excel":
			return ExcelGenerator.generateExcel(headers, data, metadata);
		case "pdf":
			return PdfGenerator.generatePdf(headers, data, metadata);
		default:
			throw new IllegalArgumentException("Invalid report format: " + format);
		}
	}

	private String formatDate(String date) {
		try {
			LocalDate parsedDate = LocalDate.parse(date, DATE_FORMATTER);
			return parsedDate.format(OUTPUT_DATE_FORMATTER);
		} catch (DateTimeParseException e) {
			log.warn("Invalid date format: {}", e.getMessage());
			return date;
		}
	}

	@Override
	public Object getEmployeeChartDetails(String empId, String year, String leaveType) {
		log.info("Fetching Leave Consumed chart");

		UserInfo user = getUserInfromations(empId);

		LeaveMaster leaveData = fetchLeaveMasterDetails(empId, year, user.getOrganization().getOrganizationCode());

		double initialBalance = leaveData.getLeaveBalanceSummary().stream()
				.filter(balance -> leaveType.equals(balance.getLeaveTypeName())).findFirst()
				.map(balance -> balance.getGranted() + balance.getOpeningBalance())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"No Leave Balance Summary found for the Leave Type: " + leaveType));

		return generateMonthlyLeaveDetails(user.getEmpId(), leaveType, year, initialBalance);
	}

	private Object generateMonthlyLeaveDetails(String empId, String leaveType, String year, double initialBalance) {

		List<LeaveApply> allLeaveApplies = leaveApplyRepo
				.findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndLeaveCancelAndFromToDateListBetween(empId,
						leaveType, LeaveCategory.LEAVE_APPLY.label, Status.APPROVED.label, "No",
						DateTimeFormatting.formatDate(LocalDate.of(Integer.parseInt(year), 1, 1), "yyyy-MM-dd"),
						DateTimeFormatting.formatDate(LocalDate.of(Integer.parseInt(year), 12, 31), "yyyy-MM-dd"),
						jwtHelper.getOrganizationCode(), mongoTemplate);

		List<LeaveApply> pendingLeaves = leaveApplyRepo
				.findByEmpIdAndLeaveTypeAndLeaveCategoryAndStatusAndLeaveCancelAndFromToDateListBetween(empId,
						leaveType, LeaveCategory.LEAVE_APPLY.label, Status.PENDING.label, "No",
						DateTimeFormatting.formatDate(LocalDate.of(Integer.parseInt(year), 1, 1), "yyyy-MM-dd"),
						DateTimeFormatting.formatDate(LocalDate.of(Integer.parseInt(year), 12, 31), "yyyy-MM-dd"),
						jwtHelper.getOrganizationCode(), mongoTemplate);

		Map<YearMonth, List<LeaveApply>> leaveAppliesByMonth = allLeaveApplies.stream().collect(
				Collectors.groupingBy(leaveApply -> YearMonth.from(LocalDate.parse(leaveApply.getFromDate()))));

		Map<YearMonth, List<LeaveApply>> pendingLeavesByMonth = pendingLeaves.stream().collect(
				Collectors.groupingBy(leaveApply -> YearMonth.from(LocalDate.parse(leaveApply.getFromDate()))));

		double[] balanceHolder = { initialBalance };

		return Stream.iterate(LocalDate.of(Integer.parseInt(year), 1, 1), date -> date.plusMonths(1)).limit(12)
				.map(monthStartDate -> {
					YearMonth yearMonth = YearMonth.from(monthStartDate);
					String monthAbbreviation = yearMonth.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH));

					List<LeaveApply> leaveApplies = leaveAppliesByMonth.getOrDefault(yearMonth,
							Collections.emptyList());

					List<LeaveApply> pendingForMonth = pendingLeavesByMonth.getOrDefault(yearMonth,
							Collections.emptyList());

					double consumedDays = leaveApplies.stream().mapToDouble(LeaveApply::getNumOfDays).sum();

					double appliedCount = pendingForMonth.stream().mapToDouble(LeaveApply::getNumOfDays).sum();

					double updatedBalance = balanceHolder[0] - consumedDays;
					balanceHolder[0] = updatedBalance;

					Map<String, Object> monthlyDetails = new LinkedHashMap<>();
					monthlyDetails.put("Month", monthAbbreviation);
					monthlyDetails.put("Balanced", updatedBalance);
					monthlyDetails.put("Applied", appliedCount);
					monthlyDetails.put("Consumed", consumedDays);

					return monthlyDetails;
				}).collect(Collectors.toList());
	}

	private LeaveMaster fetchLeaveMasterDetails(String empId, String year, String org) {
		return leaveMasterRepository.findByEmpIdAndYear(empId, year, org, mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("%s --> Leave Master not found for Employee ID: %s and Year: %s",
								AppMessages.RESOURCE_NOT_FOUND, empId, year)));
	}

	private UserInfo getUserInfromations(String empId) {

		UserInfo userInfo = userInfoRepository.findByEmpId(empId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
		return userInfo;
	}
}
