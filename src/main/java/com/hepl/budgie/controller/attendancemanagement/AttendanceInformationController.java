package com.hepl.budgie.controller.attendancemanagement;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.attendancemanagement.AttendanceReportDTO;
import com.hepl.budgie.service.attendancemanagement.AttendanceInformationService;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Employee Attendance")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@RequestMapping("/employee-attendance")
@RestController
@Slf4j
public class AttendanceInformationController {

    private final AttendanceInformationService attendanceInformationService;
    private final Translator translator;
    private final ExcelService excelService;

    @PostMapping()
    public ResponseEntity<String> uploadAttendance() {
        try {
            attendanceInformationService.attendanceFile();
            return ResponseEntity.ok("File uploaded and processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }

    @PostMapping("/citpl")
    public ResponseEntity<String> processAndSaveAttendanceCitpl() {
        try {
            attendanceInformationService.processAndSaveAttendanceCitpl();
            return ResponseEntity.ok("CITPL attendance processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CITPL attendance: " + e.getMessage());
        }
    }

    @PostMapping("/process-by-date")
    public ResponseEntity<?> processAttendance(@RequestParam LocalDate date) throws Exception {

        Map<String, List<String>> response;
        try {
            response = attendanceInformationService.processAttendance(date);
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FILE_NOT_FOUND_DATE);            
        }
        byte[] excelFile;
        String generatedFileName = "Attendance_Bulk_Upload_Report.xlsx";
        if (response.isEmpty()) {
            excelFile = excelService.generateSuccessFile(translator.toLocale(AppMessages.ATTENDANCE_BULK_UPLOAD));
        } else {
            excelFile = excelService.generateErrorFile(response);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", generatedFileName);

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }

    @GetMapping()
    public GenericResponse<List<Map<String, Object>>> getEmployeeAttendance(@RequestParam String empId,
            @RequestParam String currentMonth) {

        List<Map<String, Object>> employeeAttendance = attendanceInformationService.getEmployeeAttendance(empId,
                currentMonth);
        return GenericResponse.success(employeeAttendance);
    }

    @PostMapping(value = "/bulk-upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> attendanceBulkUpload(@RequestParam MultipartFile file) throws Exception {

        log.info("Attendance bulk upload...");
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty. Please upload a valid CSV file.");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only CSV files are allowed.");
        }
        Map<String, List<String>> response = attendanceInformationService.attendanceBulkUpload(file);
        byte[] excelFile;
        String generatedFileName = "Attendance_Bulk_Upload_Report.xlsx";
        if (response == null || response.isEmpty()) {
            excelFile = excelService
                    .generateSuccessFile(translator.toLocale(AppMessages.ATTENDANCE_BULK_UPLOAD));
        } else {
            excelFile = excelService.generateErrorFile(response);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", generatedFileName);

        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }

    @GetMapping("/report")
    public GenericResponse<List<AttendanceReportDTO>> getEmployeeAttendanceReport(@RequestParam String empId,
            @RequestParam LocalDate fromDate, @RequestParam LocalDate toDate) {

        List<AttendanceReportDTO> employeeAttendance = attendanceInformationService.getEmployeeAttendanceReport(empId,
                fromDate, toDate);
        return GenericResponse.success(employeeAttendance);
    }

    @PostMapping("/login")
    public GenericResponse<String> employeeSignIn(@RequestParam String location, @RequestParam boolean isSign,
            @RequestParam(required = false) String remarks) {

        attendanceInformationService.saveAttendance(location, isSign, remarks);
        String message = isSign
                ? translator.toLocale(AppMessages.ATTENDANCE_SIGNED_IN)
                : translator.toLocale(AppMessages.ATTENDANCE_SIGNED_OUT);

        return GenericResponse.success(message);
    }

    @GetMapping("/today-attendance")
    public GenericResponse<Map<String, Object>> getTodayAttendance() {
        Map<String, Object> employeeAttendance = attendanceInformationService.getTodayAttendance();
        return GenericResponse.success(employeeAttendance);
    }

}
