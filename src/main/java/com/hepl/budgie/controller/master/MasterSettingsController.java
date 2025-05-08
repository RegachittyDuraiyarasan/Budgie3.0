package com.hepl.budgie.controller.master;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.master.MasterSettingsService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Create and Manage settings", description = "")
@RestController
@RequestMapping("/settings")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@RequiredArgsConstructor
public class MasterSettingsController {

    private final Translator translator;
    private final MasterSettingsService masterSettingsService;
    private final MasterFormService masterFormService;
    private final JWTHelper jwtHelper;

    @GetMapping()
    @Operation(summary = "Get settings based on reference name")
    public GenericResponse<List<MasterFormOptions>> fetch(@RequestParam String referenceName) {
        log.info("Settings reference Name - {}", referenceName);
        return GenericResponse.success(masterSettingsService.getSettingsByReferenceName(referenceName));
    }

    @PostMapping()
    @Operation(summary = "Add or update settings based on reference name")
    public GenericResponse<MasterFormSettings> addOrUpdateSettings(@RequestParam String referenceName,
            @Valid @RequestBody FormRequest formRequest) {
        log.info("Received settings for reference name - {}", referenceName);
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), jwtHelper.getOrganizationCode(),
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, jwtHelper.getOrganizationCode(), AccessLevelType.ADD, formFields);

        return GenericResponse.success(masterSettingsService.addOrUpdateSettings(formRequest, referenceName));
    }

    @PutMapping("/{value}")
    @Operation(summary = "Update settings based on value")
    public GenericResponse<MasterFormSettings> updateSettings(@PathVariable String value,
            @RequestParam String referenceName,
            @Valid @RequestBody FormRequest formRequest) {
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), jwtHelper.getOrganizationCode(),
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, jwtHelper.getOrganizationCode(), AccessLevelType.ADD, formFields);

        return GenericResponse.success(masterSettingsService.updateSettings(value, referenceName, formRequest));
    }

    @DeleteMapping("/{value}")
    public GenericResponse<String> deleteSettings(@PathVariable String value, @RequestParam String referenceName) {
        log.info("Delete settings - {}, Reference - {}", value, referenceName);

        masterSettingsService.deleteOptions(value, referenceName);
        return GenericResponse.success(translator.toLocale(AppMessages.SETTINGS_OPTION_DELETED));
    }

    @PutMapping("/update-status/{value}")
    @Operation(summary = "Update status based on value")
    public GenericResponse<MasterFormSettings> updateStatusSettings(@PathVariable String value,
            @RequestParam String referenceName,
            @RequestParam String status) {
        return GenericResponse.success(masterSettingsService.updateStatusSettings(value, referenceName, status));
    }
}
