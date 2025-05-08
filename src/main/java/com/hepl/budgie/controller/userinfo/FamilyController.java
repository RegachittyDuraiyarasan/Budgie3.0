package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.FamilyDTO;
import com.hepl.budgie.entity.userinfo.EmergencyContacts;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.FamilyService;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users/family-info")
@Slf4j
public class FamilyController {

    private final FamilyService familyService;

    private final Translator translator;

    private final MasterFormService masterFormService;

    private final JWTHelper jwtHelper;

    public FamilyController(FamilyService familyService, Translator translator, MasterFormService masterFormService,
            JWTHelper jwtHelper) {
        this.familyService = familyService;
        this.translator = translator;
        this.masterFormService = masterFormService;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping("")
    public GenericResponse<String> insertFamily(@RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        familyService.insertFamily(formRequest, jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(translator.toLocale(AppMessages.FAMILY_INSERT));
    }

    @PutMapping("")
    public GenericResponse<String> updateFamily(@RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        familyService.updateFamily(formRequest, jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(translator.toLocale(AppMessages.FAMILY_UPDATE));
    }

    @GetMapping("")
    public GenericResponse<FamilyDTO> getFamily() {
        FamilyDTO familyDTO = familyService.getFamily(jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(familyDTO);
    }

    @DeleteMapping("")
    public GenericResponse<String> deleteFamily(@RequestBody EmergencyContacts emergencyContacts) {
        familyService.deleteFamily(emergencyContacts, jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(translator.toLocale(AppMessages.FAMILY_DELETE));
    }

    // HR API's
    @PostMapping("/hr/{empId}")
    public GenericResponse<String> insertHrFamily(@PathVariable String empId, @RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        familyService.insertFamily(formRequest, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.FAMILY_INSERT));
    }

    @PutMapping("/hr/{empId}")
    public GenericResponse<String> updateHrFamily(@PathVariable String empId, @RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        familyService.updateFamily(formRequest, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.FAMILY_UPDATE));
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<FamilyDTO> getFamily(@PathVariable String empId) {
        FamilyDTO familyDTO = familyService.getFamily(empId);
        return GenericResponse.success(familyDTO);
    }

    @DeleteMapping("/hr/{empId}")
    public GenericResponse<String> deleteFamily(@PathVariable String empId, @RequestBody EmergencyContacts emergencyContacts) {
        familyService.deleteFamily(emergencyContacts, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.FAMILY_DELETE));
    }

}
