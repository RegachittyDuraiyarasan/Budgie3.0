package com.hepl.budgie.controller.leavemanagement;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactionType;
import com.hepl.budgie.service.leavemanagement.LeaveTransactionTypeService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Leave Transaction Type", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/leave-transaction")
@RequiredArgsConstructor
public class LeaveTransactionTypeController {

	private final LeaveTransactionTypeService leaveTransactionTypeService;
	
	@PostMapping()
	public GenericResponse<String> saveLeaveTransaction(@Valid @RequestBody LeaveTransactionType leaveTransactionType) {
		String transactionType = leaveTransactionTypeService.saveLeaveTransactionType(leaveTransactionType);
		
		return GenericResponse.success(transactionType);
	}
	
	@DeleteMapping()
	public GenericResponse<String> deleteLeaveTransaction(@RequestParam String transactionTypeId) {
		leaveTransactionTypeService.deleteLeaveTransactionType(transactionTypeId);
		
		return GenericResponse.success("Leave Transaction Type Deleted Successfully");
	}
	
	@GetMapping()
	public GenericResponse<List<LeaveTransactionType>> getLeaveTransaction() {
		List<LeaveTransactionType> transactionTypes = leaveTransactionTypeService.fetchLeaveTransactionType();
		
		return GenericResponse.success(transactionTypes);
	}
	
	@GetMapping("/types")
	public GenericResponse<List<String>> getLeaveTransactionTypes() {
		List<String> transactionTypes = leaveTransactionTypeService.fetchLeaveTransactionTypes();
		
		return GenericResponse.success(transactionTypes);
	}
}
