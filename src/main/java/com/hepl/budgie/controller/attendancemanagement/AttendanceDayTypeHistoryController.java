package com.hepl.budgie.controller.attendancemanagement;

import java.util.*;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.attendancemanagement.AttendanceDayTypeHistoryDTO;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayTypeHistory;
import com.hepl.budgie.enums.ExcelValidationType;
import com.hepl.budgie.service.attendancemanagement.AttendanceDayTypeHistoryService;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.bulk.BulkWriteResult;

import java.io.IOException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Attendance day Type History")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/attendance-day-type-history")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AttendanceDayTypeHistoryController {

    private final AttendanceDayTypeHistoryService attendanceDayTypeHistoryService;
    private final Translator translator;
    private final ExcelService excelService;

    @PostMapping()
    public GenericResponse<AttendanceDayTypeHistory> saveAttendanceDayType(
            @RequestBody AttendanceDayTypeHistoryDTO dayTypeHistory) {

        log.info("save Attendance day type history");
        AttendanceDayTypeHistory dayType = attendanceDayTypeHistoryService.saveAttendanceDayType(dayTypeHistory);
        return GenericResponse.success(translator.toLocale(AppMessages.DAY_TYPE_HISTORY), dayType);
    }

    @GetMapping()
    public GenericResponse<List<AttendanceDayTypeHistory>> getAttendanceDayTypeHistory(@RequestParam(required = false) String empId,
            @RequestParam(required = false) String monthYear) {

        log.info("fetch all attendance day type history");
        List<AttendanceDayTypeHistory> dayType = attendanceDayTypeHistoryService.getAttendanceDayTypeHistory(empId, monthYear);
        return GenericResponse.success(dayType);

    }

    @GetMapping("/empId")
    public GenericResponse<List<AttendanceDayTypeHistory>> getByEmpId(@RequestParam String empId) {

        log.info("fetch attendance day type history by empId");
        List<AttendanceDayTypeHistory> dayType = attendanceDayTypeHistoryService.getByEmpId(empId);
        return GenericResponse.success(dayType);
    }

    @PostMapping(value = "/import", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> importData(@RequestParam MultipartFile file) throws IOException, InterruptedException, ExecutionException {

        String excelType = ExcelType.DAY_TYPE_HISTORY_SAMPLE.getLabel();
        String fileName = "Attendance Day Type History.xlsx";

        ValidationResult validationResult = excelService.excelImport(excelType, ExcelValidationType.ATTENDANCE_DAY_TYPE.label, file);
        BulkWriteResult bulkWriteResult = attendanceDayTypeHistoryService.excelImports(validationResult.getValidRows());
        byte[] excelContent = excelService.responseExcel(validationResult, bulkWriteResult, "Employee_ID");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }

    @GetMapping("/sample")
    public ResponseEntity<byte[]> downloadSampleFile() throws IOException {
        byte[] excelContent = excelService.sampleExcel(ExcelType.DAY_TYPE_HISTORY_SAMPLE.label);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Attendance Day Type History.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

    }
}
