package com.hepl.budgie.repository.attendancemanagement;

import java.time.LocalDate;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.dto.attendancemanagement.AttendanceMusterDTO;
import com.hepl.budgie.dto.attendancemanagement.DateAttendance;
import com.hepl.budgie.dto.attendancemanagement.LopDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayTypeHistory;
import com.hepl.budgie.entity.attendancemanagement.AttendanceMuster;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;
import com.hepl.budgie.entity.attendancemanagement.UpdatedDayType;
import com.hepl.budgie.entity.attendancemanagement.WeekEnd;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leave.LeaveApplyDates;
import com.hepl.budgie.entity.settings.Holiday;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.mongodb.client.model.InsertOneModel;

@Repository
public interface AttendanceMusterRepository extends MongoRepository<AttendanceMuster, String> {

        public static final String COLLECTION_NAME = "attendance_muster";

        default String getCollectionNames(String org) {
                return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
        }

        @SuppressWarnings("unchecked")
        default List<AttendanceMusterDTO> fetchallEmployeeAttendanceMuster(String empId, String reviewer,
                        String repManager, String payrollStatus, String monthYear, LocalDate startDate,
                        LocalDate endDate, String orgId, MongoTemplate mongoTemplate) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
                YearMonth currentMonth = YearMonth.parse(monthYear, formatter);
                String prevMonthYear = currentMonth.minusMonths(1).format(formatter);
                Criteria weekendCriteria = Criteria.where("month").in(prevMonthYear, monthYear);
                List<AttendanceWeekendPolicy> weekendPolicies = mongoTemplate.find(
                                Query.query(weekendCriteria), AttendanceWeekendPolicy.class,
                                "attendance_weekend_policy_" + orgId);
                Set<String> offDates = new HashSet<>();
                for (AttendanceWeekendPolicy policy : weekendPolicies) {
                        if (policy.getWeek() != null) {
                                for (WeekEnd week : policy.getWeek()) {
                                        if (week != null) {
                                                if ("OFF".equalsIgnoreCase(week.getSatStatus())
                                                                && week.getSatDate() != null) {
                                                        LocalDate satDate = LocalDate.parse(week.getSatDate());
                                                        if (!satDate.isBefore(startDate) && !satDate.isAfter(endDate)) {
                                                                offDates.add(satDate.toString());
                                                        }
                                                }
                                                if ("OFF".equalsIgnoreCase(week.getSunStatus())
                                                                && week.getSunDate() != null) {
                                                        LocalDate sunDate = LocalDate.parse(week.getSunDate());
                                                        if (!sunDate.isBefore(startDate) && !sunDate.isAfter(endDate)) {
                                                                offDates.add(sunDate.toString());
                                                        }
                                                }
                                        }
                                }
                        }
                }
                Criteria holidayCriteria = new Criteria().andOperator(
                                Criteria.where("date").gte(startDate).lte(endDate),
                                Criteria.where("status").is("Active"));
                List<Holiday> activeHolidays = mongoTemplate.find(Query.query(holidayCriteria), Holiday.class,
                                "m_holidays_" + orgId);
                Set<String> holidayDates = activeHolidays.stream()
                                .map(h -> h.getDate().toString())
                                .collect(Collectors.toSet());
                Criteria leaveCriteria = Criteria.where("leaveApply.date").gte(startDate.toString())
                                .lte(endDate.toString());
                if (empId != null && !empId.isEmpty()) {
                        leaveCriteria = leaveCriteria.and("empId").is(empId);
                }
                List<LeaveApply> approvedLeaves = mongoTemplate.find(Query.query(leaveCriteria), LeaveApply.class,
                                "leave_apply_" + orgId);

                Map<String, String> leaveTypeShortCodes = Map.of(
                                "Casual Leave", "CL",
                                "Sick Leave", "SL");
                Map<String, String> leaveMap = new HashMap<>();
                for (LeaveApply leave : approvedLeaves) {
                        String shortCode = leaveTypeShortCodes.getOrDefault(leave.getLeaveType(), "L");
                        for (LeaveApplyDates leaveDate : leave.getLeaveApply()) {
                                if ("Approved".equalsIgnoreCase(leaveDate.getStatus())) {
                                        leaveMap.put(leaveDate.getDate(), shortCode);
                                }
                        }
                }
                Criteria criteria = new Criteria().andOperator(
                                Criteria.where("organization.organizationCode").is(orgId),
                                Criteria.where("status").is("Active"));

                if (empId != null && !empId.isEmpty()) {
                        criteria = criteria.and("empId").is(empId);
                        if (reviewer != null && !reviewer.isEmpty()) {
                                criteria = criteria.and("sections.hrInformation.reviewer.managerId").is(reviewer);
                        }
                        if (repManager != null && !repManager.isEmpty()) {
                                criteria = criteria.andOperator(
                                                new Criteria().orOperator(
                                                                Criteria.where("sections.hrInformation.primary.managerId")
                                                                                .is(repManager),
                                                                Criteria.where("sections.hrInformation.secondary.managerId")
                                                                                .is(repManager)));
                        }
                        if (reviewer != null && !reviewer.isEmpty() && repManager != null && !repManager.isEmpty()) {
                                criteria = criteria.andOperator(
                                                Criteria.where("sections.hrInformation.reviewer.managerId")
                                                                .is(reviewer),
                                                new Criteria().orOperator(
                                                                Criteria.where("sections.hrInformation.primary.managerId")
                                                                                .is(repManager),
                                                                Criteria.where("sections.hrInformation.secondary.managerId")
                                                                                .is(repManager)));
                        }
                        if (payrollStatus != null && !payrollStatus.isEmpty()) {
                                criteria = criteria.and("sections.workingInformation.payrollStatus").is(payrollStatus);
                        }
                        if (repManager != null && !repManager.isEmpty()) {
                                Criteria repManagerCriteria = new Criteria().orOperator(
                                                Criteria.where("sections.hrInformation.primary.managerId")
                                                                .is(repManager),
                                                Criteria.where("sections.hrInformation.secondary.managerId")
                                                                .is(repManager));
                                if (!mongoTemplate.exists(Query.query(repManagerCriteria), "userinfo")) {
                                        return new ArrayList<>();
                                }
                        }
                } else {
                        if (reviewer != null && !reviewer.isEmpty()) {
                                criteria = criteria.and("sections.hrInformation.reviewer.managerId").is(reviewer);
                        }
                        if (repManager != null && !repManager.isEmpty()) {
                                criteria = criteria.orOperator(
                                                Criteria.where("sections.hrInformation.primary.managerId")
                                                                .is(repManager),
                                                Criteria.where("sections.hrInformation.secondary.managerId")
                                                                .is(repManager));
                        }
                        if (reviewer != null && !reviewer.isEmpty() && repManager != null && !repManager.isEmpty()) {
                                criteria = criteria.andOperator(
                                                Criteria.where("sections.hrInformation.reviewer.managerId")
                                                                .is(reviewer),
                                                new Criteria().orOperator(
                                                                Criteria.where("sections.hrInformation.primary.managerId")
                                                                                .is(repManager),
                                                                Criteria.where("sections.hrInformation.secondary.managerId")
                                                                                .is(repManager)));
                        }
                        if (payrollStatus != null && !payrollStatus.isEmpty()) {
                                criteria = criteria.and("sections.workingInformation.payrollStatus").is(payrollStatus);
                        }
                        if (repManager != null && !repManager.isEmpty()) {
                                Criteria repManagerCriteria = new Criteria().orOperator(
                                                Criteria.where("sections.hrInformation.primary.managerId")
                                                                .is(repManager),
                                                Criteria.where("sections.hrInformation.secondary.managerId")
                                                                .is(repManager));
                                if (!mongoTemplate.exists(Query.query(repManagerCriteria), "userinfo")) {
                                        return new ArrayList<>();
                                }
                        }
                }
                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(criteria),
                                Aggregation.project("empId")
                                                .andExpression("sections.basicDetails.firstName").as("firstName")
                                                .andExpression("sections.basicDetails.lastName").as("lastName")
                                                .andExpression("sections.workingInformation.designation")
                                                .as("designation")
                                                .andExpression("sections.workingInformation.doj").as("doj")
                                                .andExpression("sections.workingInformation.workLocation")
                                                .as("workLocation"),
                                Aggregation.project("empId", "designation", "doj", "location")
                                                .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(lastName, ''))")
                                                .as("empName"),
                                Aggregation.lookup("attendance_information_" + orgId, "empId", "empId",
                                                "attendanceRecords"),
                                Aggregation.unwind("attendanceRecords", true),
                                Aggregation.unwind("attendanceRecords.attendanceInfo", true),
                                Aggregation.match(Criteria.where("attendanceRecords.attendanceInfo.attendanceDate")
                                                .gte(startDate.toString()).lte(endDate.toString())),
                                Aggregation.group("empId", "empName", "designation", "doj", "location")
                                                .push(new Document("date",
                                                                "$attendanceRecords.attendanceInfo.attendanceDate")
                                                                .append("attendanceData",
                                                                                "$attendanceRecords.attendanceInfo.attendanceData"))
                                                .as("attendanceData"));
                List<AttendanceMusterDTO> attendanceMusterList = new ArrayList<>();
                List<Document> documents = mongoTemplate.aggregate(aggregation, "userinfo", Document.class)
                                .getMappedResults();
                for (Document doc : documents) {
                        AttendanceMusterDTO dto = new AttendanceMusterDTO();
                        Document idDoc = (Document) doc.get("_id");
                        if (idDoc != null) {
                                dto.setEmployeeId(idDoc.getString("empId"));
                                dto.setEmployeeName(idDoc.getString("empName"));
                                dto.setDesignation(idDoc.getString("designation"));
                                Date dojDate = idDoc.getDate("doj");
                                if (dojDate != null) {
                                        dto.setDoj(dojDate.toString());
                                }
                                dto.setLocation(idDoc.getString("workLocation"));
                        }
                        dto.setMonth(monthYear);
                        List<DateAttendance> attendanceList = new ArrayList<>();
                        List<Document> attendanceDocs = (List<Document>) doc.get("attendanceData");
                        Map<String, String> attendanceMap = new HashMap<>();
                        if (attendanceDocs != null) {
                                for (Document attDoc : attendanceDocs) {
                                        attendanceMap.put(attDoc.getString("date"), attDoc.getString("attendanceData"));
                                }
                        }
                        Criteria dayTypeCriteria = new Criteria().andOperator(
                                        Criteria.where("empId").is(dto.getEmployeeId()),
                                        Criteria.where("updatedDayType.date").gte(startDate).lte(endDate));
                        List<AttendanceDayTypeHistory> dayTypeHistories = mongoTemplate.find(
                                        Query.query(dayTypeCriteria), AttendanceDayTypeHistory.class,
                                        "attendance_day_type_history_" + orgId);
                        Map<String, String> dayTypeMap = new HashMap<>();
                        for (AttendanceDayTypeHistory history : dayTypeHistories) {
                                for (UpdatedDayType dayType : history.getUpdatedDayType()) {
                                        if (!dayType.getDate().isBefore(startDate)
                                                        && !dayType.getDate().isAfter(endDate)) {
                                                dayTypeMap.put(dayType.getDate().toString(),
                                                                dayType.getDayTypes().getDayTypeId());
                                        }
                                }
                        }
                        int presentDays = 0, holidays = 0, sickLeave = 0, casualLeave = 0;
                        LocalDate dojLocalDate = idDoc.getDate("doj") != null
                                        ? idDoc.getDate("doj").toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                        : null;
                        LocalDate dateIterator = (dojLocalDate != null && !dojLocalDate.isBefore(startDate))
                                        ? dojLocalDate
                                        : startDate;
                        while (!dateIterator.isAfter(endDate)) {
                                String dateStr = dateIterator.toString();
                                String attendanceValue = attendanceMap.getOrDefault(dateStr, "A");
                                if (dayTypeMap.containsKey(dateStr)) {
                                        String dayType = dayTypeMap.get(dateStr);
                                        switch (dayType) {
                                                case "D2":
                                                case "D4":
                                                        attendanceValue = "OFF";
                                                        break;
                                                case "D3":
                                                        attendanceValue = "H";
                                                        break;
                                        }
                                } else if (holidayDates.contains(dateStr)) {
                                        attendanceValue = "H";
                                } else if (offDates.contains(dateStr)) {
                                        attendanceValue = "OFF";
                                } else if (leaveMap.containsKey(dateStr)) {
                                        attendanceValue = leaveMap.get(dateStr);
                                }
                                DateAttendance dateAttendance = new DateAttendance();
                                dateAttendance.setDate(dateStr);
                                dateAttendance.setAttendanceData(attendanceValue);
                                attendanceList.add(dateAttendance);
                                switch (attendanceValue != null ? attendanceValue : "A") {
                                        case "P":
                                                presentDays++;
                                                break;
                                        case "H":
                                                holidays++;
                                                break;
                                        case "SL":
                                                sickLeave++;
                                                break;
                                        case "CL":
                                                casualLeave++;
                                                break;
                                }
                                dateIterator = dateIterator.plusDays(1);
                        }
                        dto.setAttendanceData(attendanceList);
                        dto.setTotalNoOfPresentDays(presentDays);
                        dto.setTotalHolidays(holidays);
                        dto.setTotalSickLeave(sickLeave);
                        dto.setTotalCasualLeave(casualLeave);
                        dto.setTotalDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);
                        attendanceMusterList.add(dto);
                }
                return attendanceMusterList;
        }

        default AttendanceMuster addLopForEmployee(LopDTO lop, String orgId, MongoTemplate mongoTemplate) {

                Query query = new Query(
                                Criteria.where("empId").is(lop.getEmpId()).and("monthYear").is(lop.getMonthYear()));

                String collection = getCollectionNames(orgId);
                Update update = new Update();
                update.setOnInsert("empId", lop.getEmpId())
                                .setOnInsert("monthYear", lop.getMonthYear())
                                .set("totalLop", lop.getLop())
                                .set("lopReversal", lop.getLopReversal());

                mongoTemplate.upsert(query, update, AttendanceMuster.class, collection);
                return mongoTemplate.findOne(query, AttendanceMuster.class, collection);
        }

        @SuppressWarnings("unchecked")
        default void saveEmployeeAttendanceMuster(List<String> empIds, boolean isAll, String monthYear,
                        LocalDate startDate, LocalDate endDate, String orgId, MongoTemplate mongoTemplate) {

                String collection = getCollectionNames(orgId);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
                YearMonth currentMonth = YearMonth.parse(monthYear, formatter);
                String prevMonthYear = currentMonth.minusMonths(1).format(formatter);

                Criteria weekendCriteria = Criteria.where("month").in(prevMonthYear, monthYear);
                List<AttendanceWeekendPolicy> weekendPolicies = mongoTemplate.find(
                                Query.query(weekendCriteria), AttendanceWeekendPolicy.class,
                                "attendance_weekend_policy_" + orgId);
                Set<String> offDates = new HashSet<>();
                for (AttendanceWeekendPolicy policy : weekendPolicies) {
                        if (policy.getWeek() != null) {
                                for (WeekEnd week : policy.getWeek()) {
                                        if (week != null) {
                                                if ("OFF".equalsIgnoreCase(week.getSatStatus())
                                                                && week.getSatDate() != null) {
                                                        LocalDate satDate = LocalDate.parse(week.getSatDate());
                                                        if (!satDate.isBefore(startDate) && !satDate.isAfter(endDate)) {
                                                                offDates.add(satDate.toString());
                                                        }
                                                }
                                                if ("OFF".equalsIgnoreCase(week.getSunStatus())
                                                                && week.getSunDate() != null) {
                                                        LocalDate sunDate = LocalDate.parse(week.getSunDate());
                                                        if (!sunDate.isBefore(startDate) && !sunDate.isAfter(endDate)) {
                                                                offDates.add(sunDate.toString());
                                                        }
                                                }
                                        }
                                }
                        }
                }
                Criteria holidayCriteria = new Criteria().andOperator(
                                Criteria.where("date").gte(startDate).lte(endDate),
                                Criteria.where("status").is("Active"));

                List<Holiday> activeHolidays = mongoTemplate.find(Query.query(holidayCriteria), Holiday.class,
                                "m_holidays_" + orgId);

                Set<String> holidayDates = activeHolidays.stream()
                                .map(h -> h.getDate().toString())
                                .collect(Collectors.toSet());

                Criteria leaveCriteria = Criteria.where("leaveApply").elemMatch(
                                Criteria.where("date").gte(startDate.toString()).lte(endDate.toString())
                                                .and("status").is("Approved"));

                List<LeaveApply> approvedLeaves = mongoTemplate.find(Query.query(leaveCriteria), LeaveApply.class,
                                "leave_apply_" + orgId);

                Map<String, String> leaveTypeShortCodes = Map.of(
                                "Casual Leave", "CL",
                                "Sick Leave", "SL");
                Map<String, String> leaveMap = new HashMap<>();
                for (LeaveApply leave : approvedLeaves) {
                        for (LeaveApplyDates leaveDate : leave.getLeaveApply()) {
                                String shortCode = leaveTypeShortCodes.getOrDefault(leave.getLeaveType(), "L");
                                if ("Approved".equalsIgnoreCase(leaveDate.getStatus())) {
                                        leaveMap.put(leaveDate.getDate(), shortCode);
                                }
                        }
                }

                Criteria empCriteria = Criteria.where("organization.organizationCode").is(orgId)
                                .and("status").is("Active");

                if (!isAll && empIds != null && !empIds.isEmpty()) {
                        empCriteria = empCriteria.and("empId").in(empIds);
                }

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(empCriteria),
                                Aggregation.project("empId")
                                                .andExpression("sections.basicDetails.firstName").as("firstName")
                                                .andExpression("sections.basicDetails.lastName").as("lastName")
                                                .andExpression("sections.workingInformation.designation")
                                                .as("designation")
                                                .andExpression("sections.workingInformation.doj").as("doj")
                                                .andExpression("sections.workingInformation.workLocation")
                                                .as("workLocation"),
                                Aggregation.project("empId", "designation", "doj", "location")
                                                .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(lastName, ''))")
                                                .as("empName"),
                                Aggregation.lookup("attendance_information_" + orgId, "empId", "empId",
                                                "attendanceRecords"),
                                Aggregation.unwind("attendanceRecords", true),
                                Aggregation.unwind("attendanceRecords.attendanceInfo", true),
                                Aggregation.match(Criteria.where("attendanceRecords.attendanceInfo.attendanceDate")
                                                .gte(startDate.toString()).lte(endDate.toString())),
                                Aggregation.group("empId", "empName", "designation", "doj", "location")
                                                .push(new Document("date",
                                                                "$attendanceRecords.attendanceInfo.attendanceDate")
                                                                .append("attendanceData",
                                                                                "$attendanceRecords.attendanceInfo.attendanceData"))
                                                .as("attendanceData"));

                List<Document> documents = mongoTemplate.aggregate(aggregation, "userinfo", Document.class)
                                .getMappedResults();

                for (Document doc : documents) {
                        AttendanceMusterDTO dto = new AttendanceMusterDTO();
                        Document idDoc = (Document) doc.get("_id");

                        if (idDoc != null) {
                                dto.setEmployeeId(idDoc.getString("empId"));
                                dto.setEmployeeName(idDoc.getString("empName"));
                                dto.setDesignation(idDoc.getString("designation"));
                                Date dojDate = idDoc.getDate("doj");
                                if (dojDate != null) {
                                        dto.setDoj(dojDate.toString());
                                }
                                dto.setLocation(idDoc.getString("workLocation"));
                        }

                        dto.setMonth(monthYear);

                        List<DateAttendance> attendanceList = new ArrayList<>();
                        Map<String, String> attendanceMap = new HashMap<>();

                        List<Document> attendanceDocs = (List<Document>) doc.get("attendanceData");

                        if (attendanceDocs != null) {
                                for (Document attDoc : attendanceDocs) {
                                        attendanceMap.put(attDoc.getString("date"), attDoc.getString("attendanceData"));
                                }
                        }
                        Criteria dayTypeCriteria = new Criteria().andOperator(
                                        Criteria.where("empId").is(dto.getEmployeeId()),
                                        Criteria.where("updatedDayType.date").gte(startDate).lte(endDate));

                        List<AttendanceDayTypeHistory> dayTypeHistories = mongoTemplate.find(
                                        Query.query(dayTypeCriteria),
                                        AttendanceDayTypeHistory.class, "attendance_day_type_history_" + orgId);

                        Map<String, String> dayTypeMap = new HashMap<>();
                        for (AttendanceDayTypeHistory history : dayTypeHistories) {
                                for (UpdatedDayType dayType : history.getUpdatedDayType()) {
                                        if (!dayType.getDate().isBefore(startDate)
                                                        && !dayType.getDate().isAfter(endDate)) {
                                                dayTypeMap.put(dayType.getDate().toString(),
                                                                dayType.getDayTypes().getDayTypeId());
                                        }
                                }
                        }

                        LocalDate dojLocalDate = idDoc.getDate("doj") != null
                                        ? idDoc.getDate("doj").toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                        : null;

                        LocalDate dateIterator = (dojLocalDate != null && !dojLocalDate.isBefore(startDate))
                                        ? dojLocalDate
                                        : startDate;

                        int presentDays = 0, holidays = 0, sickLeave = 0, casualLeave = 0;

                        while (!dateIterator.isAfter(endDate)) {
                                String dateStr = dateIterator.toString();
                                String attendanceValue = attendanceMap.getOrDefault(dateStr, "A");
                                if (attendanceValue == null) {
                                        attendanceValue = "A";
                                }
                                if (dayTypeMap.containsKey(dateStr)) {
                                        String dayType = dayTypeMap.get(dateStr);
                                        switch (dayType) {
                                                case "D2":
                                                case "D4":
                                                        attendanceValue = "OFF";
                                                        break;
                                                case "D3":
                                                        attendanceValue = "H";
                                                        break;
                                                case "D1":
                                                        break;
                                        }
                                } else if (holidayDates.contains(dateStr)) {
                                        attendanceValue = "H";
                                } else if (offDates.contains(dateStr)) {
                                        attendanceValue = "OFF";
                                } else if (leaveMap.containsKey(dateStr)) {
                                        attendanceValue = leaveMap.get(dateStr);
                                }

                                DateAttendance dateAttendance = new DateAttendance();
                                dateAttendance.setDate(dateStr);
                                dateAttendance.setAttendanceData(attendanceValue);
                                attendanceList.add(dateAttendance);

                                switch (attendanceValue) {
                                        case "P":
                                                presentDays++;
                                                break;
                                        case "H":
                                                holidays++;
                                                break;
                                        case "SL":
                                                sickLeave++;
                                                break;
                                        case "CL":
                                                casualLeave++;
                                                break;
                                }

                                dateIterator = dateIterator.plusDays(1);
                        }

                        dto.setAttendanceData(attendanceList);
                        dto.setTotalNoOfPresentDays(presentDays);
                        dto.setTotalHolidays(holidays);
                        dto.setTotalSickLeave(sickLeave);
                        dto.setTotalCasualLeave(casualLeave);
                        dto.setTotalDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);

                        Query query = new Query(Criteria.where("employeeId").is(dto.getEmployeeId())
                                        .and("month").is(dto.getMonth()));

                        Update update = new Update()
                                        .set("empName", dto.getEmployeeName())
                                        .set("designation", dto.getDesignation())
                                        .set("doj", dto.getDoj())
                                        .set("location", dto.getLocation())
                                        .set("attendanceData", dto.getAttendanceData())
                                        .set("totalNoOfPresentDays", dto.getTotalNoOfPresentDays())
                                        .set("totalHolidays", dto.getTotalHolidays())
                                        .set("totalSickLeave", dto.getTotalSickLeave())
                                        .set("totalCasualLeave", dto.getTotalCasualLeave())
                                        .set("totalDays", dto.getTotalDays());

                        mongoTemplate.upsert(query, update, collection);
                }
        }

        @SuppressWarnings("unchecked")
        default List<AttendanceMusterDTO> employeeAttendanceMuster(String monthYear, LocalDate startDate,
                        LocalDate endDate, String orgId, String empId, MongoTemplate mongoTemplate) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
                YearMonth currentMonth = YearMonth.parse(monthYear, formatter);
                String prevMonthYear = currentMonth.minusMonths(1).format(formatter);

                Criteria weekendCriteria = Criteria.where("month").in(prevMonthYear, monthYear);
                List<AttendanceWeekendPolicy> weekendPolicies = mongoTemplate.find(
                                Query.query(weekendCriteria), AttendanceWeekendPolicy.class,
                                "attendance_weekend_policy_" + orgId);

                Set<String> offDates = new HashSet<>();
                for (AttendanceWeekendPolicy policy : weekendPolicies) {
                        if (policy.getWeek() != null) {
                                for (WeekEnd week : policy.getWeek()) {
                                        if (week != null) {
                                                if ("OFF".equalsIgnoreCase(week.getSatStatus())
                                                                && week.getSatDate() != null) {
                                                        LocalDate satDate = LocalDate.parse(week.getSatDate());
                                                        if (!satDate.isBefore(startDate) && !satDate.isAfter(endDate)) {
                                                                offDates.add(satDate.toString());
                                                        }
                                                }
                                                if ("OFF".equalsIgnoreCase(week.getSunStatus())
                                                                && week.getSunDate() != null) {
                                                        LocalDate sunDate = LocalDate.parse(week.getSunDate());
                                                        if (!sunDate.isBefore(startDate) && !sunDate.isAfter(endDate)) {
                                                                offDates.add(sunDate.toString());
                                                        }
                                                }
                                        }
                                }
                        }
                }

                // Fetch active holidays
                Criteria holidayCriteria = new Criteria().andOperator(
                                Criteria.where("date").gte(startDate).lte(endDate),
                                Criteria.where("status").is("Active"));

                List<Holiday> activeHolidays = mongoTemplate.find(
                                Query.query(holidayCriteria), Holiday.class, "m_holidays_" + orgId);

                Set<String> holidayDates = activeHolidays.stream()
                                .map(h -> h.getDate().toString())
                                .collect(Collectors.toSet());

                Criteria leaveCriteria = Criteria.where("leaveApply.date").gte(startDate.toString())
                                .lte(endDate.toString());
                List<LeaveApply> approvedLeaves = mongoTemplate.find(Query.query(leaveCriteria), LeaveApply.class,
                                "leave_apply_" + orgId);
                Map<String, String> leaveTypeShortCodes = Map.of(
                                "Casual Leave", "CL",
                                "Sick Leave", "SL");

                Map<String, String> leaveMap = new HashMap<>();
                for (LeaveApply leave : approvedLeaves) {
                        String shortCode = leaveTypeShortCodes.getOrDefault(leave.getLeaveType(), "L");
                        for (LeaveApplyDates leaveDate : leave.getLeaveApply()) {
                                if ("Approved".equalsIgnoreCase(leaveDate.getStatus())) {
                                        leaveMap.put(leaveDate.getDate(), shortCode);
                                }
                        }
                }

                Criteria primaryCriteria = Criteria.where("sections.hrInformation.primary.managerId").is(empId);
                Criteria secondaryCriteria = Criteria.where("sections.hrInformation.secondary.managerId").is(empId);
                Criteria reviewerCriteria = Criteria.where("sections.hrInformation.reviewer.managerId").is(empId);
                Criteria combinedCriteria = new Criteria().orOperator(primaryCriteria, secondaryCriteria,
                                reviewerCriteria);

                List<UserInfo> managedEmployees = mongoTemplate.find(
                                Query.query(combinedCriteria), UserInfo.class, "userinfo");

                List<String> managedEmployeeIds = managedEmployees.stream()
                                .map(UserInfo::getEmpId)
                                .collect(Collectors.toList());

                if (managedEmployeeIds.isEmpty()) {
                        return Collections.emptyList();
                }

                Criteria attendanceCriteria = new Criteria().andOperator(
                                Criteria.where("empId").in(managedEmployeeIds),
                                Criteria.where("status").is("Active"));

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(attendanceCriteria),
                                Aggregation.project("empId")
                                                .andExpression("sections.basicDetails.firstName").as("firstName")
                                                .andExpression("sections.basicDetails.lastName").as("lastName")
                                                .andExpression("sections.workingInformation.designation")
                                                .as("designation")
                                                .andExpression("sections.workingInformation.doj").as("doj")
                                                .andExpression("sections.workingInformation.workLocation")
                                                .as("workLocation"),
                                Aggregation.project("empId", "designation", "doj", "workLocation")
                                                .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(lastName, ''))")
                                                .as("empName"),
                                Aggregation.lookup("attendance_information_" + orgId, "empId", "empId",
                                                "attendanceRecords"),
                                Aggregation.unwind("attendanceRecords", true),
                                Aggregation.unwind("attendanceRecords.attendanceInfo", true),
                                Aggregation.match(Criteria.where("attendanceRecords.attendanceInfo.attendanceDate")
                                                .gte(startDate.toString()).lte(endDate.toString())),
                                Aggregation.group("empId", "empName", "designation", "doj", "workLocation")
                                                .push(new Document("date",
                                                                "$attendanceRecords.attendanceInfo.attendanceDate")
                                                                .append("attendanceData",
                                                                                "$attendanceRecords.attendanceInfo.attendanceData"))
                                                .as("attendanceData"));

                List<AttendanceMusterDTO> attendanceMusterList = new ArrayList<>();
                List<Document> documents = mongoTemplate.aggregate(aggregation, "userinfo", Document.class)
                                .getMappedResults();

                for (Document doc : documents) {
                        AttendanceMusterDTO dto = new AttendanceMusterDTO();
                        Document idDoc = (Document) doc.get("_id");

                        if (idDoc != null) {
                                dto.setEmployeeId(idDoc.getString("empId"));
                                dto.setEmployeeName(idDoc.getString("empName"));
                                dto.setDesignation(idDoc.getString("designation"));
                                Date dojDate = idDoc.getDate("doj");
                                if (dojDate != null) {
                                        dto.setDoj(dojDate.toString());
                                }
                                dto.setLocation(idDoc.getString("workLocation"));
                        }

                        dto.setMonth(monthYear);
                        List<DateAttendance> attendanceList = new ArrayList<>();
                        List<Document> attendanceDocs = (List<Document>) doc.get("attendanceData");
                        Map<String, String> attendanceMap = new HashMap<>();

                        if (attendanceDocs != null) {
                                for (Document attDoc : attendanceDocs) {
                                        attendanceMap.put(attDoc.getString("date"), attDoc.getString("attendanceData"));
                                }
                        }
                        Criteria dayTypeCriteria = new Criteria().andOperator(
                                        Criteria.where("empId").is(dto.getEmployeeId()),
                                        Criteria.where("updatedDayType.date").gte(startDate).lte(endDate));

                        List<AttendanceDayTypeHistory> dayTypeHistories = mongoTemplate.find(
                                        Query.query(dayTypeCriteria),
                                        AttendanceDayTypeHistory.class, "attendance_day_type_history_" + orgId);

                        Map<String, String> dayTypeMap = new HashMap<>();
                        for (AttendanceDayTypeHistory history : dayTypeHistories) {
                                for (UpdatedDayType dayType : history.getUpdatedDayType()) {
                                        if (!dayType.getDate().isBefore(startDate)
                                                        && !dayType.getDate().isAfter(endDate)) {
                                                dayTypeMap.put(dayType.getDate().toString(),
                                                                dayType.getDayTypes().getDayTypeId());
                                        }
                                }
                        }
                        Date dojDate = idDoc.getDate("doj");
                        LocalDate dojLocalDate = (idDoc.getDate("doj") != null)
                                        ? dojDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                        : null;

                        LocalDate dateIterator = (dojLocalDate != null && !dojLocalDate.isBefore(startDate))
                                        ? dojLocalDate
                                        : startDate;

                        int presentDays = 0, holidaysCount = 0, sickLeave = 0, casualLeave = 0;

                        while (!dateIterator.isAfter(endDate)) {
                                String dateStr = dateIterator.toString();
                                String attendanceValue = attendanceMap.getOrDefault(dateStr, "A");

                                if (dayTypeMap.containsKey(dateStr)) {
                                        String dayType = dayTypeMap.get(dateStr);
                                        switch (dayType) {
                                                case "D2":
                                                case "D4":
                                                        attendanceValue = "OFF";
                                                        break;
                                                case "D3":
                                                        attendanceValue = "H";
                                                        break;
                                                case "D1":
                                                        break;
                                        }
                                } else if (holidayDates.contains(dateStr)) {
                                        attendanceValue = "H";
                                } else if (offDates.contains(dateStr)) {
                                        attendanceValue = "OFF";
                                } else if (leaveMap.containsKey(dateStr)) {
                                        attendanceValue = leaveMap.get(dateStr);
                                }

                                DateAttendance dateAttendance = new DateAttendance();
                                dateAttendance.setDate(dateStr);
                                dateAttendance.setAttendanceData(attendanceValue);
                                attendanceList.add(dateAttendance);

                                switch (attendanceValue) {
                                        case "P":
                                                presentDays++;
                                                break;
                                        case "H":
                                                holidaysCount++;
                                                break;
                                        case "SL":
                                                sickLeave++;
                                                break;
                                        case "CL":
                                                casualLeave++;
                                                break;
                                }

                                dateIterator = dateIterator.plusDays(1);
                        }

                        dto.setAttendanceData(attendanceList);
                        dto.setTotalNoOfPresentDays(presentDays);
                        dto.setTotalHolidays(holidaysCount);
                        dto.setTotalSickLeave(sickLeave);
                        dto.setTotalCasualLeave(casualLeave);
                        dto.setTotalDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);

                        attendanceMusterList.add(dto);
                }

                return attendanceMusterList;
        }

        @SuppressWarnings("unchecked")
        default List<AttendanceMusterDTO> employeeMusterList(String empId, String monthYear, LocalDate startDate,
                        LocalDate endDate,
                        String orgId, MongoTemplate mongoTemplate) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
                YearMonth currentMonth = YearMonth.parse(monthYear, formatter);
                String prevMonthYear = currentMonth.minusMonths(1).format(formatter);

                Criteria weekendCriteria = Criteria.where("month").in(prevMonthYear, monthYear);
                List<AttendanceWeekendPolicy> weekendPolicies = mongoTemplate.find(
                                Query.query(weekendCriteria), AttendanceWeekendPolicy.class,
                                "attendance_weekend_policy_" + orgId);

                Set<String> offDates = new HashSet<>();
                for (AttendanceWeekendPolicy policy : weekendPolicies) {
                        if (policy.getWeek() != null) {
                                for (WeekEnd week : policy.getWeek()) {
                                        if (week != null) {
                                                if ("OFF".equalsIgnoreCase(week.getSatStatus())
                                                                && week.getSatDate() != null) {
                                                        LocalDate satDate = LocalDate.parse(week.getSatDate());
                                                        if (!satDate.isBefore(startDate) && !satDate.isAfter(endDate)) {
                                                                offDates.add(satDate.toString());
                                                        }
                                                }
                                                if ("OFF".equalsIgnoreCase(week.getSunStatus())
                                                                && week.getSunDate() != null) {
                                                        LocalDate sunDate = LocalDate.parse(week.getSunDate());
                                                        if (!sunDate.isBefore(startDate) && !sunDate.isAfter(endDate)) {
                                                                offDates.add(sunDate.toString());
                                                        }
                                                }
                                        }
                                }
                        }
                }

                // Fetch active holidays
                Criteria holidayCriteria = new Criteria().andOperator(
                                Criteria.where("date").gte(startDate).lte(endDate),
                                Criteria.where("status").is("Active"));

                List<Holiday> activeHolidays = mongoTemplate.find(
                                Query.query(holidayCriteria), Holiday.class, "m_holidays_" + orgId);

                Set<String> holidayDates = activeHolidays.stream()
                                .map(h -> h.getDate().toString())
                                .collect(Collectors.toSet());

                Criteria leaveCriteria = Criteria.where("leaveApply.date").gte(startDate.toString())
                                .lte(endDate.toString());
                List<LeaveApply> approvedLeaves = mongoTemplate.find(Query.query(leaveCriteria), LeaveApply.class,
                                "leave_apply_" + orgId);
                Map<String, String> leaveTypeShortCodes = Map.of(
                                "Casual Leave", "CL",
                                "Sick Leave", "SL");

                Map<String, String> leaveMap = new HashMap<>();
                for (LeaveApply leave : approvedLeaves) {
                        String shortCode = leaveTypeShortCodes.getOrDefault(leave.getLeaveType(), "L");
                        for (LeaveApplyDates leaveDate : leave.getLeaveApply()) {
                                if ("Approved".equalsIgnoreCase(leaveDate.getStatus())) {
                                        leaveMap.put(leaveDate.getDate(), shortCode);
                                }
                        }
                }

                // Criteria primaryCriteria =
                // Criteria.where("sections.hrInformation.primary.managerId").is(empId);
                // Criteria secondaryCriteria =
                // Criteria.where("sections.hrInformation.secondary.managerId").is(empId);
                // Criteria reviewerCriteria =
                // Criteria.where("sections.hrInformation.reviewer.managerId").is(empId);
                // Criteria combinedCriteria = new Criteria().orOperator(primaryCriteria,
                // secondaryCriteria,
                // reviewerCriteria);

                // List<UserInfo> managedEmployees = mongoTemplate.find(
                // Query.query(combinedCriteria), UserInfo.class, "userinfo");

                // List<String> managedEmployeeIds = managedEmployees.stream()
                // .map(UserInfo::getEmpId)
                // .collect(Collectors.toList());

                // if (managedEmployeeIds.isEmpty()) {
                // return Collections.emptyList();
                // }

                Criteria attendanceCriteria = new Criteria().andOperator(
                                Criteria.where("empId").in(empId),
                                Criteria.where("status").is("Active"));

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(attendanceCriteria),
                                Aggregation.project("empId")
                                                .andExpression("sections.basicDetails.firstName").as("firstName")
                                                .andExpression("sections.basicDetails.lastName").as("lastName")
                                                .andExpression("sections.workingInformation.designation")
                                                .as("designation")
                                                .andExpression("sections.workingInformation.doj").as("doj")
                                                .andExpression("sections.workingInformation.workLocation")
                                                .as("workLocation"),
                                Aggregation.project("empId", "designation", "doj", "workLocation")
                                                .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(lastName, ''))")
                                                .as("empName"),
                                Aggregation.lookup("attendance_information_" + orgId, "empId", "empId",
                                                "attendanceRecords"),
                                Aggregation.unwind("attendanceRecords", true),
                                Aggregation.unwind("attendanceRecords.attendanceInfo", true),
                                Aggregation.match(Criteria.where("attendanceRecords.attendanceInfo.attendanceDate")
                                                .gte(startDate.toString()).lte(endDate.toString())),
                                Aggregation.group("empId", "empName", "designation", "doj", "workLocation")
                                                .push(new Document("date",
                                                                "$attendanceRecords.attendanceInfo.attendanceDate")
                                                                .append("attendanceData",
                                                                                "$attendanceRecords.attendanceInfo.attendanceData"))
                                                .as("attendanceData"));

                List<AttendanceMusterDTO> attendanceMusterList = new ArrayList<>();
                List<Document> documents = mongoTemplate.aggregate(aggregation, "userinfo", Document.class)
                                .getMappedResults();

                for (Document doc : documents) {
                        AttendanceMusterDTO dto = new AttendanceMusterDTO();
                        Document idDoc = (Document) doc.get("_id");

                        if (idDoc != null) {
                                dto.setEmployeeId(idDoc.getString("empId"));
                                dto.setEmployeeName(idDoc.getString("empName"));
                                dto.setDesignation(idDoc.getString("designation"));
                                Date dojDate = idDoc.getDate("doj");
                                if (dojDate != null) {
                                        dto.setDoj(dojDate.toString());
                                }
                                dto.setLocation(idDoc.getString("workLocation"));
                        }

                        dto.setMonth(monthYear);
                        List<DateAttendance> attendanceList = new ArrayList<>();
                        List<Document> attendanceDocs = (List<Document>) doc.get("attendanceData");
                        Map<String, String> attendanceMap = new HashMap<>();

                        if (attendanceDocs != null) {
                                for (Document attDoc : attendanceDocs) {
                                        attendanceMap.put(attDoc.getString("date"), attDoc.getString("attendanceData"));
                                }
                        }
                        Criteria dayTypeCriteria = new Criteria().andOperator(
                                        Criteria.where("empId").is(dto.getEmployeeId()),
                                        Criteria.where("updatedDayType.date").gte(startDate).lte(endDate));

                        List<AttendanceDayTypeHistory> dayTypeHistories = mongoTemplate.find(
                                        Query.query(dayTypeCriteria),
                                        AttendanceDayTypeHistory.class, "attendance_day_type_history_" + orgId);

                        Map<String, String> dayTypeMap = new HashMap<>();
                        for (AttendanceDayTypeHistory history : dayTypeHistories) {
                                for (UpdatedDayType dayType : history.getUpdatedDayType()) {
                                        if (!dayType.getDate().isBefore(startDate)
                                                        && !dayType.getDate().isAfter(endDate)) {
                                                dayTypeMap.put(dayType.getDate().toString(),
                                                                dayType.getDayTypes().getDayTypeId());
                                        }
                                }
                        }
                        Date dojDate = idDoc.getDate("doj");
                        LocalDate dojLocalDate = (idDoc.getDate("doj") != null)
                                        ? dojDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                        : null;

                        LocalDate dateIterator = (dojLocalDate != null && !dojLocalDate.isBefore(startDate))
                                        ? dojLocalDate
                                        : startDate;

                        int presentDays = 0, holidaysCount = 0, sickLeave = 0, casualLeave = 0;

                        while (!dateIterator.isAfter(endDate)) {
                                String dateStr = dateIterator.toString();
                                String attendanceValue = attendanceMap.getOrDefault(dateStr, "A");

                                if (dayTypeMap.containsKey(dateStr)) {
                                        String dayType = dayTypeMap.get(dateStr);
                                        switch (dayType) {
                                                case "D2":
                                                case "D4":
                                                        attendanceValue = "OFF";
                                                        break;
                                                case "D3":
                                                        attendanceValue = "H";
                                                        break;
                                                case "D1":
                                                        break;
                                        }
                                } else if (holidayDates.contains(dateStr)) {
                                        attendanceValue = "H";
                                } else if (offDates.contains(dateStr)) {
                                        attendanceValue = "OFF";
                                } else if (leaveMap.containsKey(dateStr)) {
                                        attendanceValue = leaveMap.get(dateStr);
                                }

                                DateAttendance dateAttendance = new DateAttendance();
                                dateAttendance.setDate(dateStr);
                                dateAttendance.setAttendanceData(attendanceValue);
                                attendanceList.add(dateAttendance);

                                switch (attendanceValue) {
                                        case "P":
                                                presentDays++;
                                                break;
                                        case "H":
                                                holidaysCount++;
                                                break;
                                        case "SL":
                                                sickLeave++;
                                                break;
                                        case "CL":
                                                casualLeave++;
                                                break;
                                }

                                dateIterator = dateIterator.plusDays(1);
                        }

                        dto.setAttendanceData(attendanceList);
                        dto.setTotalNoOfPresentDays(presentDays);
                        dto.setTotalHolidays(holidaysCount);
                        dto.setTotalSickLeave(sickLeave);
                        dto.setTotalCasualLeave(casualLeave);
                        dto.setTotalDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);

                        attendanceMusterList.add(dto);
                }

                return attendanceMusterList;
        }

        @SuppressWarnings("unchecked")
        default AttendanceMusterDTO employeeMuster(String empId, String monthYear, LocalDate startDate,
                        LocalDate endDate,
                        String orgId, MongoTemplate mongoTemplate) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
                YearMonth currentMonth = YearMonth.parse(monthYear, formatter);
                String prevMonthYear = currentMonth.minusMonths(1).format(formatter);

                Criteria weekendCriteria = Criteria.where("month").in(prevMonthYear, monthYear);
                List<AttendanceWeekendPolicy> weekendPolicies = mongoTemplate.find(
                                Query.query(weekendCriteria), AttendanceWeekendPolicy.class,
                                "attendance_weekend_policy_" + orgId);

                Set<String> offDates = new HashSet<>();
                for (AttendanceWeekendPolicy policy : weekendPolicies) {
                        if (policy.getWeek() != null) {
                                for (WeekEnd week : policy.getWeek()) {
                                        if (week != null) {
                                                if ("OFF".equalsIgnoreCase(week.getSatStatus())
                                                                && week.getSatDate() != null) {
                                                        LocalDate satDate = LocalDate.parse(week.getSatDate());
                                                        if (!satDate.isBefore(startDate) && !satDate.isAfter(endDate)) {
                                                                offDates.add(satDate.toString());
                                                        }
                                                }
                                                if ("OFF".equalsIgnoreCase(week.getSunStatus())
                                                                && week.getSunDate() != null) {
                                                        LocalDate sunDate = LocalDate.parse(week.getSunDate());
                                                        if (!sunDate.isBefore(startDate) && !sunDate.isAfter(endDate)) {
                                                                offDates.add(sunDate.toString());
                                                        }
                                                }
                                        }
                                }
                        }
                }

                Criteria holidayCriteria = new Criteria().andOperator(
                                Criteria.where("date").gte(startDate).lte(endDate),
                                Criteria.where("status").is("Active"));

                List<Holiday> activeHolidays = mongoTemplate.find(
                                Query.query(holidayCriteria), Holiday.class, "m_holidays_" + orgId);

                Set<String> holidayDates = activeHolidays.stream()
                                .map(h -> h.getDate().toString())
                                .collect(Collectors.toSet());

                Criteria leaveCriteria = Criteria.where("leaveApply.date").gte(startDate.toString())
                                .lte(endDate.toString());
                List<LeaveApply> approvedLeaves = mongoTemplate.find(Query.query(leaveCriteria), LeaveApply.class,
                                "leave_apply_" + orgId);
                Map<String, String> leaveTypeShortCodes = Map.of(
                                "Casual Leave", "CL",
                                "Sick Leave", "SL");

                Map<String, String> leaveMap = new HashMap<>();
                for (LeaveApply leave : approvedLeaves) {
                        String shortCode = leaveTypeShortCodes.getOrDefault(leave.getLeaveType(), "L");
                        for (LeaveApplyDates leaveDate : leave.getLeaveApply()) {
                                if ("Approved".equalsIgnoreCase(leaveDate.getStatus())) {
                                        leaveMap.put(leaveDate.getDate(), shortCode);
                                }
                        }
                }

                Criteria attendanceCriteria = new Criteria().andOperator(
                                Criteria.where("empId").is(empId),
                                Criteria.where("status").is("Active"));

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(attendanceCriteria),
                                Aggregation.project("empId")
                                                .andExpression("sections.basicDetails.firstName").as("firstName")
                                                .andExpression("sections.basicDetails.lastName").as("lastName")
                                                .andExpression("sections.workingInformation.designation")
                                                .as("designation")
                                                .andExpression("sections.workingInformation.doj").as("doj")
                                                .andExpression("sections.workingInformation.workLocation")
                                                .as("workLocation"),
                                Aggregation.project("empId", "designation", "doj", "workLocation")
                                                .andExpression("concat(ifNull(firstName, ''), ' ', ifNull(lastName, ''))")
                                                .as("empName"),
                                Aggregation.lookup("attendance_information_" + orgId, "empId", "empId",
                                                "attendanceRecords"),
                                Aggregation.unwind("attendanceRecords", true),
                                Aggregation.unwind("attendanceRecords.attendanceInfo", true),
                                Aggregation.match(Criteria.where("attendanceRecords.attendanceInfo.attendanceDate")
                                                .gte(startDate.toString()).lte(endDate.toString())),
                                Aggregation.group("empId", "empName", "designation", "doj", "workLocation")
                                                .push(new Document("date",
                                                                "$attendanceRecords.attendanceInfo.attendanceDate")
                                                                .append("attendanceData",
                                                                                "$attendanceRecords.attendanceInfo.attendanceData"))
                                                .as("attendanceData"));

                List<Document> documents = mongoTemplate.aggregate(aggregation, "userinfo", Document.class)
                                .getMappedResults();

                if (documents.isEmpty()) {
                        return null;
                }

                Document doc = documents.get(0);
                AttendanceMusterDTO dto = new AttendanceMusterDTO();
                Document idDoc = (Document) doc.get("_id");

                if (idDoc != null) {
                        dto.setEmployeeId(idDoc.getString("empId"));
                        dto.setEmployeeName(idDoc.getString("empName"));
                        dto.setDesignation(idDoc.getString("designation"));
                        Date dojDate = idDoc.getDate("doj");
                        if (dojDate != null) {
                                dto.setDoj(dojDate.toString());
                        }
                        dto.setLocation(idDoc.getString("workLocation"));
                }

                dto.setMonth(monthYear);
                List<DateAttendance> attendanceList = new ArrayList<>();
                List<Document> attendanceDocs = (List<Document>) doc.get("attendanceData");
                Map<String, String> attendanceMap = new HashMap<>();

                if (attendanceDocs != null) {
                        for (Document attDoc : attendanceDocs) {
                                attendanceMap.put(attDoc.getString("date"), attDoc.getString("attendanceData"));
                        }
                }

                Criteria dayTypeCriteria = new Criteria().andOperator(
                                Criteria.where("empId").is(dto.getEmployeeId()),
                                Criteria.where("updatedDayType.date").gte(startDate).lte(endDate));

                List<AttendanceDayTypeHistory> dayTypeHistories = mongoTemplate.find(
                                Query.query(dayTypeCriteria), AttendanceDayTypeHistory.class,
                                "attendance_day_type_history_" + orgId);

                Map<String, String> dayTypeMap = new HashMap<>();
                for (AttendanceDayTypeHistory history : dayTypeHistories) {
                        for (UpdatedDayType dayType : history.getUpdatedDayType()) {
                                if (!dayType.getDate().isBefore(startDate) && !dayType.getDate().isAfter(endDate)) {
                                        dayTypeMap.put(dayType.getDate().toString(),
                                                        dayType.getDayTypes().getDayTypeId());
                                }
                        }
                }

                LocalDate dojLocalDate = (idDoc.getDate("doj") != null)
                                ? idDoc.getDate("doj").toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                : null;

                LocalDate dateIterator = (dojLocalDate != null && !dojLocalDate.isBefore(startDate))
                                ? dojLocalDate
                                : startDate;

                int presentDays = 0, holidaysCount = 0, sickLeave = 0, casualLeave = 0;

                while (!dateIterator.isAfter(endDate)) {
                        String dateStr = dateIterator.toString();
                        String attendanceValue = attendanceMap.getOrDefault(dateStr, "A");

                        if (dayTypeMap.containsKey(dateStr)) {
                                switch (dayTypeMap.get(dateStr)) {
                                        case "D2":
                                        case "D4":
                                                attendanceValue = "OFF";
                                                break;
                                        case "D3":
                                                attendanceValue = "H";
                                                break;
                                }
                        } else if (holidayDates.contains(dateStr)) {
                                attendanceValue = "H";
                        } else if (offDates.contains(dateStr)) {
                                attendanceValue = "OFF";
                        } else if (leaveMap.containsKey(dateStr)) {
                                attendanceValue = leaveMap.get(dateStr);
                        }

                        attendanceList.add(new DateAttendance(dateStr, attendanceValue));

                        switch (attendanceValue) {
                                case "P":
                                        presentDays++;
                                        break;
                                case "H":
                                        holidaysCount++;
                                        break;
                                case "SL":
                                        sickLeave++;
                                        break;
                                case "CL":
                                        casualLeave++;
                                        break;
                        }

                        dateIterator = dateIterator.plusDays(1);
                }

                dto.setAttendanceData(attendanceList);
                dto.setTotalNoOfPresentDays(presentDays);
                dto.setTotalHolidays(holidaysCount);
                dto.setTotalSickLeave(sickLeave);
                dto.setTotalCasualLeave(casualLeave);
                dto.setTotalDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);

                return dto;
        }

        default List<LopDTO> findByEmpIdAndMonth(MongoTemplate mongoTemplate, String org, String empId,
                        List<String> month) {

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(Criteria.where("empId").is(empId).and("monthYear").in(month)),
                                Aggregation.project()
                                                .and("totalLop").as("lop")
                                                .and("empId").as("empId")
                                                .and("monthYear").as("monthYear")
                                                .and("lopReversal").as("lopReversal")
                                                .andExclude("_id"));

                return mongoTemplate.aggregate(aggregation, getCollectionNames(org), LopDTO.class).getMappedResults();

        }

        default void bulkSave(MongoTemplate mongoTemplate, String orgId, List<AttendanceMuster> musterList) {
                String collection = getCollectionNames(orgId);
                BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                                AttendanceMuster.class, collection);

                List<InsertOneModel<AttendanceMuster>> inserts = musterList.stream()
                                .map(InsertOneModel::new)
                                .toList();

                bulkOps.insert(inserts);
                bulkOps.execute();
        }

}
