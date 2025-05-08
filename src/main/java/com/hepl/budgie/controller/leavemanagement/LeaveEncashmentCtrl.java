package com.hepl.budgie.controller.leavemanagement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.entity.leavemanagement.LeaveEncashment;
import com.hepl.budgie.service.leavemanagement.LeaveEncashmentService;
import com.hepl.budgie.service.master.MasterFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Manage Leave Encashment", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/leave/encashment")
@Slf4j
public class LeaveEncashmentCtrl {
    private final LeaveEncashmentService leaveEncashmentService;
    private final Translator translator;
    private final MasterFormService masterFormService;

    public LeaveEncashmentCtrl(LeaveEncashmentService leaveEncashmentService, Translator translator,
            MasterFormService masterFormService) {
        this.leaveEncashmentService = leaveEncashmentService;
        this.translator = translator;
        this.masterFormService = masterFormService;
    }

    @GetMapping()
    @Operation(summary = "Get Leave Encashment")
    public GenericResponse<List<LeaveEncashment>> fetch() {
        log.info("Get Leave Encashment");
        return GenericResponse.success(leaveEncashmentService.getLeaveEncashmentList());
    }
}
