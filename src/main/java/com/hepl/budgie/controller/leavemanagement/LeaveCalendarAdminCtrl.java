package com.hepl.budgie.controller.leavemanagement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.leavemanagement.AdminLeaveCalendarDateFilterDTO;
import com.hepl.budgie.dto.leavemanagement.AdminLeaveCalenderDTO;
import com.hepl.budgie.service.leavemanagement.LeaveCalendarAdminService;
import com.hepl.budgie.service.master.MasterFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Manage Leave Calendar Admin", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/leave/calendar-admin")
@Slf4j
public class LeaveCalendarAdminCtrl {
    private final LeaveCalendarAdminService leaveCalendarAdminService;

    public LeaveCalendarAdminCtrl(LeaveCalendarAdminService leaveCalendarAdminService, Translator translator,
            MasterFormService masterFormService) {
        this.leaveCalendarAdminService = leaveCalendarAdminService;
    }

    @GetMapping()
    @Operation(summary = "Get Leave Calendar Admin")
    public GenericResponse<List<AdminLeaveCalendarDateFilterDTO>> fetch(
            @Valid AdminLeaveCalenderDTO adminCalenderFilterData) {
        log.info("Get Leave Calendar Admin");
        return GenericResponse.success(leaveCalendarAdminService.getLeaveCalendarAdminList(adminCalenderFilterData));
    }

    @GetMapping("/details")
    public GenericResponse<List<Map<String, Object>>> getEmployeeLeaveCalendar(
            @RequestParam(required = false) String empId, @RequestParam String monthYear, @RequestParam boolean isTeams,
            @RequestParam(required = false) String reviewer, @RequestParam(required = false) String repManager,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String payrollStatus, @RequestParam(required = false) String location,
            @RequestParam(required = false) String fromDate, @RequestParam(required = false) String toDate) {

        log.info("fetch employee leave calendar");
        List<Map<String, Object>> leaves = leaveCalendarAdminService.getEmployeeLeaveCalendar(empId, monthYear,
                isTeams, reviewer, repManager, department, designation,
                payrollStatus, location, fromDate, toDate);
        return GenericResponse.success(leaves);
    }

}
