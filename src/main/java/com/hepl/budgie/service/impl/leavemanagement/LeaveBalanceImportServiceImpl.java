package com.hepl.budgie.service.impl.leavemanagement;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.EmployeeLeaveBalanceReportDTO;
import com.hepl.budgie.dto.leavemanagement.LeaveTransactionReportDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.leavemanagement.Details;
import com.hepl.budgie.entity.leavemanagement.LeaveBalanceSummary;
import com.hepl.budgie.entity.leavemanagement.LeaveGranter;
import com.hepl.budgie.entity.leavemanagement.LeaveMaster;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactionType;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.enums.LeaveBalanceHeader;
import com.hepl.budgie.repository.leavemanagement.LeaveGranterRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveMasterRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveTransactionTypeRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveTypeCategoryRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.leavemanagement.LeaveBalanceImportService;
import com.hepl.budgie.service.leavemanagement.LeaveTypeCategoryService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.ExcelTemplateHelper;
import com.hepl.budgie.utils.IdGenerator;
import com.mongodb.bulk.BulkWriteInsert;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.bulk.BulkWriteUpsert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveBalanceImportServiceImpl implements LeaveBalanceImportService {

	private final ObjectMapper objectMapper;
	private final LeaveGranterRepository leaveGranterRepository;
	private final LeaveTransactionTypeRepository leaveTransactionTypeRepository;
	private final LeaveTypeCategoryRepository leaveTypeCategoryRepository;
	private final LeaveTypeCategoryService leaveTypeCategoryService;
	private final UserInfoRepository userInfoRepository;
	private final LeaveMasterRepository leaveMasterRepository;
	private final IdGenerator idGenerator;
	private final JWTHelper jwtHelper;
	private final MongoTemplate mongoTemplate;

	@Override
	public byte[] createExcelTemplate() {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Leave Balance");
			ExcelTemplateHelper.createHeadersFromEnum(sheet, LeaveBalanceHeader.class);

			return ExcelTemplateHelper.writeWorkbookToByteArray(workbook);

		} catch (Exception e) {
			log.error("Error in createExcelTemplate: ", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					AppMessages.EXCEL_TEMPLATE_CREATION_FAILED);
		}
	}

	@Override
	public Map<String, List<String>> importLeaveBalance(MultipartFile file) throws IOException {
		log.info("Leave Balance Import Processing: {}", file.getOriginalFilename());
		Map<String, List<String>> errorMap = new HashMap<>();
		String orgId = jwtHelper.getOrganizationCode();
		String collection = leaveGranterRepository.getCollectionName(orgId);

		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
			Sheet sheet = workbook.getSheetAt(0);
			if (sheet == null) {
				errorMap.put("File Error", List.of("Empty Excel Sheet"));
				return errorMap;
			}
			Iterator<Row> rowIterator = sheet.iterator();
			List<String> headers = new ArrayList<>();
			if (rowIterator.hasNext()) {
				Row headerRow = rowIterator.next();
				for (Cell cell : headerRow) {
					headers.add(cell.getStringCellValue().trim());
				}
			}

			List<LeaveGranter> leaveGranters = new ArrayList<>();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				List<String> rowErrors = validateRow(row, headers);

				if (!rowErrors.isEmpty()) {
					errorMap.put("Row " + row.getRowNum(), rowErrors);
					continue;
				}

				LeaveGranter leaveGranter = processRow(row, headers, errorMap);
				if (leaveGranter != null) {
					leaveGranters.add(leaveGranter);
				}
			}

			if (!leaveGranters.isEmpty()) {
				mongoTemplate.insert(leaveGranters, collection);
				log.info("Leave balance records imported successfully.");
			}
		} catch (IOException e) {
			log.error("Error reading Excel file: {}", e.getMessage());
			errorMap.put("File Processing Error", List.of(e.getMessage()));
		}
		return errorMap;
	}

	private List<String> validateRow(Row row, List<String> headers) {
		List<String> errors = new ArrayList<>();

		int periodIndex = headers.indexOf("Period");
		int yearIndex = headers.indexOf("Year");
		int monthIndex = headers.indexOf("Months");

		if (periodIndex == -1 || yearIndex == -1 || monthIndex == -1) {
			errors.add("Missing required headers: Period, Year, or Months.");
			return errors;
		}

		String period = getCellValue(row.getCell(periodIndex));
		String year = getCellValue(row.getCell(yearIndex));
		String month = getCellValue(row.getCell(monthIndex));

		List<String> validMonths = List.of("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December");

		if ("Year".equalsIgnoreCase(period)) {
			if (year.isEmpty() || !year.matches("\\d{4}")) {
				errors.add("Invalid Year format. Expected format: YYYY.");
			}
			if (!month.isEmpty()) {
				errors.add("Months should not be provided when Period is 'Year'.");
			}
		} else if ("Month".equalsIgnoreCase(period)) {
			if (!year.isEmpty()) {
				errors.add("Year should not be provided when Period is 'Months'.");
			}
			if (!validMonths.contains(month)) {
				errors.add("Invalid Month name: " + month);
			}
		} else {
			errors.add("Invalid value for Period. Allowed values: 'Year' or 'Months'.");
		}
		return errors;
	}

	private LeaveGranter processRow(Row row, List<String> headers, Map<String, List<String>> errorMap) {
		try {
			LeaveGranter leaveGranter = new LeaveGranter();
			leaveGranter.setEmpId(getCellValue(row.getCell(0)));
			leaveGranter.setProcessedType("Leave Importer");

			String periodicity = getCellValue(row.getCell(1));
			String range = getCellValue(row.getCell(periodicity.equalsIgnoreCase("Year") ? 2 : 3));

			LocalDate startDate, endDate;

			if ("Year".equalsIgnoreCase(periodicity)) {
				startDate = LocalDate.of(Integer.parseInt(range), 1, 1);
				endDate = LocalDate.of(Integer.parseInt(range), 12, 31);
			} else {
				Map<String, Integer> monthMap = Map.ofEntries(Map.entry("January", 1), Map.entry("February", 2),
						Map.entry("March", 3), Map.entry("April", 4), Map.entry("May", 5), Map.entry("June", 6),
						Map.entry("July", 7), Map.entry("August", 8), Map.entry("September", 9),
						Map.entry("October", 10), Map.entry("November", 11), Map.entry("December", 12));

				int monthNumber = monthMap.getOrDefault(range, -1);
				if (monthNumber == -1) {
					errorMap.put("Row " + row.getRowNum(), List.of("Invalid month name: " + range));
					return null;
				}

				YearMonth monthYear = YearMonth.of(LocalDate.now().getYear(), monthNumber);
				startDate = monthYear.atDay(1);
				endDate = monthYear.atEndOfMonth();
			}

			leaveGranter.setFromDate(startDate.toString());
			leaveGranter.setToDate(endDate.toString());

			List<Details> detailsList = new ArrayList<>();

			for (int i = 3; i < headers.size(); i++) {
				String header = headers.get(i);
				String[] parts = header.split(" ");
				if (parts.length < 3)
					continue;

				String leaveType = parts[0] + " " + parts[1];
				String transactionType = parts[2];
				String value = getCellValue(row.getCell(i));

				if (!value.isEmpty() && !value.equals("0")) {
					Details detail = new Details();
					detail.setType(leaveType);
					detail.setTransaction(transactionType);
					detail.setDays(value);
					detailsList.add(detail);
				}
			}

			if (!detailsList.isEmpty()) {
				leaveGranter.setDetails(detailsList);
				return leaveGranter;
			}
		} catch (Exception e) {
			log.error("Error processing row {}: {}", row.getRowNum(), e.getMessage());
			errorMap.put("Row " + row.getRowNum(), List.of(e.getMessage()));
		}
		return null;
	}

	private String getCellValue(Cell cell) {
		if (cell == null)
			return "";
		return switch (cell.getCellType()) {
		case STRING -> cell.getStringCellValue().trim();
		case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
		case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
		default -> "";
		};
	}

	@Override
	public byte[] exportLeaveBalance(EmployeeLeaveBalanceReportDTO reportRequest) {

		validateRequest(reportRequest);
		LocalDate fromDate = LocalDate.parse(reportRequest.getFromDate());
		LocalDate toDate = LocalDate.parse(reportRequest.getToDate());

		if (fromDate.isAfter(toDate)) {
			throw new IllegalArgumentException("From date cannot be after To date");
		}

		int year = fromDate.getYear();
		String leaveScheme = reportRequest.getLeaveScheme();

		List<UserInfo> users = fetchUsersByLeaveScheme(leaveScheme);
		List<LeaveTypeCategory> leaveCategories = fetchLeaveTypeCategories(leaveScheme);
//		List<LeaveTransactionType> transactionTypes = transactionTypeService.activeList();

//		List<String> columnHeaders = prepareColumnHeaders(leaveCategories, transactionTypes);
//		List<List<Object>> reportData = assembleReportData(users, year, fromDate, toDate, leaveCategories,
//				transactionTypes);

//		return excelGenerator.generateExcel(columnHeaders, reportData);
		return null;
	}

	private void validateRequest(EmployeeLeaveBalanceReportDTO request) {
		if (request.getFromDate() == null || request.getToDate() == null) {
			throw new IllegalArgumentException("From date and to date are required");
		}
	}

	private List<UserInfo> fetchUsersByLeaveScheme(String scheme) {
		if ("All".equalsIgnoreCase(scheme)) {
			return userInfoRepository.findByStatus(Status.ACTIVE.label);
		} else {
			List<UserInfo> users = userInfoRepository.findByLeaveSchemeAndStatus(scheme, Status.ACTIVE.label,
					mongoTemplate);
			return users;
		}
	}

	private List<LeaveTypeCategory> fetchLeaveTypeCategories(String scheme) {
		String org = jwtHelper.getOrganizationCode();
		return "All".equalsIgnoreCase(scheme)
				? leaveTypeCategoryRepository.findByBalanceDeduction(YesOrNoEnum.YES.label, org, mongoTemplate)
				: leaveTypeCategoryRepository.findBySchemeNameAndBalanceDeduction(scheme, YesOrNoEnum.YES.label, org,
						mongoTemplate);
	}

	@Override
	public List<String> getLeaveTypeMap() {

		String orgId = jwtHelper.getOrganizationCode();
		List<LeaveTransactionType> transactions = leaveTransactionTypeRepository.findByActiveStatus(orgId,
				mongoTemplate);
		List<LeaveTypeCategory> leaveTypes = leaveTypeCategoryRepository.findAllByActiveStatus(orgId, mongoTemplate);

		if (CollectionUtils.isEmpty(leaveTypes) || CollectionUtils.isEmpty(transactions)) {
			log.warn("No leave types or transaction types found for organization: {}", orgId);
			return Collections.emptyList();
		}

		List<LeaveTransactionType> directTransactions = transactions.stream()
				.filter(transaction -> YesOrNoEnum.YES.label.equalsIgnoreCase(transaction.getDirectTransaction()))
				.collect(Collectors.toList());

		return leaveTypes.stream().flatMap(leaveType -> directTransactions.stream().map(
				transaction -> generateMappingKey(leaveType.getLeaveTypeCode(), transaction.getLeaveTransactionType())))
				.collect(Collectors.toList());
	}

	private String generateMappingKey(String leaveTypeCode, String transactionType) {
		return Stream.of(leaveTypeCode, transactionType).map(String::trim).map(s -> s.replaceAll("\\s+", "_"))
				.collect(Collectors.joining("_"));
	}

	@Override
	public BulkWriteResult excelImport(List<Map<String, Object>> validRows) {
		if (CollectionUtils.isEmpty(validRows)) {
			return BulkWriteResult.unacknowledged();
		}

		String orgCode = jwtHelper.getOrganizationCode();
		BulkWriteResult masterResult = processLeaveMasterEntries(validRows, orgCode);
		BulkWriteResult granterResult = processLeaveGranterEntries(validRows, orgCode);

		return combineBulkResults(masterResult, granterResult);
	}

	private BulkWriteResult processLeaveMasterEntries(List<Map<String, Object>> validRows, String organizationCode) {

		Map<String, LeaveMaster> existingLeaveMasters = loadExistingLeaveMasters(validRows, organizationCode);
		Map<String, String> employeeSchemes = userInfoRepository
				.fetchEmployeeLeaveSchemes(extractEmployeeIds(validRows), mongoTemplate);
		Map<String, Set<String>> schemeLeaveTypes = leaveTypeCategoryRepository
				.getSchemeLeaveTypeMapping(organizationCode, mongoTemplate);
		Map<String, String> leaveNameByCodeMap = leaveTypeCategoryService.fetchLeaveTypeNameMap(organizationCode,
				mongoTemplate);
		Map<String, Map<String, String>> employeeDateRanges = new HashMap<>();
		Map<String, Integer> leaveTypePeriodicityDays = leaveTypeCategoryRepository
				.getLeaveTypePeriodicityDays(organizationCode, mongoTemplate);
		Map<String, Map<String, Map<String, Map<String, Double>>>> employeeData = new HashMap<>();
		Map<String, String> employeeYears = new HashMap<>();

		for (Map<String, Object> row : validRows) {
			String empId = row.get("Employee_ID").toString();
			String period = row.get("Period").toString();

			Map<String, String> dateRange = getDateRangeForPeriod(period,
					row.get("Year") != null ? row.get("Year").toString() : null,
					row.get("Months") != null ? row.get("Months").toString() : null);

			employeeDateRanges.put(empId, dateRange);

			String year = Optional.ofNullable(row.get("Year")).map(Object::toString)
					.orElseGet(() -> String.valueOf(LocalDate.now().getYear()));
			employeeYears.put(empId, year);

			String schemeName = employeeSchemes.get(empId);
			if (schemeName == null || !schemeLeaveTypes.containsKey(schemeName)) {
				continue;
			}

			Map<String, Map<String, Map<String, Double>>> yearData = employeeData.computeIfAbsent(empId,
					k -> new HashMap<>());
			Map<String, Map<String, Double>> leaveData = yearData.computeIfAbsent(year, k -> new HashMap<>());

			Set<String> allowedLeaveTypes = schemeLeaveTypes.get(schemeName);
			Set<String> leaveTypeCodes = extractLeaveTypesFromRow(row);

			for (String leaveTypeCode : leaveTypeCodes) {
				if (allowedLeaveTypes.contains(leaveTypeCode)) {
					String leaveTypeName = leaveNameByCodeMap.get(leaveTypeCode);
					if (leaveTypeName != null) {
						Map<String, Double> changes = groupLeaveDataByType(row, leaveTypeCode);
						if (!changes.isEmpty() && !changes.values().stream().allMatch(val -> val == 0)) {
							leaveData.put(leaveTypeName, changes);
						}
					}
				}
			}
		}

		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, LeaveMaster.class,
				leaveMasterRepository.getCollectionName(organizationCode));

		LocalDateTime now = LocalDateTime.now();
		String currentUser = jwtHelper.getUserRefDetail().getEmpId();

		employeeData.forEach((empId, yearData) -> {
			AtomicInteger counter = new AtomicInteger(1);
			yearData.forEach((year, leaveData) -> {
				LeaveMaster existingMaster = existingLeaveMasters.get(empId);
				boolean exists = existingMaster != null && existingMaster.getYear().equals(year);
				Map<String, String> dateRange = employeeDateRanges.get(empId);
				Update update = new Update();
				List<LeaveTransactions> allTransactions = new ArrayList<>();
				List<LeaveBalanceSummary> updatedSummaries = exists
						? new ArrayList<>(existingMaster.getLeaveBalanceSummary())
						: new ArrayList<>();

				leaveData.forEach((leaveTypeName, currentChanges) -> {

					Optional<LeaveBalanceSummary> existingSummary = updatedSummaries.stream()
							.filter(s -> s.getLeaveTypeName().equals(leaveTypeName)).findFirst();
					Integer maxAllowed = leaveTypePeriodicityDays.get(leaveTypeName);
					if (maxAllowed == null) {
						maxAllowed = Integer.MAX_VALUE;
					}
					double existingGranted = existingSummary.map(LeaveBalanceSummary::getGranted).orElse(0.0);
					double requestedGrant = currentChanges.getOrDefault("granted", 0.0);
					double allowedGrant = Math.max(0, Math.min(requestedGrant, maxAllowed - existingGranted));

					if (requestedGrant > allowedGrant) {
						currentChanges.put("granted", allowedGrant);
					}
					LeaveBalanceSummary newSummary = calculateNewSummary(leaveTypeName, currentChanges,
							existingSummary);

					List<LeaveTransactions> transactions = createTransactions(leaveTypeName, currentChanges,
							existingSummary, year, currentUser, now, dateRange, existingMaster, counter);
					allTransactions.addAll(transactions);

					if (existingSummary.isPresent()) {
						updatedSummaries.remove(existingSummary.get());
						updatedSummaries.add(newSummary);
					} else {
						updatedSummaries.add(newSummary);
					}
				});

				update.set("leaveBalanceSummary", updatedSummaries);

				if (!allTransactions.isEmpty()) {
					update.push("leaveTransactions").each(allTransactions);
				}

				update.set("lastModifiedDate", now).set("modifiedByUser", currentUser);

				if (!exists) {
					update.setOnInsert("empId", empId).setOnInsert("year", year).setOnInsert("status", "Active")
							.setOnInsert("createdDate", now).setOnInsert("createdByUser", currentUser);
				}

				bulkOps.upsert(new Query(Criteria.where("empId").is(empId).and("year").is(year)), update);
			});
		});

		// 6. Execute bulk operations
		return bulkOps.execute();
	}

	private BulkWriteResult processLeaveGranterEntries(List<Map<String, Object>> validRows, String organizationCode) {
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, LeaveGranter.class,
				leaveGranterRepository.getCollectionName(organizationCode));

		String currentUser = jwtHelper.getUserRefDetail().getEmpId();

		for (Map<String, Object> row : validRows) {
			processLeaveGranterRow(row, bulkOps, currentUser);
		}

		return bulkOps.execute();
	}

	private void processLeaveGranterRow(Map<String, Object> row, BulkOperations bulkOps, String currentUser) {
		String empId = row.get("Employee_ID").toString();
		String period = row.get("Period").toString();

		Map<String, String> dateRange = getDateRangeForPeriod(period,
				row.get("Year") != null ? row.get("Year").toString() : null,
				row.get("Months") != null ? row.get("Months").toString() : null);

		Query query = new Query(Criteria.where("empId").is(empId).and("processedType").is("Leave Import")
				.and("fromDate").is(dateRange.get("fromDate")).and("toDate").is(dateRange.get("toDate")));
		Map<String, Integer> leaveTypePeriodicityDays = leaveTypeCategoryRepository
				.getLeaveTypePeriodicityDays(jwtHelper.getOrganizationCode(), mongoTemplate);
		Update update = buildGranterUpdate(row, currentUser, leaveTypePeriodicityDays);
		bulkOps.upsert(query, update);
	}

	private Update buildGranterUpdate(Map<String, Object> row, String currentUser,
			Map<String, Integer> leaveTypePeriodicityDays) {
		Update update = new Update();
		update.set("empId", row.get("Employee_ID")).set("processedType", "Leave Import").set("postedOn",
				LocalDate.now().toString());

		List<Details> details = buildGranterDetails(row, leaveTypePeriodicityDays);
		update.set("details", details);

		return addAuditInfo(update, true, currentUser);
	}

	private List<Details> buildGranterDetails(Map<String, Object> row, Map<String, Integer> leaveTypePeriodicityDays) {
		List<Details> details = new ArrayList<>();
		Map<String, String> leaveTypeMap = leaveTypeCategoryService
				.fetchLeaveTypeNameMap(jwtHelper.getOrganizationCode(), mongoTemplate);
		Map<String, Double> grantedDaysPerType = new HashMap<>();
		for (Map.Entry<String, Object> entry : row.entrySet()) {
			String key = entry.getKey();
			if (key == null || List.of("Employee_ID", "Period", "Year", "Months").contains(key)) {
				continue;
			}

			String[] parts = key.split("[ _]", 2);
			if (parts.length != 2)
				continue;

			String leaveAbbr = parts[0].trim();
			String transaction = parts[1].trim().replace("_", " ");
			Object dayValue = entry.getValue();

			if (dayValue == null)
				continue;

			String dayStr = dayValue.toString().trim();
			if (dayStr.isEmpty() || !dayStr.matches("\\d+(\\.\\d+)?"))
				continue;

			double dayDouble = Double.parseDouble(dayStr);
			int dayInt = (int) dayDouble;
			if ("granted".equalsIgnoreCase(transaction)) {
				String leaveTypeName = leaveTypeMap.getOrDefault(leaveAbbr, leaveAbbr);
				Integer maxAllowed = leaveTypePeriodicityDays.get(leaveTypeName);
				if (maxAllowed != null) {
					double alreadyGranted = grantedDaysPerType.getOrDefault(leaveTypeName, 0.0);
					double allowedToGrant = Math.max(0, maxAllowed - alreadyGranted);
					dayInt = (int) Math.min(dayInt, allowedToGrant);
					grantedDaysPerType.put(leaveTypeName, alreadyGranted + dayInt);
				}
			}
			if (dayInt <= 0)
				continue;

			Details detail = new Details();
			detail.setType(leaveTypeMap.getOrDefault(leaveAbbr, leaveAbbr));
			detail.setTransaction(transaction);
			detail.setDays(String.valueOf(dayInt));
			details.add(detail);
		}

		return details;
	}

	private Map<String, LeaveMaster> loadExistingLeaveMasters(List<Map<String, Object>> rows, String orgCode) {
		List<String> empIds = extractEmployeeIds(rows);
		return leaveMasterRepository.findByEmpIdInAndOrganizationCode(empIds, orgCode, mongoTemplate).stream()
				.collect(Collectors.toMap(LeaveMaster::getEmpId, Function.identity()));
	}

	private List<String> extractEmployeeIds(List<Map<String, Object>> rows) {
		return rows.stream().map(row -> row.get("Employee_ID").toString()).distinct().collect(Collectors.toList());
	}

	private Set<String> extractLeaveTypesFromRow(Map<String, Object> row) {
		return row.keySet().stream().filter(key -> key.contains("_")).map(key -> key.split("_")[0])
				.filter(part -> !List.of("Employee", "Period", "Year", "Months").contains(part))
				.collect(Collectors.toSet());
	}

	private Map<String, Double> groupLeaveDataByType(Map<String, Object> row, String leaveTypeCode) {
		return row.entrySet().stream().filter(entry -> entry.getKey().startsWith(leaveTypeCode + "_"))
				.collect(Collectors.toMap(entry -> entry.getKey().substring(leaveTypeCode.length() + 1).toLowerCase(),
						entry -> getDoubleValue(entry.getValue()), (v1, v2) -> v1));
	}

	private LeaveBalanceSummary calculateNewSummary(String leaveTypeName, Map<String, Double> currentChanges,
			Optional<LeaveBalanceSummary> existingSummary) {

		LeaveBalanceSummary summary = new LeaveBalanceSummary();
		summary.setLeaveTypeName(leaveTypeName);

		double opening = existingSummary.map(LeaveBalanceSummary::getOpeningBalance).orElse(0.0);
		double granted = existingSummary.map(LeaveBalanceSummary::getGranted).orElse(0.0);
		double availed = existingSummary.map(LeaveBalanceSummary::getAvailed).orElse(0.0);
		double lapsed = existingSummary.map(LeaveBalanceSummary::getLapsed).orElse(0.0);

		summary.setOpeningBalance(opening + currentChanges.getOrDefault("opening_balance", 0.0));
		summary.setGranted(granted + currentChanges.getOrDefault("granted", 0.0));
		summary.setAvailed(availed + currentChanges.getOrDefault("availed", 0.0));
		summary.setLapsed(lapsed + currentChanges.getOrDefault("lapsed", 0.0));
		summary.setBalance(
				summary.getOpeningBalance() + summary.getGranted() - summary.getAvailed() - summary.getLapsed());
		summary.setStatus(1);

		return summary;
	}

	private List<LeaveTransactions> createTransactions(String leaveTypeName, Map<String, Double> currentChanges,
			Optional<LeaveBalanceSummary> existingSummary, String year, String currentUser, LocalDateTime now,
			Map<String, String> dateRange, LeaveMaster existingMaster, AtomicInteger counter) {

		List<LeaveTransactions> transactions = new ArrayList<>();

		Map<String, Double> existingValues = existingSummary
				.map(summary -> Map.of("opening_balance", summary.getOpeningBalance(), "granted", summary.getGranted(),
						"availed", summary.getAvailed(), "lapsed", summary.getLapsed()))
				.orElse(Collections.emptyMap());

		for (Map.Entry<String, Double> entry : currentChanges.entrySet()) {

			if (entry.getValue() > 0) {
				double delta = entry.getValue() - existingValues.getOrDefault(entry.getKey(), 0.0);
				if (delta > 0) {
					transactions.add(createTransaction(leaveTypeName, getTransactionType(entry.getKey()), delta, year,
							currentUser, now, dateRange, existingMaster, counter));
				}
			}
		}
		return transactions;
	}

	private LeaveTransactions createTransaction(String leaveTypeName, String type, double days, String year,
			String user, LocalDateTime now, Map<String, String> dateRange, LeaveMaster existingMaster,
			AtomicInteger counter) {

		LeaveTransactions txn = new LeaveTransactions();

		if (existingMaster != null) {
			txn.setTransactionId(idGenerator.generateTransactionId(existingMaster));
		} else {
			txn.setTransactionId("T" + String.format("%03d", counter.getAndIncrement()));
		}
		txn.setLeaveTypeName(leaveTypeName);
		txn.setTransactionType(type);
		txn.setProcessedBy(user);
		txn.setPostedOn(LocalDate.now().toString());
		txn.setFromDate(dateRange.get("fromDate"));
		txn.setToDate(dateRange.get("toDate"));
		txn.setFromSession("Session 1");
		txn.setToSession("Session 2");
		txn.setNoOfDays(days);
		txn.setCreatedDate(now);
		txn.setLastModifiedDate(now);
		txn.setCreatedByUser(user);
		txn.setModifiedByUser(user);
		return txn;
	}

	private String getTransactionType(String field) {
		switch (field.toLowerCase()) {
		case "opening_balance":
			return "Opening Balance";
		case "granted":
			return "Granted";
		case "availed":
			return "Availed";
		case "lapsed":
			return "Lapsed";
		default:
			return Arrays.stream(field.split("_")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
					.collect(Collectors.joining(" "));
		}
	}

	private Update addAuditInfo(Update update, boolean isNew, String authUser) {
		if (isNew) {
			update.setOnInsert("createdDate", LocalDateTime.now());
			update.setOnInsert("createdByUser", authUser);
		}
		update.set("lastModifiedDate", LocalDateTime.now());
		update.set("modifiedByUser", authUser);
		return update;
	}

	private Map<String, String> getDateRangeForPeriod(String period, String yearStr, String month) {
		Map<String, String> dateRange = new HashMap<>();
		int year = yearStr != null ? Integer.parseInt(yearStr) : LocalDate.now().getYear();

		if ("Year".equalsIgnoreCase(period)) {
			dateRange.put("fromDate", LocalDate.of(year, 1, 1).toString());
			dateRange.put("toDate", LocalDate.of(year, 12, 31).toString());
		} else {
			Month monthEnum = month != null ? Month.valueOf(month.toUpperCase()) : LocalDate.now().getMonth();
			LocalDate fromDate = LocalDate.of(year, monthEnum, 1);
			dateRange.put("fromDate", fromDate.toString());
			dateRange.put("toDate", fromDate.withDayOfMonth(fromDate.lengthOfMonth()).toString());
		}
		return dateRange;
	}

	private double getDoubleValue(Object value) {
		if (value == null)
			return 0.0;
		if (value instanceof Number)
			return ((Number) value).doubleValue();
		try {
			return Double.parseDouble(value.toString());
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	private BulkWriteResult combineBulkResults(BulkWriteResult masterResult, BulkWriteResult granterResult) {
		return new BulkWriteResult() {
			@Override
			public int getInsertedCount() {
				return masterResult.getInsertedCount() + granterResult.getInsertedCount();
			}

			@Override
			public int getMatchedCount() {
				return masterResult.getMatchedCount() + granterResult.getMatchedCount();
			}

			@Override
			public int getModifiedCount() {
				return masterResult.getModifiedCount() + granterResult.getModifiedCount();
			}

			@Override
			public List<BulkWriteUpsert> getUpserts() {
				List<BulkWriteUpsert> combinedUpserts = new ArrayList<>();
				combinedUpserts.addAll(masterResult.getUpserts());
				combinedUpserts.addAll(granterResult.getUpserts());
				return combinedUpserts;
			}

			@Override
			public boolean wasAcknowledged() {
				return masterResult.wasAcknowledged() && granterResult.wasAcknowledged();
			}

			@Override
			public int getDeletedCount() {
				return masterResult.getDeletedCount() + granterResult.getDeletedCount();
			}

			@Override
			public List<BulkWriteInsert> getInserts() {
				List<BulkWriteInsert> combinedUpserts = new ArrayList<>();
				combinedUpserts.addAll(masterResult.getInserts());
				combinedUpserts.addAll(granterResult.getInserts());
				return combinedUpserts;
			}
		};
	}

}
