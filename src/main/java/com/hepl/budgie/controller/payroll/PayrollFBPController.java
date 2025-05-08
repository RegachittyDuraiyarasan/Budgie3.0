package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.payroll.FbpCreatePlanDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPCreatePlan;
import com.hepl.budgie.entity.payroll.PayrollFBPComponentMaster;
import com.hepl.budgie.entity.payroll.PayrollFBPMaster;
import com.hepl.budgie.entity.payroll.PayrollFBPRange;
import com.hepl.budgie.service.payroll.PayrollFBPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Payroll FBP Component", description = "Create and Manage the Payroll FBP Component Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll/fbp")
public class PayrollFBPController {

    private final PayrollFBPService payrollFBPService;

    @GetMapping("/component/list")
    @Operation(summary = "List the Payroll FBP Range Components")
    public GenericResponse<List<PayrollFBPComponentMaster>> list() {
        return GenericResponse.success(payrollFBPService.list());
    }
    @PostMapping("/component/add")
    @Operation(summary = "Add the Payroll FBP Component")
    public GenericResponse<String> add(@RequestBody PayrollFBPComponentMaster request) {
        log.info("Payroll FBP Component - {}", request);
        payrollFBPService.add("save",request);
        return GenericResponse.success("success");
    }
    @PutMapping("/component/{id}")
    @Operation(summary = "Edit the Payroll FBP Component")
    public GenericResponse<String> update(@PathVariable String id, @RequestBody PayrollFBPComponentMaster request) {
        log.info("Payroll FBP Component - {}", request);
        request.setComponentId(id);
        payrollFBPService.add("update",request);
        return GenericResponse.success("success");
    }

    @PutMapping("/component/status/{id}")
    @Operation(summary = "Status the Payroll FBP Component")
    public GenericResponse<String> status(@PathVariable String id) {
        log.info("Payroll FBP Component Status Id - {}", id);
        boolean status = payrollFBPService.status(id);
        return GenericResponse.success("success");
    }

    @PutMapping("/component/delete/{id}")
    @Operation(summary = "Delete the Payroll FBP Component")
    public GenericResponse<String> delete(@PathVariable String id) {
        log.info("Payroll FBP Component Status Id - {}", id);
        boolean status = payrollFBPService.deleteStatus(id);
        return GenericResponse.success("success");
    }

    @GetMapping("/range/list")
    @Operation(summary = "List the Payroll FBP Range Components")
    public GenericResponse<List<PayrollFBPRange>> listRange() {
        return GenericResponse.success(payrollFBPService.listRange());
    }
    @PostMapping("/range/add")
    @Operation(summary = "Add the Payroll FBP Range Components")
    public GenericResponse<String> addRange(@RequestBody PayrollFBPRange request) {
        payrollFBPService.range("save",request);
        return GenericResponse.success("Success");
    }

    @PutMapping("/range/{id}")
    @Operation(summary = "Add the Payroll FBP Range Components")
    public GenericResponse<String> updateRange(@PathVariable String id, @RequestBody PayrollFBPRange request) {
        request.setRangeId(id);
        payrollFBPService.range("update",request);
        return GenericResponse.success("Success");
    }

    @PutMapping("/range/status/{id}")
    @Operation(summary = "Status the Payroll FBP Range")
    public GenericResponse<String> statusRange(@PathVariable String id) {
        log.info("Payroll FBP Component Status Id - {}", id);
        boolean status = payrollFBPService.statusRange(id);
        return GenericResponse.success("success");
    }

    @PutMapping("/range/delete/{id}")
    @Operation(summary = "Delete the Payroll FBP Range")
    public GenericResponse<String> deleteRange(@PathVariable String id) {
        log.info("Payroll FBP Component Status Id - {}", id);
        boolean status = payrollFBPService.deleteStatusRange(id);
        return GenericResponse.success("success");
    }

    @GetMapping("/master/list/{id}")
    @Operation(summary = "List the Payroll FBP Master Components")
    public GenericResponse<List<PayrollFBPMaster>> listFBPMasterComponent(@PathVariable String id) {
        return GenericResponse.success(payrollFBPService.listMaster(id));
    }
    @PostMapping("/master/add")
    @Operation(summary = "Add the Payroll FBP Master Components")
    public GenericResponse<String> addFBPMasterComponent(@RequestBody List<PayrollFBPMaster> request) {
        payrollFBPService.addFBPMaster("save",request);
        return GenericResponse.success("Success");
    }

    @PutMapping("/master/update/{id}")
    @Operation(summary = "Add the Payroll FBP Master Components")
    public GenericResponse<String> addFBPMasterComponent(@PathVariable String id, @RequestBody PayrollFBPMaster request) {
        request.setRangeId(id);
        payrollFBPService.updateFBPMaster(request);
        return GenericResponse.success("Updated Successfully");
    }

    @PutMapping("/master/status/{id}")
    @Operation(summary = "Status the Payroll FBP Master")
    public GenericResponse<String> statusMaster(@PathVariable String id) {
        log.info("Payroll FBP Component Status Id - {}", id);
        boolean status = payrollFBPService.statusMaster(id);
        return GenericResponse.success("success");
    }

    @PutMapping("/master/delete/{id}")
    @Operation(summary = "Delete the Payroll FBP Master")
    public GenericResponse<String> deleteMaster(@PathVariable String id) {
        log.info("Payroll FBP Component Status Id - {}", id);
        boolean status = payrollFBPService.deleteStatusMaster(id);
        return GenericResponse.success("success");
    }

    /* FBP HR Action Functions */

    @GetMapping("/hr/employee/list")
    @Operation(summary = "Employees List")
    public GenericResponse<List<EmployeeActiveDTO>> employeeList() {
        return GenericResponse.success(payrollFBPService.employeeList());
    }
    
    @GetMapping("/hr/active-employee/list")
    @Operation(summary = "Active Employee List")
    public GenericResponse<List<EmployeeActiveDTO>> activeEmployeeList(){
        System.out.println("5165465");
        return GenericResponse.success(payrollFBPService.activeEmployeeList());
    }

    @GetMapping("/hr/consider-employee/list")
    @Operation(summary="Consider Status Employee List")
    public GenericResponse<List<EmployeeActiveDTO>> considerEmployeeList(){
        return GenericResponse.success(payrollFBPService.considerEmployeeList());
    }

    @PostMapping("/hr/create-plan")
    @Operation(summary = "Create FBP Plan for Employees")
    public GenericResponse<String> createPlan(@Valid @RequestBody List<PayrollFBPCreatePlan> request) {
        payrollFBPService.createPlan(request);
        return GenericResponse.success("Plan Created Successfully");
    }

    @GetMapping("hr/consider-list")
    @Operation(summary = "List of FBP consider for Employees")
    public GenericResponse<List<FbpCreatePlanDTO>> listPlan() {
        return GenericResponse.success(payrollFBPService.listPlan());
    }

    @PutMapping("hr/consider-plan/{id}")
    @Operation(summary = "Consider for payroll")
    public GenericResponse<String> considerPlan(@RequestBody List<FbpCreatePlanDTO> empIds) {
        payrollFBPService.considerPlan(empIds);
        return GenericResponse.success("Consider Successfully");
    }

}
