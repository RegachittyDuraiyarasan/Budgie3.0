package com.hepl.budgie.service.leave;

import java.util.List;

import com.hepl.budgie.entity.leave.LeaveApply;

public interface LeaveCancelService {

	List<LeaveApply> getLeaveApproved();

	void leaveCancel(String leaveCode, List<String> appliedCC, String reason);

}
