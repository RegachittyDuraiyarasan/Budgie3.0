package com.hepl.budgie.controller.leavemanagement;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.EmployeeLeaveBalanceReportDTO;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.enums.ExcelValidationType;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.service.leavemanagement.LeaveBalanceImportService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.bulk.BulkWriteResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Leave Balance Import", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/leave/balance")
public class LeaveBalanceImportCtrl {

	private final LeaveBalanceImportService leaveBalanceImportService;
	private final Translator translator;
	private final ExcelService excelService;

	@GetMapping("/sample-download")
	@Operation(summary = "Sample Excel Template")
	public ResponseEntity<byte[]> downloadSampleFile() throws IOException {
		byte[] excelContent = excelService.sampleExcel(ExcelType.LEAVE_BALANCE.label);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", "Leave Balance.xlsx");

		return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

	}

	@PostMapping(value = "/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@Operation(summary = "Leave Import by Excel")
	public ResponseEntity<?> importData(@RequestParam MultipartFile file)
			throws IOException, InterruptedException, ExecutionException {

		final String excelType = ExcelType.LEAVE_BALANCE.label;
		final String fileName = "Leave_Balance_Import_Report.xlsx";

		if (!file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			log.error("Invalid file type uploaded. Only .xlsx files are allowed.");
			return ResponseEntity.badRequest().body(GenericResponse
					.error(translator.toLocale(AppMessages.FILE_TYPE_NOT_SUPPORTED), "Accept only Xlsx file"));
		}
//		Map<String, List<String>> response = leaveBalanceImportService.importLeaveBalance(file);
		ValidationResult validationResult = excelService.excelImport(excelType,
				ExcelValidationType.LEAVE_BALANCE_IMPORT.label, file);
		BulkWriteResult bulkWriteResult = leaveBalanceImportService.excelImport(validationResult.getValidRows());
		byte[] excelContent = excelService.responseExcel(validationResult, bulkWriteResult, "Employee_ID");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", fileName);

		return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

	}

	@PostMapping("/export")
	@Operation(summary = "Employee Leave Balance Report")
	public ResponseEntity<byte[]> employeeLeaveBalance(@RequestBody EmployeeLeaveBalanceReportDTO reportDTO) {
		log.info("Export Leave Balance " + reportDTO);
		// masterFormService.formValidate(formRequest, org);
		byte[] transactionData = leaveBalanceImportService.exportLeaveBalance(reportDTO);
		String filename = String.format("Leave_Balance_Report_%s.elsx",
				LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

		return new ResponseEntity<>(transactionData, headers, HttpStatus.OK);
	}
}
