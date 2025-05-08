package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollFBPEmpIndexDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPEmpListDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPSaveDTO;
import com.hepl.budgie.service.payroll.PayrollFBPEmpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll FBP Employee Controller", description = "Create and Manage the Payroll FBP Employee Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll/fbp/emp")
public class PayrollFBPEmpController {

    private final PayrollFBPEmpService payrollFBPEmpService;

    @GetMapping("")
    @Operation(summary = "FBP Employee Index")
    public GenericResponse<PayrollFBPEmpIndexDTO> index() {
        return GenericResponse.success(payrollFBPEmpService.index());
    }

    @GetMapping("/list")
    @Operation(summary = "FBP Employee List")
    public GenericResponse<List<PayrollFBPEmpListDTO>> fbpList() {
        return GenericResponse.success(payrollFBPEmpService.fbpList());
    }

    @PostMapping("/add")
    @Operation(summary = "FBP Employee add")
    public GenericResponse<String> fbpAdd(@RequestBody PayrollFBPSaveDTO request) {
        payrollFBPEmpService.fbpAdd(request);
        return GenericResponse.success("FBP Data Added Successfully");
    }

}
