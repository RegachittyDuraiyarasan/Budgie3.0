package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollESICDto;
import com.hepl.budgie.entity.payroll.PayrollESIC;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.service.payroll.PayrollESICService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll ESIC", description = "Create and Manage the Employee State Insurance Corporation")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/payroll/esic")
public class PayrollESICController {
    private final Translator translator;
    private final PayrollESICService payrollESICService;

    public PayrollESICController(Translator translator, PayrollESICService payrollESICService) {
        this.translator = translator;
        this.payrollESICService = payrollESICService;
    }

    @GetMapping
    @Operation(summary = "List of ESIC")
    public GenericResponse<List<PayrollESIC>> list() {
        return GenericResponse.success(payrollESICService.list());
    }

    @PostMapping
    @Operation(summary = "Add ESI")
    public GenericResponse<String> add(@Valid @RequestBody PayrollESICDto request) {
        log.info("ESI Insert Request - {}", request);
        payrollESICService.upsert(request, DataOperations.SAVE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "ESI" }));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ESI")
    public GenericResponse<String> update(@Valid @RequestBody PayrollESICDto request, @PathVariable String id) {
        log.info("ESI Update Request - {}", request);
        request.setEsicId(id);
        payrollESICService.upsert(request, DataOperations.UPDATE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_UPDATE, new String[] { "ESI" }));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ESI")
    public GenericResponse<String> delete(@PathVariable String id) {
        payrollESICService.updateStatus(id, DataOperations.DELETE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "ESI " }));
    }

    @PutMapping("updateStatus/{id}")
    @Operation(summary = "Update ESI status")
    public GenericResponse<String> updateStatus(@PathVariable String id) {
        String status = payrollESICService.updateStatus(id, DataOperations.UPDATE.label);
        log.info("Updated status of Tds Slab is " + status);
        return GenericResponse
                .success(translator.toLocale(AppMessages.PAYROLL_STATUS, new String[] { "ESI " + status }));
    }
}
