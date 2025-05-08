package com.hepl.budgie.controller.pms;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.pms.PmsDTO;
import com.hepl.budgie.entity.pms.PmsListDTO;
import com.hepl.budgie.service.pms.PmsAnalyticsService;
import com.hepl.budgie.service.pms.PmsModifyService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pms")
public class PmsEmployeeModifyController {
    private final PmsModifyService pmsEmployeeModifyService;
    private final Translator translator;
    private final PmsAnalyticsService pmsAnalyticsService;

    @PostMapping()
    @Operation(summary = "Employee Apply for PMS")
    public GenericResponse<String> addPms(@RequestBody PmsDTO request) {
        boolean isAlreadySubmitted = pmsEmployeeModifyService.addPms(request);

        if (isAlreadySubmitted) {
            return GenericResponse.success(translator.toLocale(AppMessages.PMS_ALREADY_SAVED));
        } else {
            return GenericResponse.success(translator.toLocale(AppMessages.PMS_APPLIED));
        }
    }

    @GetMapping()
    @Operation(summary = "Fetch Pms data based on level")
    public GenericResponse<Object> fetchPmsByLevel(@RequestBody PmsListDTO request) {
        return GenericResponse.success(pmsEmployeeModifyService.fetchDataByLevel(request)
        );
    }

    @PutMapping()
    @Operation(summary = "update level time start and out")
    public GenericResponse<String> updatePmsByLevel(@RequestBody List<PmsDTO> request) {
        pmsEmployeeModifyService.updatePmsByLevel(request);
        return GenericResponse.success(translator.toLocale(AppMessages.PMS_UPDATED)
        );
    }

    @GetMapping("/rm-analytics-chart")
    @Operation(summary = "Fetch data for RM analytics tab")
    public GenericResponse <List<Map<String, String>> >fetchChartData(@RequestParam String pmsYear , @RequestParam String levelName) {
        return GenericResponse.success(pmsAnalyticsService.fetchChartData(pmsYear, levelName)
        );
    }




}
