package com.hepl.budgie.controller.leavemanagement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.leavemanagement.LeaveReportDTO;
import com.hepl.budgie.service.leavemanagement.LeaveReportService;
import com.hepl.budgie.service.master.MasterFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Manage Leave Report", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/leave/report")
@Slf4j
public class LeaveReportCtrl {
    private final LeaveReportService leaveReportService;
    private final Translator translator;
    private final MasterFormService masterFormService;

    public LeaveReportCtrl(LeaveReportService leaveReportService, Translator translator,
            MasterFormService masterFormService) {
        this.leaveReportService = leaveReportService;
        this.translator = translator;
        this.masterFormService = masterFormService;
    }

    @GetMapping()
    @Operation(summary = "Get Leave Report")
    public GenericResponse<List<LeaveReportDTO>> fetch(@RequestParam String yearMonth) {
        log.info("Leave Report");
        return GenericResponse.success(leaveReportService.getLeaveReportList(yearMonth));
    }

}
