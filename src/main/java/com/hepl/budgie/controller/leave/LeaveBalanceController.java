package com.hepl.budgie.controller.leave;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leave.LeaveBalanceSummaryResponse;
import com.hepl.budgie.service.leave.LeaveBalanceService;
import com.hepl.budgie.service.leavemanagement.EmployeeLeaveBalanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Leave Balance", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/leave-balance")
public class LeaveBalanceController {

	private final Translator translator;
	private final LeaveBalanceService leaveBalanceService;
	private final EmployeeLeaveBalanceService employeeLeaveBalanceService;

	@GetMapping("/{year}")
	@Operation(summary = "Fetch Leave Details By Year")
	public GenericResponse<Object> fetchLeaveByYear(@PathVariable String year) {
		List<LeaveBalanceSummaryResponse> response = leaveBalanceService.fetchLeaveByYear(year);

		return GenericResponse.success(response);
	}

	@GetMapping("/year-list")
	public GenericResponse<List<Integer>> year() {
		List<Integer> years = leaveBalanceService.fetchLeaveByYear();

		return GenericResponse.success(years);
	}

	@GetMapping("/transactions-history")
	@Operation(summary = "Fetch Leave Transaction History's")
	public GenericResponse<Object> fetchTransaction(@RequestParam String year, @RequestParam String leaveType) {
		Object response = leaveBalanceService.fetchTransactions(year, leaveType);

		return GenericResponse.success(response);
	}

	@GetMapping("/summary")
	@Operation(summary = "Fetch Leave Transaction Summary ")
	public GenericResponse<Object> fetchSummary(@RequestParam String year, @RequestParam String leaveType) {
		Object response = leaveBalanceService.fetchSummary(year, leaveType);

		return GenericResponse.success(response);
	}

	@GetMapping("/leave-count")
	public GenericResponse<Object> fetchLeaveCount(@RequestParam String year, @RequestParam String leaveType) {
		Object response = leaveBalanceService.fetchLeaveCount(year, leaveType);

		return GenericResponse.success(response);
	}

	@GetMapping("/leave-chart")
	@Operation(summary = "Fetch Leave Balance and Consumed by Year")
	public GenericResponse<Object> fetchLeaveChart(@RequestParam String year, @RequestParam String leaveType) {
		Object response = leaveBalanceService.fetchLeaveChart(year, leaveType);

		return GenericResponse.success(response);
	}

	@PostMapping("/export/{empId}")
	@Operation(summary = "Leave Transaction Report")
	public ResponseEntity<byte[]> exportLeaveData(@PathVariable String empId, @RequestBody FormRequest formRequest,
			@RequestParam String org) {
//		masterFormService.formValidate(formRequest,org);
		byte[] transactionData = employeeLeaveBalanceService.exportLeaveData(empId, formRequest);

		Map<String, Object> format = formRequest.getFormFields();
		String fileFormat = (String) format.get("generateAs");
		if (fileFormat.equalsIgnoreCase("excel")) {
			fileFormat = "xlsx";
		}
		String filename = String.format("Leave_Transaction_Report_%s_%s.%s", empId,
				LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), fileFormat);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

		return new ResponseEntity<>(transactionData, headers, HttpStatus.OK);
	}
}
