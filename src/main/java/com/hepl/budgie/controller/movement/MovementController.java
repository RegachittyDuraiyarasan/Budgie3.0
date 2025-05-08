package com.hepl.budgie.controller.movement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.movement.*;
import com.hepl.budgie.service.movement.MovementService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/movement")
public class MovementController {
    private final MovementService movementService;
    private final Translator translator;

    public MovementController(MovementService movementService, Translator translator) {
        this.movementService = movementService;
        this.translator = translator;
    }

    @GetMapping("/empCode-underRM")
    @Operation(summary = "Fetch Employee under ReportingManager")
    public GenericResponse<List<EmpCodeValueDTO>> fetchEmployeeUnderRM(@RequestParam String empId) {
        List<EmpCodeValueDTO> employees = movementService.getEmployeeCodeUnderRM(empId);
        return GenericResponse.success(employees);
    }

    @PostMapping()
    @Operation(summary = "Initiate Movement By Reporting manager")
    public GenericResponse<String> initiateMovementByRM(@RequestBody MovementInitiateDTO request) {
        movementService.initiateMovementByRM(request);
        return GenericResponse.success(translator.toLocale(AppMessages.MOVEMENT_INITIATED)
        );
    }

    @GetMapping("/employeesByReportingManager")
    public GenericResponse<List<MovementFetchDTO>> getEmployeesForPrimaryReportingManager(
            @RequestParam(required = false) String HRStatus,
            @RequestParam(required = false) String WithdrawStatus) {

        Map<String, Object> statusFilter = movementService.extractHrAndWithdrawStatus(HRStatus, WithdrawStatus);

        return GenericResponse.success(
                movementService.getEmployeesForPrimaryReportingManager(
                        (String[]) statusFilter.get("hrStatus"),
                        (Boolean) statusFilter.get("initializerWithdraw"))
        );
    }

    @GetMapping("/employeesUnderReviewer")
    public GenericResponse<List<MovementFetchDTO>> getEmployeesUnderReviewer(@RequestParam(required = false) String teamType) {
        return GenericResponse.success(movementService.getEmployeesUnderReviewer(teamType));
    }
    @PutMapping("/updateReviewerStatus")
    public GenericResponse<String> updateReviewerStatus(@RequestBody List<ReviewerUpdateDTO> requestList) {
        movementService.updateReviewerStatus(requestList);
        return GenericResponse.success(translator.toLocale(AppMessages.MOVEMENT_INITIATED));
    }
    @PutMapping("/updateHRStatus")
    public GenericResponse<String> updateHRStatus(@RequestBody List<HrUpdateDTO> request) {
        movementService.updateHRStatus(request);
        return GenericResponse.success(translator.toLocale(AppMessages.HR_MOVEMENT_UPDATE));
    }
    @GetMapping("/getMovementInfoByStatus")
    public GenericResponse<List<MovementFetchDTO>> getMovementInfoByStatus(
            @RequestParam(required = false) String HRStatus,
            @RequestParam(required = false) String InitializerWithdraw) {

        return GenericResponse.success(movementService.getMovementInfoByHrStatus(HRStatus, InitializerWithdraw));
    }
    @PutMapping("/withdrawStatus")
    public GenericResponse<String> updateWithdrawStatus(@RequestParam String empId, @RequestParam String movementId) {
        movementService.updateWithdrawStatus(empId,movementId);
        return GenericResponse.success(translator.toLocale(AppMessages.MOVEMENT_WITHDRAW));
    }
    @GetMapping("/emp-old-detail")
    @Operation(summary = "Fetch Employee Details")
    public GenericResponse<EmployeeCurrentDetail> fetchEmployeeOldDetail(@RequestParam String empId) {
        EmployeeCurrentDetail employeeOldDetails = movementService.getEmployeeOldDetails(empId);
        return GenericResponse.success(employeeOldDetails);
    }

}
