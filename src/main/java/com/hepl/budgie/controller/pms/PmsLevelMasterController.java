package com.hepl.budgie.controller.pms;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.pms.LevelDateDTO;
import com.hepl.budgie.dto.pms.LevelFlowUpdateDTO;
import com.hepl.budgie.entity.pms.LevelDetails;
import com.hepl.budgie.entity.pms.PmsLevel;
import com.hepl.budgie.service.pms.PmsLevelMasterService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pmsLevel")
@RequiredArgsConstructor
public class PmsLevelMasterController {
    private final PmsLevelMasterService pmsLevelMasterService;
    private final Translator translator;

    @PostMapping()
    @Operation(summary = "Add PmsLevel Master")
    public GenericResponse<String> addPmsLevel(@RequestBody PmsLevel request) {
        pmsLevelMasterService.addPmsLevel(request);
        return GenericResponse.success(translator.toLocale(AppMessages.PMS_LEVEL_ADDED)
        );
    }

    @PutMapping("/flow")
    @Operation(summary = "update flow")
    public GenericResponse<String> updateFlow(@RequestBody LevelFlowUpdateDTO request) {
        pmsLevelMasterService.updateFlow(request);
        return GenericResponse.success(translator.toLocale(AppMessages.PMS_FLOW_UPDATED)
        );
    }

    @PutMapping("/levelDetails")
    @Operation(summary = "update level details")
    public GenericResponse<String> updateLevelDetails(@RequestBody LevelDetails request) {
        pmsLevelMasterService.updateLevelDetails(request);
        return GenericResponse.success(translator.toLocale(AppMessages.PMS_LEVEL_DETAILS_UPDATED)
        );
    }

    @PutMapping("/levelTime")
    @Operation(summary = "update level time start and out")
    public GenericResponse<String> updateLevelTime(@RequestBody LevelDetails request) {
        pmsLevelMasterService.updateLevelTime(request);
        return GenericResponse.success(translator.toLocale(AppMessages.PMS_LEVEL_TIME)
        );
    }

    @GetMapping("/pmsLevelTime")
    @Operation(summary = "Fetch Pms Level start and endTime")
    public GenericResponse<List<LevelDateDTO>> getPmsLevelTime() {
        List<LevelDateDTO> pmsLevel = pmsLevelMasterService.getPmsLevelTime();
        return GenericResponse.success(pmsLevel);
    }


}
