package com.hepl.budgie.service.leave;

import java.util.List;
import java.util.Map;

import com.hepl.budgie.dto.leave.CheckDate;
import com.hepl.budgie.dto.leave.CompOffDto;
import com.hepl.budgie.dto.leave.HolidayApplyDto;
import com.hepl.budgie.dto.leave.LeaveApplyBalanceDTO;
import com.hepl.budgie.dto.leave.LeaveApplyDTO;
import com.hepl.budgie.dto.leave.LeaveEmpDetailsResponseDTO;
import com.hepl.budgie.entity.settings.Holiday;

import jakarta.validation.Valid;

public interface LeaveApplyService1 {

	LeaveEmpDetailsResponseDTO fetchEmployeeDetails(String id);

	List<String> fetchLeaveType(String empId, String type);

	LeaveApplyBalanceDTO fetchEmployeeLeaveBalance(String empId, String type, String leaveType);

	CheckDate checkLeaveApplyDate(String empId, String fromDate, String toDate, String fromSession, String toSession);

	Object leaveApply(@Valid LeaveApplyDTO leaveApply);

    String applyRestictedHolidays(List<HolidayApplyDto> apply);

	List<Holiday> getRestictedHolidaysList();

    void applyCompOff(CompOffDto comp);

    List<Map<String, Object>> workedDate();

}
