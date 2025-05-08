package com.hepl.budgie.controller.master;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.master.ModuleMaster;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.master.ModuleMasterSettingsService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/module-settings")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ModuleMasterSettingsController {

    private final ModuleMasterSettingsService moduleMasterSettingsService;
    private final MasterFormService masterFormService;
    private final Translator translator;
    private final JWTHelper jwtHelper;

    @PostMapping
    public GenericResponse<String> addModuleMaster(@RequestBody FormRequest formRequest,
            @RequestParam String referenceName) {
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(),
                jwtHelper.getOrganizationCode(),
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, jwtHelper.getOrganizationCode(), AccessLevelType.ADD, formFields);
        moduleMasterSettingsService.addModuleMaster(formRequest, referenceName, jwtHelper.getOrganizationCode());
        return GenericResponse.success(translator.toLocale(AppMessages.MODULE_SETTINGS_ADDED));
    }

    @GetMapping
    public List<ModuleMaster> getModuleMasterOptions(@RequestParam String referenceName) {
        log.info("Reference Name {}", referenceName);
        log.info("ORG {}", jwtHelper.getOrganizationCode());
        return moduleMasterSettingsService.getOptionsByReferenceName(referenceName, jwtHelper.getOrganizationCode());
    }

    @PutMapping()
    public GenericResponse<String> updateModuleMasterOptions(
            @RequestBody FormRequest formRequest,
            @RequestParam String referenceName,
            @RequestParam Map<String, String> request) {
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(),
                jwtHelper.getOrganizationCode(),
                AccessLevelType.EDIT);
        masterFormService.formValidate(formRequest, jwtHelper.getOrganizationCode(), AccessLevelType.EDIT, formFields);

        String moduleId = request.get("moduleId");
        if (moduleId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,AppMessages.MODULE_ID);
        }

        moduleMasterSettingsService.updateModuleMaster(formRequest, referenceName, jwtHelper.getOrganizationCode(), moduleId);
        return GenericResponse.success(translator.toLocale(AppMessages.MODULE_SETTINGS_UPDATED));
    }

    @DeleteMapping()
    public GenericResponse<String> deleteModuleMasterOptions(@RequestParam Map<String, String> request, @RequestParam String referenceName) {
        log.info("Delete settings - {}, Reference - {}", referenceName);
        String moduleId = request.get("moduleId");
        if (moduleId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,AppMessages.MODULE_ID);
        }
        moduleMasterSettingsService.deleteOptions(moduleId, referenceName);
        return GenericResponse.success(translator.toLocale(AppMessages.MODULE_SETTINGS_DELETED));
    }

}
