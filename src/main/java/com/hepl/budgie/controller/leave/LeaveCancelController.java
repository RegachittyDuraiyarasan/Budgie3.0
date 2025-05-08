package com.hepl.budgie.controller.leave;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.leave.LeaveApplyDTO;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.service.leave.LeaveCancelService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Leave Cancel", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/leave-cancel")
public class LeaveCancelController {

	private final Translator translator;
	private final LeaveCancelService leaveCancelService;
	
	
	@GetMapping()
	public GenericResponse<List<LeaveApply>> fetchEmployeeDetails() {
		List<LeaveApply> empDetails = leaveCancelService.getLeaveApproved();

		return GenericResponse.success(empDetails);
	}
	
	@PostMapping()
	@Operation(summary = "Leave Cancel")
	public GenericResponse<String> applyLeave(@RequestParam String leaveCode, @RequestParam List<String> appliedCC, @RequestParam String reason){
		leaveCancelService.leaveCancel(leaveCode, appliedCC, reason);
		
		return GenericResponse.success(translator.toLocale(AppMessages.LEAVE_CANCEL));
	}
}
