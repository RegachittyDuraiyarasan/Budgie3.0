package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.DivisionHeadDTO;
import com.hepl.budgie.dto.userinfo.HRInfoDTO;
import com.hepl.budgie.dto.userinfo.PrimaryDTO;
import com.hepl.budgie.dto.userinfo.ReviewerDTO;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.HRInformationService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/hr-info")
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class HRInformationController {
    private final MasterFormService masterFormService;

    private final HRInformationService hrInformationService;

    private final Translator translator;

    private final JWTHelper jwtHelper;

    @GetMapping("/reporting-manager")
    public GenericResponse<List<PrimaryDTO>> getReportingManager() {
        log.info("Fetching primary manager details");
        return GenericResponse.success(hrInformationService.getReportingManager());
    }

    @GetMapping("/reviewer")
    public GenericResponse<List<ReviewerDTO>> getReviewer() {
        log.info("Fetching Reviewer details");
        return GenericResponse.success(hrInformationService.getReviewer());
    }

    @GetMapping("/division-head")
    public GenericResponse<List<DivisionHeadDTO>> getDivisionHead() {
        log.info("Fetching DivisionHead details");
        return GenericResponse.success(hrInformationService.getDivisionHead());
    }

    @PutMapping("/hr/{empId}")
    public GenericResponse<String> updateHRInfo(@PathVariable String empId, @RequestBody FormRequest formRequest) {
        log.info("");
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.EDIT);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        hrInformationService.updateHRInfo(empId, formRequest);
        return GenericResponse.success(translator.toLocale(AppMessages.HR_INFO_UPDATE));
    }

    @GetMapping()
    public GenericResponse<HRInfoDTO> getHRInfoEmpSide() {
        return GenericResponse.success(hrInformationService.getHRInfo(jwtHelper.getUserRefDetail().getEmpId()));
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<HRInfoDTO> getHRInfo(@PathVariable String empId) {
        return GenericResponse.success(hrInformationService.getHRInfo(empId));
    }
}
