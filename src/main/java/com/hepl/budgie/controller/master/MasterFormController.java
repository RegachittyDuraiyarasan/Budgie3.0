package com.hepl.budgie.controller.master;

import com.fasterxml.jackson.annotation.JsonView;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.dto.formbuilder.FormBuilderDTO;
import com.hepl.budgie.dto.formbuilder.FormBuilderFields;
import com.hepl.budgie.entity.master.MasterForm;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormDTO;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.form.FormView;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.master.MasterSettingsService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "Create and Manage forms", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/forms")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MasterFormController {
    private final MasterFormService masterFormService;
    private final MasterSettingsService masterSettingsService;
    private final Translator translator;

    @GetMapping()
    @Operation(summary = "Get forms based on form name")
    public GenericResponse<FormDTO> fetch(@RequestParam String formName,
            Authentication authentication) {
        log.info("Get forms - {}", formName);

        return GenericResponse.success(masterFormService.getFormByName(formName, AccessLevelType.ADD));
    }

    @GetMapping("/all")
    @Operation(summary = "Get forms based on form name")
    public GenericResponse<List<MasterForm>> fetchAllForms(
            @RequestParam(defaultValue = "", required = false) String org) {
        log.info("Get all forms");

        return GenericResponse.success(masterFormService.allForms(org));
    }

    @PostMapping()
    @Operation(summary = "Submit Form")
    public GenericResponse<String> saveForm(@Valid @RequestBody FormDTO formDTO,
            @RequestParam(required = false, defaultValue = "") String org) {
        log.info("Save forms - {}", formDTO.getFormName());
        masterFormService.saveForm(formDTO, org);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_SAVED));
    }

    @GetMapping("/builder/{id}")
    public GenericResponse<List<FormBuilderFields>> getFormBuilderFields(@PathVariable String id,
            @RequestParam(required = false, defaultValue = "") String org) {
        log.info("Get form builder fields {}", id);

        return GenericResponse.success(masterFormService.getBuilderFormByOrgAndId(org, id));
    }

    @PostMapping("/builder")
    @Operation(summary = "Submit Form")
    public GenericResponse<String> saveBuildForm(@Valid @RequestBody FormBuilderDTO formDTO,
            @RequestParam(defaultValue = "", required = false) String org) {
        log.info("Save form builder - {}", formDTO.getFormName());
        masterFormService.buildForm(formDTO, org);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_SAVED));
    }

    @PutMapping("/builder/{id}")
    @Operation(summary = "Update Form")
    public GenericResponse<String> updateBuildForm(@Valid @RequestBody FormBuilderDTO formDTO,
            @RequestParam(defaultValue = "", required = false) String org, @PathVariable String id) {
        log.info("Update form builder - {}", formDTO.getFormName());
        masterFormService.buildForm(formDTO, org);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_SAVED));
    }

    @PutMapping(value = "/{formId}")
    @Operation(summary = "Update Form")
    public GenericResponse<String> updateForm(@PathVariable String formId, @Valid @RequestBody FormDTO formDTO,
            @RequestParam(defaultValue = "", required = false) String org) {
        log.info("Update forms - {}", formDTO.getFormName());
        masterFormService.updateForms(formDTO, formId, org);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_UPDATE));
    }

    @DeleteMapping(value = "/{formId}")
    @Operation(summary = "Update Form")
    public GenericResponse<String> deleteFormById(@PathVariable String formId,
            @RequestParam(defaultValue = "", required = false) String org) {
        log.info("Delete forms - {}", formId);
        masterFormService.deleteForm(formId, org);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_UPDATE));
    }

    @DeleteMapping(value = "/{formId}/{fieldId}")
    @Operation(summary = "Update Form")
    public GenericResponse<String> deleteFormByFieldIdAndForm(@PathVariable String formId,
            @PathVariable String fieldId,
            @RequestParam(defaultValue = "", required = false) String org) {
        log.info("Delete forms - {}, field id - {}", formId, fieldId);
        masterFormService.deleteFormByFieldId(formId, fieldId, org);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_FIELD_DELETED));
    }

    @PostMapping(value = "/validate-forms", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit Form application/json")
    public GenericResponse<String> validateFormJSON(
            @Valid @RequestBody @JsonView(FormView.Single.class) FormRequest formRequest,
            @RequestParam(defaultValue = "", required = false) String org) {
        log.info("Submit forms - {}", formRequest.getFormName());
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_VALIDATED));
    }

    @PostMapping(value = "/validate-forms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit Form multipart/form-data")
    public GenericResponse<String> validateFormMultipart(
            @Valid @ModelAttribute @JsonView(FormView.Single.class) FormRequest formRequest,
            @RequestParam(defaultValue = "", required = false) String org) {
        log.info("Submit form data - {}", formRequest.getFormName());
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_VALIDATED));
    }

    @PutMapping(value = "/field/{formId}")
    @Operation(summary = "Update form field")
    public GenericResponse<String> updateFormData(@PathVariable String formId,
            @Valid @RequestBody FormFieldsDTO formField,
            @RequestParam(defaultValue = "", required = false) String org) {
        log.info("Update forms");
        masterFormService.updateFormFields(formField, formId, org);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_VALIDATED));
    }

    @GetMapping("/options/predefined")
    @Operation(summary = "Fetch pre defined options")
    public GenericResponse<List<OptionsResponseDTO>> fetchPredefinedOptions() {
        log.info("Fetching predefine options from masters");

        return GenericResponse.success(masterSettingsService.fetchOptions(""));
    }

    @GetMapping("/options/api")
    @Operation(summary = "Fetch all api options")
    public GenericResponse<List<OptionsResponseDTO>> fetchAllAPIOptions() {
        log.info("Fetching api options");
        List<OptionsResponseDTO> options = new ArrayList<>();

        OptionsResponseDTO optionM = OptionsResponseDTO.builder().name("Test").value("/test/tenant-per/category")
                .build();
        OptionsResponseDTO optionC = OptionsResponseDTO.builder().name("Test2")
                .value("/forms/fetch-options?name=%24formName&value=%24_id&collectionName=m_forms")
                .build();

        options.add(optionC);
        options.add(optionM);

        return GenericResponse.success(options);

    }

    @GetMapping("/fetch-options")
    @Operation(summary = "Fetch Specific Fields")
    public GenericResponse<List<OptionsResponseDTO>> fetchDataByField(
            @Parameter(name = "name", description = "Field to be shown as label on dropdown", example = "$field1,-,$field2") @RequestParam @NotBlank(message = "{validation.error.notBlank}") String name,
            @Parameter(name = "value", description = "Field value on dropdown", example = "$field") @RequestParam @NotBlank(message = "{validation.error.notBlank}") String value,
            @Parameter(name = "collectionName", description = "Collection name from which dropdown is fetched", example = "name") @RequestParam @NotBlank(message = "{validation.error.notBlank}") String collectionName,
            @Parameter(name = "filter", description = "Filter condition for collections", example = "field:value:datatype") @RequestParam(required = false) String filter) {
        log.info("Fetching options from collection - {}", collectionName);
        // Fetch data and map it to the DTO
        return GenericResponse.success(masterFormService.fetchField(name, value, collectionName, filter));

    }

}
