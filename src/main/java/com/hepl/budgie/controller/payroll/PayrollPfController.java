package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollPfDTO;
import com.hepl.budgie.dto.payroll.PayrollPfListDTO;
import com.hepl.budgie.repository.payroll.PayrollPfRepository;
import com.hepl.budgie.service.payroll.PayrollPfService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll PF", description = "Create and Manage the Payroll Provident Fund")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/payroll/pf")
public class PayrollPfController {
    private final PayrollPfService payrollPfService;
    private final PayrollPfRepository  payrollPfReo;
    private final Translator translator;

    public PayrollPfController(PayrollPfService payrollPfService, PayrollPfRepository payrollPfReo, Translator translator) {
        this.payrollPfService = payrollPfService;
        this.payrollPfReo = payrollPfReo;
        this.translator = translator;
    }

    @GetMapping
    @Operation(summary = "List of PF Employee Details")
    public GenericResponse<List<PayrollPfListDTO>> listIndex() {
        return GenericResponse.success(payrollPfService.listIndex());
    }
    @PostMapping
    @Operation(summary = "Add PF")
    public GenericResponse<String> add(@Valid @RequestBody PayrollPfDTO request) {
        String id ="";
        payrollPfService.addorupdate(request,"save",id);
         return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD,new String[]{"PF"}));

    }

    @GetMapping("/{orgid}")
    @Operation(summary = "List PF")
    public  GenericResponse   listgetpf(@PathVariable String orgid) {

         return GenericResponse.success(payrollPfService.listpf(orgid));
//        return null;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update PF")
    public GenericResponse<String> update(@Valid @RequestBody PayrollPfDTO request, @PathVariable String id) {
         payrollPfService.addorupdate(request,"update",id);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_UPDATE,new String[]{"PF"}));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete PF")
    public GenericResponse<String> delete(@PathVariable String id) {
        payrollPfService.delete(id);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "PF" }));
    }


//    @GetMapping("/{orgid}")
//    @Operation(summary = "PF Employee List ")
//    public  GenericResponse   listgetemployeepf(@PathVariable String orgid) {
//
//        return GenericResponse.success(usder.listgetemployeepf(orgid));
////        return null;
//    }


}
