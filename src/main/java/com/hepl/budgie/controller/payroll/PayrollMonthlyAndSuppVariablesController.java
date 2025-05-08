package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.hepl.budgie.dto.payroll.PayrollMonthlySuppImportDTO;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.entity.payroll.PayrollMonthlyAndSuppVariables;
import com.hepl.budgie.entity.payroll.payrollEnum.VariablesType;
import com.hepl.budgie.enums.ExcelValidationType;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.service.payroll.PayrollMonthlyAndSuppVariableService;
import com.hepl.budgie.utils.AppMessages;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Tag(name = "Payroll Monthly & Supplementary Variable", description = "Update the existing value of components in pay sheet")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payroll/monthly-import")
public class PayrollMonthlyAndSuppVariablesController {

    private final PayrollMonthlyAndSuppVariableService suppVariableService;
    private final ExcelService excelService;
    private final Translator translator;

    @GetMapping("/sample-excel")
    @Operation(summary = "Sample Excel File")
    public ResponseEntity<byte[]> sampleExcel(@RequestParam @ValueOfEnum(enumClass = VariablesType.class, message = "{validation.error.invalid}")
                                        String type) throws IOException {

        String excelType = type.equalsIgnoreCase(VariablesType.SUPP_VARIABLE.label) ? ExcelType.SUPPLEMENTARY_SAMPLE_EXPORT.label : ExcelType.MONTHLY_IMPORT_SAMPLE_EXPORT.getLabel();
        String fileName = type.equalsIgnoreCase(VariablesType.SUPP_VARIABLE.label) ? "Supplementary Variables.xlsx" : "Monthly Variables.xlsx";

        byte[] excelContent = excelService.sampleExcel(excelType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Import the Excel File")
    public ResponseEntity<byte[]> importExcel(@ModelAttribute PayrollMonthlySuppImportDTO dto) throws IOException, InterruptedException, ExecutionException {

        String excelType = dto.getType().equalsIgnoreCase(VariablesType.SUPP_VARIABLE.label) ? ExcelType.SUPPLEMENTARY_SAMPLE_EXPORT.label : ExcelType.MONTHLY_IMPORT_SAMPLE_EXPORT.getLabel();
        String fileName = dto.getType().equalsIgnoreCase(VariablesType.SUPP_VARIABLE.label) ? "Supplementary Variables Validations.xlsx" : "Monthly Variables Validations.xlsx";

        ValidationResult validationResult = excelService.excelImport(excelType, ExcelValidationType.SUPP_VALIDATION.label, dto.getFile());
        BulkWriteResult bulkWriteResult = suppVariableService.excelImport(validationResult.getValidRows(), dto.getType());
        byte[] excelContent = excelService.responseExcel(validationResult, bulkWriteResult, "Employee_ID");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

    }

    @GetMapping()
    @Operation(summary = "Monthly Variables list")
    public GenericResponse<List<Map<String, Object>>> list(
            @RequestParam(required = false) String month,
            @RequestParam @ValueOfEnum(enumClass = VariablesType.class, message = "{validation.error.invalid}")
            String type) {

        List<Map<String, Object>> data = suppVariableService.list(month, type);
        log.info("Monthly Supplementary variables Data: {}", data);

        return GenericResponse.success(data);
    }
    @PostMapping("/single-upload")
    @Operation(summary = "Upload the data for monthly variables for a single employee")
    public GenericResponse<String> singleUpload(@Valid @RequestBody PayrollMonthlyAndSuppVariables month) {
        boolean result = suppVariableService.singleUpload(month);
        log.info("Monthly Variables : {}", result);

        return result ? GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "Monthly Import" })) :
                GenericResponse.error("UPDATE_FAILED", "Failed to update monthly variables of employee.");
    }

    @GetMapping("/headers")
    @Operation(summary = "Data Table Headers")
    public GenericResponse<List<String>> getHeaders(@RequestParam @ValueOfEnum(enumClass = VariablesType.class, message = "{validation.error.invalid}")
                                                  String type) {
        List<String> result = suppVariableService.getHeaders(type);
        log.info("Monthly Variables : {}", result);

        return GenericResponse.success(result);
    }
}
