package com.hepl.budgie.service.impl.attendancemanagement;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.attendancemanagement.AttendanceMusterDTO;
import com.hepl.budgie.dto.attendancemanagement.AttendanceOverride;
import com.hepl.budgie.dto.attendancemanagement.BulkOverrideDTO;
import com.hepl.budgie.dto.attendancemanagement.LopDTO;
import com.hepl.budgie.dto.attendancemanagement.MusterHistoryDeleteDto;
import com.hepl.budgie.entity.attendancemanagement.AttendanceData;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInfo;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInformationHepl;
import com.hepl.budgie.entity.attendancemanagement.AttendanceMuster;
import com.hepl.budgie.entity.attendancemanagement.AttendancePunchInformation;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;
import com.hepl.budgie.entity.attendancemanagement.DailyAttendance;
import com.hepl.budgie.entity.attendancemanagement.RosterDetails;
import com.hepl.budgie.entity.attendancemanagement.ShiftMaster;
import com.hepl.budgie.entity.attendancemanagement.ShiftRoster;
import com.hepl.budgie.entity.attendancemanagement.WeekEnd;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.entity.settings.Holiday;
import com.hepl.budgie.entity.userinfo.BasicDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.attendancemanagement.AttendanceInformationRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceMusterRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendancePunchInformationRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceWeekendPolicyRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftMasterRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftRosterRepository;
import com.hepl.budgie.repository.master.HolidayRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.attendancemanagement.AttendanceMusterService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttendanceMusterServiceImpl implements AttendanceMusterService {

    private final AttendanceMusterRepository attendanceMusterRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private final PayrollLockMonthRepository payrollLockMonthRepository;
    private final UserInfoRepository userInfoRepository;
    private final AttendanceInformationRepository attendanceInformationRepository;
    private final ShiftMasterRepository shiftMasterRepository;
    private final ShiftRosterRepository shiftRosterRepository;
    private final HolidayRepository holidayRepository;
    private final AttendanceWeekendPolicyRepository weekendPolicyRepository;
    private final AttendancePunchInformationRepository attendancePunchInformationRepository;

    @Override
    public List<AttendanceMusterDTO> getAttendanceMuster(String empId, String reviewer, String repManager,
            String payrollStatus, String month) {

        log.info("fetch attendance muster data");
        String orgId = jwtHelper.getOrganizationCode();

        LocalDate date = LocalDate.now();
        String currentMonth = DateTimeFormatter.ofPattern("MM-yyyy").format(date);
        String monthYear = (month == null || month.isEmpty()) ? currentMonth : month;
        String finYear = getFinancialYear(monthYear);

        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId,
                "IN");
        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(monthYear))
                .findFirst()
                .orElse(null);

        LocalDate startDate = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return attendanceMusterRepository.fetchallEmployeeAttendanceMuster(empId, reviewer, repManager, payrollStatus,
                monthYear, startDate, endDate, orgId,
                mongoTemplate);
    }

    public String getFinancialYearMonth(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int financialYear = (month >= 4) ? year : year - 1;
        return String.format("%02d-%d", month, financialYear + 1);
    }

    @Override
    public AttendanceMuster addLopForEmployee(LopDTO lop) {

        log.info("add lop for employee");
        String orgId = jwtHelper.getOrganizationCode();
        UserInfo user = userInfoRepository.findByEmpId(lop.getEmpId()).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND);
        }
        return attendanceMusterRepository.addLopForEmployee(lop, orgId, mongoTemplate);
    }

    @Override
    public void saveAttendanceMusterForEmployee(String monthYear, boolean isAll, List<String> empId) {

        log.info("save attendance muster for employee");
        String orgId = jwtHelper.getOrganizationCode();
        String finYear = getFinancialYear(monthYear);
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId,
                "IN");
        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(monthYear))
                .findFirst()
                .orElse(null);

        LocalDate startDate = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        attendanceMusterRepository.saveEmployeeAttendanceMuster(empId, isAll, monthYear, startDate, endDate, orgId,
                mongoTemplate);
    }

    private String getFinancialYear(String monthYear) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth yearMonth = YearMonth.parse(monthYear, formatter);
        int month = yearMonth.getMonthValue();
        int year = yearMonth.getYear();
        int finStart, finEnd;

        if (month >= 4) {
            finStart = year;
            finEnd = year + 1;
        } else {
            finStart = year - 1;
            finEnd = year;
        }
        return String.format("%d-%d", finStart, finEnd);
    }

    @Override
    public List<AttendanceMusterDTO> getEmployeeAttendanceMuster(String employeeId, String monthYear) {

        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();

        UserInfo user = null;
        if (employeeId == null) {
            user = userInfoRepository.findByEmpId(empId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND));
        } else {
            user = userInfoRepository.findByEmpId(employeeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND));
        }
        String primaryId = user.getSections().getHrInformation().getPrimary().getManagerId();
        String secondaryId = user.getSections().getHrInformation().getSecondary().getManagerId();
        String reviewerId = user.getSections().getHrInformation().getReviewer().getManagerId();
        if (employeeId != null) {
            if (!primaryId.equals(empId) && !secondaryId.equals(empId) && !reviewerId.equals(empId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EMPLOYEE_NOT_REVIEWER);
            }
        }
        final String currentMonth = (monthYear == null || monthYear.isEmpty())
                ? DateTimeFormatter.ofPattern("MM-yyyy").format(LocalDate.now())
                : monthYear;

        String finYear = getFinancialYear(currentMonth);

        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId,
                "IN");
        if (payrollLock == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_LOCK_NOT_FOUND);
        }
        final String payrollMonthYear = monthYear;

        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(payrollMonthYear))
                .findFirst()
                .orElse(null);

        LocalDate startDate = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (monthYear != null && employeeId != null) {
            return attendanceMusterRepository.employeeMusterList(employeeId, monthYear, startDate, endDate, orgId,
                    mongoTemplate);
        } else {
            return attendanceMusterRepository.employeeAttendanceMuster(monthYear, startDate, endDate, orgId, empId,
                    mongoTemplate);
        }
    }

    @Override
    public AttendanceMusterDTO employeeMuster(String empId, String monthYear) {

        String orgId = jwtHelper.getOrganizationCode();
        String employeeId = jwtHelper.getUserRefDetail().getEmpId();

        UserInfo user = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND));
        String primaryId = user.getSections().getHrInformation().getPrimary().getManagerId();
        String secondaryId = user.getSections().getHrInformation().getSecondary().getManagerId();
        String reviewerId = user.getSections().getHrInformation().getReviewer().getManagerId();

        if (!primaryId.equals(employeeId) && !secondaryId.equals(employeeId) && !reviewerId.equals(employeeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EMPLOYEE_NOT_REVIEWER);
        }
        String finYear = getFinancialYear(monthYear);
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId,
                "IN");
        if (payrollLock == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_LOCK_NOT_FOUND);
        }
        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(monthYear))
                .findFirst()
                .orElse(null);

        LocalDate startDate = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return attendanceMusterRepository.employeeMuster(empId, monthYear, startDate, endDate, orgId, mongoTemplate);
    }

    @Override
    public List<Map<String, String>> fetchEmployeeList() {

        log.info("fetch employee list");
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        List<UserInfo> users = userInfoRepository.findReportingUsers(empId, mongoTemplate);
        return users.stream()
                .map(user -> {
                    Map<String, String> employeeMap = new HashMap<>();
                    employeeMap.put("empId", user.getEmpId());

                    BasicDetails basicDetails = user.getSections().getBasicDetails();
                    if (basicDetails != null) {
                        String empName = Stream
                                .of(basicDetails.getFirstName(), basicDetails.getLastName())
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(" "));
                        employeeMap.put("empName", empName.trim());
                    } else {
                        employeeMap.put("empName", "");
                    }
                    return employeeMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BulkOverrideDTO> getOverrideEmployeeDetails(String empId, String monthYear) {

        log.info("fetch employee details for bulk override");
        String orgId = jwtHelper.getOrganizationCode();
        if (empId == null || empId.trim().isEmpty() || monthYear == null || monthYear.trim().isEmpty()) {
            return Collections.emptyList();
        }
        UserInfo user = userInfoRepository.findByEmpId(empId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND));
        LocalDate doj = user.getSections().getWorkingInformation().getDoj().toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth yearMonth = YearMonth.parse(monthYear, formatter);
        YearMonth previousMonth = yearMonth.minusMonths(1);
        String previousMonthYear = previousMonth.format(formatter);
        String finYear = getFinancialYear(monthYear);
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId,
                "IN");
        if (payrollLock == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_LOCK_NOT_FOUND);
        }
        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(monthYear))
                .findFirst()
                .orElse(null);

        LocalDate startDate = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (doj.isAfter(startDate)) {
            startDate = doj;
        }
        List<String> monthYears = Arrays.asList(monthYear, previousMonthYear);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        List<String> formattedMonthYears = monthYears.stream()
        .map(m -> LocalDate.parse("01-" + m, DateTimeFormatter.ofPattern("dd-MM-yyyy")))
        .map(outputFormatter::format)
        .collect(Collectors.toList());

        List<AttendanceInformationHepl> attendanceRecords = attendanceInformationRepository.findByempIdAndMonths(empId,
                formattedMonthYears,
                orgId, mongoTemplate);
        Map<LocalDate, AttendanceInfo> attendanceMap = new HashMap<>();
        for (AttendanceInformationHepl record : attendanceRecords) {
            if (record.getAttendanceInfo() != null) {
                for (AttendanceInfo info : record.getAttendanceInfo()) {
                    LocalDate attendanceDate = LocalDate.parse(info.getAttendanceDate());
                    attendanceMap.put(attendanceDate, info);
                }
            }
        }
        ShiftRoster roster = shiftRosterRepository.findByMonthYearAndEmpId(monthYear, empId, orgId, mongoTemplate);
        List<BulkOverrideDTO> overrideList = new ArrayList<>();
        String yearM = yearMonth.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        String previousYearM = previousMonth.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        List<AttendanceWeekendPolicy> weekendPolicies = weekendPolicyRepository.findByMonthYearBtw(previousYearM,
        yearM, orgId, mongoTemplate);
        Set<LocalDate> offDates = new HashSet<>();

        for (AttendanceWeekendPolicy weekendPolicy : weekendPolicies) {
            if (weekendPolicy.getWeek() != null) {
                for (WeekEnd week : weekendPolicy.getWeek()) {
                    if ("OFF".equalsIgnoreCase(week.getSatStatus())) {
                        offDates.add(LocalDate.parse(week.getSatDate()));
                    }
                    if ("OFF".equalsIgnoreCase(week.getSunStatus())) {
                        offDates.add(LocalDate.parse(week.getSunDate()));
                    }
                }
            }
        }
        List<Holiday> holidays = holidayRepository.findByDateBetween(
                orgId, startDate, endDate, mongoTemplate);
        Set<LocalDate> holidayDates = holidays.stream().map(Holiday::getDate).collect(Collectors.toSet());
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            BulkOverrideDTO dto = new BulkOverrideDTO();
            dto.setDate(date.toString());
            String shiftCode = "S1";
            ShiftMaster shiftMaster = null;
            if (roster != null) {
                for (RosterDetails rosterDetails : roster.getRosterDetails()) {
                    if (rosterDetails.getDate().equals(date)) {
                        shiftCode = rosterDetails.getShift();
                        shiftMaster = shiftMasterRepository.findByShiftCode(shiftCode, orgId, mongoTemplate);
                        break;
                    }
                }
            }
            if (shiftMaster == null && user != null && user.getSections() != null
                    && user.getSections().getWorkingInformation() != null) {
                String shift = user.getSections().getWorkingInformation().getShift();
                shiftMaster = shiftMasterRepository.findByShiftName(shift, orgId, mongoTemplate);
                if (shiftMaster != null) {
                    shiftCode = shiftMaster.getShiftCode();
                }
            }
            AttendanceInfo info = attendanceMap.get(date);
            if (info == null) {
                dto.setFirstIn("");
                dto.setLastOut("");
                dto.setShiftCode(shiftCode);
                if(offDates.contains(date)) {
                    dto.setTittle("OFF");
                    dto.setSession1("OFF");
                    dto.setSession2("OFF");
                }else if(holidayDates.contains(date)) {
                    dto.setTittle("H");
                    dto.setSession1("H");
                    dto.setSession2("H");
                }else{
                    dto.setSession1("A");
                    dto.setSession2("A");
                }
            } else {
                dto.setFirstIn(info.getInTime() != null ? info.getInTime() : "");
                dto.setLastOut(info.getOutTime() != null ? info.getOutTime() : "");
                dto.setShiftCode(shiftCode);
                String attendanceData = info.getAttendanceData();
                if ("P".equals(attendanceData)) {
                    dto.setSession1("P");
                    dto.setSession2("P");
                } else if ("A:P".equals(attendanceData)) {
                    dto.setSession1("A");
                    dto.setSession2("P");
                } else if ("P:A".equals(attendanceData)) {
                    dto.setSession1("P");
                    dto.setSession2("A");
                } else {
                    dto.setSession1("A");
                    dto.setSession2("A");
                }
            }
            overrideList.add(dto);
        }
        return overrideList;
    }

    @Override
    public void updateOverride(AttendanceOverride data) {

        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        attendanceInformationRepository.updateOverride(empId, data.getEmpId(), data.getMonthYear(),
                data.getOverrideList(),
                orgId, mongoTemplate);

    }

    @Override
    public Map<String, List<String>> bulkImport(MultipartFile file) {
        String orgId = jwtHelper.getOrganizationCode();
        Map<String, List<String>> errors = new HashMap<>();
        List<AttendanceMuster> validMusterList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null)
                throw new RuntimeException("Header row is missing");

            // Step 1: Build header to column index map
            Map<String, Integer> headerIndexMap = new HashMap<>();
            List<String> dateHeaders = new ArrayList<>();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                String headerVal = (cell != null) ? cell.toString().trim() : "";
                if (!headerVal.isEmpty()) {
                    headerIndexMap.put(headerVal, i);
                    if (headerVal.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
                        dateHeaders.add(headerVal);
                    }
                }
            }

            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM-yyyy");

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;

                try {
                    String empId = getCellStringValue(row.getCell(headerIndexMap.getOrDefault("Employee", -1)));
                    String month = getCellStringValue(row.getCell(headerIndexMap.getOrDefault("Month", -1)));

                    if (empId == null || month == null) {
                        errors.computeIfAbsent("Missing Required Fields", k -> new ArrayList<>())
                                .add("Row " + (r + 1) + ": Missing Employee or Month");
                        continue;
                    }

                    List<DailyAttendance> attendanceList = new ArrayList<>();
                    LocalDate fromDate = null, toDate = null;

                    for (String dateStr : dateHeaders) {
                        int colIndex = headerIndexMap.get(dateStr);
                        String value = (row.getCell(colIndex) != null
                                && row.getCell(colIndex).getCellType() != CellType.BLANK)
                                        ? row.getCell(colIndex).toString().trim()
                                        : null;

                        attendanceList.add(new DailyAttendance(dateStr, value));

                        LocalDate currentDate = LocalDate.parse(dateStr, dateFormatter);
                        if (fromDate == null)
                            fromDate = currentDate;
                        toDate = currentDate;
                    }

                    Optional<UserInfo> userOpt = userInfoRepository.findByEmpId(empId);
                    if (userOpt.isEmpty()) {
                        errors.computeIfAbsent("Employee Not Found", k -> new ArrayList<>())
                                .add("Row " + (r + 1) + ": Employee ID " + empId + " not found");
                        continue;
                    }

                    UserInfo user = userOpt.get();
                    String empName = user.getSections().getBasicDetails().getFirstName() + " " +
                            user.getSections().getBasicDetails().getLastName();
                    String designation = user.getSections().getWorkingInformation().getDesignation();
                    String doj = user.getSections().getWorkingInformation().getDoj().toString();
                    String location = user.getSections().getWorkingInformation().getWorkLocation();

                    String currentMonth = monthFormatter.format(fromDate);
                    String monthYear = (month.isEmpty()) ? currentMonth : month;
                    String finYear = getFinancialYear(monthYear);

                    AttendanceMuster muster = new AttendanceMuster();
                    muster.setEmpId(empId);
                    muster.setEmpName(empName);
                    muster.setDesignation(designation);
                    muster.setDoj(doj);
                    muster.setWorkLocation(location);
                    muster.setMonthYear(monthYear);
                    muster.setFromDate(fromDate);
                    muster.setToDate(toDate);
                    muster.setFinYear(finYear);
                    muster.setAttendanceInfo(attendanceList);

                    // Set summary fields dynamically
                    muster.setTotalPresent(getNullableDoubleFromHeader(row, headerIndexMap, "Total Present"));
                    muster.setTotalLop(getNullableDoubleFromHeader(row, headerIndexMap, "Total Lop"));
                    muster.setTotalWeekOff(getNullableDoubleFromHeader(row, headerIndexMap, "Week Off"));
                    muster.setTotalHolidays(getNullableDoubleFromHeader(row, headerIndexMap, "Total Holidays"));
                    muster.setTotalDays(getNullableDoubleFromHeader(row, headerIndexMap, "Total Days"));
                    muster.setTotalSick(getNullableDoubleFromHeader(row, headerIndexMap, "Sick Leave"));
                    muster.setTotalCasualLeave(getNullableDoubleFromHeader(row, headerIndexMap, "Casual Leave"));
                    muster.setTotalLeave(getNullableDoubleFromHeader(row, headerIndexMap, "Total Leave"));

                    // Optional/Default summary fields
                    muster.setLopReversal(0.0);
                    muster.setRestDay(0.0);
                    muster.setOnDuty(0.0);

                    validMusterList.add(muster);

                } catch (Exception e) {
                    errors.computeIfAbsent("Parsing Error", k -> new ArrayList<>())
                            .add("Row " + (r + 1) + ": " + e.getMessage());
                }
            }

            if (!validMusterList.isEmpty()) {
                attendanceMusterRepository.bulkSave(mongoTemplate, orgId, validMusterList);
            }

        } catch (Exception e) {
            errors.computeIfAbsent("File Error", k -> new ArrayList<>())
                    .add("Failed to process file: " + e.getMessage());
        }

        return errors;
    }

    private Double getNullableDoubleFromHeader(Row row, Map<String, Integer> headerIndexMap, String key) {
        if (!headerIndexMap.containsKey(key))
            return 0.0;
        int index = headerIndexMap.get(key);
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK)
            return 0.0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else {
                String text = cell.toString().trim();
                return text.isEmpty() ? 0.0 : Double.parseDouble(text);
            }
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static String getCellStringValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Format the date as MM-yyyy
                    LocalDate localDate = cell.getDateCellValue().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return DateTimeFormatter.ofPattern("MM-yyyy").format(localDate);
                } else {
                    double d = cell.getNumericCellValue();
                    long longVal = (long) d;
                    return (d == longVal) ? String.valueOf(longVal) : String.valueOf(d);
                }
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return cell.toString().trim();
        }
    }

    @Override
    public List<Map<String,Object>> getOverrideHistory(String empId, String monthYear) {

        log.info("fetch employee details for bulk override");
        String orgId = jwtHelper.getOrganizationCode();

        if (empId == null || empId.trim().isEmpty() || monthYear == null || monthYear.trim().isEmpty()) {
            return Collections.emptyList();
        }

        UserInfo user = userInfoRepository.findByEmpId(empId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND));
        LocalDate doj = user.getSections().getWorkingInformation().getDoj().toLocalDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth yearMonth = YearMonth.parse(monthYear, formatter);
        YearMonth previousMonth = yearMonth.minusMonths(1);
        String previousMonthYear = previousMonth.format(formatter);

        String finYear = getFinancialYear(monthYear);
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId, "IN");

        if (payrollLock == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_LOCK_NOT_FOUND);
        }

        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(monthYear))
                .findFirst()
                .orElse(null);

        LocalDate startDate = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (doj.isAfter(startDate)) {
            startDate = doj;
        }

        List<String> monthYears = Arrays.asList(monthYear, previousMonthYear);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<String> formattedMonthYears = monthYears.stream()
                .map(m -> LocalDate.parse("01-" + m, DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .map(outputFormatter::format)
                .collect(Collectors.toList());

        List<AttendanceInformationHepl> attendanceRecords = attendanceInformationRepository.findByempIdAndMonths(empId,
                formattedMonthYears, orgId, mongoTemplate);

        List<Map<String, Object>> overrideList = new ArrayList<>();

        for (AttendanceInformationHepl record : attendanceRecords) {
            if (record.getAttendanceInfo() != null) {
                for (AttendanceInfo info : record.getAttendanceInfo()) {
                    if ("OVERRIDE".equalsIgnoreCase(info.getOverride())) {
                        LocalDate attendanceDate = LocalDate.parse(info.getAttendanceDate());
                        if ((attendanceDate.isEqual(startDate) || attendanceDate.isAfter(startDate)) &&
                                (attendanceDate.isEqual(endDate) || attendanceDate.isBefore(endDate))) {

                            Map<String, Object> data = new HashMap<>();
                            data.put("empId", empId);
                            data.put("employeeName",user.getSections().getBasicDetails().getFirstName() + " " + user.getSections().getBasicDetails().getLastName());
                            data.put("date", attendanceDate.toString());
                            data.put("overriddenOn",info.getUpdatedAt().toString());
                            data.put("remark","");
                            if(info.getAttendanceData().equalsIgnoreCase("P")){
                                data.put("inTime", attendanceDate.toString()+"@" + "P");
                                data.put("outTime", attendanceDate.toString()+"@" + "P");
                                data.put("status","P");
                            }else if(info.getAttendanceData().equalsIgnoreCase("A:P")){
                                data.put("inTime", attendanceDate.toString()+"@"+ "A");
                                data.put("outTime", attendanceDate.toString()+"@" + "P");
                                data.put("status","A:P");
                            }else if(info.getAttendanceData().equalsIgnoreCase("P:A")) {
                                data.put("inTime", attendanceDate.toString()+"@" + "P");
                                data.put("outTime", attendanceDate.toString()+"@" + "A");
                                data.put("status","P:A");
                            }else{
                                data.put("inTime", attendanceDate.toString()+"@" + "A");
                                data.put("outTime", attendanceDate.toString()+"@" + "A");
                                data.put("status","A");
                            }
                            overrideList.add(data);
                        }
                    }
                }
            }
        }
        return overrideList;
    }

    @Override
    public GenericResponse<String> deleteAttendanceMuster(List<MusterHistoryDeleteDto> deleteHistory) {

        
        for (MusterHistoryDeleteDto dto : deleteHistory) {

            AttendanceInformationHepl optionalInfo = attendanceInformationRepository
                .findByEmpIdAndMonthYear(dto.getEmpId(), dto.getYearMonth(), jwtHelper.getOrganizationCode(), mongoTemplate);

            if (optionalInfo != null) {
                AttendanceInformationHepl info = optionalInfo;
                boolean updated = false;

                for (AttendanceInfo dayInfo : info.getAttendanceInfo()) {

                    if (dayInfo.getAttendanceDate().equals(dto.getAttendanceDate())) {
                        dayInfo.setOverride(null);
                        UserInfo user = userInfoRepository.findByEmpId(dto.getEmpId()).orElse(null);
                        ShiftMaster shift = shiftMasterRepository.findByShiftName(user.getSections().getWorkingInformation().getShift(), jwtHelper.getOrganizationCode(), mongoTemplate);
                        AttendancePunchInformation punch = attendancePunchInformationRepository.findByEmpIdAndMonthYear(dto.getEmpId(), dto.getYearMonth(), jwtHelper.getOrganizationCode(), mongoTemplate);

                        String recalculatedData = calculateAttendanceData(shift, punch, dto.getAttendanceDate());
                        dayInfo.setAttendanceData(recalculatedData);

                        attendanceInformationRepository.updateAttendance(dto.getEmpId(),dto.getYearMonth(),jwtHelper.getOrganizationCode(),mongoTemplate, recalculatedData, dto.getAttendanceDate());
                    }
                }
            }
        }

        return GenericResponse.success("Attendance history deleted successfully");
    }

    private String calculateAttendanceData(ShiftMaster shift, AttendancePunchInformation punch, String date) {

        String shiftIn = shift.getInTime().substring(0, 5);     // "HH:mm"
        String shiftOut = shift.getOutTime().substring(0, 5);   // "HH:mm"
    
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime shiftInTime = LocalTime.parse(shiftIn, timeFormatter);
        LocalTime shiftOutTime = LocalTime.parse(shiftOut, timeFormatter);
    
        if (shiftOutTime.isBefore(shiftInTime)) {
            shiftOutTime = shiftOutTime.plusHours(12);
        }
    
        for (AttendanceData data : punch.getAttendanceData()) {
            if (data.getDate().equals(date)) {

                List<String> inPunches = data.getPunchIn();
                List<String> outPunches = data.getPunchOut();
    
                if (inPunches.isEmpty() && outPunches.isEmpty()) {
                    return "A";
                }
    
                LocalTime firstIn = null;
                LocalTime lastOut = null;
    
                if (!inPunches.isEmpty()) {
                    firstIn = LocalTime.parse(inPunches.get(0), timeFormatter);
                }
                if (!outPunches.isEmpty()) {
                    lastOut = LocalTime.parse(outPunches.get(outPunches.size() - 1), timeFormatter);
                }
    
                boolean inMatch = firstIn != null && !firstIn.isAfter(shiftInTime.plusMinutes(5));  // grace period optional
                boolean outMatch = lastOut != null && !lastOut.isBefore(shiftOutTime.minusMinutes(5));
    
                if (inMatch && outMatch) return "P";
                if (inMatch) return "P:A";
                if (outMatch) return "A:P";
                return "A";
            }
        }
    
        return "A";
    }
    

}
