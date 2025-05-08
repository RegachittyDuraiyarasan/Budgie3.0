package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollLwfDTO;
import com.hepl.budgie.entity.payroll.PayrollLwf;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.service.payroll.PayrollLwfService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll LWF", description = "Create and Manage the Payroll Labour Welfare Fund")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/payroll/lwf")
public class PayrollLwfController {
    private final PayrollLwfService payrollLwfService;
    private final Translator translator;

    public PayrollLwfController(PayrollLwfService payrollLwfService, Translator translator) {
        this.payrollLwfService = payrollLwfService;
        this.translator = translator;
    }

    @GetMapping
    @Operation(summary = "List of LWF")
    public GenericResponse<List<PayrollLwf>> list() {
        return GenericResponse.success(payrollLwfService.list());
    }

    @PostMapping
    @Operation(summary = "Add LWF")
    public GenericResponse<String> add(@Valid @RequestBody PayrollLwfDTO request) {
        log.info("LWF Request - {}", request);
        payrollLwfService.upsert(request, DataOperations.SAVE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "LWF" }));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update LWF")
    public GenericResponse<String> update(@Valid @RequestBody PayrollLwfDTO request, @PathVariable String id) {
        log.info("LWF Request - {}", request);
        request.setLwfId(id);
        payrollLwfService.upsert(request, DataOperations.UPDATE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_UPDATE, new String[] { "LWF" }));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete LWF")
    public GenericResponse<String> delete(@PathVariable String id) {
        payrollLwfService.delete(id);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "LWF" }));
    }
}
