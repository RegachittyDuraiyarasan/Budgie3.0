package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.BasicDetailsDTO;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.BasicDetailsService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/basic-info")
@Slf4j
@RequiredArgsConstructor
public class BasicDetailsController {

    private final BasicDetailsService basicDetailsService;

    private final MasterFormService masterFormService;

    private final Translator translator;

    private final JWTHelper jwtHelper;

    @PutMapping()
    public GenericResponse<String> updateBasicDetails(@RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        basicDetailsService.updateBasicDetails(formRequest,jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(translator.toLocale(AppMessages.BASIC_DETAILS_UPDATE));
    }

    @PutMapping("/hr/{empId}")
    public GenericResponse<String> updateHRBasicDetails(@PathVariable String empId,@RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        basicDetailsService.updateBasicDetails(formRequest,empId);
        return GenericResponse.success(translator.toLocale(AppMessages.BASIC_DETAILS_UPDATE));
    }

    @GetMapping()
    public GenericResponse<BasicDetailsDTO> getBasicDetails() {
        BasicDetailsDTO basicDetailsDTO = basicDetailsService.getBasicDetails(jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(basicDetailsDTO);
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<BasicDetailsDTO> getHRBasicDetails(@PathVariable String empId) {
        BasicDetailsDTO basicDetailsDTO = basicDetailsService.getBasicDetails(empId);
        return GenericResponse.success(basicDetailsDTO);
    }
}
