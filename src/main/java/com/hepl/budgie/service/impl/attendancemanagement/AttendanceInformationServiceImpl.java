package com.hepl.budgie.service.impl.attendancemanagement;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.attendancemanagement.AttendanceReportDTO;
import com.hepl.budgie.dto.attendancemanagement.PunchEntry;
import com.hepl.budgie.dto.attendancemanagement.PunchPair;
import com.hepl.budgie.entity.attendancemanagement.AttendanceCitpl;
import com.hepl.budgie.entity.attendancemanagement.AttendanceData;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayTypeHistory;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInfo;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInformationHepl;
import com.hepl.budgie.entity.attendancemanagement.AttendancePunchInformation;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;
import com.hepl.budgie.entity.attendancemanagement.RosterDetails;
import com.hepl.budgie.entity.attendancemanagement.ShiftMaster;
import com.hepl.budgie.entity.attendancemanagement.ShiftRoster;
import com.hepl.budgie.entity.attendancemanagement.UpdatedDayType;
import com.hepl.budgie.entity.attendancemanagement.WeekEnd;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leave.LeaveApplyDates;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.entity.settings.Holiday;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.attendancemanagement.AttendanceCitplRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceDayTypeHistoryRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceInformationRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendancePunchInformationRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceWeekendPolicyRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftMasterRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftRosterRepository;
import com.hepl.budgie.repository.leave.LeaveApplyRepo;
import com.hepl.budgie.repository.master.HolidayRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.attendancemanagement.AttendanceInformationService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Service
@Slf4j
public class AttendanceInformationServiceImpl implements AttendanceInformationService {

    private final AttendanceInformationRepository attendanceInformationRepository;
    private final AttendancePunchInformationRepository attendancePunchInformationRepository;
    private final AttendanceCitplRepository attendanceCitplRepository;
    private final PayrollLockMonthRepository payrollLockMonthRepository;
    private final UserInfoRepository userInfoRepository;
    private final ShiftMasterRepository shiftMasterRepository;
    private final AttendanceWeekendPolicyRepository weekendPolicyRepository;
    private final HolidayRepository holidayRepository;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final AttendanceDayTypeHistoryRepository attendanceDayTypeHistoryRepository;
    private final ShiftRosterRepository shiftRosterRepository;
    private final LeaveApplyRepo leaveApplyRepo;

    @Scheduled(cron = "0 30 14 * * *")
    @Override
    public void attendanceFile() throws Exception {

        String orgId = "ORG00001";
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String formattedDate = yesterday.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));

        String baseUrl = "https://dev.budgie.co.in/attendance_test/";
        String fileName = "HEPL_ATTENDANCE_" + formattedDate + ".csv";
        String fileUrl = baseUrl + fileName;

        try (InputStream inputStream = downloadFile(fileUrl)) {
            if (inputStream == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        AppMessages.FAILED_TO_DOWNLOAD + fileUrl);
            }
            processAttendanceData(orgId, inputStream);
        }
    }

    private void processAttendanceData(String orgId, InputStream inputStream) throws Exception {
        Map<String, AttendanceInformationHepl> attendanceMap = new HashMap<>();
        Map<String, AttendancePunchInformation> punchMap = new HashMap<>();
        Map<String, List<PunchEntry>> punchBufferMap = new HashMap<>();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            for (CSVRecord record : csvParser) {
                if (record.stream().allMatch(String::isEmpty))
                    continue;

                String empId = record.get(0).trim();
                String inOutStatus = record.get(2).trim();
                String dateTimeStr = record.get(4).trim();

                LocalDateTime dateTime = parseDateTime(dateTimeStr);
                if (dateTime == null) {
                    continue;
                }

                punchBufferMap.computeIfAbsent(empId, k -> new ArrayList<>())
                        .add(new PunchEntry(inOutStatus, dateTime));
            }
        }

        for (Map.Entry<String, List<PunchEntry>> entry : punchBufferMap.entrySet()) {
            String empId = entry.getKey();
            List<PunchEntry> punches = entry.getValue();

            punches.sort(Comparator.comparing(PunchEntry::getDateTime));
            List<PunchPair> pairedPunches = pairPunches(punches);

            for (PunchPair pair : pairedPunches) {
                processPairedPunch(orgId, empId, pair, attendanceMap, punchMap, null, new HashMap<>());
            }
        }

        attendanceInformationRepository.saveAttendanceData(mongoTemplate, orgId, attendanceMap.values(), null);
        attendancePunchInformationRepository.savePunchAttendanceData(mongoTemplate, orgId, punchMap.values(), null);
    }

    private InputStream downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return connection.getInputStream();
        } else {
            return null;
        }
    }

    // private void processAttendanceRow(String orgId, String empId, String
    // inOutStatus, String dateTimeStr,
    // Map<String, AttendanceInformationHepl> attendanceMap,
    // Map<String, AttendancePunchInformation> punchMap) {

    // LocalDateTime dateTime = parseDateTime(dateTimeStr);
    // if (dateTime == null) {
    // throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
    // AppMessages.INVALID_DATE_TIME);
    // }

    // String monthYear = dateTime.format(DateTimeFormatter.ofPattern("MM-yyyy"));
    // String attendanceDate = dateTime.toLocalDate().toString();
    // String punchTime = dateTime.toLocalTime().toString();

    // LocalDate attendanceLocalDate = dateTime.toLocalDate();
    // UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
    // String shiftCode = "S1";
    // ShiftMaster shiftMaster = null;
    // ShiftRoster roster = shiftRosterRepository.findByMonthYearAndEmpId(monthYear,
    // empId, orgId, mongoTemplate);
    // if (roster != null) {
    // for (RosterDetails rosterDetails : roster.getRosterDetails()) {
    // if (rosterDetails.getDate().equals(attendanceLocalDate)) {
    // shiftCode = rosterDetails.getShift();
    // shiftMaster = shiftMasterRepository.findByShiftCode(shiftCode, orgId,
    // mongoTemplate);
    // break;
    // }
    // }
    // }
    // if (shiftMaster == null && user != null && user.getSections() != null
    // && user.getSections().getWorkingInformation() != null) {
    // String shift = user.getSections().getWorkingInformation().getShift();
    // shiftMaster = shiftMasterRepository.findByShiftName(shift, orgId,
    // mongoTemplate);
    // if (shiftMaster != null) {
    // shiftCode = shiftMaster.getShiftCode();
    // }
    // }
    // AttendanceInformationHepl attendance = attendanceMap.computeIfAbsent(empId, k
    // -> {
    // AttendanceInformationHepl foundAttendance =
    // attendanceInformationRepository.findByEmpIdAndMonthYear(empId,
    // monthYear, orgId, mongoTemplate);
    // return (foundAttendance != null) ? foundAttendance
    // : new AttendanceInformationHepl(null, empId, monthYear, null, new
    // ArrayList<>());
    // });

    // List<AttendanceInfo> existingAttendanceList = attendance.getAttendanceInfo();
    // Optional<AttendanceInfo> existingInfoOpt = existingAttendanceList.stream()
    // .filter(a -> a.getInTime() != null && a.getOutTime() == null)
    // .findFirst();

    // if (existingInfoOpt.isPresent()) {
    // AttendanceInfo existingInfo = existingInfoOpt.get();
    // if ("Out".equalsIgnoreCase(inOutStatus)) {
    // existingInfo.setOutTime(punchTime);
    // } else {
    // existingInfo.setInTime(punchTime);
    // }
    // existingInfo.setShift(shiftCode);
    // calculateWorkHours(existingInfo, shiftMaster);
    // existingInfo.setAttendanceData(determineAttendanceStatus(existingInfo,
    // shiftMaster));
    // } else {
    // AttendanceInfo newInfo = new AttendanceInfo(attendanceDate,
    // "Out".equalsIgnoreCase(inOutStatus) ? null : punchTime,
    // "Out".equalsIgnoreCase(inOutStatus) ? punchTime : null,
    // shiftCode,
    // null,
    // "09:00:00",
    // null,
    // null,
    // "P",
    // "Bio Metric",
    // null, null, false, null, null);

    // existingAttendanceList.add(newInfo);
    // }

    // attendanceMap.put(empId, attendance);
    // AttendancePunchInformation punchInfo =
    // attendancePunchInformationRepository.findByEmpIdAndMonthYear(empId,
    // monthYear, orgId, mongoTemplate);
    // if (punchInfo == null) {
    // punchInfo = new AttendancePunchInformation(null, empId, monthYear, new
    // ArrayList<>());
    // }
    // punchMap.put(empId, punchInfo);

    // List<AttendanceData> punchDataList = punchInfo.getAttendanceData();
    // Optional<AttendanceData> existingPunchDataOpt = punchDataList.stream()
    // .filter(p -> p.getDate().equals(attendanceDate))
    // .findFirst();

    // if (existingPunchDataOpt.isPresent()) {
    // AttendanceData existingPunchData = existingPunchDataOpt.get();

    // if ("Out".equalsIgnoreCase(inOutStatus)) {
    // if (!existingPunchData.getPunchOut().isEmpty()) {
    // existingPunchData.getPunchOut().set(existingPunchData.getPunchOut().size() -
    // 1, punchTime);
    // } else {
    // existingPunchData.getPunchOut().add(punchTime);
    // }
    // } else {
    // if (!existingPunchData.getPunchIn().isEmpty()) {
    // existingPunchData.getPunchIn().set(existingPunchData.getPunchIn().size() - 1,
    // punchTime);
    // } else {
    // existingPunchData.getPunchIn().add(punchTime);
    // }
    // }
    // } else {
    // List<String> punchInList = new ArrayList<>();
    // List<String> punchOutList = new ArrayList<>();

    // if ("Out".equalsIgnoreCase(inOutStatus)) {
    // punchOutList.add(punchTime);
    // } else {
    // punchInList.add(punchTime);
    // }

    // AttendanceData newPunchData = new AttendanceData(attendanceDate, punchInList,
    // punchOutList, null, null);
    // punchDataList.add(newPunchData);
    // }
    // punchMap.put(empId, punchInfo);
    // }

    @Override
    public void processAndSaveAttendanceCitpl() {

        String orgId = "ORG00002";
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        String currentMonth = startDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));

        List<AttendanceCitpl> allPunches = attendanceCitplRepository.findByAttendanceDate(startDate, endDate);
        if (allPunches == null || allPunches.isEmpty()) {
            return;
        }

        Map<String, AttendanceInformationHepl> attendanceMap = new HashMap<>();
        Map<String, AttendancePunchInformation> punchMap = new HashMap<>();
        for (AttendanceCitpl punch : allPunches) {
            String empId = punch.getEmpId();
            String key = empId + "_" + currentMonth;
            String attendanceDate = punch.getAttendanceDate().toString();
            LocalDate attendanceLocalDate = punch.getAttendanceDate();

            UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
            String shiftCode = "S1";
            ShiftMaster shiftMaster = null;
            ShiftRoster roster = shiftRosterRepository.findByMonthYearAndEmpId(currentMonth, empId, orgId,
                    mongoTemplate);
            if (roster != null) {
                for (RosterDetails rosterDetails : roster.getRosterDetails()) {
                    if (rosterDetails.getDate().equals(attendanceLocalDate)) {
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
            AttendanceInformationHepl attendance = attendanceMap.computeIfAbsent(key, k -> {
                AttendanceInformationHepl existingAttendance = attendanceInformationRepository
                        .findByEmpIdAndMonthYear(empId, currentMonth, orgId, mongoTemplate);
                if (existingAttendance != null) {
                    return existingAttendance;
                }
                AttendanceInformationHepl newAttendance = new AttendanceInformationHepl();
                newAttendance.setEmpId(empId);
                newAttendance.setMonthYear(currentMonth);
                newAttendance.setAttendanceInfo(new ArrayList<>());
                return newAttendance;
            });

            List<AttendanceInfo> existingAttendanceList = attendance.getAttendanceInfo();
            Optional<AttendanceInfo> existingInfoOpt = existingAttendanceList.stream()
                    .filter(a -> a.getAttendanceDate().equals(attendanceDate))
                    .findFirst();

            if (existingInfoOpt.isPresent()) {
                AttendanceInfo existingInfo = existingInfoOpt.get();
                if (punch.getPunchIn() != null) {
                    existingInfo.setInTime(punch.getPunchIn());
                }
                if (punch.getPunchOut() != null) {
                    existingInfo.setOutTime(punch.getPunchOut());
                }
                existingInfo.setShift(shiftCode);
                calculateWorkHours(existingInfo, shiftMaster);
                existingInfo.setAttendanceData(determineAttendanceStatus(existingInfo, shiftMaster));
            } else {
                AttendanceInfo newInfo = new AttendanceInfo(
                        attendanceDate,
                        punch.getPunchIn(),
                        punch.getPunchOut(),
                        shiftCode, null,
                        "09:00:00",
                        null,
                        null,
                        "P",
                        "Bio Metric",
                        null, null, null, null, null,null,null);

                calculateWorkHours(newInfo, shiftMaster);
                newInfo.setAttendanceData(determineAttendanceStatus(newInfo, shiftMaster));
                existingAttendanceList.add(newInfo);
            }

            AttendancePunchInformation punchInfo = punchMap.computeIfAbsent(key, k -> {
                AttendancePunchInformation newPunchInfo = new AttendancePunchInformation();
                newPunchInfo.setEmpId(empId);
                newPunchInfo.setMonthYear(currentMonth);
                newPunchInfo.setAttendanceData(new ArrayList<>());
                return newPunchInfo;
            });

            Optional<AttendanceData> existingDataOpt = punchInfo.getAttendanceData().stream()
                    .filter(a -> a.getDate().equals(attendanceDate))
                    .findFirst();

            if (existingDataOpt.isPresent()) {
                AttendanceData existingData = existingDataOpt.get();
                if (punch.getPunchIn() != null) {
                    existingData.getPunchIn().add(punch.getPunchIn());
                }
                if (punch.getPunchOut() != null) {
                    existingData.getPunchOut().add(punch.getPunchOut());
                }
            } else {
                AttendanceData newData = new AttendanceData();
                newData.setDate(attendanceDate);
                newData.setPunchIn(new ArrayList<>());
                newData.setPunchOut(new ArrayList<>());

                if (punch.getPunchIn() != null) {
                    newData.getPunchIn().add(punch.getPunchIn());
                }
                if (punch.getPunchOut() != null) {
                    newData.getPunchOut().add(punch.getPunchOut());
                }

                punchInfo.getAttendanceData().add(newData);
            }
        }

        if (!attendanceMap.isEmpty())

        {
            attendanceInformationRepository.saveAttendanceData(mongoTemplate, orgId, attendanceMap.values(), null);
        }
        if (!punchMap.isEmpty()) {
            attendancePunchInformationRepository.savePunchAttendanceData(mongoTemplate, orgId, punchMap.values(), null);
        }
    }

    private void calculateWorkHours(AttendanceInfo info, ShiftMaster shiftMaster) {
        if (info.getInTime() == null || info.getOutTime() == null) {
            info.setTotalWorkHours("00:00");
            info.setShortFallHours("09:00");
            info.setExcessHours("00:00");
            return;
        }

        if (shiftMaster == null) {
            shiftMaster = new ShiftMaster();
            shiftMaster.setInTime("09:00");
            shiftMaster.setOutTime("18:00");
        }

        LocalTime inTime = parseTime(info.getInTime());
        LocalTime outTime = parseTime(info.getOutTime());
        LocalTime shiftInTime = parseTime(shiftMaster.getInTime());
        LocalTime shiftOutTime = parseTime(shiftMaster.getOutTime());

        int inMinutes = inTime.toSecondOfDay() / 60;
        int outMinutes = outTime.toSecondOfDay() / 60;
        int shiftInMinutes = shiftInTime.toSecondOfDay() / 60;
        int shiftOutMinutes = shiftOutTime.toSecondOfDay() / 60;

        if (outMinutes < inMinutes) {
            outMinutes += 24 * 60;
        }
        if (shiftOutMinutes < shiftInMinutes) {
            shiftOutMinutes += 24 * 60;
        }

        int totalWorkMinutes = outMinutes - inMinutes;
        info.setTotalWorkHours(formatHoursMinutes(totalWorkMinutes));

        int expectedWorkMinutes = shiftOutMinutes - shiftInMinutes;
        info.setActualWorkHours(formatHoursMinutes(expectedWorkMinutes));

        if (totalWorkMinutes > expectedWorkMinutes) {
            int excessMinutes = totalWorkMinutes - expectedWorkMinutes;
            info.setExcessHours(formatHoursMinutes(excessMinutes));
            info.setShortFallHours("00:00");
        } else {
            int shortfallMinutes = expectedWorkMinutes - totalWorkMinutes;
            info.setShortFallHours(formatHoursMinutes(shortfallMinutes));
            info.setExcessHours("00:00");
        }
    }

    private String formatHoursMinutes(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            return LocalTime.of(0, 0, 0);
        }
        DateTimeFormatter formatterWithSeconds = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter formatterWithoutSeconds = DateTimeFormatter.ofPattern("HH:mm");

        try {
            return LocalTime.parse(time, formatterWithSeconds);
        } catch (DateTimeParseException e) {
            return LocalTime.parse(time, formatterWithoutSeconds);
        }
    }

    public String determineAttendanceStatus(AttendanceInfo info, ShiftMaster shiftMaster) {
        if (info.getInTime() == null || info.getOutTime() == null) {
            return "A";
        }

        LocalTime inTime = parseTimes(info.getInTime());
        LocalTime outTime = parseTimes(info.getOutTime());

        LocalTime shiftInTime = LocalTime.of(9, 0);
        LocalTime shiftOutTime = LocalTime.of(18, 0);
        Duration halfDayDuration = Duration.ofMinutes(270);
        if (shiftMaster != null) {
            shiftInTime = parseTimes(shiftMaster.getInTime());
            shiftOutTime = parseTimes(shiftMaster.getOutTime());

            String[] halfDayParts = shiftMaster.getHalfDayTime().split(":");
            halfDayDuration = Duration.ofMinutes(
                    Integer.parseInt(halfDayParts[0]) * 60L + Integer.parseInt(halfDayParts[1]));
        }
        Duration totalWorkDuration = getContinuousDuration(inTime, outTime);
        Duration shiftDuration = getContinuousDuration(shiftInTime, shiftOutTime);
        if (totalWorkDuration.compareTo(shiftDuration) >= 0) {
            return "P";
        }
        LocalTime firstHalfEnd = shiftInTime.plus(halfDayDuration);
        Duration firstHalfWork = getContinuousDuration(inTime, firstHalfEnd);
        Duration secondHalfWork = getContinuousDuration(firstHalfEnd, outTime);
        if (totalWorkDuration.compareTo(halfDayDuration) >= 0) {
            return (secondHalfWork.compareTo(firstHalfWork) > 0) ? "A:P" : "P:A";
        }
        return "A";
    }

    private static LocalTime parseTimes(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.parse(timeStr, formatter);
    }

    private Duration getContinuousDuration(LocalTime start, LocalTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (minutes < 0) {
            minutes += 1440;
        }
        return Duration.ofMinutes(minutes);
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("M/d/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
                DateTimeFormatter.ofPattern("M/d/yyyy h:mm a"),
                DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm:ss a"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Unrecognized date format: " + dateTimeStr);
    }

    @Override
    public List<Map<String, Object>> getEmployeeAttendance(String empId, String currentMonth) {

        String orgId = jwtHelper.getOrganizationCode();
        String finYear = getFinancialYear(currentMonth);
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId,
                "IN");

        if (payrollLock == null) {
            return Collections.emptyList();
        }

        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(currentMonth))
                .findFirst()
                .orElse(null);

        if (payrollMonth == null) {
            return Collections.emptyList();
        }

        UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
        if (user == null || user.getSections() == null || user.getSections().getWorkingInformation() == null) {
            return Collections.emptyList();
        }
        ZonedDateTime dojs = user.getSections().getWorkingInformation().getDoj();
        LocalDate doj = dojs.withZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDate();
        String empName = user.getSections().getBasicDetails().getFirstName() + " "
                + user.getSections().getBasicDetails().getLastName();

        LocalDate payrollStartDate = payrollMonth.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate startDate = doj.isAfter(payrollStartDate) ? doj : payrollStartDate;
        LocalDate payrollEndDate = payrollMonth.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        String startMonthYear = payrollStartDate.format(formatter);
        String endMonthYear = payrollEndDate.format(formatter);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalTime nowTime = LocalTime.now();

        LocalTime cutoffTime = LocalTime.of(13, 30);
        LocalDate adjustedEndDate = LocalTime.now().isBefore(cutoffTime) ? today.minusDays(1) : today;
        LocalDate endDate = adjustedEndDate.isBefore(payrollEndDate) ? adjustedEndDate : payrollEndDate;
        List<AttendanceInformationHepl> attendanceRecords = attendanceInformationRepository
                .findAttendanceRecords(mongoTemplate, empId, startDate.toString(), endDate.toString(), orgId);

        Map<LocalDate, AttendanceInfo> attendanceMap = attendanceRecords.stream()
                .flatMap(record -> record.getAttendanceInfo().stream())
                .collect(Collectors.toMap(
                        att -> LocalDate.parse(att.getAttendanceDate()),
                        att -> att,
                        (existing, replacement) -> existing));

        List<AttendanceWeekendPolicy> weekendPolicies = weekendPolicyRepository.findByMonthYearBtw(startMonthYear,
                endMonthYear, orgId, mongoTemplate);
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
        List<Holiday> holidays = holidayRepository.findByDateBetween(orgId, startDate, endDate, mongoTemplate);
        // Map<LocalDate, String> holidayMap = holidays.stream()
        //         .collect(Collectors.toMap(Holiday::getDate, Holiday::getOccasion));

        Map<LocalDate, String> holidayMap = holidays.stream()
        .collect(Collectors.toMap(
                Holiday::getDate,
                Holiday::getOccasion,
                (oldValue, newValue) -> oldValue + ", " + newValue
        ));

        AttendanceDayTypeHistory attendanceDayTypeHistory = attendanceDayTypeHistoryRepository
                .findByEmpIdAndMonthYear(empId, currentMonth, orgId, mongoTemplate);

        Map<LocalDate, String> updatedDayTypeIds = new HashMap<>();

        if (attendanceDayTypeHistory != null) {
            for (UpdatedDayType updatedDayType : attendanceDayTypeHistory.getUpdatedDayType()) {
                updatedDayTypeIds.put(updatedDayType.getDate(), updatedDayType.getDayTypes().getDayTypeId());
            }
        }
        List<LeaveApply> leaveApplications = leaveApplyRepo.findByEmpIdAndFromDateAndToDateBetween(
                empId, orgId, payrollStartDate, payrollEndDate, mongoTemplate);

        String leaveStatus = null;

        for (LeaveApply leave : leaveApplications) {
            String leaveType = leave.getLeaveType();
            String leaveCode;
            switch (leaveType.toLowerCase()) {
                case "casual leave":
                    leaveCode = "CL";
                    break;
                case "sick leave":
                    leaveCode = "SL";
                    break;
                case "privilege leave":
                    leaveCode = "PL";
                    break;
                default:
                    leaveCode = leaveType;
            }
            for (LeaveApplyDates leaveDate : leave.getLeaveApply()) {
                LocalDate leaveDay = LocalDate.parse(leaveDate.getDate());
                String status = leaveDate.getStatus();
                if (!leaveDay.isBefore(startDate) && !leaveDay.isAfter(endDate)) {
                    String session = "A";

                    if ("Approved".equalsIgnoreCase(status)) {
                        if ("1".equals(leaveDate.getFromSession()) && "1".equals(leaveDate.getToSession())) {
                            session = leaveCode + ":P";
                        } else if ("2".equals(leaveDate.getFromSession()) && "2".equals(leaveDate.getToSession())) {
                            session = "P:" + leaveCode;
                        } else {
                            session = leaveCode;
                        }
                        leaveStatus = "Approved";
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                        leaveStatus = "Rejected";
                    } else {
                        leaveStatus = "Pending";
                    }

                    AttendanceInfo attInfo = attendanceMap.getOrDefault(leaveDay, new AttendanceInfo());
                    attInfo.setAttendanceDate(leaveDay.toString());
                    attInfo.setAttendanceData(session);

                    attendanceMap.put(leaveDay, attInfo);
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            if (date.equals(yesterday) && nowTime.isBefore(cutoffTime)) {
                continue; 
            }
            
            Map<String, Object> attendanceEntry = new HashMap<>();
            attendanceEntry.put("empId", empId);
            attendanceEntry.put("empName", empName);
            attendanceEntry.put("attendanceDate", date.toString());
            String shiftCode = "S1";
            ShiftMaster shiftMaster = null;
            ShiftRoster roster = shiftRosterRepository.findByMonthYearAndEmpId(currentMonth, empId, orgId,
                    mongoTemplate);
            if (roster != null) {
                for (RosterDetails rosterDetails : roster.getRosterDetails()) {
                    if (rosterDetails.getDate().equals(date)) {
                        shiftCode = rosterDetails.getShift();
                        shiftMaster = shiftMasterRepository.findByShiftCode(shiftCode, orgId, mongoTemplate);
                        break;
                    }
                }
            }

            if (shiftMaster == null && user.getSections().getWorkingInformation() != null) {
                String shift = user.getSections().getWorkingInformation().getShift();
                shiftMaster = shiftMasterRepository.findByShiftName(shift, orgId, mongoTemplate);
                if (shiftMaster != null) {
                    shiftCode = shiftMaster.getShiftCode();
                }
            }
            attendanceEntry.put("shift", shiftMaster != null ? shiftMaster.getShiftCode() : shiftCode);

            String dayTypeId = updatedDayTypeIds.get(date);

            if (attendanceMap.containsKey(currentDate) &&
                    attendanceMap.get(currentDate).getAttendanceData().contains(":")) {
                AttendanceInfo att = attendanceMap.get(currentDate);
                attendanceEntry.put("attendanceData", att.getAttendanceData());
                attendanceEntry.put("leave", "Approved");
            } else if (leaveStatus != null && leaveApplications.stream()
                    .anyMatch(l -> l.getLeaveApply().stream()
                            .anyMatch(ld -> LocalDate.parse(ld.getDate()).equals(currentDate)))) {

                attendanceEntry.put("attendanceData", "A");
                attendanceEntry.put("leave", leaveStatus);
            } else {
                attendanceEntry.put("leave", null);
            }
            if (dayTypeId != null) {
                switch (dayTypeId) {
                    case "D1":
                        if (attendanceMap.containsKey(date)) {
                            AttendanceInfo att = attendanceMap.get(date);
                            attendanceEntry.put("attendanceData", att.getAttendanceData());
                            attendanceEntry.put("totalWorkHours", att.getTotalWorkHours());
                            attendanceEntry.put("actualWorkHours", att.getActualWorkHours());
                            attendanceEntry.put("shortFallHours", att.getShortFallHours());
                            attendanceEntry.put("excessHours", att.getExcessHours());
                            attendanceEntry.put("inTime", att.getInTime());
                            attendanceEntry.put("outTime", att.getOutTime());
                            attendanceEntry.put("regularization", att.getRegularization());
                        } else {
                            attendanceEntry.put("attendanceData", "A");
                            attendanceEntry.put("totalWorkHours", "00:00");
                            attendanceEntry.put("actualWorkHours", "00:00");
                            attendanceEntry.put("shortFallHours", "00:00");
                            attendanceEntry.put("excessHours", "00:00");
                            attendanceEntry.put("inTime", null);
                            attendanceEntry.put("outTime", null);
                            attendanceEntry.put("regularization", null);
                        }
                        break;
                    case "D2":
                        attendanceEntry.put("attendanceData", "OFF");
                        attendanceEntry.put("totalWorkHours", "00:00");
                        attendanceEntry.put("actualWorkHours", "00:00");
                        attendanceEntry.put("shortFallHours", "00:00");
                        attendanceEntry.put("excessHours", "00:00");
                        attendanceEntry.put("inTime", null);
                        attendanceEntry.put("outTime", null);
                        attendanceEntry.put("regularization", null);
                        break;
                    case "D3":
                    case "D4":
                        attendanceEntry.put("attendanceData", "H");
                        attendanceEntry.put("totalWorkHours", null);
                        attendanceEntry.put("actualWorkHours", null);
                        attendanceEntry.put("shortFallHours", null);
                        attendanceEntry.put("excessHours", null);
                        attendanceEntry.put("inTime", null);
                        attendanceEntry.put("outTime", null);
                        attendanceEntry.put("regularization", null);
                        break;
                }
            } else {
                if (holidayMap.containsKey(date)) {
                    attendanceEntry.put("attendanceData", "H");
                    attendanceEntry.put("holiday", holidayMap.get(date));
                } else if (offDates.contains(date)) {
                    attendanceEntry.put("attendanceData", "OFF");
                } else if (attendanceMap.containsKey(date)) {
                    AttendanceInfo att = attendanceMap.get(date);
                    attendanceEntry.put("attendanceData", att.getAttendanceData());
                    attendanceEntry.put("totalWorkHours", att.getTotalWorkHours());
                    attendanceEntry.put("actualWorkHours", att.getActualWorkHours());
                    attendanceEntry.put("shortFallHours", att.getShortFallHours());
                    attendanceEntry.put("excessHours", att.getExcessHours());
                    attendanceEntry.put("inTime", att.getInTime());
                    attendanceEntry.put("outTime", att.getOutTime());
                    attendanceEntry.put("regularization", att.getRegularization());
                } else if (date.isBefore(today)) {
                    attendanceEntry.put("attendanceData", "A");
                    attendanceEntry.put("totalWorkHours", null);
                    attendanceEntry.put("actualWorkHours", null);
                    attendanceEntry.put("shortFallHours", null);
                    attendanceEntry.put("excessHours", null);
                    attendanceEntry.put("inTime", null);
                    attendanceEntry.put("outTime", null);
                    attendanceEntry.put("regularization", null);
                } else {
                    attendanceEntry.put("attendanceData", null);
                    attendanceEntry.put("totalWorkHours", null);
                    attendanceEntry.put("actualWorkHours", null);
                    attendanceEntry.put("shortFallHours", null);
                    attendanceEntry.put("excessHours", null);
                    attendanceEntry.put("inTime", null);
                    attendanceEntry.put("outTime", null);
                    attendanceEntry.put("regularization", null);
                }
            }

            result.add(attendanceEntry);
        }

        return result;
    }

    public String getFinancialYear(String currentMonth) {
        int month = Integer.parseInt(currentMonth.split("-")[0]);
        int year = Integer.parseInt(currentMonth.split("-")[1]);

        if (month >= 4) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }

    @Override
    public Map<String, List<String>> processAttendance(LocalDate date) throws Exception {

        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        Map<String, List<String>> errors = new HashMap<>();

        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH));

        String baseUrl = "https://dev.budgie.co.in/attendance_test/";
        String fileName = "HEPL_ATTENDANCE_" + formattedDate + ".csv";
        String fileUrl = baseUrl + fileName;

        try (InputStream inputStream = downloadFile(fileUrl)) {
            if (inputStream == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FILE_NOT_FOUND_DATE);
            }

            if (orgId.equals("ORG00001")) {
                processAttendanceDataByDate(orgId, inputStream, empId, errors);
            } else if (orgId.equals("ORG00002")) {
                SaveAttendanceCitpl(orgId, date, empId);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            errors.put("Processing Error", Collections.singletonList("Unexpected error: " + e.getMessage()));
        }

        return errors;
    }

    private void processAttendanceDataByDate(String orgId, InputStream inputStream, String updatedBy,
            Map<String, List<String>> errors) throws Exception {

        Map<String, List<PunchEntry>> punchBufferMap = new HashMap<>();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            int rowNum = 1;
            for (CSVRecord record : csvParser) {
                try {
                    if (record.stream().allMatch(String::isEmpty)) {
                        rowNum++;
                        continue;
                    }

                    String empId = record.get(0).trim();
                    String inOutStatus = record.get(2).trim();
                    String dateTimeStr = record.get(4).trim();

                    LocalDateTime dateTime = parseDateTime(dateTimeStr);
                    if (dateTime == null) {
                        errors.computeIfAbsent("Row " + rowNum, k -> new ArrayList<>())
                                .add("Invalid date format: " + dateTimeStr);
                        continue;
                    }

                    punchBufferMap.computeIfAbsent(empId, k -> new ArrayList<>())
                            .add(new PunchEntry(inOutStatus, dateTime));

                } catch (Exception e) {
                    errors.computeIfAbsent("Row " + rowNum, k -> new ArrayList<>())
                            .add("Error processing row: " + e.getMessage());
                }
                rowNum++;
            }

        } catch (Exception e) {
            errors.put("File Error", Collections.singletonList("Invalid file format or corrupted file."));
            return;
        }

        // Final maps to pass to repository
        Map<String, AttendanceInformationHepl> attendanceMap = new HashMap<>();
        Map<String, AttendancePunchInformation> punchMap = new HashMap<>();

        for (Map.Entry<String, List<PunchEntry>> entry : punchBufferMap.entrySet()) {
            String empId = entry.getKey();
            List<PunchEntry> punches = entry.getValue();

            // Sort all punches by date-time
            punches.sort(Comparator.comparing(PunchEntry::getDateTime));

            List<PunchPair> pairedPunches = pairPunches(punches);

            for (PunchPair pair : pairedPunches) {
                processPairedPunch(orgId, empId, pair, attendanceMap, punchMap, updatedBy, errors);
            }
        }

        if (errors.isEmpty()) {
            attendanceInformationRepository.saveAttendanceData(mongoTemplate, orgId, attendanceMap.values(), updatedBy);
            attendancePunchInformationRepository.savePunchAttendanceData(mongoTemplate, orgId, punchMap.values(),
                    updatedBy);
        }
    }

    public void SaveAttendanceCitpl(String orgId, LocalDate date, String updatedBy) {

        LocalDate startDate = date;
        LocalDate endDate = date.plusDays(1);
        String currentMonth = startDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        // LocalDate attendanceLocalDate = dateTime.toLocalDate();

        List<AttendanceCitpl> allPunches = attendanceCitplRepository.findByAttendanceDate(startDate, endDate);
        if (allPunches == null || allPunches.isEmpty()) {
            return;
        }

        Map<String, AttendanceInformationHepl> attendanceMap = new HashMap<>();
        Map<String, AttendancePunchInformation> punchMap = new HashMap<>();
        for (AttendanceCitpl punch : allPunches) {
            String empId = punch.getEmpId();
            String key = empId + "_" + currentMonth;
            String attendanceDate = punch.getAttendanceDate().toString();

            UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
            String shiftCode = "S1";
            ShiftMaster shiftMaster = null;

            ShiftRoster roster = shiftRosterRepository.findByMonthYearAndEmpId(currentMonth, empId, orgId,
                    mongoTemplate);
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

            AttendanceInformationHepl attendance = attendanceMap.computeIfAbsent(key, k -> {
                AttendanceInformationHepl existingAttendance = attendanceInformationRepository
                        .findByEmpIdAndMonthYear(empId, currentMonth, orgId, mongoTemplate);
                if (existingAttendance != null) {
                    return existingAttendance;
                }
                AttendanceInformationHepl newAttendance = new AttendanceInformationHepl();
                newAttendance.setEmpId(empId);
                newAttendance.setMonthYear(currentMonth);
                newAttendance.setAttendanceInfo(new ArrayList<>());
                return newAttendance;
            });

            List<AttendanceInfo> existingAttendanceList = attendance.getAttendanceInfo();
            Optional<AttendanceInfo> existingInfoOpt = existingAttendanceList.stream()
                    .filter(a -> a.getAttendanceDate().equals(attendanceDate))
                    .findFirst();

            if (existingInfoOpt.isPresent()) {
                AttendanceInfo existingInfo = existingInfoOpt.get();
                if (punch.getPunchIn() != null) {
                    existingInfo.setInTime(punch.getPunchIn());
                }
                if (punch.getPunchOut() != null) {
                    existingInfo.setOutTime(punch.getPunchOut());
                }
                calculateWorkHours(existingInfo, shiftMaster);
                existingInfo.setShift(shiftCode);
                existingInfo.setAttendanceData(determineAttendanceStatus(existingInfo, shiftMaster));
                existingInfo.setUpdatedBy(updatedBy);
                existingInfo.setUpdatedAt(LocalDate.now());
            } else {
                AttendanceInfo newInfo = new AttendanceInfo(
                        attendanceDate,
                        punch.getPunchIn(),
                        punch.getPunchOut(),
                        shiftCode, null,
                        "09:00:00",
                        null,
                        null,
                        "P",
                        "Bio Metric",
                        null, null, null, null, null,null,null);

                calculateWorkHours(newInfo, shiftMaster);
                newInfo.setAttendanceData(determineAttendanceStatus(newInfo, shiftMaster));
                newInfo.setUpdatedBy(updatedBy);
                newInfo.setUpdatedAt(LocalDate.now());
                existingAttendanceList.add(newInfo);
            }
            AttendancePunchInformation punchInfo = punchMap.computeIfAbsent(key, k -> {
                AttendancePunchInformation newPunchInfo = new AttendancePunchInformation();
                newPunchInfo.setEmpId(empId);
                newPunchInfo.setMonthYear(currentMonth);
                newPunchInfo.setAttendanceData(new ArrayList<>());
                return newPunchInfo;
            });

            Optional<AttendanceData> existingDataOpt = punchInfo.getAttendanceData().stream()
                    .filter(a -> a.getDate().equals(attendanceDate))
                    .findFirst();

            if (existingDataOpt.isPresent()) {
                AttendanceData existingData = existingDataOpt.get();
                if (punch.getPunchIn() != null) {
                    existingData.getPunchIn().add(punch.getPunchIn());
                }
                if (punch.getPunchOut() != null) {
                    existingData.getPunchOut().add(punch.getPunchOut());
                }
            } else {
                AttendanceData newData = new AttendanceData();
                newData.setDate(attendanceDate);
                newData.setPunchIn(new ArrayList<>());
                newData.setPunchOut(new ArrayList<>());

                if (punch.getPunchIn() != null) {
                    newData.getPunchIn().add(punch.getPunchIn());
                }
                if (punch.getPunchOut() != null) {
                    newData.getPunchOut().add(punch.getPunchOut());
                }

                punchInfo.getAttendanceData().add(newData);
            }
        }

        if (!attendanceMap.isEmpty())

        {
            attendanceInformationRepository.saveAttendanceData(mongoTemplate, orgId, attendanceMap.values(), updatedBy);
        }
        if (!punchMap.isEmpty()) {
            attendancePunchInformationRepository.savePunchAttendanceData(mongoTemplate, orgId, punchMap.values(),
                    updatedBy);
        }
    }

    // private void processAttendanceRowByDate(String orgId, String empId, String
    // inOutStatus, String dateTimeStr,
    // Map<String, AttendanceInformationHepl> attendanceMap,
    // Map<String, AttendancePunchInformation> punchMap, String updatedBy,
    // Map<String, List<String>> errors, int rowNum) {

    // LocalDateTime dateTime = parseDateTime(dateTimeStr);
    // if (dateTime == null) {
    // errors.computeIfAbsent("Row " + rowNum, k -> new ArrayList<>()).add("Invalid
    // date format: " + dateTimeStr);
    // return;
    // }

    // String monthYear = dateTime.format(DateTimeFormatter.ofPattern("MM-yyyy"));
    // String attendanceDate = dateTime.toLocalDate().toString();
    // String punchTime = dateTime.toLocalTime().toString();
    // LocalDate attendanceLocalDate = dateTime.toLocalDate();

    // UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
    // // if (user == null) {
    // // errors.computeIfAbsent("Row " + rowNum, k -> new
    // ArrayList<>()).add("Employee
    // // ID not found: " + empId);
    // // return;
    // // }
    // String shiftCode = "S1";
    // ShiftMaster shiftMaster = null;

    // ShiftRoster roster = shiftRosterRepository.findByMonthYearAndEmpId(monthYear,
    // empId, orgId, mongoTemplate);
    // if (roster != null) {
    // for (RosterDetails rosterDetails : roster.getRosterDetails()) {
    // if (rosterDetails.getDate().equals(attendanceLocalDate)) {
    // shiftCode = rosterDetails.getShift();
    // shiftMaster = shiftMasterRepository.findByShiftCode(shiftCode, orgId,
    // mongoTemplate);
    // break;
    // }
    // }
    // }
    // if (shiftMaster == null && user != null && user.getSections() != null
    // && user.getSections().getWorkingInformation() != null) {
    // String shift = user.getSections().getWorkingInformation().getShift();
    // shiftMaster = shiftMasterRepository.findByShiftName(shift, orgId,
    // mongoTemplate);
    // if (shiftMaster != null) {
    // shiftCode = shiftMaster.getShiftCode();
    // }
    // }

    // AttendanceInformationHepl attendance = attendanceMap.computeIfAbsent(empId, k
    // -> {
    // AttendanceInformationHepl foundAttendance =
    // attendanceInformationRepository.findByEmpIdAndMonthYear(empId,
    // monthYear, orgId, mongoTemplate);
    // return (foundAttendance != null) ? foundAttendance
    // : new AttendanceInformationHepl(null, empId, monthYear, null, new
    // ArrayList<>());
    // });

    // List<AttendanceInfo> existingAttendanceList = attendance.getAttendanceInfo();
    // Optional<AttendanceInfo> existingInfoOpt = existingAttendanceList.stream()
    // .filter(a -> a.getInTime() != null && a.getOutTime() == null)
    // .findFirst();

    // if (existingInfoOpt.isPresent() && "Out".equalsIgnoreCase(inOutStatus)) {
    // AttendanceInfo existingInfo = existingInfoOpt.get();
    // existingInfo.setOutTime(punchTime);
    // existingInfo.setShift(shiftCode);
    // calculateWorkHours(existingInfo, shiftMaster);
    // existingInfo.setAttendanceData(determineAttendanceStatus(existingInfo,
    // shiftMaster));
    // } else if ("In".equalsIgnoreCase(inOutStatus)) {
    // AttendanceInfo newInfo = new AttendanceInfo(
    // attendanceDate,
    // punchTime,
    // null,
    // shiftCode,
    // null,
    // "09:00:00",
    // null,
    // null,
    // "P",
    // "Bio Metric",
    // null, null, false, null, null);
    // newInfo.setUpdatedBy(updatedBy);
    // newInfo.setUpdatedAt(LocalDate.now());
    // existingAttendanceList.add(newInfo);
    // }

    // attendanceMap.put(empId, attendance);
    // AttendancePunchInformation punchInfo =
    // attendancePunchInformationRepository.findByEmpIdAndMonthYear(empId,
    // monthYear, orgId, mongoTemplate);
    // if (punchInfo == null) {
    // punchInfo = new AttendancePunchInformation(null, empId, monthYear, new
    // ArrayList<>());
    // }
    // punchMap.put(empId, punchInfo);

    // List<AttendanceData> punchDataList = punchInfo.getAttendanceData();
    // Optional<AttendanceData> existingPunchDataOpt = punchDataList.stream()
    // .filter(p -> p.getDate().equals(attendanceDate))
    // .findFirst();

    // if (existingPunchDataOpt.isPresent()) {
    // AttendanceData existingPunchData = existingPunchDataOpt.get();

    // if ("Out".equalsIgnoreCase(inOutStatus)) {
    // if (!existingPunchData.getPunchOut().isEmpty()) {
    // existingPunchData.getPunchOut().set(existingPunchData.getPunchOut().size() -
    // 1, punchTime);
    // } else {
    // existingPunchData.getPunchOut().add(punchTime);
    // }
    // } else {
    // if (!existingPunchData.getPunchIn().isEmpty()) {
    // existingPunchData.getPunchIn().set(existingPunchData.getPunchIn().size() - 1,
    // punchTime);
    // } else {
    // existingPunchData.getPunchIn().add(punchTime);
    // }
    // }
    // existingPunchData.setUpdatedBy(updatedBy);
    // existingPunchData.setUpdatedAt(LocalDate.now());
    // } else {
    // List<String> punchInList = new ArrayList<>();
    // List<String> punchOutList = new ArrayList<>();

    // if ("Out".equalsIgnoreCase(inOutStatus)) {
    // punchOutList.add(punchTime);
    // } else {
    // punchInList.add(punchTime);
    // }

    // AttendanceData newPunchData = new AttendanceData(attendanceDate, punchInList,
    // punchOutList, null, null);
    // newPunchData.setUpdatedBy(updatedBy);
    // newPunchData.setUpdatedAt(LocalDate.now());
    // punchDataList.add(newPunchData);
    // }
    // punchMap.put(empId, punchInfo);
    // }

    @Override
    public Map<String, List<String>> attendanceBulkUpload(MultipartFile file) throws Exception {
        log.info("Attendance bulk upload...");
        String orgId = jwtHelper.getOrganizationCode();
        String updatedBy = jwtHelper.getUserRefDetail().getEmpId();
        Map<String, List<String>> errors = new HashMap<>();

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FILE_IS_EMPTY);
        }

        Map<String, List<PunchEntry>> punchBufferMap = new HashMap<>();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            int rowNum = 1;
            for (CSVRecord record : csvParser) {
                try {
                    if (record.stream().allMatch(String::isEmpty)) {
                        rowNum++;
                        continue;
                    }

                    String empId = record.get(0).trim();
                    String inOutStatus = record.get(2).trim();
                    String dateTimeStr = record.get(4).trim();

                    LocalDateTime dateTime = parseDateTime(dateTimeStr);
                    if (dateTime == null) {
                        errors.computeIfAbsent("Row " + rowNum, k -> new ArrayList<>())
                                .add("Invalid date format: " + dateTimeStr);
                        continue;
                    }

                    punchBufferMap.computeIfAbsent(empId, k -> new ArrayList<>())
                            .add(new PunchEntry(inOutStatus, dateTime));

                } catch (Exception e) {
                    errors.computeIfAbsent("Row " + rowNum, k -> new ArrayList<>())
                            .add("Error processing row: " + e.getMessage());
                }
                rowNum++;
            }

        } catch (Exception e) {
            errors.put("File Error", Collections.singletonList("Invalid file format or corrupted file."));
            return errors;
        }

        // Final maps to pass to repository
        Map<String, AttendanceInformationHepl> attendanceMap = new HashMap<>();
        Map<String, AttendancePunchInformation> punchMap = new HashMap<>();

        for (Map.Entry<String, List<PunchEntry>> entry : punchBufferMap.entrySet()) {
            String empId = entry.getKey();
            List<PunchEntry> punches = entry.getValue();

            // Sort all punches by date-time
            punches.sort(Comparator.comparing(PunchEntry::getDateTime));

            List<PunchPair> pairedPunches = pairPunches(punches);

            for (PunchPair pair : pairedPunches) {
                processPairedPunch(orgId, empId, pair, attendanceMap, punchMap, updatedBy, errors);
            }
        }

        if (errors.isEmpty()) {
            attendanceInformationRepository.saveAttendanceData(mongoTemplate, orgId, attendanceMap.values(), updatedBy);
            attendancePunchInformationRepository.savePunchAttendanceData(mongoTemplate, orgId, punchMap.values(),
                    updatedBy);
        }

        return errors;
    }

    public List<PunchPair> pairPunches(List<PunchEntry> punches) {
        List<PunchPair> result = new ArrayList<>();
        Deque<LocalDateTime> inQueue = new ArrayDeque<>();

        for (PunchEntry entry : punches) {
            if ("In".equalsIgnoreCase(entry.getStatus())) {
                inQueue.offer(entry.getDateTime());
            } else if ("Out".equalsIgnoreCase(entry.getStatus())) {
                if (!inQueue.isEmpty()) {
                    result.add(new PunchPair(inQueue.poll(), entry.getDateTime()));
                } else {
                    // Handle unmatched out as first punch (maybe log for now)
                    result.add(new PunchPair(null, entry.getDateTime()));
                }
            }
        }

        while (!inQueue.isEmpty()) {
            result.add(new PunchPair(inQueue.poll(), null));
        }

        return result;
    }

    public void processPairedPunch(String orgId, String empId, PunchPair pair,
            Map<String, AttendanceInformationHepl> attendanceMap,
            Map<String, AttendancePunchInformation> punchMap,
            String updatedBy,
            Map<String, List<String>> errors) {

        LocalDateTime inDateTime = pair.getInTime();
        LocalDateTime outDateTime = pair.getOutTime();
        LocalDate referenceDate = (inDateTime != null) ? inDateTime.toLocalDate()
                : (outDateTime != null) ? outDateTime.toLocalDate()
                        : null;

        if (referenceDate == null)
            return;

        String monthYear = referenceDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        String attendanceDate = referenceDate.toString();

        UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
        String shiftCode = "S1";
        ShiftMaster shiftMaster = null;

        ShiftRoster roster = shiftRosterRepository.findByMonthYearAndEmpId(monthYear, empId, orgId, mongoTemplate);
        if (roster != null) {
            for (RosterDetails rosterDetails : roster.getRosterDetails()) {
                if (rosterDetails.getDate().equals(referenceDate)) {
                    shiftCode = rosterDetails.getShift();
                    shiftMaster = shiftMasterRepository.findByShiftCode(shiftCode, orgId, mongoTemplate);
                    break;
                }
            }
        }

        if (shiftMaster == null && user != null && user.getSections() != null &&
                user.getSections().getWorkingInformation() != null) {
            String shift = user.getSections().getWorkingInformation().getShift();
            shiftMaster = shiftMasterRepository.findByShiftName(shift, orgId, mongoTemplate);
            if (shiftMaster != null) {
                shiftCode = shiftMaster.getShiftCode();
            }
        }

        AttendanceInformationHepl attendance = attendanceMap.computeIfAbsent(empId, k -> {
            AttendanceInformationHepl found = attendanceInformationRepository.findByEmpIdAndMonthYear(empId,
                    monthYear, orgId, mongoTemplate);
            return (found != null) ? found
                    : new AttendanceInformationHepl(null, empId, monthYear, null, new ArrayList<>());
        });

        AttendanceInfo info = new AttendanceInfo(
                attendanceDate,
                inDateTime != null ? inDateTime.toLocalTime().toString() : null,
                outDateTime != null ? outDateTime.toLocalTime().toString() : null,
                shiftCode,
                null,
                "09:00:00",
                null,
                null,
                "P",
                "Bio Metric",
                null, null, null, null, null,null,null);
        info.setUpdatedBy(updatedBy);
        info.setUpdatedAt(LocalDate.now());

        calculateWorkHours(info, shiftMaster);
        info.setAttendanceData(determineAttendanceStatus(info, shiftMaster));

        attendance.getAttendanceInfo().add(info);
        attendanceMap.put(empId, attendance);

        // Update punch map
        AttendancePunchInformation punchInfo = punchMap.computeIfAbsent(empId, k -> {
            AttendancePunchInformation found = attendancePunchInformationRepository.findByEmpIdAndMonthYear(empId,
                    monthYear, orgId, mongoTemplate);
            return (found != null) ? found : new AttendancePunchInformation(null, empId, monthYear, new ArrayList<>());
        });

        List<AttendanceData> punchDataList = punchInfo.getAttendanceData();
        AttendanceData punchData = punchDataList.stream()
                .filter(p -> p.getDate().equals(attendanceDate))
                .findFirst()
                .orElseGet(() -> {
                    AttendanceData newData = new AttendanceData(attendanceDate, new ArrayList<>(), new ArrayList<>(),
                            null, null);
                    punchDataList.add(newData);
                    return newData;
                });

        if (inDateTime != null) {
            String inTime = inDateTime.toLocalTime().toString();
            if (!punchData.getPunchIn().contains(inTime)) {
                punchData.getPunchIn().add(inTime);
            }
        }

        if (outDateTime != null) {
            String outTime = outDateTime.toLocalTime().toString();
            if (!punchData.getPunchOut().contains(outTime)) {
                punchData.getPunchOut().add(outTime);
            }
        }

        // Optional cleanup to remove accidental duplicates (if any)
        punchData.setPunchIn(deduplicateList(punchData.getPunchIn()));
        punchData.setPunchOut(deduplicateList(punchData.getPunchOut()));
        punchData.setUpdatedBy(updatedBy);
        punchData.setUpdatedAt(LocalDate.now());

        punchMap.put(empId, punchInfo);
    }

    private List<String> deduplicateList(List<String> list) {
        return list == null ? null : new ArrayList<>(new LinkedHashSet<>(list));
    }
    
    @Override
    public List<AttendanceReportDTO> getEmployeeAttendanceReport(String empId, LocalDate fromDate, LocalDate toDate) {

        log.info("Attendance report by date...");
        String orgId = jwtHelper.getOrganizationCode();
        Optional<UserInfo> user = userInfoRepository.findByEmpId(empId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.USER_NOT_FOUND);
        }
        String empName = user.get().getSections().getBasicDetails().getFirstName() + " "
                + user.get().getSections().getBasicDetails().getLastName();
        return attendanceInformationRepository.getEmployeeAttendanceReport(mongoTemplate, orgId, empId, fromDate,
                toDate, empName);
    }

    @Override
    public void saveAttendance(String location, boolean isSign, String remarks) {

        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        LocalDateTime date = LocalDateTime.now();
        String monthYear = date.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        String todayDate = date.toLocalDate().toString();
        String timeNow = date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
        String shiftCode = "S1";
        ShiftMaster shiftMaster = null;

        if (user != null && user.getSections() != null && user.getSections().getWorkingInformation() != null) {
            String shift = user.getSections().getWorkingInformation().getShift();
            shiftMaster = shiftMasterRepository.findByShiftName(shift, orgId, mongoTemplate);
            if (shiftMaster != null) {
                shiftCode = shiftMaster.getShiftCode();
            }
        }
        AttendanceInformationHepl existingRecord = attendanceInformationRepository.findByEmpIdAndMonthYear(empId,
                monthYear, orgId, mongoTemplate);
        if (existingRecord != null && existingRecord.getAttendanceInfo() != null) {
            Optional<AttendanceInfo> existingAttendance = existingRecord.getAttendanceInfo().stream()
                    .filter(att -> todayDate.equals(att.getAttendanceDate()))
                    .findFirst();
            if (existingAttendance.isPresent()) {
                AttendanceInfo attendanceInfo = existingAttendance.get();

                if (isSign) {
                    if (attendanceInfo.getInTime() != null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ALREADY_SIGNED_IN);
                    }
                    attendanceInfo.setInTime(timeNow);
                    attendanceInfo.setShift(shiftCode);
                } else {
                    if (attendanceInfo.getInTime() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PLEASE_SIGN_IN_FIRST);
                    }
                    attendanceInfo.setOutTime(timeNow);
                    calculateWorkHours(attendanceInfo, shiftMaster);
                    attendanceInfo.setAttendanceData(determineAttendanceStatus(attendanceInfo, shiftMaster));
                }
                attendanceInformationRepository.updateAttendance(mongoTemplate, orgId, empId, monthYear, todayDate,
                        attendanceInfo);
                return;
            }
        }
        if (!isSign) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PLEASE_SIGN_IN_FIRST);
        }

        AttendanceInfo newAttendance = createNewAttendance(shiftMaster, todayDate, timeNow, true, location, remarks);
        attendanceInformationRepository.saveAttendance(mongoTemplate, orgId, empId, monthYear, newAttendance);
    }

    private AttendanceInfo createNewAttendance
    (ShiftMaster shiftMaster, String todayDate, String timeNow,
            boolean isSign, String location, String remarks) {
        AttendanceInfo attendanceInfo = new AttendanceInfo();
        attendanceInfo.setAttendanceDate(todayDate);
        if (isSign) {
            attendanceInfo.setInTime(timeNow);
            attendanceInfo.setLocation(location);
            attendanceInfo.setRemark(remarks);
            attendanceInfo.setAttendanceSchema("Web Login");
        } else {
            attendanceInfo.setOutTime(timeNow);
            attendanceInfo.setActualWorkHours("09:00:00");
            attendanceInfo.setLocation(location);
            attendanceInfo.setRemark(remarks);
            calculateWorkHours(attendanceInfo, shiftMaster);
            attendanceInfo.setAttendanceData(determineAttendanceStatus(attendanceInfo, shiftMaster));
        }
        attendanceInfo.setUpdatedAt(LocalDate.now());
        return attendanceInfo;
    }

    @Override
    public Map<String, Object> getTodayAttendance() {
        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-yyyy"));
        
        AttendanceInformationHepl attendance = attendanceInformationRepository
            .findByEmpIdAndMonthYear(empId, monthYear, orgId, mongoTemplate);
    
        Map<String, Object> data = new HashMap<>();
    
        if (attendance != null && attendance.getAttendanceInfo() != null) {
            Optional<AttendanceInfo> todayAttendanceOpt = attendance.getAttendanceInfo().stream()
                .filter(att -> LocalDate.now().equals(LocalDate.parse(att.getAttendanceDate())))
                .findFirst();
    
            if (todayAttendanceOpt.isPresent()) {
                AttendanceInfo today = todayAttendanceOpt.get();
    
                data.put("empId", empId);
                data.put("shift", today.getShift());
                data.put("attendanceDate", today.getAttendanceDate());
                data.put("inTime", today.getInTime());
                data.put("outTime", today.getOutTime());
                data.put("attendanceData", today.getAttendanceData());
                data.put("regularization", today.getRegularization());
                data.put("actualWorkHours", today.getActualWorkHours());
                data.put("totalWorkHours", today.getTotalWorkHours());
                data.put("shortFallHours", today.getShortFallHours());
                data.put("excessHours", today.getExcessHours());
                data.put("location", today.getLocation());
            } else {
                return Collections.emptyMap();
            }
        } else {
            return Collections.emptyMap();
        }
    
        return data;
    }
    
}
