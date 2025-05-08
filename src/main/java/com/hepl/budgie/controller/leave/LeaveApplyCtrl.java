package com.hepl.budgie.controller.leave;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.leave.CheckDate;
import com.hepl.budgie.dto.leave.CompOffDto;
import com.hepl.budgie.dto.leave.HolidayApplyDto;
import com.hepl.budgie.dto.leave.LeaveApplyBalanceDTO;
import com.hepl.budgie.dto.leave.LeaveApplyDTO;
import com.hepl.budgie.dto.leave.LeaveEmpDetailsResponseDTO;
import com.hepl.budgie.entity.settings.Holiday;
import com.hepl.budgie.service.leave.LeaveApplyService1;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Leave Apply", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/leave-apply")
public class LeaveApplyCtrl {

	private final Translator translator;
	private final LeaveApplyService1 leaveApplyService;

	@GetMapping("/{id}")
	@Operation(summary = "Fetch Employee Details")
	public GenericResponse<LeaveEmpDetailsResponseDTO> fetchEmployeeDetails(@PathVariable String id) {
		LeaveEmpDetailsResponseDTO empDetails = leaveApplyService.fetchEmployeeDetails(id);

		return GenericResponse.success(empDetails);
	}

	@GetMapping("/leave-type")
	@Operation(summary = "Fetch Employee Leave Type")
	public GenericResponse<List<String>> fetchEmployeeLeaveType(@RequestParam String empId, @RequestParam String type) {
		List<String> leaveType = leaveApplyService.fetchLeaveType(empId, type);

		return GenericResponse.success(leaveType);
	}

	@GetMapping("/leave-balance")
	@Operation(summary = "Fetch Employee Leave Balance")
	public GenericResponse<LeaveApplyBalanceDTO> fetchEmployeeLeaveBalance(@RequestParam String empId,
			@RequestParam String type, @RequestParam String leaveType) {
		LeaveApplyBalanceDTO leaveBalance = leaveApplyService.fetchEmployeeLeaveBalance(empId, type, leaveType);

		return GenericResponse.success(leaveBalance);
	}

	@GetMapping("/check-date")
	@Operation(summary = "From - To date check ")
	public GenericResponse<CheckDate> checkDate(@RequestParam String empId, @RequestParam String fromDate,
			@RequestParam String toDate, @RequestParam String fromSession, @RequestParam String toSession) {

		CheckDate response = leaveApplyService.checkLeaveApplyDate(empId, fromDate, toDate, fromSession, toSession);
		
		return GenericResponse.success(response);
	}
	
	@PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Apply Leave")
	public GenericResponse<String> applyLeave(@Valid @ModelAttribute LeaveApplyDTO leaveApply){
		leaveApplyService.leaveApply(leaveApply);
		
		return GenericResponse.success(translator.toLocale(AppMessages.LEAVE_APPLIED));
	}

	@PostMapping("/resticted-holidays-apply")
	@Operation(summary = "Apply Leave")
	public GenericResponse<String> applyRestictedHolidays(@RequestBody List<HolidayApplyDto> applyHoliday) {

		String result = leaveApplyService.applyRestictedHolidays(applyHoliday);
		return GenericResponse.success(result);

	}

	@GetMapping("/resticted-holidays-list")
	@Operation(summary = "Apply Leave")
	public GenericResponse<List<Holiday>> getRestictedHolidaysList() {

		List<Holiday> result = leaveApplyService.getRestictedHolidaysList();
		return GenericResponse.success(result);
	}

	@PostMapping("/comp-off-apply")
	public GenericResponse<String> applyCompOff(@RequestBody CompOffDto comp) {
		leaveApplyService.applyCompOff(comp);
		return GenericResponse.success(translator.toLocale(AppMessages.COMP_OFF_APPLIED));
	}

	@GetMapping("/worked-date-list")
	public GenericResponse<List<Map<String, Object>>> workedDate() {
		List<Map<String, Object>> result = leaveApplyService.workedDate();
		return GenericResponse.success(result);
	}
	
}
