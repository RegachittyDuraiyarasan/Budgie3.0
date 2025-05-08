package com.hepl.budgie.service.impl.attendancemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.math3.analysis.function.Constant;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.DocumentOperators.Shift;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.regularization.RegularizationApproveDto;
import com.hepl.budgie.dto.regularization.RegularizationDto;
import com.hepl.budgie.dto.regularization.RegularizationInfoDto;
import com.hepl.budgie.entity.attendancemanagement.AppliedRegularization;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayTypeHistory;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInfo;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInformationHepl;
import com.hepl.budgie.entity.attendancemanagement.AttendanceRegularization;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;
import com.hepl.budgie.entity.attendancemanagement.ShiftMaster;
import com.hepl.budgie.entity.attendancemanagement.UpdatedDayType;
import com.hepl.budgie.entity.attendancemanagement.WeekEnd;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.entity.settings.Holiday;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.attendancemanagement.AttendanceDayTypeHistoryRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceInformationRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceRegularizationRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceWeekendPolicyRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftMasterRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveApplyRepository;
import com.hepl.budgie.repository.master.HolidayRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.MailService;
import com.hepl.budgie.service.TemplateService;
import com.hepl.budgie.service.attendancemanagement.AttendanceRegularizationService;
import com.hepl.budgie.utils.AppMessages;

import jakarta.mail.MessagingException;

import org.springframework.data.mongodb.core.query.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceRegularizationServiceImpl implements AttendanceRegularizationService {

    private final AttendanceRegularizationRepository attendanceRegularizationRepository;
    private final AttendanceInformationRepository attendanceInformationRepository;
    private final UserInfoRepository userInfoRepository;
    private final ShiftMasterRepository shiftMasterRepository;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final PayrollLockMonthRepository payrollLockMonthRepository;
    private final AttendanceWeekendPolicyRepository weekendPolicyRepository;
    private final HolidayRepository holidayRepository;
    private final LeaveApplyRepository leaveApplyRepository;
    private final TemplateService templateService;
    private final MailService mailService;

    @Override
    public AttendanceRegularization applyAttendanceRegularization(String empId, RegularizationDto regularizationDto) throws MessagingException {
    
        String org = jwtHelper.getOrganizationCode();
        String col = attendanceInformationRepository.getCollectionName(org);
    
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
    
        Map<String, List<RegularizationInfoDto>> groupedByMonth = regularizationDto.getRegularizationInfo().stream()
                .collect(Collectors.groupingBy(info -> info.getDate().format(monthYearFormatter)));
    
        List<AppliedRegularization> appliedRegularizations = new ArrayList<>();
        List<String> regularizationDates = new ArrayList<>();
    
        UserInfo userInfo = userInfoRepository.findByEmpId(empId).orElse(null);
        if (userInfo == null) throw new RuntimeException("User info not found for employee ID: " + empId);
    
        ShiftMaster shiftMaster = shiftMasterRepository.findByShiftName(
                userInfo.getSections().getWorkingInformation().getShift(), org, mongoTemplate);
    
        if (shiftMaster == null) {
            throw new RuntimeException("Shift master not found for employee ID: " + empId);
        }
    
        LocalTime shiftInTime = LocalTime.parse(shiftMaster.getInTime().substring(0, 5), timeFormatter);
        LocalTime shiftOutTime = LocalTime.parse(shiftMaster.getOutTime().substring(0, 5), timeFormatter);
        if (shiftOutTime.isBefore(shiftInTime)) {
            shiftOutTime = shiftOutTime.plusHours(12);
        }
    
        Duration actualWorkDuration = Duration.between(shiftInTime, shiftOutTime);
        String actualWorkHours = String.format("%02d:%02d:%02d",
                actualWorkDuration.toHours(),
                actualWorkDuration.toMinutesPart(),
                actualWorkDuration.toSecondsPart());
    
        for (Map.Entry<String, List<RegularizationInfoDto>> entry : groupedByMonth.entrySet()) {
            String monthYear = entry.getKey(); 
            List<RegularizationInfoDto> regList = entry.getValue();
    
            Query query = new Query(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));
            AttendanceInformationHepl attendanceRecords = mongoTemplate.findOne(query, AttendanceInformationHepl.class, col);
    
            if (attendanceRecords == null) {
                attendanceRecords = new AttendanceInformationHepl();
                attendanceRecords.setEmpId(empId);
                attendanceRecords.setMonthYear(monthYear);
                attendanceRecords.setAttendanceInfo(new ArrayList<>());
            }
    
            Set<String> existingAttendanceDates = attendanceRecords.getAttendanceInfo().stream()
                    .map(AttendanceInfo::getAttendanceDate)
                    .collect(Collectors.toSet());
    
            for (RegularizationInfoDto regInfo : regList) {
                String dateStr = regInfo.getDate().toString();
                String reasonText = regInfo.getReason() != null ? regInfo.getReason() : "No reason provided";
    
                if (!existingAttendanceDates.contains(dateStr)) {
                    LocalTime inTime = LocalTime.parse(regInfo.getInTime(), timeFormatter);
                    LocalTime outTime = LocalTime.parse(regInfo.getOutTime(), timeFormatter);
                    Duration workDuration = Duration.between(inTime, outTime);
    
                    String totalWorkHours = String.format("%02d:%02d:%02d",
                            workDuration.toHours(),
                            workDuration.toMinutesPart(),
                            workDuration.toSecondsPart());
    
                    AttendanceInfo newAttendance = new AttendanceInfo();
                    newAttendance.setAttendanceDate(dateStr);
                    newAttendance.setTotalWorkHours(totalWorkHours);
                    newAttendance.setActualWorkHours(actualWorkHours);
                    newAttendance.setInTime(regInfo.getInTime());
                    newAttendance.setOutTime(regInfo.getOutTime());
    
                    Duration excessDuration = workDuration.minus(actualWorkDuration);
                    String excessHours = excessDuration.isNegative() ? "00:00:00" : String.format("%02d:%02d:%02d",
                            excessDuration.toHours(),
                            excessDuration.toMinutesPart(),
                            excessDuration.toSecondsPart());
                    newAttendance.setExcessHours(excessHours);
    
                    Duration shortfallDuration = actualWorkDuration.minus(workDuration);
                    String shortFallHours = shortfallDuration.isNegative() ? "00:00:00" : String.format("%02d:%02d:%02d",
                            shortfallDuration.toHours(),
                            shortfallDuration.toMinutesPart(),
                            shortfallDuration.toSecondsPart());
                    newAttendance.setShortFallHours(shortFallHours);
    
                    newAttendance.setAttendanceData("A");
                    newAttendance.setRegularization("Pending");
    
                    attendanceRecords.getAttendanceInfo().add(newAttendance);
                    existingAttendanceDates.add(dateStr);
                }
    
                for (AttendanceInfo info : attendanceRecords.getAttendanceInfo()) {
                    if (dateStr.equals(info.getAttendanceDate()) &&
                            (info.getAttendanceData().equals("A") || info.getAttendanceData().equals("P:A") || info.getAttendanceData().equals("A:P"))) {
    
                        double totalHours = parseTotalWorkHours(info.getTotalWorkHours());
                        if (totalHours < 9) {
                            AppliedRegularization appliedReg = new AppliedRegularization();
                            appliedReg.setAttendanceDate(dateStr);
                            appliedReg.setStartTime(regInfo.getInTime());
                            appliedReg.setEndTime(regInfo.getOutTime());
                            appliedReg.setActualInTime(shiftInTime.toString());
                            appliedReg.setActualOutTime(shiftOutTime.toString());
                            appliedReg.setReason(reasonText);
                            appliedReg.setStatus("Pending");
                            appliedReg.setShift(shiftMaster.getShiftCode());
    
                            appliedRegularizations.add(appliedReg);
                            regularizationDates.add(dateStr);
                        }
                    }
                }
    
            }
    
            attendanceRecords.getAttendanceInfo().stream()
                    .filter(info -> regularizationDates.contains(info.getAttendanceDate()))
                    .forEach(info -> info.setRegularization("Pending"));
    
            mongoTemplate.save(attendanceRecords, col);
        }
    
        if (appliedRegularizations.isEmpty()) {
            throw new RuntimeException("No eligible records found for regularization.");
        }
    
        String colName = attendanceRegularizationRepository.getCollectionName(org);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-ss-SSS");
        String regularizationCode = LocalDateTime.now().format(formatter);
    
        AttendanceRegularization regularization = new AttendanceRegularization();
        regularization.setEmployeeId(empId);
        regularization.setEmployeeName(userInfo.getSections().getBasicDetails().getFirstName() + " " + userInfo.getSections().getBasicDetails().getLastName());
        regularization.setRegularizationCode("REG-" + regularizationCode);
        regularization.setRemarks(regularizationDto.getRemark());
        regularization.setAppliedDate(LocalDate.now());
        regularization.setNoOfDays(regularizationDates.size());
        regularization.setAppliedTo(regularizationDto.getAppliedTo());
        regularization.setRegularizationDates(regularizationDates);
        regularization.setAppliedRegularizations(appliedRegularizations);
        regularization.setStatus("Pending");
        regularization.setCreatedAt(LocalDateTime.now());
        regularization.setUpdatedAt(LocalDateTime.now());
    
        AttendanceRegularization savedRegularization = mongoTemplate.save(regularization, colName);
        // if(savedRegularization != null){
        //     UserInfo user = userInfoRepository.findByEmpId(regularizationDto.getAppliedTo()).orElse(null);
        //     String approverEmail = user.getSections().getWorkingInformation().getOfficialEmail();
        //     String approverName = user.getSections().getBasicDetails().getFirstName() + " " + user.getSections().getBasicDetails().getLastName();
        //     String employeeName = userInfo.getSections().getBasicDetails().getFirstName() + " " + userInfo.getSections().getBasicDetails().getLastName();
        //     String subject = "Attendance Regularization Request From "+employeeName+"["+empId+"] "+"||" +"Budgie";
        //     mailService.sendMailByTemplate(templateService.regularizationApply(regularization,employeeName,approverName),approverEmail,subject);
        // }
        return savedRegularization;
    }
    


    private double parseTotalWorkHours(String totalWorkHours) {
        if(totalWorkHours == null) return 0;
        try {
            return Double.parseDouble(totalWorkHours);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public AttendanceRegularization approvedRegularization(String empId, String regCode, String key, List<LocalDate> approvedDate, List<LocalDate> rejectedDate, String reason, List<String> remark,String month) throws MessagingException {

        String org = jwtHelper.getOrganizationCode();
        String colName = attendanceRegularizationRepository.getCollectionName(org);
        Query query = new Query(Criteria.where("regularizationCode").is(regCode));

        AttendanceRegularization regularization = mongoTemplate.findOne(query, AttendanceRegularization.class, colName);

        if (regularization == null || regularization.getAppliedRegularizations() == null
                || regularization.getAppliedRegularizations().isEmpty()) {
            throw new RuntimeException("No applied regularizations found for this employee.");
        }

        List<AppliedRegularization> appliedRegularizations = regularization.getAppliedRegularizations();
        List<String> approvedDates = new ArrayList<>();
        List<String> rejectedDates = new ArrayList<>();

        Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmpId(empId);
        if (userInfoOpt.isEmpty()) {
            throw new RuntimeException("User info not found for employee ID: " + empId);
        }
        String shiftColName = shiftMasterRepository.getCollectionName(org);

        Query newQuery = new Query(
                Criteria.where("shiftName").is(userInfoOpt.get().getSections().getWorkingInformation().getShift()));
        ShiftMaster shiftOpt = mongoTemplate.findOne(newQuery, ShiftMaster.class, shiftColName);

        if (shiftOpt == null) {
            throw new RuntimeException("Shift information not found.");
        }

        ShiftMaster shift = shiftOpt;
        int remarkIndex = 0;

        for (AppliedRegularization appliedReg : appliedRegularizations) {
            String attendDate = appliedReg.getAttendanceDate();
            
                if ("Approved".equalsIgnoreCase(key) && approvedDate.contains(LocalDate.parse(attendDate))) {
                    appliedReg.setStartTime(appliedReg.getStartTime());
                    appliedReg.setEndTime(appliedReg.getEndTime());
                    appliedReg.setStatus("Approved");
                    appliedReg.setApprovedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    appliedReg.setApproverRemarks(remarkIndex < remark.size() ? remark.get(remarkIndex++) : null);
                    approvedDates.add(attendDate);

                    updateAttendanceInformationByApprover(empId, attendDate, shift, key, appliedReg,approvedDate,rejectedDate);
                    
                } else if ("Rejected".equalsIgnoreCase(key) && rejectedDate.contains(LocalDate.parse(attendDate))) {
                    if (reason == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.REASON);
                    }
                    appliedReg.setStatus("Rejected");
                    appliedReg.setApproverRemarks(remarkIndex < remark.size() ? remark.get(remarkIndex++) : null);
                    appliedReg.setRejectedDate(
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    rejectedDates.add(attendDate);
                    updateAttendanceInformationByApprover(empId, attendDate, shift, "Rejected",appliedReg,approvedDate,rejectedDate);
                    
                } else if ("Partially Approved".equalsIgnoreCase(key)) {
                    if (approvedDate.contains(LocalDate.parse(attendDate))) {
                        appliedReg.setStatus("Approved");
                        appliedReg.setStartTime(appliedReg.getStartTime());
                        appliedReg.setEndTime(appliedReg.getEndTime());
                        appliedReg.setApproverRemarks(remarkIndex < remark.size() ? remark.get(remarkIndex++) : null);
                        approvedDates.add(attendDate);
                        updateAttendanceInformationByApprover(empId, attendDate, shift, "Approved", appliedReg,approvedDate,rejectedDate);
                    } else if (rejectedDate.contains(LocalDate.parse(attendDate))) {
                        appliedReg.setStatus("Rejected");
                        appliedReg.setApproverRemarks(remarkIndex < remark.size() ? remark.get(remarkIndex++) : null);
                        appliedReg.setRejectedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        rejectedDates.add(attendDate);
                        updateAttendanceInformationByApprover(empId, attendDate, shift, "Rejected",appliedReg,approvedDate,rejectedDate);
                    }
                }
            
        }

        regularization.setApprovedDates(approvedDates);
        regularization.setRejectedDates(rejectedDates);
        regularization.setStatus(key);
        regularization.setUpdatedAt(LocalDateTime.now());

        AttendanceRegularization saveReg =  mongoTemplate.save(regularization, colName);
        // if (!approvedDates.isEmpty()) {
        //     String userName = userInfoOpt.get().getSections().getBasicDetails().getFirstName() + " " + userInfoOpt.get().getSections().getBasicDetails().getLastName();
        //     mailService.sendMailByTemplate(templateService.regularizationApprove(regularization,userName), userInfoOpt.get().getSections().getWorkingInformation().getOfficialEmail(), "Attendance Regularization Approved");
        // }
        return saveReg;
    }

    @Override
    public List<AttendanceRegularization> adminApproveRegularization(List<RegularizationApproveDto> regularizationApproveDtos) {
    
        String org = jwtHelper.getOrganizationCode();
        String colName = attendanceRegularizationRepository.getCollectionName(org);
        
        List<AttendanceRegularization> updatedRegularizations = new ArrayList<>();
    
        for (RegularizationApproveDto dto : regularizationApproveDtos) {
            String empId = dto.getEmpId();
            String regCode = dto.getRegCode();
            String action = dto.getAction();
            String remark = dto.getRemark();
    
            // Fetch the regularization record
            Query query = new Query(Criteria.where("regularizationCode").is(regCode));
            AttendanceRegularization regularization = mongoTemplate.findOne(query, AttendanceRegularization.class, colName);
    
            if (regularization == null || regularization.getAppliedRegularizations().isEmpty()) {
                throw new RuntimeException("No applied regularizations found for this employee: " + empId);
            }
    
            List<AppliedRegularization> appliedRegularizations = regularization.getAppliedRegularizations();
            List<String> approvedDates = new ArrayList<>();
            List<String> rejectedDates = new ArrayList<>();
    
            // Fetch Employee Information
            Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmpId(empId);
            if (userInfoOpt.isEmpty()) {
                throw new RuntimeException("User info not found for employee ID: " + empId);
            }
    
            // Fetch Shift Information
            String shiftColName = shiftMasterRepository.getCollectionName(org);
            Query shiftQuery = new Query(
                    Criteria.where("shiftName").is(userInfoOpt.get().getSections().getWorkingInformation().getShift()));
            ShiftMaster shift = mongoTemplate.findOne(shiftQuery, ShiftMaster.class, shiftColName);
    
            if (shift == null) {
                throw new RuntimeException("Shift information not found for employee ID: " + empId);
            }
    
            // Process each applied regularization
            for (AppliedRegularization appliedReg : appliedRegularizations) {
                String attendDate = appliedReg.getAttendanceDate();
    
                if ("Approved".equalsIgnoreCase(action)) {
                    appliedReg.setStartTime(appliedReg.getStartTime());
                    appliedReg.setEndTime(appliedReg.getEndTime());
                    appliedReg.setStatus("Approved");
                    appliedReg.setApprovedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    appliedReg.setReason(null);
                    appliedReg.setApproverRemarks(remark);
                    approvedDates.add(attendDate);
    
                    updateAttendanceInformation(empId, attendDate, shift, "Approved", appliedReg, dto.getMonth());
    
                } else if ("Rejected".equalsIgnoreCase(action)) {
                    appliedReg.setStatus("Rejected");
                    appliedReg.setReason(remark);
                    appliedReg.setRejectedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    rejectedDates.add(attendDate);
    
                    updateAttendanceInformation(empId, attendDate, shift, "Rejected",appliedReg, dto.getMonth());
    
                } else {
                    throw new IllegalArgumentException("Invalid action: " + action + ". Allowed values: Approved, Rejected");
                }
            }
    
            // Update and save the regularization record
            regularization.setApprovedDates(approvedDates);
            regularization.setRejectedDates(rejectedDates);
            regularization.setStatus(action);
            regularization.setUpdatedAt(LocalDateTime.now());
    
            mongoTemplate.save(regularization, colName);
            updatedRegularizations.add(regularization);
        }
    
        return updatedRegularizations;
    }
    
    private void updateAttendanceInformation(String empId, String attendanceDate, ShiftMaster shift, String key, AppliedRegularization appliedReg,String month) {
        
        String col = attendanceInformationRepository.getCollectionNames(jwtHelper.getOrganizationCode());
        Query query = new Query(Criteria.where("empId").is(empId).and("monthYear").is(month));
        AttendanceInformationHepl attedOpt = mongoTemplate.findOne(query, AttendanceInformationHepl.class, col);

        if (attedOpt != null) {
            AttendanceInformationHepl atted = attedOpt;
            if("Approved".equalsIgnoreCase(key)){
                for (AttendanceInfo attendInfo : atted.getAttendanceInfo()) {
                    if (attendInfo.getAttendanceDate().equals(attendanceDate)) {
                        attendInfo.setInTime(appliedReg.getStartTime());
                        attendInfo.setOutTime(appliedReg.getEndTime());
                        attendInfo.setExcessHours("00:00:00");
                        attendInfo.setShortFallHours("00:00:00");
                        attendInfo.setAttendanceData("P");
                        attendInfo.setRegularization(key);
                    }
                }
            }else if("Rejected".equalsIgnoreCase(key)){
                for (AttendanceInfo attendInfo : atted.getAttendanceInfo()) {
                    if (attendInfo.getAttendanceDate().equals(attendanceDate)) {
                        attendInfo.setRegularization(key);
                    }
                }
            }
            mongoTemplate.save(atted, col);
        }
    }

    private void updateAttendanceInformationByApprover(String empId, 
        String attendanceDate, ShiftMaster shift, String key,
        AppliedRegularization appliedReg,List<LocalDate>
        approvedDate,List<LocalDate> rejectedDate) {

        String col = attendanceInformationRepository.getCollectionNames(jwtHelper.getOrganizationCode());

        for (LocalDate date : approvedDate) {
            String monthYear = date.format(DateTimeFormatter.ofPattern("MM-yyyy"));
            Query query = new Query(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));
            AttendanceInformationHepl atted = mongoTemplate.findOne(query, AttendanceInformationHepl.class, col);

            if (atted != null && "Approved".equalsIgnoreCase(key)) {
                for (AttendanceInfo attendInfo : atted.getAttendanceInfo()) {
                    if (attendInfo.getAttendanceDate().equals(date.toString())) {
                        attendInfo.setInTime(appliedReg.getStartTime());
                        attendInfo.setOutTime(appliedReg.getEndTime());
                        attendInfo.setExcessHours("00:00:00");
                        attendInfo.setShortFallHours("00:00:00");
                        attendInfo.setAttendanceData("P");
                        attendInfo.setRegularization(key);
                    }
                }
                mongoTemplate.save(atted, col);
            }
        }

        for (LocalDate date : rejectedDate) {
            String monthYear = date.format(DateTimeFormatter.ofPattern("MM-yyyy"));
            Query query = new Query(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));
            AttendanceInformationHepl atted = mongoTemplate.findOne(query, AttendanceInformationHepl.class, col);

            if (atted != null && "Rejected".equalsIgnoreCase(key)) {
                for (AttendanceInfo attendInfo : atted.getAttendanceInfo()) {
                    if (attendInfo.getAttendanceDate().equals(date.toString())) {
                        attendInfo.setRegularization(key);
                    }
                }
                mongoTemplate.save(atted, col);
            }
        }
    }

    @Override
    public Map<String, Object> getRegulization(String key) {

        String org = jwtHelper.getOrganizationCode();
        String colName = attendanceRegularizationRepository.getCollectionName(org);
        String tlId = jwtHelper.getUserRefDetail().getEmpId();
        Map<String, Object> response = new HashMap<>();

        PayrollLockMonth lockMonth = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, org, "IN");

        if (lockMonth == null) {
            return Collections.emptyMap();
        }

        Query query;
        if ("Pending".equalsIgnoreCase(key)) {
            query = new Query(Criteria.where("status").is("Pending").and("appliedTo").is(tlId));
        } else if ("Approved".equalsIgnoreCase(key)) {
            query = new Query(Criteria.where("status").nin("Pending").and("appliedTo").is(tlId));
        } else {
            response.put("count", 0);
            response.put("data", Collections.emptyMap());
            return response;
        }

        List<AttendanceRegularization> records = mongoTemplate.find(query, AttendanceRegularization.class, colName);
        long count = mongoTemplate.count(query, AttendanceRegularization.class, colName);
        List<Map<String, Object>> mappedRecords = new ArrayList<>();

        if (records.isEmpty()) {
            return Collections.emptyMap();
        }
        for (AttendanceRegularization record : records) {

            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId()!=null?record.getId():"");
            recordMap.put("empId", record.getEmployeeId()!=null?record.getEmployeeId():"");
            recordMap.put("regularizationCode", record.getRegularizationCode()!=null?record.getRegularizationCode():"");
            recordMap.put("appliedRegularizations", record.getAppliedRegularizations()!=null?record.getAppliedRegularizations():"");
            recordMap.put("noOfDays", record.getNoOfDays());
            recordMap.put("regularizationDates", record.getRegularizationDates()!=null?record.getRegularizationDates():"");
            recordMap.put("status", record.getStatus()!=null?record.getStatus():"");
            recordMap.put("appliedDate", record.getAppliedDate()!=null?record.getAppliedDate():"");
            recordMap.put("appliedTo", record.getAppliedTo()!=null?record.getAppliedTo():"");
            recordMap.put("remarks", record.getRemarks()!=null?record.getRemarks():"");
            recordMap.put("approvedDates", record.getApprovedDates()!=null?record.getApprovedDates():"");
            recordMap.put("rejectedDates", record.getRejectedDates()!=null?record.getRejectedDates():"");

            UserInfo userInfo = userInfoRepository.findByEmpId(record.getEmployeeId()).orElse(null);
            String name = "";
            String employeeDept = "";
            if(userInfo != null && 
                userInfo.getSections() != null && 
                userInfo.getSections().getBasicDetails() != null && 
                userInfo.getSections().getWorkingInformation() != null){

                String firstName = Optional.ofNullable(userInfo.getSections().getBasicDetails().getFirstName()).orElse("");
                String lastName = Optional.ofNullable(userInfo.getSections().getBasicDetails().getLastName()).orElse("");
                name = firstName + " " + lastName;
                employeeDept = Optional.ofNullable(userInfo.getSections().getWorkingInformation().getDepartment()).orElse("");

                recordMap.put("employeeName", name);
                recordMap.put("employeeDept", employeeDept);
            }

            UserInfo appliedToUserInfo = userInfoRepository.findByEmpId(tlId).orElse(null);
            String approverName = "";
            String approverDept = "";

            String firstNames = Optional.ofNullable(appliedToUserInfo.getSections().getBasicDetails().getFirstName()).orElse("");
            String lastNames = Optional.ofNullable(appliedToUserInfo.getSections().getBasicDetails().getLastName()).orElse("");
            approverName = firstNames + " " + lastNames;
            approverDept = Optional.ofNullable(appliedToUserInfo.getSections().getWorkingInformation().getDepartment()).orElse("");

            recordMap.put("approverName", approverName);
            recordMap.put("approverDept", approverDept);
            recordMap.put("approverId",tlId!=null?tlId:"");
            mappedRecords.add(recordMap);

        }
        Map<String, Object> data = new HashMap<>();
        data.put("records", mappedRecords);
        data.put("count", count);        

        LocalDate currentDate = LocalDate.now();
        int currentDay = currentDate.getDayOfMonth();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();
        String attendanceRepoLockDate = lockMonth.getAttendanceRepoLockDate();
        String formattedDay = String.format("%02d", currentDay);
        
        if (formattedDay.compareTo(attendanceRepoLockDate) <= 0) {
            data.put("repoLockEndDate", String.format("%d-%02d-%s", currentYear, currentMonth, attendanceRepoLockDate));
            LocalDate repoLockStart = LocalDate.of(currentYear, currentMonth, Integer.parseInt(attendanceRepoLockDate)).minusMonths(1);
            data.put("repoLockStartDate", repoLockStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            LocalDate repoLockStart = LocalDate.of(currentYear, currentMonth, Integer.parseInt(attendanceRepoLockDate));
            data.put("repoLockStartDate", repoLockStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            LocalDate repoLockEnd = repoLockStart.plusMonths(1);
            data.put("repoLockEndDate", repoLockEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        return data;
    }

    @Override
    public Map<String, Object> getEmpRegulization(String key) {

        String org = jwtHelper.getOrganizationCode();
        String colName = attendanceRegularizationRepository.getCollectionName(org);
        String employeeId = jwtHelper.getUserRefDetail().getEmpId(); 
        Map<String, Object> response = new HashMap<>();

        PayrollLockMonth lockMonth = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, org, "IN");

        if (lockMonth == null) {
            return Collections.emptyMap();
        }
    
        UserInfo userInfo = userInfoRepository.findByEmpId(employeeId).orElse(null);
        String name = "";
        String employeeDept = "";
        if (userInfo != null && 
            userInfo.getSections() != null && 
            userInfo.getSections().getBasicDetails() != null && 
            userInfo.getSections().getWorkingInformation() != null) {
        
            String firstName = Optional.ofNullable(userInfo.getSections().getBasicDetails().getFirstName()).orElse("");
            String lastName = Optional.ofNullable(userInfo.getSections().getBasicDetails().getLastName()).orElse("");
            name = firstName + " " + lastName;
            employeeDept = Optional.ofNullable(userInfo.getSections().getWorkingInformation().getDepartment()).orElse("");
        }

        Query query;
        
        if (key.equalsIgnoreCase("Pending")) {
            query = new Query(Criteria.where("status").is(key).and("employeeId").is(employeeId)); 
            
        } else if (key.equalsIgnoreCase("Approved")) {
            query = new Query(Criteria.where("status").nin("Pending").and("employeeId").is(employeeId));
            
        } else {
            return response; 
        }

        List<AttendanceRegularization> recordList = mongoTemplate.find(query, AttendanceRegularization.class, colName);
        long recordCount = mongoTemplate.count(query, AttendanceRegularization.class, colName);

        List<Map<String, Object>> updatedRecords = new ArrayList<>();
        for (AttendanceRegularization record : recordList) {

            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId()!=null?record.getId():"");
            recordMap.put("empId", record.getEmployeeId()!=null?record.getEmployeeId():"");
            recordMap.put("regularizationCode", record.getRegularizationCode()!=null?record.getRegularizationCode():"");
            recordMap.put("appliedRegularizations", record.getAppliedRegularizations()!=null?record.getAppliedRegularizations():"");
            recordMap.put("noOfDays", record.getNoOfDays());
            recordMap.put("regularizationDates", record.getRegularizationDates()!=null?record.getRegularizationDates():"");
            recordMap.put("status", record.getStatus()!=null?record.getStatus():"");
            recordMap.put("appliedDate", record.getAppliedDate()!=null?record.getAppliedDate():"");
            recordMap.put("appliedTo", record.getAppliedTo()!=null?record.getAppliedTo():"");
            recordMap.put("remarks", record.getRemarks()!=null?record.getRemarks():"");
            recordMap.put("approvedDates", record.getApprovedDates()!=null?record.getApprovedDates():"");
            recordMap.put("rejectedDates", record.getRejectedDates()!=null?record.getRejectedDates():"");
            recordMap.put("employeeName", name);
            recordMap.put("employeeDept", employeeDept);

            UserInfo appliedUserInfo = userInfoRepository.findByEmpId(record.getAppliedTo()).orElse(null);
            String approverName = "";
            if (appliedUserInfo != null && appliedUserInfo.getSections() != null && appliedUserInfo.getSections().getBasicDetails() != null &&
            appliedUserInfo.getSections().getWorkingInformation() != null) {
                
                String firstNames = Optional.ofNullable(appliedUserInfo.getSections().getBasicDetails().getFirstName()).orElse("");
                String lastNames = Optional.ofNullable(appliedUserInfo.getSections().getBasicDetails().getLastName()).orElse("");
                approverName = firstNames + " " + lastNames;
                String approverDept = Optional.ofNullable(appliedUserInfo.getSections().getWorkingInformation().getDepartment()).orElse("");

                employeeDept = Optional.ofNullable(appliedUserInfo.getSections().getWorkingInformation().getDepartment()).orElse("");
                recordMap.put("approverId", record.getAppliedTo()!=null?record.getAppliedTo():"");
                recordMap.put("approverName", approverName);
                recordMap.put("approverDept", approverDept);
            } else {
                recordMap.put("employeeName", "Unknown");
            }

            updatedRecords.add(recordMap);
        }

        response.put("count", recordCount);
        LocalDate date  = LocalDate.now();
        int day = date.getDayOfMonth();
        int month = date.getMonthValue();
        int year = date.getYear();
        String attendanceEmpLockDate = lockMonth.getAttendanceEmpLockDate();
        String days = String.format("%02d", day);

        if (days.compareTo(attendanceEmpLockDate) <= 0) {
            response.put("lockEndDate", String.format("%d-%02d-%s", year, month, attendanceEmpLockDate));
            LocalDate lockStart = LocalDate.of(year, month, Integer.parseInt(attendanceEmpLockDate)).minusMonths(1);
            response.put("lockStartDate", lockStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            LocalDate lockStart = LocalDate.of(year, month, Integer.parseInt(attendanceEmpLockDate));
            response.put("lockStartDate", lockStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            LocalDate lockEnd = lockStart.plusMonths(1);
            response.put("lockEndDate", lockEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        response.put("records", updatedRecords);

        return response;
    }


    @Override
    public Map<String, Object> getAbsentAttendance(String empId, String monthYear) {
    
        String orgId = jwtHelper.getOrganizationCode();
        
        // Fetch locked payroll months
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, orgId, "IN");
        if (payrollLock == null) {
            return Map.of(
                    "empId", empId,
                    "monthYear", monthYear,
                    "payrollLock", Collections.emptyList());
        }
    
        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(monthYear))
                .findFirst()
                .orElse(null);
    
        if (payrollMonth == null) {
            return Map.of(
                    "empId", empId,
                    "monthYear", monthYear,
                    "payrollMonth", Collections.emptyList());
        }

        UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
        if (user == null) {
            return Map.of(
                    "empId", empId,
                    "monthYear", monthYear,
                    "userInfo", Collections.emptyList());
        }
        ShiftMaster shift = shiftMasterRepository.findByShiftName(user.getSections().getWorkingInformation().getShift(), orgId, mongoTemplate);
        if (shift == null) {
            return Map.of(
                    "empId", empId,
                    "monthYear", monthYear,
                    "shift", Collections.emptyList());
        }
        LocalDate startDate = payrollMonth.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate doj = user.getSections().getWorkingInformation().getDoj()
                .toInstant()  // Convert Date to Instant
                .atZone(ZoneId.of("UTC")) // Interpret as UTC
                .withZoneSameInstant(ZoneId.systemDefault()) // Convert to system's time zone
                .toLocalDate(); // Extract LocalDate
                
        if (doj.isAfter(startDate)) {
            startDate = doj;
        }
        LocalDate endDate = payrollMonth.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
    
        // Fetch attendance records
        String col = attendanceInformationRepository.getCollectionNames(orgId);

        // DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
        // DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        // YearMonth yearMonth = YearMonth.parse(monthYear, inputFormatter);
        // String output = yearMonth.format(outputFormatter);

        Query query = new Query(Criteria.where("empId").is(empId).and("monthYear").is(monthYear));
        AttendanceInformationHepl attendanceRecord = mongoTemplate.findOne(query, AttendanceInformationHepl.class, col);
    
        Map<LocalDate, AttendanceInfo> attendanceMap = new HashMap<>();
        if (attendanceRecord != null && attendanceRecord.getAttendanceInfo() != null) {
            attendanceMap = attendanceRecord.getAttendanceInfo().stream()
                    .collect(Collectors.toMap(
                            att -> LocalDate.parse(att.getAttendanceDate()),
                            att -> att,
                            (existing, replacement) -> existing));
        }
    
        // Fetch Weekend Policy
        // AttendanceWeekendPolicy weekendPolicy = weekendPolicyRepository.findByMonth(monthYear, orgId, mongoTemplate);
        String startMonth = startDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        String endMonth = endDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        List<String> months = Arrays.asList(startMonth, endMonth); 

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = LocalDate.now().atTime(13, 0);

        List<AttendanceWeekendPolicy> weekendPolicies = weekendPolicyRepository.findByMonths(orgId,months, mongoTemplate);
        Set<LocalDate> offDates = new HashSet<>();

        for (AttendanceWeekendPolicy policy : weekendPolicies) {
            for (WeekEnd week : policy.getWeek()) {
                try {
                    LocalDate satDate = LocalDate.parse(week.getSatDate());
                    LocalDate sunDate = LocalDate.parse(week.getSunDate());

                    if (satDate != null && "off".equalsIgnoreCase(week.getSatStatus())) {
                        offDates.add(satDate);
                    }
                    if (sunDate != null && "off".equalsIgnoreCase(week.getSunStatus())) {
                        offDates.add(sunDate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        List<Holiday> holidays = holidayRepository.findByDateBetween(orgId, startDate, endDate, mongoTemplate);
        Set<LocalDate> holidayDates = holidays.stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());
    
        List<Map<String, Object>> absentRecords = new ArrayList<>();
    
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (holidayDates.contains(date) || offDates.contains(date)) {
                continue; 
            }
            if (date.equals(today.minusDays(1)) && now.isBefore(cutoffTime)) {
                continue;
            }
            if (!attendanceMap.containsKey(date) && date.isBefore(today)) {
                absentRecords.add(Map.of(
                        "attendanceDate", date.toString(),
                        "inTime", shift.getInTime(),
                        "outTime", shift.getOutTime(),
                        "actualInTime", "00:00",
                        "actualOutTime", "00:00",
                        "attendanceData", "A"
                ));
            } else {
                AttendanceInfo att = attendanceMap.get(date);
                if (att != null ) {

                      boolean isAbsent = List.of("A:P", "P:A","A").contains(att.getAttendanceData());
                        boolean isRegularizationExcluded = att.getRegularization() != null
                                && (att.getRegularization().equalsIgnoreCase("Pending") 
                                    || att.getRegularization().equalsIgnoreCase("Approved"));

                        if (isAbsent && !isRegularizationExcluded) {
                            absentRecords.add(Map.of(
                                    "attendanceDate", att.getAttendanceDate(),
                                    "inTime", shift.getInTime(),
                                    "outTime", shift.getOutTime(),
                                    "actualInTime", att.getInTime(),
                                    "actualOutTime", att.getOutTime(),
                                    "attendanceData", att.getAttendanceData()
                            ));
                        }
                }
            }
        }
    
        return Map.of(
                "empId", empId,
                "monthYear", monthYear,
                "attendanceInfo", absentRecords);
    }
    

    @Override
    public GenericResponse<Map<String, Object>> getAbsentAndPresentAttendance(String empId, String currentMonth) {

        String orgId = jwtHelper.getOrganizationCode();
        String finYear = getFinancialYear(currentMonth);
        // PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, orgId, "IN");
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId, "IN");
        if (payrollLock == null) {
            return GenericResponse.success(Collections.emptyMap());
        }

        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(currentMonth))
                .findFirst()
                .orElse(null);

        if (payrollMonth == null) {
            return GenericResponse.success(Collections.emptyMap());
        }

        LocalDate startDate = payrollMonth.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = payrollMonth.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalTime nowTime = LocalTime.now();

        LocalTime cutoffTime = LocalTime.of(13, 30);
        // Fetch attendance records
        List<AttendanceInformationHepl> attendanceRecords = attendanceInformationRepository
                .findAttendanceRecords(mongoTemplate, empId, startDate.toString(), endDate.toString(), orgId);

        Map<LocalDate, AttendanceInfo> attendanceMap = attendanceRecords.stream()
                .flatMap(record -> record.getAttendanceInfo().stream())
                .collect(Collectors.toMap(
                        att -> LocalDate.parse(att.getAttendanceDate()),
                        att -> att,
                        (existing, replacement) -> existing));

        // Fetch Weekend Policy
        String startMonth = startDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        String endMonth = endDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        List<String> months = Arrays.asList(startMonth, endMonth);
        List<AttendanceWeekendPolicy> weekendPolicies = weekendPolicyRepository.findByMonths(orgId,months, mongoTemplate);
        Set<LocalDate> offDates = new HashSet<>();

        for (AttendanceWeekendPolicy policy : weekendPolicies) {
            for (WeekEnd week : policy.getWeek()) {
                try {
                    LocalDate satDate = LocalDate.parse(week.getSatDate());
                    LocalDate sunDate = LocalDate.parse(week.getSunDate());

                    if (satDate != null && "off".equalsIgnoreCase(week.getSatStatus())) {
                        offDates.add(satDate);
                    }
                    if (sunDate != null && "off".equalsIgnoreCase(week.getSunStatus())) {
                        offDates.add(sunDate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Fetch Holiday List
        List<Holiday> holidays = holidayRepository.findByDateBetween(orgId, startDate, endDate, mongoTemplate);
        Set<LocalDate> holidayDates = holidays.stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        Optional<UserInfo> user = userInfoRepository.findByEmpId(empId); 

        if (user.isEmpty()) {
            return GenericResponse.success(Collections.emptyMap());
        } 
        UserInfo userInfo = user.get();
        ShiftMaster shiftMaster = shiftMasterRepository.findByShiftName(userInfo.getSections().getWorkingInformation().getShift(), orgId, mongoTemplate);

        if (shiftMaster == null) {
            return GenericResponse.success(Collections.emptyMap());
        }
        LocalDate doj = userInfo.getSections().getWorkingInformation().getDoj()
        .toInstant()  // Convert Date to Instant
        .atZone(ZoneId.of("UTC")) // Interpret as UTC
        .withZoneSameInstant(ZoneId.systemDefault()) // Convert to system's time zone
        .toLocalDate(); // Extract LocalDate
        if (doj.isAfter(startDate)) {
            startDate = doj;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        int count = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<String, Object> attendanceEntry = new HashMap<>();

            if (date.equals(yesterday) && nowTime.isBefore(cutoffTime)) {
                continue; // Skip adding yesterday's attendance before 1:30 PM
            }
            attendanceEntry.put("empId", empId);
            attendanceEntry.put("attendanceDate", date.toString());

            if (holidayDates.contains(date)) {
                attendanceEntry.put("attendanceData", "H"); // Mark Holiday
            } else if (offDates.contains(date)) {
                attendanceEntry.put("attendanceData", "OFF"); // Mark Week Off
            } else if (attendanceMap.containsKey(date)) {
                AttendanceInfo att = attendanceMap.get(date);
                if ("P".equals(att.getAttendanceData())) {
                    attendanceEntry.put("attendanceData", "P"); 
                    attendanceEntry.put("inTime", att.getInTime());
                    attendanceEntry.put("outTime", att.getOutTime());
                    attendanceEntry.put("actualInTime", att.getInTime());
                    attendanceEntry.put("actualOutTime", att.getOutTime());
                    attendanceEntry.put("regularization", att.getRegularization());

                } else if ("A:P".equals(att.getAttendanceData())) {
                    count++;
                    attendanceEntry.put("attendanceData", "A:P");
                    attendanceEntry.put("inTime", shiftMaster.getInTime());
                    attendanceEntry.put("outTime", shiftMaster.getOutTime());
                    attendanceEntry.put("actualInTime", att.getInTime());
                    attendanceEntry.put("actualOutTime", att.getOutTime());
                    attendanceEntry.put("regularization", att.getRegularization());
                } else if ("P:A".equals(att.getAttendanceData())) {
                    count++;
                    attendanceEntry.put("attendanceData", "P:A"); 
                    attendanceEntry.put("inTime", shiftMaster.getInTime());
                    attendanceEntry.put("outTime", shiftMaster.getOutTime());
                    attendanceEntry.put("actualInTime", att.getInTime());
                    attendanceEntry.put("actualOutTime", att.getOutTime());
                    attendanceEntry.put("regularization", att.getRegularization());

                } else {
                    count++;
                    attendanceEntry.put("attendanceData", "A"); 
                    attendanceEntry.put("inTime",shiftMaster.getInTime());
                    attendanceEntry.put("outTime", shiftMaster.getOutTime());
                    attendanceEntry.put("actualInTime", "00:00");
                    attendanceEntry.put("actualOutTime", "00:00");
                    attendanceEntry.put("regularization", att.getRegularization());
                }
            } else if (date.isBefore(today)) {
                count++;
                attendanceEntry.put("attendanceData", "A");
                attendanceEntry.put("inTime", shiftMaster.getInTime());
                attendanceEntry.put("outTime", shiftMaster.getOutTime()); 
                attendanceEntry.put("actualInTime", "00:00");
                attendanceEntry.put("actualOutTime", "00:00");
            } else {
                attendanceEntry.put("attendanceData", null);
                attendanceEntry.put("attendanceDate", null);
                attendanceEntry.put("empId", null);
            }

            result.add(attendanceEntry);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("attendanceData", result);
        response.put("count", count);

        return GenericResponse.success(response);
    }

    @Override
    public GenericResponse<String> withdrawal(String empId, String regCode, String monthYear) {

        String colName = attendanceRegularizationRepository.getCollectionName(jwtHelper.getOrganizationCode());
        String cln = attendanceInformationRepository.getCollectionNames(jwtHelper.getOrganizationCode());
        Query regQuery = new Query(Criteria.where("regularizationCode").is(regCode)
                .and("employeeId").is(empId)
                .and("status").is("Pending"));

        AttendanceRegularization regularization = mongoTemplate.findOne(regQuery, AttendanceRegularization.class,
                colName);
        if (regularization == null) {
            return GenericResponse.error("Regularization", "Regularization not found");
        }

        regularization.setStatus("Withdrawal");
        
        Query attendanceQuery = new Query(Criteria.where("empId").is(empId)
                .and("monthYear").is(monthYear));
        
        AttendanceInformationHepl attendanceInfo = mongoTemplate.findOne(attendanceQuery, AttendanceInformationHepl.class, cln);
        if (attendanceInfo == null) {
                return GenericResponse.error("Attendance","Attendance records not found");
        }
        
        boolean updated = false;
        for (AttendanceInfo details : attendanceInfo.getAttendanceInfo()) {
            if ("Pending".equals(details.getRegularization())) {
                details.setRegularization(null);
                updated = true;
            }
        }
        mongoTemplate.save(regularization, colName);
        if (updated) {
            mongoTemplate.save(attendanceInfo, cln);
        }
        return GenericResponse.success("Regularization withdrawn successfully");
    }
        
        
    @Override
    public Map<String, Object> getAppliedTo() {

        String empId = jwtHelper.getUserRefDetail().getEmpId();
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);

        if (userInfoOptional.isPresent()) {
            UserInfo userInfo = userInfoOptional.get();

            String primaryManagerId = userInfo.getSections().getHrInformation().getPrimary().getManagerId();
            String reviewerManagerId = userInfo.getSections().getHrInformation().getReviewer().getManagerId();

            String primaryManagerName = getManagerName(primaryManagerId);
            String reviewerManagerName = getManagerName(reviewerManagerId);

            Map<String, Object> response = new LinkedHashMap<>();
            
            Map<String, String> primaryManagerDetails = new LinkedHashMap<>();
            primaryManagerDetails.put("primaryManagerName", primaryManagerId + "-" + primaryManagerName);
            primaryManagerDetails.put("primaryManagerId", primaryManagerId);
            response.put("primary", primaryManagerDetails);

            Map<String, String> reviewerManagerDetails = new LinkedHashMap<>();
            reviewerManagerDetails.put("reviewerManagerName", reviewerManagerId + "-" + reviewerManagerName);
            reviewerManagerDetails.put("reviewerManagerId", reviewerManagerId);
            response.put("reviewer", reviewerManagerDetails);

            return response;
        }

        return Collections.emptyMap(); 

    }

    private String getManagerName(String managerId) {
        Optional<UserInfo> managerInfo = userInfoRepository.findByEmpId(managerId);
        return managerInfo.map(info -> info.getSections().getBasicDetails().getFirstName()).orElse("Unknown");
    }

    @Override
    public List<AttendanceRegularization> getAdminRegularization(String month, String empId) {
        
        String roleName = jwtHelper.getUserRefDetail().getActiveRole();
        String colName = attendanceRegularizationRepository.getCollectionName(jwtHelper.getOrganizationCode());

        // PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(
        //     mongoTemplate, jwtHelper.getOrganizationCode(), "IN"
        // );
        String finYear = null;
        if(month != null){
             finYear = getFinancialYear(month);
        }
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, jwtHelper.getOrganizationCode(), "IN");

        Query query = new Query();
        query.addCriteria(Criteria.where("status").is("Pending")); 

        if (payrollLock != null && month != null) {
            if(payrollLock.getPayrollMonths() == null){
                return Collections.emptyList();
            }
            PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                    .filter(pm -> pm.getPayrollMonth().equals(month))
                    .findFirst()
                    .orElse(null);

            if (payrollMonth != null) {
                LocalDate startDate = payrollMonth.getStartDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate endDate = payrollMonth.getEndDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String startDateStr = startDate.format(formatter);
                    String endDateStr = endDate.format(formatter);

                    query.addCriteria(Criteria.where("appliedRegularizations.attendanceDate").gte(startDateStr).lte(endDateStr));
     
            }
            else{
                return Collections.emptyList();
            }

        }

        if (empId != null && !empId.isEmpty()) {
            query.addCriteria(Criteria.where("employeeId").is(empId));
        }

        if ("Payroll Admin".equalsIgnoreCase(roleName)) {
            List<AttendanceRegularization> adminRegularization = mongoTemplate.find(query, AttendanceRegularization.class, colName);
            return adminRegularization != null ? adminRegularization : Collections.emptyList();
        }
        
        return Collections.emptyList();
    }


    @Override
    public List<LeaveApply> getAdminLeaveApply(String leaveType, String empId, String month) {

        String roleName = jwtHelper.getUserRefDetail().getActiveRole();
        String colName = leaveApplyRepository.getCollectionName(jwtHelper.getOrganizationCode());

        String finYear = null;
        if(month != null){
             finYear = getFinancialYear(month);
        }
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(
            finYear, mongoTemplate, jwtHelper.getOrganizationCode(), "IN");

        Query query = new Query();
        query.addCriteria(Criteria.where("status").is("Pending"));

        if (payrollLock != null && month != null) {
            PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                    .filter(pm -> pm.getPayrollMonth().equals(month))
                    .findFirst()
                    .orElse(null);

            if (payrollMonth != null) {
                LocalDate startDate = payrollMonth.getStartDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate endDate = payrollMonth.getEndDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String startDateStr = startDate.format(formatter);
                String endDateStr = endDate.format(formatter);

                query.addCriteria(
                    new Criteria().orOperator(
                        Criteria.where("fromDate").gte(startDateStr).lte(endDateStr),
                        Criteria.where("toDate").gte(startDateStr).lte(endDateStr),
                        Criteria.where("fromDate").lte(startDateStr).and("toDate").gte(endDateStr)
                    )
                );
            } else {
                return Collections.emptyList();
            }
        }

        if (leaveType != null && !leaveType.isEmpty()) {
            query.addCriteria(Criteria.where("leaveType").is(leaveType));
        }

        if (empId != null && !empId.isEmpty()) {
            query.addCriteria(Criteria.where("empId").is(empId));
        }

        if ("Payroll Admin".equalsIgnoreCase(roleName)) {
            List<LeaveApply> leaveApplications = mongoTemplate.find(query, LeaveApply.class, colName);
            return leaveApplications != null ? leaveApplications : Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    public GenericResponse<Map<String, Object>> presentAbsentList(String empId, String currentMonth) {

        String orgId = jwtHelper.getOrganizationCode();
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, orgId, "IN");
        if (payrollLock == null) {
            return GenericResponse.success(Collections.emptyMap());
        }

        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(currentMonth))
                .findFirst()
                .orElse(null);
                
        if (payrollMonth == null) {
            return GenericResponse.success(Collections.emptyMap());
        }

        UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);
        if (user == null || user.getSections() == null || user.getSections().getWorkingInformation() == null) {
            return GenericResponse.success(Collections.emptyMap());
        }

        LocalDate payrollStartDate = payrollMonth.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate payrollEndDate = payrollMonth.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate doj = user.getSections().getWorkingInformation().getDoj()
                .toInstant()
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.systemDefault())
                .toLocalDate();

        if (payrollStartDate.isBefore(doj)) {
            payrollStartDate = doj;
        }

        // Get current date and time
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime cutoffTime = LocalTime.of(13, 30); // 1:30 PM

        // Fetch attendance records
        List<AttendanceInformationHepl> attendanceRecords = attendanceInformationRepository
                .findAttendanceRecords(mongoTemplate, empId, payrollStartDate.toString(), payrollEndDate.toString(), orgId);

        List<String> presentList = new ArrayList<>();
        List<String> absentList = new ArrayList<>();
        List<String> regPendingList = new ArrayList<>();
        List<String> regApprovedList = new ArrayList<>();
        List<String> regRejectedList = new ArrayList<>();
        List<String> dojList = new ArrayList<>();
        dojList.add(doj.toString());
        Map<String, List<String>> absentListInAndOut = new HashMap<>();

        Map<LocalDate, AttendanceInfo> attendanceMap = attendanceRecords.stream()
                .flatMap(record -> record.getAttendanceInfo().stream())
                .collect(Collectors.toMap(
                        att -> LocalDate.parse(att.getAttendanceDate()),
                        att -> att,
                        (existing, replacement) -> existing));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        String startMonthYear = payrollStartDate.format(formatter);
        String endMonthYear = payrollEndDate.format(formatter);
        List<AttendanceWeekendPolicy> weekendPolicies = weekendPolicyRepository.findByMonthYearBtw(
                startMonthYear, endMonthYear, orgId, mongoTemplate);
        Set<LocalDate> weekendDates = new HashSet<>();

        for (AttendanceWeekendPolicy weekendPolicy : weekendPolicies) {
            if (weekendPolicy.getWeek() != null) {
                for (WeekEnd week : weekendPolicy.getWeek()) {
                    
                    // LocalDate satDate = LocalDate.parse(week.getSatDate());
                    // LocalDate sunDate = LocalDate.parse(week.getSunDate());

                    LocalDate satDate = Optional.ofNullable(week.getSatDate())
                            .map(LocalDate::parse)
                            .orElse(null);

                    LocalDate sunDate = Optional.ofNullable(week.getSunDate())
                            .map(LocalDate::parse)
                            .orElse(null);

                    if ("OFF".equalsIgnoreCase(week.getSatStatus()) &&
                            !satDate.isBefore(payrollStartDate) && !satDate.isAfter(payrollEndDate)) {
                        weekendDates.add(satDate);
                    }
                    if ("OFF".equalsIgnoreCase(week.getSunStatus()) &&
                            !sunDate.isBefore(payrollStartDate) && !sunDate.isAfter(payrollEndDate)) {
                        weekendDates.add(sunDate);
                    }
                }
            }
        }

        List<Holiday> holidays = holidayRepository.findByDateBetween(
                orgId, payrollStartDate, payrollEndDate, mongoTemplate);
        Set<LocalDate> holidayDates = holidays.stream().map(Holiday::getDate).collect(Collectors.toSet());

        for (LocalDate date = payrollStartDate; !date.isAfter(payrollEndDate); date = date.plusDays(1)) {
            // Skip future dates
            if (date.isAfter(today)) {
                continue;
            }

            // Skip today
            if (date.isEqual(today)) {
                continue;
            }

            // Skip yesterday if before 1:30 PM
            if (date.isEqual(today.minusDays(1)) && now.isBefore(cutoffTime)) {
                continue;
            }

            if (attendanceMap.containsKey(date)) {
                AttendanceInfo attendance = attendanceMap.get(date);
                String regularization = attendance.getRegularization();
                String inTime = attendance.getInTime() != null ? attendance.getInTime() : " ";
                String outTime = attendance.getOutTime() != null ? attendance.getOutTime() : " ";
                String attendanceData = attendance.getAttendanceData();

                switch (regularization != null ? regularization.toLowerCase() : "") {
                    case "pending":
                        regPendingList.add(date.toString());
                        break;
                    case "approved":
                        regApprovedList.add(date.toString());
                        break;
                    case "rejected":
                        regRejectedList.add(date.toString());
                        break;
                }

                if ("P".equalsIgnoreCase(attendanceData)) {
                    presentList.add(date.toString());
                } else if ((regularization == null || "Rejected".equalsIgnoreCase(regularization))
                        && ("A".equalsIgnoreCase(attendanceData) || "A:P".equalsIgnoreCase(attendanceData)
                        || "P:A".equalsIgnoreCase(attendanceData))) {
                    absentList.add(date.toString());
                    absentListInAndOut.put(date.toString(), Arrays.asList(inTime, outTime));
                }
            } else {
                if (!holidayDates.contains(date) && !weekendDates.contains(date)) {
                    absentList.add(date.toString());
                    absentListInAndOut.put(date.toString(), Arrays.asList(" ", " "));
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("presentList", presentList);
        response.put("absentList", absentList);
        response.put("regPendingList", regPendingList);
        response.put("regApprovedList", regApprovedList);
        response.put("regRejectedList", regRejectedList);
        response.put("absentListInAndOut", absentListInAndOut);
        response.put("holidayList", new ArrayList<>(holidayDates));
        response.put("weekendPolicyList", new ArrayList<>(weekendDates));
        response.put("dojList", dojList);
        return GenericResponse.success(response);
    }




    @Override
    public GenericResponse<Map<String, Object>> getPayrollLockDate(String currentMonth) {
        
        // PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, jwtHelper.getOrganizationCode(), "IN");
        String orgId = jwtHelper.getOrganizationCode();
        String finYear = getFinancialYear(currentMonth);
        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate, orgId, "IN");
        if (payrollLock == null) {
            return GenericResponse.success(Map.of(
                "monthYear", currentMonth,
                "payrollLock", Collections.emptyList()));
        }
    
        PayrollLockMonth.PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(currentMonth))
                .findFirst()
                .orElse(null);
    
        if (payrollMonth == null) {
            return GenericResponse.success(Map.of(
        "monthYear", currentMonth,
        "payrollMonth", Collections.emptyList()));
        }
        LocalDate startDate = payrollMonth.getStartDate().toInstant()
        .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = payrollMonth.getEndDate().toInstant()
        .atZone(ZoneId.systemDefault()).toLocalDate();
        
        Map<String, Object> payrollMonthMap = new HashMap<>();
        payrollMonthMap.put("startDate", startDate);
        payrollMonthMap.put("endDate", endDate);
        
        return GenericResponse.success(Map.of(
            "monthYear", currentMonth,
            "payrollMonth", payrollMonthMap
        ));
    }

    @Override
    // @Scheduled(cron = "0 02 18 * * ?")
    public void attendanceShortFall() throws MessagingException {
        // String orgId = jwtHelper.getOrganizationCode();
        
        List<UserInfo>employeeList=userInfoRepository.findAll();

        String date= LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate date1 = LocalDate.now();
        String monthYear = date1.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        for(UserInfo user:employeeList){
            boolean found=false;
            if(user!=null && user.getSections().getWorkingInformation().getOfficialEmail()!=null){
                String orgId = user.getSections().getWorkingInformation().getPayrollStatus();
                String collectionName = attendanceInformationRepository.getCollectionName(orgId);
                ShiftMaster shiftMaster=shiftMasterRepository.findByShiftName(user.getSections().getWorkingInformation().getShift(),orgId,mongoTemplate);
                String inTime = shiftMaster.getInTime().substring(0, 5);
                String outTime = shiftMaster.getOutTime().substring(0, 5);
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime shiftInTime = LocalTime.parse(inTime, timeFormatter);
                LocalTime shiftOutTime = LocalTime.parse(outTime, timeFormatter);
                if (shiftOutTime.isBefore(shiftInTime)) {
                    shiftOutTime = shiftOutTime.plusHours(12); 
                }
                Duration actualWorkDuration = Duration.between(shiftInTime, shiftOutTime);
                String actualWorkingHours = String.format("%02d:%02d:%02d",
                actualWorkDuration.toHours(),
                actualWorkDuration.toMinutesPart(),
                actualWorkDuration.toSecondsPart());
                String col = attendanceInformationRepository.getCollectionName(orgId);
                Query newQuery = new Query(Criteria.where("empId").is(user.getEmpId()).and("monthYear").is(monthYear));
                AttendanceInformationHepl attendanceInformations=mongoTemplate.findOne(newQuery, AttendanceInformationHepl.class,col);
                if(attendanceInformations!=null){
                    
                    String totalworkHours= attendanceInformations.getAttendanceInfo().stream().filter(f->f.getAttendanceDate().equals(date))
                    .map(attendance->attendance.getTotalWorkHours()).findFirst().orElse(null);

                    if(totalworkHours==null){
                        Query query = new Query(Criteria.where("empId").is(user.getEmpId()).and("leaveApply.date").in(date).and("status").is("Approved"));
                        List<LeaveApply> leaveApply=mongoTemplate.find(query, LeaveApply.class,collectionName);
                        if(leaveApply.isEmpty() || leaveApply==null){
                            found=true;
                            
                        }
                    }else{

                        LocalTime actualTime = LocalTime.parse(actualWorkingHours);
                        LocalTime totalTime = LocalTime.parse(totalworkHours);

                        Duration actualWorkHours = Duration.between(LocalTime.MIN, actualTime);
                        Duration totalWorkedHours = Duration.between(LocalTime.MIN, totalTime);
                        if(totalWorkedHours.compareTo(actualWorkHours) < 0){
                            found=true;

                        }
    
                    }
                }
                
                
            }
            if(found){
                String empName=user.getSections().getBasicDetails().getFirstName()+" "+user.getSections().getBasicDetails().getLastName();
                LocalDate parsedDate = LocalDate.parse(date);
                String formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String subject="Attention! You have not fulfilled min hours required for a full day " +formattedDate+" || Budgie";
                String empId = user.getEmpId();
                mailService.sendMailByTemplate(templateService.absentMail(empName,empId,formattedDate),"hr@hepl.com",subject);
            }
        }
            

        
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
}
