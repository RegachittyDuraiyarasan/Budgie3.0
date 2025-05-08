package com.hepl.budgie.controller.attendancemanagement;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.attendancemanagement.AttendanceMusterDTO;
import com.hepl.budgie.dto.attendancemanagement.AttendanceOverride;
import com.hepl.budgie.dto.attendancemanagement.BulkOverrideDTO;
import com.hepl.budgie.dto.attendancemanagement.LopDTO;
import com.hepl.budgie.dto.attendancemanagement.MusterHistoryDeleteDto;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.entity.attendancemanagement.AttendanceMuster;
import com.hepl.budgie.service.attendancemanagement.AttendanceMusterService;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Attendance Muster")
@RequestMapping("attendance-muster")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AttendanceMusterController {

    private final AttendanceMusterService attendanceMusterService;
    private final Translator translator;
    private final ExcelService excelService;

    @GetMapping()
    public GenericResponse<List<AttendanceMusterDTO>> getAttendanceMuster(@RequestParam(required = false) String empId,
            @RequestParam(required = false) String reviewer,
            @RequestParam(required = false) String repManager, @RequestParam(required = false) String payrollStatus,
            @RequestParam(required = false) String monthYear) {

        log.info("save attendance muster");
        List<AttendanceMusterDTO> attendanceMuster = attendanceMusterService.getAttendanceMuster(empId, reviewer,
                repManager, payrollStatus, monthYear);
        return GenericResponse.success(attendanceMuster);
    }

    @PostMapping("/lop")
    public GenericResponse<AttendanceMuster> addLopForEmployee(@RequestBody LopDTO lop) {

        log.info("add lop for employee");
        AttendanceMuster attendanceMuster = attendanceMusterService.addLopForEmployee(lop);
        return GenericResponse.success(attendanceMuster);
    }

    @PostMapping()
    public GenericResponse<String> saveAttendanceMuster(@RequestParam String monthYear, @RequestParam boolean isAll,
            @RequestParam(required = false) List<String> empId) {

        log.info("save attendance muster for employee");
        attendanceMusterService.saveAttendanceMusterForEmployee(monthYear, isAll, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.ATTENDANCE_MUSTER_SAVED));
    }

    @GetMapping("/employee")
    public GenericResponse<List<AttendanceMusterDTO>> getEmployeeAttendanceMuster(
            @RequestParam(required = false) String empId, @RequestParam(required = false) String monthYear) {

        log.info("employee attendance muster");
        List<AttendanceMusterDTO> attendanceMuster = attendanceMusterService.getEmployeeAttendanceMuster(empId,
                monthYear);
        return GenericResponse.success(attendanceMuster);
    }

    @GetMapping("/employee-details")
    public GenericResponse<AttendanceMusterDTO> employeeMuster(@RequestParam String empId,
            @RequestParam String monthYear) {

        log.info("employee attendance muster");
        AttendanceMusterDTO attendanceMuster = attendanceMusterService.employeeMuster(empId, monthYear);
        return GenericResponse.success(attendanceMuster);
    }

    @GetMapping("/employee-list")
    public GenericResponse<List<Map<String, String>>> fetchEmployeeList() {

        log.info("fetch employee list");
        List<Map<String, String>> employeeList = attendanceMusterService.fetchEmployeeList();
        return GenericResponse.success(employeeList);
    }

    @GetMapping("/override")
    public GenericResponse<List<BulkOverrideDTO>> getOverrideEmployeeDetails(
            @RequestParam(required = false) String empId,
            @RequestParam(required = false) String monthYear) {

        log.info("fetch employee details for bulk override");
        List<BulkOverrideDTO> details = attendanceMusterService.getOverrideEmployeeDetails(empId, monthYear);
        return GenericResponse.success(details);
    }

    @PutMapping("/emp-override")
    public GenericResponse<String> updateOverride(@RequestBody AttendanceOverride data) {

        attendanceMusterService.updateOverride(data);
        return GenericResponse.success(translator.toLocale(AppMessages.ATTENDANCE_OVERRIDE));
    }

    @GetMapping("/sample")
    public ResponseEntity<byte[]> downloadSampleFile() throws IOException {
        byte[] excelContent = excelService.sampleExcel(ExcelType.ATTENDANCE_MUSTER_SAMPLE.label);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Attendance Muster.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

    }

    @PostMapping(value = "/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> bulkImport(@RequestParam MultipartFile file) throws IOException {

        if (!file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            log.error("Invalid file type uploaded. Only .xlsx files are allowed.");
            return ResponseEntity.badRequest().body(
                    GenericResponse.error(translator.toLocale(AppMessages.FILE_TYPE_NOT_SUPPORTED),
                            "Accept only Xlsx file"));
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(GenericResponse.error(HttpStatus.BAD_REQUEST.toString(),
                    translator.toLocale(AppMessages.FILE_IS_EMPTY)));
        }

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                log.error("Uploaded Excel file is empty.");
                return ResponseEntity.badRequest().body(
                        GenericResponse.error(HttpStatus.BAD_REQUEST.toString(), "Uploaded Excel file has no data."));
            }

            int totalRows = sheet.getPhysicalNumberOfRows(); // Get total rows
            int headerRowIndex = sheet.getFirstRowNum(); // Usually 0
            Row headerRow = sheet.getRow(headerRowIndex);

            if (headerRow == null) {
                log.error("Excel file does not have a valid header.");
                return ResponseEntity.badRequest().body(
                        GenericResponse.error(HttpStatus.BAD_REQUEST.toString(),
                                "Excel file must have a valid header row."));
            }

            boolean hasData = false;
            for (int i = headerRowIndex + 1; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    for (Cell cell : row) {
                        if (cell.getCellType() != CellType.BLANK) {
                            hasData = true;
                            break;
                        }
                    }
                }
                if (hasData)
                    break;
            }

            if (!hasData) {
                log.error("Excel file contains only headers, no actual data.");
                return ResponseEntity.badRequest().body(
                        GenericResponse.error(HttpStatus.BAD_REQUEST.toString(),
                                "Uploaded Excel file must contain data beyond the header row."));
            }
        }
        Map<String, List<String>> response = attendanceMusterService.bulkImport(file);
        byte[] excelFile;
        String generatedFileName = "Attendance_Day_Type_Report.xlsx";
        if (response == null || response.isEmpty()) {
            excelFile = excelService.generateSuccessFile(translator.toLocale(AppMessages.ATTENDANCE_DAY_TYPE_HISTORY));
        } else {
            excelFile = excelService.generateErrorFile(response);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", generatedFileName);
        return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
    }

    @GetMapping("/override-history")
    public GenericResponse<List<Map<String,Object>>> getOverrideHistory(
            @RequestParam(required = false) String empId,
            @RequestParam(required = false) String monthYear) {

        log.info("fetch employee details for bulk override");
        List<Map<String,Object>> details = attendanceMusterService.getOverrideHistory(empId, monthYear);
        return GenericResponse.success(details);
    }

    @DeleteMapping()
    public GenericResponse<String> deleteAttendanceMuster(@RequestBody List<MusterHistoryDeleteDto> deleteHistory) { 
        return attendanceMusterService.deleteAttendanceMuster(deleteHistory);
    }
}
