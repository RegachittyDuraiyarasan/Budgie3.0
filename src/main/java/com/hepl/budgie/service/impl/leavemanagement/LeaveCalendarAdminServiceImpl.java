package com.hepl.budgie.service.impl.leavemanagement;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.leavemanagement.AdminLeaveCalendarDateFilterDTO;
import com.hepl.budgie.dto.leavemanagement.AdminLeaveCalenderDTO;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leave.LeaveApplyDates;
import com.hepl.budgie.entity.leavemanagement.LeaveApplys;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.entity.payroll.PayrollLockMonth.PayrollMonths;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.leave.LeaveApplyRepo;
import com.hepl.budgie.repository.leavemanagement.LeaveCalendarAdminRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.leavemanagement.LeaveCalendarAdminService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class LeaveCalendarAdminServiceImpl implements LeaveCalendarAdminService {

    private final LeaveCalendarAdminRepository leaveCalendarAdminRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private final PayrollLockMonthRepository payrollLockMonthRepository;
    private final LeaveApplyRepo leaveApplyRepo;
    private final UserInfoRepository userInfoRepository;

    @Override
    public List<AdminLeaveCalendarDateFilterDTO> getLeaveCalendarAdminList(
            AdminLeaveCalenderDTO adminCalenderFilterData) {
        // Mandatory parameter
        String yearMonth = adminCalenderFilterData.getYearMonth();
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Define the date format pattern (this should match your constant DATE_FORMAT
        // pattern)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Date Criteria
        Criteria dateCriteria = Criteria.where("leaveApply.date");
        if (adminCalenderFilterData.getFromDate() != null && adminCalenderFilterData.getToDate() != null) {
            dateCriteria = dateCriteria.gte(adminCalenderFilterData.getFromDate().format(dateFormatter))
                    .lt(adminCalenderFilterData.getToDate().plusDays(1).format(dateFormatter));
        } else if (adminCalenderFilterData.getFromDate() != null) {
            dateCriteria = dateCriteria.gte(adminCalenderFilterData.getFromDate().format(dateFormatter))
                    .lt(endDate.plusDays(1).format(dateFormatter));
        } else if (adminCalenderFilterData.getToDate() != null) {
            dateCriteria = dateCriteria.gte(startDate.format(dateFormatter))
                    .lt(adminCalenderFilterData.getToDate().plusDays(1).format(dateFormatter));
        } else {
            dateCriteria = dateCriteria.gte(startDate.format(dateFormatter))
                    .lt(endDate.plusDays(1).format(dateFormatter));
        }

        Query dateQuery = new Query(dateCriteria);
        List<LeaveApplys> leaveApplies = mongoTemplate.find(dateQuery, LeaveApplys.class);

        Criteria userId = new Criteria();
        boolean hasUserInfoFilter = false;
        if (adminCalenderFilterData.getEmpId() != null) {
            userId.and("empId").is(adminCalenderFilterData.getEmpId());
            hasUserInfoFilter = true;
        }
        Set<String> userEmpIds = new HashSet<>();
        if (hasUserInfoFilter) {
            Query userQuery = new Query(userId);
            List<UserInfo> userResult = mongoTemplate.find(userQuery, UserInfo.class);
            userEmpIds = Optional.of(userResult)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(UserInfo::getEmpId)
                    .collect(Collectors.toSet());
        }

        // Combine Work Info Criteria
        Criteria workInfoCriteria = new Criteria();
        boolean hasWorkInfoFilter = false;
        if (adminCalenderFilterData.getLocation() != null) {
            workInfoCriteria.and("workLocation").is(adminCalenderFilterData.getLocation());
            hasWorkInfoFilter = true;
        }
        if (adminCalenderFilterData.getDepartment() != null) {
            workInfoCriteria.and("department").is(adminCalenderFilterData.getDepartment());
            hasWorkInfoFilter = true;
        }
        if (adminCalenderFilterData.getDesignation() != null) {
            workInfoCriteria.and("designation").is(adminCalenderFilterData.getDesignation());
            hasWorkInfoFilter = true;
        }
        if (adminCalenderFilterData.getPayRollStatus() != null) {
            workInfoCriteria.and("roleOfIntake").is(adminCalenderFilterData.getPayRollStatus());
            hasWorkInfoFilter = true;
        }
        if (adminCalenderFilterData.getBand() != null) {
            workInfoCriteria.and("grade").is(adminCalenderFilterData.getBand());
            hasWorkInfoFilter = true;
        }
        Set<String> workInfoEmpIds = new HashSet<>();
        // if (hasWorkInfoFilter) {
        // Query workInfoQuery = new Query(workInfoCriteria);
        // List<UserWorkInfo> userWorkInfo = mongoTemplate.find(workInfoQuery,
        // UserWorkInfo.class);
        // workInfoEmpIds = Optional.ofNullable(userWorkInfo)
        // .orElse(Collections.emptyList())
        // .stream()
        // .map(UserWorkInfo::getEmpId)
        // .collect(Collectors.toSet());
        // }

        // HR Info Criteria
        Criteria hrInfoCriteria = new Criteria();
        boolean hasHrInfoFilter = false;
        if (adminCalenderFilterData.getReviewerId() != null) {
            hrInfoCriteria.and("reviewer").is(adminCalenderFilterData.getReviewerId());
            hasHrInfoFilter = true;
        }
        if (adminCalenderFilterData.getReportingManagerId() != null) {
            hrInfoCriteria.and("primaryReportingManager").is(adminCalenderFilterData.getReportingManagerId());
            hasHrInfoFilter = true;
        }

        Set<String> hrInfoEmpIds = new HashSet<>();
        // if (hasHrInfoFilter) {
        // Query hrInfoQuery = new Query(hrInfoCriteria);
        // List<UserHRInfo> userHRInfo = mongoTemplate.find(hrInfoQuery,
        // UserHRInfo.class);
        // hrInfoEmpIds = Optional.ofNullable(userHRInfo)
        // .orElse(Collections.emptyList())
        // .stream()
        // .map(UserHRInfo::getEmpId)
        // .collect(Collectors.toSet());
        //
        // // Check if hrInfoEmpIds is empty
        // if (hrInfoEmpIds.isEmpty()) {
        // return new ApiResponse<>(HttpStatus.OK.value(),
        // Constant.ResponseMessages.FETCH, Collections.emptyList());
        // }
        // }

        Set<String> combinedEmpIds = new HashSet<>();
        boolean hasCombinedEmpIds = false;
        if (!userEmpIds.isEmpty()) {
            combinedEmpIds.addAll(userEmpIds);
            hasCombinedEmpIds = true;
        }
        if (!workInfoEmpIds.isEmpty()) {
            if (combinedEmpIds.isEmpty()) {
                combinedEmpIds.addAll(workInfoEmpIds);
            } else {
                combinedEmpIds.retainAll(workInfoEmpIds);
            }
            hasCombinedEmpIds = true;
        }
        if (!hrInfoEmpIds.isEmpty()) {
            if (combinedEmpIds.isEmpty()) {
                combinedEmpIds.addAll(hrInfoEmpIds);
            } else {
                combinedEmpIds.retainAll(hrInfoEmpIds);
            }
            hasCombinedEmpIds = true;
        }

        List<AdminLeaveCalendarDateFilterDTO> filteredLeaveApplies;
        if (!hasWorkInfoFilter && !hasHrInfoFilter && !hasUserInfoFilter) {
            filteredLeaveApplies = dtoDataSetter(leaveApplies).stream()
                    // .filter(leave -> "Approved".equals(leave.getStatus()))
                    .toList();
        } else {
            filteredLeaveApplies = dtoDataSetter(leaveApplies).stream()
                    // .filter(leave -> combinedEmpIds.contains(leave.getEmpId()) &&
                    // "Approved".equals(leave.getStatus()))
                    .toList();
        }
        return filteredLeaveApplies;

    }

    public List<AdminLeaveCalendarDateFilterDTO> dtoDataSetter(List<LeaveApplys> apply) {
        List<AdminLeaveCalendarDateFilterDTO> filterDtoList = new ArrayList<>();
        apply.forEach(format -> {
            // Optional<UserInfo> user = UserInfoRepository.findByEmpId(format.getEmpId());
            AdminLeaveCalendarDateFilterDTO filterData = new AdminLeaveCalendarDateFilterDTO();
            // if (user.isPresent()){
            // String userName= user.get().getFirstName()+" "+user.get().getLastName();
            // filterData.setEmpName(userName);
            // }
            filterData.setId(format.getId());
            filterData.setLeaveCode(format.getLeaveCode());
            filterData.setEmpId(format.getEmpId());
            filterData.setAppliedTo(format.getAppliedTo());
            filterData.setLeaveType(format.getLeaveType());
            filterData.setLeaveCategory(format.getLeaveCategory());
            filterData.setChildType(format.getChildType());
            filterData.setType(format.getType());
            filterData.setLeaveApply(format.getLeaveApply());
            filterData.setNumOfDays(format.getNumOfDays());
            filterData.setBalance(format.getBalance());
            filterData.setCompOffWorkDate(format.getCompOffWorkDate());
            filterData.setEmpReason(format.getEmpReason());
            filterData.setContactNo(format.getContactNo());
            filterData.setApproverReason(format.getApproverReason());
            filterData.setFromDate(format.getFromDate());
            filterData.setToDate(format.getToDate());
            filterData.setAppliedCC(format.getAppliedCC());
            filterData.setFile(format.getFile());
            filterData.setLeaveCancel(format.getLeaveCancel());
            filterData.setExpectedDate(format.getExpectedDate());
            filterData.setMaternityLeaveType(format.getMaternityLeaveType());
            filterData.setOldLeaveCode(format.getOldLeaveCode());
            filterData.setStatus(format.getStatus());
            filterData.setApproveOrRejectDate(String.valueOf(format.getApproveOrRejectDate()));
            filterDtoList.add(filterData);
        });
        return filterDtoList;
    }

    @Override
    public List<Map<String, Object>> getEmployeeLeaveCalendar(String empId, String monthYear, boolean isTeams,
            String reviewer, String repManager, String department, String designation, String payrollStatus,
            String location, String fromDate, String toDate) {

        String orgId = jwtHelper.getOrganizationCode();
        String employeeId = jwtHelper.getUserRefDetail().getEmpId();
        String role = jwtHelper.getUserRefDetail().getActiveRole();
        String finYear = getFinancialYear(monthYear);

        PayrollLockMonth payrollLock = payrollLockMonthRepository.getPayrollByFinYear(finYear, mongoTemplate,
                orgId, "IN");
        PayrollMonths payrollMonth = payrollLock.getPayrollMonths().stream()
                .filter(pm -> pm.getPayrollMonth().equals(monthYear))
                .findFirst()
                .orElse(null);

        LocalDate payrollStartDate = payrollMonth.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate payrollEndDate = payrollMonth.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        List<String> empIds = new ArrayList<>();
        Map<String, String> empNames = new HashMap<>();

        if ("Payroll Admin".equalsIgnoreCase(role)) {
            List<UserInfo> filteredUsers;

            filteredUsers = userInfoRepository.findFilteredEmployeesForPayrollAdmin(
                    empId, orgId, reviewer, repManager, department, designation,
                    payrollStatus, location, mongoTemplate);
            if (filteredUsers.isEmpty()) {
                return Collections.emptyList();
            }
            for (UserInfo user : filteredUsers) {
                empIds.add(user.getEmpId());
                empNames.put(user.getEmpId(), getEmployeeFullName(user));
            }

            if (!filteredUsers.isEmpty()) {
                List<String> userEmpIds = filteredUsers.stream()
                        .map(UserInfo::getEmpId)
                        .collect(Collectors.toList());

                return calculateEmployeeLeave(
                        userEmpIds,
                        orgId,
                        payrollStartDate,
                        payrollEndDate,
                        empNames,
                        fromDate,
                        toDate);
            }
        } else if ("Employee".equalsIgnoreCase(role) && !isTeams && empId == null) {
            empIds.add(employeeId);
            UserInfo userInfo = userInfoRepository.findByEmpId(employeeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND));
            if (userInfo != null) {
                empNames.put(employeeId, getEmployeeFullName(userInfo));
            }
        } else if (isTeams && empId == null) {
            List<UserInfo> reportingUsers = userInfoRepository.findReportingUsers(employeeId, mongoTemplate);
            for (UserInfo user : reportingUsers) {
                empIds.add(user.getEmpId());
                empNames.put(user.getEmpId(), getEmployeeFullName(user));
            }
        } else if (isTeams) {
            empIds.add(empId);
            UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND));
            if (userInfo != null) {
                empNames.put(empId, getEmployeeFullName(userInfo));
            }
        }
        return calculateEmployeeLeave(empIds, orgId, payrollStartDate, payrollEndDate, empNames, fromDate, toDate);
    }

    private String getEmployeeFullName(UserInfo userInfo) {
        return userInfo.getSections().getBasicDetails().getFirstName() + " " +
                userInfo.getSections().getBasicDetails().getLastName();
    }

    private List<Map<String, Object>> calculateEmployeeLeave(
            List<String> empIds,
            String orgId,
            LocalDate defaultStartDate,
            LocalDate defaultEndDate,
            Map<String, String> empNames,
            String fromDate,
            String toDate) {
        // Decide which date range to use: either fromDate/toDate or fallback to
        // defaultStartDate/defaultEndDate
        LocalDate startDate = defaultStartDate;
        LocalDate endDate = defaultEndDate;

        if (fromDate != null && !fromDate.isBlank() && toDate != null && !toDate.isBlank()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            startDate = LocalDate.parse(fromDate, formatter);
            endDate = LocalDate.parse(toDate, formatter);
        }
        List<LeaveApply> leaveApplyList = leaveApplyRepo.findByEmpIdsAndFromDateAndToDateBetween(
                empIds, orgId, startDate, endDate, mongoTemplate);
        Map<String, List<Map<String, String>>> leaveCalendar = new HashMap<>();
        for (LeaveApply leave : leaveApplyList) {
            String empFullName = empNames.getOrDefault(leave.getEmpId(), leave.getEmpId());
            List<LeaveApplyDates> approvedLeaveDates = leave.getLeaveApply().stream()
                    .filter(leaveDate -> "approved".equalsIgnoreCase(leaveDate.getStatus()))
                    .collect(Collectors.toList());
            for (LeaveApplyDates leaveDate : approvedLeaveDates) {
                String dateKey = leaveDate.getDate();
                leaveCalendar.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(
                        Map.of(
                                "empId", leave.getEmpId(),
                                "empName", empFullName,
                                "leaveType", leave.getLeaveType(),
                                "reason", leave.getEmpReason()));
            }
        }
        return leaveCalendar.entrySet().stream()
                .map(entry -> Map.of(
                        "date", entry.getKey(),
                        "employees", entry.getValue()))
                .collect(Collectors.toList());
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
