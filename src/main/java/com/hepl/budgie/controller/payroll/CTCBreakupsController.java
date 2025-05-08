package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.annotation.FileChecker;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.EmpIdDTO;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.enums.ExcelValidationType;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.service.payroll.CTCBreakupsService;
import com.mongodb.bulk.BulkWriteResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Tag(name = "Payroll CTC Breakups", description = "Managing the CTC breakups of employee")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/payroll/ctc")
@RequiredArgsConstructor
public class CTCBreakupsController {
    private final CTCBreakupsService ctcBreakupsService;
    private final ExcelService excelService;
    private final Translator translator;

    @GetMapping()
    @Operation(summary = "Sample Excel File")
    public ResponseEntity<byte[]> fetch() throws IOException {

        byte[] excelContent = excelService.sampleExcel(ExcelType.CTC_SAMPLE_EXPORT.label);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "CTC Breakups.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Import the Excel File")
    public ResponseEntity<byte[]> importExcel(@RequestBody @Valid
                                              @FileChecker(
                                                      ext = "application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                      isMandatory = true,
                                                      message = "{error.fileNotSupported}",
                                                      allowedFormatArgs = ".xls, .xlsx"
                                              ) MultipartFile file) throws IOException , InterruptedException , ExecutionException {

        ValidationResult validationResult = excelService.excelImport(ExcelType.CTC_SAMPLE_EXPORT.label, ExcelValidationType.CTC_VALIDATION.label, file);
        BulkWriteResult result = ctcBreakupsService.excelImport(validationResult.getValidRows());

        byte[] excelContent = excelService.responseExcel(validationResult, result, "Employee_ID" );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Validations.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

    }
    @GetMapping("/{list}")
    @Operation(summary = "All Employee CTC Breakups List")
    public GenericResponse<List<Map<String, Object>>> list() {
        List<Map<String, Object>> ctcBreakupsDTOS = ctcBreakupsService.list();
        log.info("CTC Breakups: {}", ctcBreakupsDTOS);
        return GenericResponse.success(ctcBreakupsDTOS);
    }
    @PostMapping("/{ctcById}")
    @Operation(summary = "Single Employee CTC Breakups List")
    public GenericResponse<List<Map<String, Object>>> findByEmpId(@RequestBody @Valid EmpIdDTO dto) {
        List<Map<String, Object>> ctcBreakupsDTOS = ctcBreakupsService.singleEmpCTC(dto.getEmpId());
        log.info("Employee CTC Breakups: {}", ctcBreakupsDTOS);
        return GenericResponse.success(ctcBreakupsDTOS);
    }
    @GetMapping("/tableHeaders")
    @Operation(summary = "Employee CTC Breakups Datatable Headers")
    public GenericResponse<List<String>> headers() {
        List<String> headers = ctcBreakupsService.dataTableHeaders();
        log.info("CTC Breakups: {}", headers);
        return GenericResponse.success(headers);
    }

}
