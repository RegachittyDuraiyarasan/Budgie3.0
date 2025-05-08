package com.hepl.budgie.service.leavemanagement;

import java.util.List;
import java.util.Map;

import com.hepl.budgie.dto.leavemanagement.AdminLeaveCalendarDateFilterDTO;
import com.hepl.budgie.dto.leavemanagement.AdminLeaveCalenderDTO;

public interface LeaveCalendarAdminService {

    List<AdminLeaveCalendarDateFilterDTO> getLeaveCalendarAdminList(AdminLeaveCalenderDTO adminCalenderFilterData);

    List<Map<String, Object>> getEmployeeLeaveCalendar(String empId, String monthYear, boolean isTeams, String reviewer, String repManager, String department, String designation, String payrollStatus, String location, String fromDate, String toDate);
}
