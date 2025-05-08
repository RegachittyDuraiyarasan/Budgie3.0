package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PTListDTO;
import com.hepl.budgie.dto.payroll.PayrollPtDTO;
import com.hepl.budgie.service.payroll.PayrollPtService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll PT", description = "Create and Manage the PT Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll/pt")
public class PayrollPtController {
    private final Translator translator;
    private final PayrollPtService payrollPtService;

    @GetMapping()
    public GenericResponse<List<PTListDTO>> list() {
        log.info("Payroll PT Controller");
        return GenericResponse.success(payrollPtService.list());
    }

    @PostMapping()
    public GenericResponse<String> add(@Valid @RequestBody PayrollPtDTO request) {
        log.info("Payroll PT Info : {}", request);
        payrollPtService.add(request);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] {"PT"}));
    }

    @PutMapping("/{id}")
    public GenericResponse<String> update(@PathVariable String id, @RequestBody PayrollPtDTO request) {
        log.info("Payroll PT Update ID Info : {}", id);
        return GenericResponse.success(translator.toLocale("Updated PT List"));
    }
}
