package com.hepl.budgie.controller.leavemanagement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.leavemanagement.LeaveApply;
import com.hepl.budgie.service.leavemanagement.LeaveApplyService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Leave Apply", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/leave/apply")
public class LeaveApplyController {
    private final LeaveApplyService leaveApplyService;
    private final Translator translator;

    public LeaveApplyController(LeaveApplyService leaveApplyService, Translator translator) {
        this.leaveApplyService = leaveApplyService;
        this.translator = translator;
    }

    @PostMapping()
    @Operation(summary = "Leave Apply")
    public GenericResponse<String> add(@Valid @RequestBody FormRequest request) {
        leaveApplyService.add(request);
        return GenericResponse.success(AppMessages.LEAVE_APPLIED);
    }

    @PutMapping()
    @Operation(summary = "Leave Approve")
    public GenericResponse<String> update(@Valid @RequestBody FormRequest request) {
        leaveApplyService.update(request);
        return GenericResponse.success(translator.toLocale(AppMessages.LEAVE_UPDATED));
    }
    @GetMapping()
    @Operation(summary = "Leave fetch")
    public GenericResponse<List<LeaveApply>> fetch(@RequestParam String role) {
        List<LeaveApply> filteredLeaves = leaveApplyService.getFilteredLeaveApplications(role);
        return GenericResponse.success(filteredLeaves);
    }



}
