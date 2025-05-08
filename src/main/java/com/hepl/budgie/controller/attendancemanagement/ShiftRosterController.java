package com.hepl.budgie.controller.attendancemanagement;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.entity.attendancemanagement.ShiftRoster;
import com.hepl.budgie.enums.ExcelValidationType;
import com.hepl.budgie.service.attendancemanagement.ShiftRosterService;
import com.hepl.budgie.service.excel.ExcelService;
import com.mongodb.bulk.BulkWriteResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Shift Roster", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/shift-roster")
public class ShiftRosterController {

	private final ShiftRosterService shiftRosterService;
	private final ExcelService excelService;

	@GetMapping()
	@Operation(summary = "Get All Shift Roster")
	public GenericResponse<List<ShiftRoster>> fetchShiftRoster(@RequestParam(required = false) String monthYear, @RequestParam(required = false) String empId) {
		List<ShiftRoster>  shiftRoster = shiftRosterService.fetch(monthYear, empId);

		return GenericResponse.success(shiftRoster);
	}

	@GetMapping("/template")
	@Operation(summary = "Excel Template For Shift Roster")
	public ResponseEntity<byte[]> shiftRoster(@RequestParam String monthYear) {
		byte[] excelContent = shiftRosterService.shiftRosterTemplate(monthYear);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Shift_Roster_Template.xlsx")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(excelContent);
	}
	
	@GetMapping("/template-daytype")
	@Operation(summary = "Excel Template For Shift Roster DayType")
	public ResponseEntity<byte[]> shiftRosterDayType() {
		byte[] excelContent = shiftRosterService.shiftRosterDayType();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Day_Type_Template.xlsx")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(excelContent);
	}

	@GetMapping("/sample")
    public ResponseEntity<byte[]> downloadSampleFile() throws IOException {
        byte[] excelContent = excelService.sampleExcel(ExcelType.SHIFT_ROSTER_SAMPLE.label);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Shift Roster.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }

	@PostMapping(value = "/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> importData(@RequestParam MultipartFile file) throws IOException, InterruptedException, ExecutionException {

		String excelType = ExcelType.SHIFT_ROSTER_SAMPLE.getLabel();
        String fileName = "Shift Roster.xlsx";

        ValidationResult validationResult = excelService.excelImport(excelType, ExcelValidationType.ATTENDANCE_SHIFT_ROSTER.label, file);
        BulkWriteResult bulkWriteResult = shiftRosterService.excelBulkImport(validationResult.getValidRows());
        byte[] excelContent = excelService.responseExcel(validationResult, bulkWriteResult, "Employee_ID");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);    }
}
