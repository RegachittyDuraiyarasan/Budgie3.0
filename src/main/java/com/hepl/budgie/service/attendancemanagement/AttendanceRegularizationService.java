package com.hepl.budgie.service.attendancemanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.regularization.RegularizationApproveDto;
import com.hepl.budgie.dto.regularization.RegularizationDto;
import com.hepl.budgie.entity.attendancemanagement.AttendanceRegularization;
import com.hepl.budgie.entity.leave.LeaveApply;

import jakarta.mail.MessagingException;

public interface AttendanceRegularizationService {

    AttendanceRegularization applyAttendanceRegularization(String empId, RegularizationDto regularizationDto) throws MessagingException;

    AttendanceRegularization approvedRegularization(String empId,String regCode, String key, List<LocalDate> approvedDate, List<LocalDate> rejectedDate,String reason,List<String> remark,String month) throws MessagingException;

    Map<String, Object> getRegulization(String key);

    Map<String, Object> getEmpRegulization(String key);

    Map<String, Object> getAbsentAttendance(String empId, String monthYear);

    GenericResponse<Map<String, Object>> getAbsentAndPresentAttendance(String empId, String currentMonth);

    GenericResponse<String> withdrawal(String empId, String regCode, String monthYear);

    Map<String, Object> getAppliedTo();

    List<AttendanceRegularization> getAdminRegularization(String month,String empId);

    List<LeaveApply> getAdminLeaveApply(String leaveType, String empId, String month);

    List<AttendanceRegularization> adminApproveRegularization(List<RegularizationApproveDto> regularizationApproveDto);

    GenericResponse<Map<String, Object>> presentAbsentList(String empId, String currentMonth);

    GenericResponse<Map<String, Object>> getPayrollLockDate(String currentMonth);

    void attendanceShortFall() throws MessagingException;
    
}
