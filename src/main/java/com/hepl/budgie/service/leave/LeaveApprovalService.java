package com.hepl.budgie.service.leave;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.hepl.budgie.dto.leave.AdminLeaveApproveDto;
import com.hepl.budgie.dto.leave.LeaveApprovalDTO;

public interface LeaveApprovalService {

	List<LeaveApprovalDTO> getLeaveDatas(String status, String roleType);

	String approveOrRejectLeave(String leaveCode, String type, String rejectReason);

	Object getLeaveData(String leaveCode) throws IllegalAccessException, InvocationTargetException;

	String withdrawLeave(String leaveCode);

	String adminApproveOrRejectLeave(List<AdminLeaveApproveDto> leaveRequests);


}
