package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollTdsDTO;
import com.hepl.budgie.entity.payroll.PayrollTds;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.service.payroll.PayrollTdsService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll TDS", description = "Create and Manage the Payroll Tax Deducted at Source")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/payroll/tds")
public class PayrollTdsController {
    private final PayrollTdsService payrollTdsService;
    private final Translator translator;

    public PayrollTdsController(PayrollTdsService payrollTdsService, Translator translator) {
        this.payrollTdsService = payrollTdsService;
        this.translator = translator;
    }

    @GetMapping
    @Operation(summary = "List of LWF")
    public GenericResponse<List<PayrollTds>> list() {
        return GenericResponse.success(payrollTdsService.list());
    }

    @PostMapping
    @Operation(summary = "Add TDS Slab")
    public GenericResponse<String> add(@Valid @RequestBody PayrollTdsDTO request) {
        log.info("LWF Request - {}", request);
        payrollTdsService.upsert(request, DataOperations.SAVE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "TDS" }));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update TDS Slab")
    public GenericResponse<String> update(@Valid @RequestBody PayrollTdsDTO request, @PathVariable String id) {
        log.info("LWF Request - {}", request);
        request.setTdsSlabId(id);
        payrollTdsService.upsert(request, DataOperations.UPDATE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_UPDATE, new String[] { "TDS Slab" }));
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "Update TDS Slab status")
    public GenericResponse<String> updateStatus(@PathVariable String id) {
        String status = payrollTdsService.updateStatus(id);
        log.info("Updated status of Tds Slab is " + status);
        return GenericResponse
                .success(translator.toLocale(AppMessages.PAYROLL_STATUS, new String[] { "TDS Slab " + status }));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete TDS Slab")
    public GenericResponse<String> delete(@PathVariable String id) {
        payrollTdsService.delete(id);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "TDS Slab" }));
    }

}
