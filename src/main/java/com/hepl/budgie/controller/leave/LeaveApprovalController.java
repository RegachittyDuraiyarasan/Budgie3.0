package com.hepl.budgie.controller.leave;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.leave.AdminLeaveApproveDto;
import com.hepl.budgie.dto.leave.LeaveApprovalDTO;
import com.hepl.budgie.service.leave.LeaveApprovalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Leave Approval", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/leave-approval")
public class LeaveApprovalController {

	private final Translator translator;
	private final LeaveApprovalService leaveApprovalService;

	@GetMapping("")
	@Operation(summary = "Fetch Leave Active and Closed Data's")
	public GenericResponse<List<LeaveApprovalDTO>> fetchLeaveDatas(@RequestParam String status,
			@RequestParam String roleType) {
		List<LeaveApprovalDTO> response = leaveApprovalService.getLeaveDatas(status, roleType);

		return GenericResponse.success(response);
	}

	@PostMapping("/approve-or-reject")
	@Operation(summary = "Approve or Reject a Leave Request")
	public GenericResponse<String> approveOrRejectLeave(@RequestParam String leaveCode, @RequestParam String type,
			@RequestParam(required = false) String rejectReason) {
		String result = leaveApprovalService.approveOrRejectLeave(leaveCode, type, rejectReason);
		return GenericResponse.success(result);
	}
	
	@GetMapping("/view")
	@Operation(summary = "Fetch Leave data by LeaveCode")
	public GenericResponse<?> fetchLeaveData(@RequestParam String leaveCode) throws IllegalAccessException, InvocationTargetException {
		Object response = leaveApprovalService.getLeaveData(leaveCode);

		return GenericResponse.success(response);
	}
	
	@PostMapping("/withdraw")
	@Operation(summary = "Withdraw Leave Apply")
	public GenericResponse<String> withdrawLeave(@RequestParam String leaveCode) {
		String result = leaveApprovalService.withdrawLeave(leaveCode);
		
		return GenericResponse.success(result);
	}

	@PostMapping("/admin/approve-or-reject")
    @Operation(summary = "Admin Approve or Reject Leave Requests")
    public GenericResponse<String> adminApproveOrRejectLeave(@RequestBody List<AdminLeaveApproveDto> leaveRequests) {
        String result = leaveApprovalService.adminApproveOrRejectLeave(leaveRequests);
        return GenericResponse.success(result);
    }
}
