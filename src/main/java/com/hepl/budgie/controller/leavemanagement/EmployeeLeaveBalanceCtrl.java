package com.hepl.budgie.controller.leavemanagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LeaveBalanceDTO;
import com.hepl.budgie.entity.leavemanagement.LeaveMaster;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;
import com.hepl.budgie.service.leavemanagement.EmployeeLeaveBalanceService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Employee Leave Balance", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/employee/leave")
public class EmployeeLeaveBalanceCtrl {

	private final EmployeeLeaveBalanceService employeeLeaveBalanceService;
	private final Translator translator;
	private final MasterFormService masterFormService;

	
	@GetMapping("/{empId}")
	@Operation(summary = "Fetch Employee Details")
	public GenericResponse<Object> getEmployeeDetails(@PathVariable String empId){
		Object response = employeeLeaveBalanceService.getEmployeeDetails(empId);
		
		return GenericResponse.success(response);
	}
	
	@GetMapping("/{empId}/{year}")
	@Operation(summary = "Get Employee Leave Balance Summary")
	public GenericResponse<List<LeaveBalanceDTO>> fetchLeaveTypeAndLeaveBalance(@PathVariable String empId,
			@PathVariable String year) {
		log.info("Fetching leave types for Employee ID: ", empId);
		List<LeaveBalanceDTO> leaveData = employeeLeaveBalanceService.getEmployeeLeaveBalance(empId, year);

		return GenericResponse.success(leaveData);
	}

	@GetMapping("")
	@Operation(summary = "Get Employee Leave Balance ,Transactions and Chart")
	public GenericResponse<Object> fetch(@RequestParam String empId, @RequestParam String year,
			@RequestParam String leaveType, @RequestParam(required = false) String transactionType) {
		log.info("Featching leave data for employee leave type :" + empId, leaveType, year);
		Object leaveData = employeeLeaveBalanceService.getEmployeeLeaveMaster(empId, year, leaveType, transactionType);

		return GenericResponse.success(leaveData);
	}

	@GetMapping("/filter")
	@Operation(summary = "Leave Filter by Leave Type and Transaction Type")
	public GenericResponse<List<LeaveTransactions>> fetchFilterData(@RequestParam String empId,
			@RequestParam(required = false) String leaveType, @RequestParam(required = false) String transactionType) {
		log.info("Filter the data by employee leave type and transaction type");
		List<LeaveTransactions> filterData = employeeLeaveBalanceService.filterByLeaveTypeAndTransactionType(empId,
				leaveType, transactionType);

		return GenericResponse.success(filterData);
	}

	@PostMapping("/{empId}")
	@Operation(summary = "Post Leave Transaction")
	public GenericResponse<String> postLeaveTransaction(@PathVariable String empId,
			@RequestBody FormRequest formRequest) {
		log.info("Request received for post leave transaction " + formRequest.getFormName());
		// masterFormService.formValidate(formRequest,org);
		employeeLeaveBalanceService.postLeaveTransaction(empId, formRequest);
		return GenericResponse.success(translator.toLocale(AppMessages.POST_LEAVE_TRANSACTION));
	}

	@PostMapping("/export/{empId}")
	@Operation(summary = "Leave Transaction Report")
	public ResponseEntity<byte[]> exportLeaveData(@PathVariable String empId, @RequestBody FormRequest formRequest) {
		log.info("Export Leave Transaction Data for Employee .. " + empId);
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
