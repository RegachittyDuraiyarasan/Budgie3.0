package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollVpfDTO;
import com.hepl.budgie.entity.payroll.PayrollVpf;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.service.payroll.PayrollVpfService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll/vpf")
@Tag(name = "Payroll VPF", description = "Create and Manage the Payroll Volunteer Provident Fund")
@Slf4j
public class PayrollVpfController {

    private final PayrollVpfService payrollVpfService;
    private final Translator translator;

    public PayrollVpfController(PayrollVpfService payrollVpfService, Translator translator) {
        this.payrollVpfService = payrollVpfService;
        this.translator = translator;

    }

    @GetMapping()
    @Operation(summary = "Get data vpf")
    public GenericResponse<List<PayrollVpf>> getAllData() {
        log.info("Get VPF list");
        return GenericResponse.success(payrollVpfService.getAllData());
    }

    @PostMapping()
    @Operation(summary = "Store the data for VPF ")
    public GenericResponse<String> add(@Valid @RequestBody PayrollVpfDTO payrollVpfDTO) {
        log.info("Add VPF - {}", payrollVpfDTO);
        payrollVpfService.upsert(payrollVpfDTO, DataOperations.SAVE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "VPF" }));

    }

    @DeleteMapping("/{id}")
    public GenericResponse<String> delete(@PathVariable String id) {
        payrollVpfService.updateStatus(id, DataOperations.DELETE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "VPF" }));
    }



    @PutMapping("/{id}")
    public GenericResponse<String> update(@Valid  @RequestBody PayrollVpfDTO payrollVpfDTO,@PathVariable String id) {
        payrollVpfDTO.setRcpfId(id);
        payrollVpfService.upsert(payrollVpfDTO, DataOperations.UPDATE.label);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_UPDATE, new String[] { "VPF" }));
    }

}
