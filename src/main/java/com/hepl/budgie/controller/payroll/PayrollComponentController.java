package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollComponentDTO;
import com.hepl.budgie.dto.payroll.PayrollPayTypeCompDTO;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.payroll.payrollEnum.ComponentType;
import com.hepl.budgie.entity.payroll.payrollEnum.PayType;
import com.hepl.budgie.entity.payroll.payrollEnum.VariablesType;
import com.hepl.budgie.service.payroll.PayrollComponentService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Payroll Component", description = "Create and Manage the Payroll Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll/component")
public class PayrollComponentController {

    private final PayrollComponentService payrollComponentService;
    private final Translator translator;

    @GetMapping()
    @Operation(summary = "List Payroll Component")
    public GenericResponse<List<PayrollComponent>> fetch() {
        log.info("Payroll Component Fetched Successfully");
        return GenericResponse.success(payrollComponentService.fetch());
    }

    @GetMapping("/list")
    @Operation(summary = "List of Payroll Component Based on Component Type")
    public GenericResponse<List<PayrollPayTypeCompDTO>> getComponents() {
        log.info("Payroll Component Fetched Successfully");
        return GenericResponse.success(payrollComponentService.list(List.of(ComponentType.EARNINGS.label), PayType.VARIABLE_PAY.label));
    }


    @PostMapping()
    @Operation(summary = "Add Payroll Component")
    public GenericResponse<String> add(@Valid @RequestBody PayrollComponentDTO request) {
        log.info("Payroll Component - {}", request);
        payrollComponentService.upsertComponent("save", request);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "Component" }));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit the Payroll Component")
    public GenericResponse<String> upsert(@Valid @RequestBody PayrollComponentDTO request, @PathVariable String id) {
        log.info("Payroll Component - {}", request);
        request.setComponentId(id);
        payrollComponentService.upsertComponent("update",request);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_UPDATE, new String[] { "Component" }));
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "Status Change Payroll Component")
    public GenericResponse<String> status(@PathVariable String id) {
        log.info("Status Change Payroll Component - " + id);
        String status = payrollComponentService.status(id);
        return GenericResponse
                .success(translator.toLocale(AppMessages.PAYROLL_STATUS, new String[] { "Component " + status }));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete the Payroll Component")
    public GenericResponse<String> delete(@PathVariable String id) {
        log.info("Payroll Component Delete - " + id);
        payrollComponentService.delete(id);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "Component" }));
    }

}
