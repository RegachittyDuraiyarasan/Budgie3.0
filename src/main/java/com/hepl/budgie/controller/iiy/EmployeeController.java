package com.hepl.budgie.controller.iiy;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.hepl.budgie.dto.iiy.*;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.iiy.Action;
import com.hepl.budgie.enums.ExcelValidationType;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.service.iiy.EmployeeService;
import com.hepl.budgie.service.iiy.HRService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.bulk.BulkWriteResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Tag(name = "Create and Manage IIY Activity", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/iiy")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final HRService hrService;
    private final FileService fileService;
    private final Translator translator;
    private final ExcelService excelService;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Add Activity")
    public GenericResponse<String> addActivity(@RequestParam(value = "doc", required = false) MultipartFile doc,
            @ModelAttribute ActivityRequestDTO data)
            throws IOException {
        log.info("Add Activity - {}", data);
        employeeService.addActivityWithCertificate(data, doc);
        return GenericResponse.success(translator.toLocale(AppMessages.ADDED_ACTIVITY));
    }

    @PostMapping("/list")
    @Operation(summary = "Fetch Activity list")
    public GenericResponse<List<ActivityFetchDTO>> fetchActivityList(@RequestBody IIYEmployeeRequestDTO data) {
        log.info("Fetch Activity List - {}", data);
        List<ActivityFetchDTO> result = employeeService.fetchActivityList(data);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_ACTIVITY), result);

    }

    @PostMapping("/team/activity")
    @Operation(summary = "Fetch Team Activity list")
    public GenericResponse<List<ActivityFetchDTO>> fetchTeamActivityList(@RequestBody IIYEmployeeRequestDTO data) {
        log.info("Fetch Team Activity List - {}", data);
        List<ActivityFetchDTO> result = employeeService.fetchTeamActivityList(data);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_TEAM_ACTIVITY), result);
    }

    @PostMapping("/team/report")
    @Operation(summary = "Fetch Team Activity Report List")
    public GenericResponse<List<ActivityFetchDTO>> fetchTeamActivityReportList(
            @RequestBody IIYEmployeeRequestDTO data) {
        log.info("Fetch Team Activity Report List- {}", data);
        List<ActivityFetchDTO> result = employeeService.fetchTeamActivityReportList(data,Action.TEAM.label);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_TEAM_ACTIVITY_REPORT), result);

    }

    @PutMapping("/approve")
    @Operation(summary = "Approve Activity")
    public GenericResponse<String> approveTeamActivity(@RequestBody List<ActivityRequestDTO> data) {
        log.info("Approve Activity - {}", data);
        Map<String, List<String>> response = employeeService.approveTeamActivity(data);
        List<String> errorList = response.getOrDefault("errors", new ArrayList<>());
        if (errorList.isEmpty()) {
            return GenericResponse.success(translator.toLocale(AppMessages.APPROVE_ACTIVITY));
        } else {
            return GenericResponse.bulkErrors(response);
        }
    }

    @PutMapping("/reject")
    @Operation(summary = "Reject Activity")
    public GenericResponse<String> rejectTeamActivity(@RequestBody List<ActivityRequestDTO> data) {
        log.info("Reject Activity - {}", data);
        Map<String, List<String>> response = employeeService.rejectTeamActivity(data);

        List<String> errorList = response.getOrDefault("errors", new ArrayList<>());
        if (errorList.isEmpty()) {
            return GenericResponse.success(translator.toLocale(AppMessages.REJECT_ACTIVITY));
        } else {
            return GenericResponse.bulkErrors(response);
        }
    }

    @PostMapping("/overall/activity")
    @Operation(summary = "Fetch Overall Activity Report List")
    public GenericResponse<List<ActivityFetchDTO>> fetchOverAllActivityReportList(
            @RequestBody IIYEmployeeRequestDTO data) {
        log.info("Fetch Overall Activity List - {}", data);
        List<ActivityFetchDTO> result = employeeService.fetchTeamActivityReportList(data,Action.OVERALL.label);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_OVERALL_ACTIVITY_REPORT), result);

    }

    @PostMapping("/report")
    public GenericResponse<List<IIYReportFetchDTO>> fetchIiyReportList(@RequestBody IIYEmployeeRequestDTO data) {
        log.info("Fetch IIY Report List - {}", data);
        List<IIYReportFetchDTO> result = employeeService.fetchIiyReportList(data,Action.OVERALL.label);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_IIY_REPORT), result);

    }

    @PostMapping("/employee/course")
    @Operation(summary = "Fetch IIY Processing Course List")
    public GenericResponse<List<CourseFetchDTO>> fetchIiyProcessingCourseListByEmpId(
            @RequestBody IIYEmployeeRequestDTO data) {
        log.info("Fetch IIY Processing Course List - {}", data);
        List<CourseFetchDTO> result = employeeService.fetchIiyProcessingCourseListByEmpId(data);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_IIY_PROCESSING_COURSE), result);
    }

    @PostMapping("/employee/report")
    @Operation(summary = "Fetch IIY Employee Report List")
    public GenericResponse<List<IIYReportFetchDTO>> fetchIiyEmployeeReportList(
            @RequestBody IIYEmployeeRequestDTO data) {
        log.info("Fetch IIY Employee Dashboard - {}", data);
        List<IIYReportFetchDTO> result = employeeService.fetchIiyReportList(data,Action.EMPLOYEE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_IIY_EMPLOYEE_REPORT), result);

    }

    @GetMapping("/sample")
    public ResponseEntity<byte[]> downloadSampleFile() throws IOException {
        byte[] excelContent = excelService.sampleExcel(ExcelType.IIY_SAMPLE_EXPORT.label);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "IIY_Sample_Form.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

    }

    @PostMapping("/import")
    public ResponseEntity<byte[]> importActivityLists(@RequestParam("file") MultipartFile file)
            throws IOException, InterruptedException, ExecutionException {

        ValidationResult validationResult = excelService.excelImport(ExcelType.IIY_SAMPLE_EXPORT.label,
                ExcelValidationType.IIY_ACTIVITY_VALIDATION.label, file);
        BulkWriteResult bulkWriteResult = hrService.excelImport(validationResult.getValidRows());
        byte[] excelContent = excelService.responseExcel(validationResult, bulkWriteResult, "Employee_ID");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "IIYResponse");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }

  
    @GetMapping("/preview/{filename}")
    public ResponseEntity<byte[]> generateAndReturnFile(@PathVariable String filename) throws IOException {
        log.info("Get filename .. {}", filename);
        Resource file = fileService.loadAsResource(filename, FileType.IIY_CERTIFICATE);

        if (file == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FILE_NOT_FOUND);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file.getContentAsByteArray());
    }

}
