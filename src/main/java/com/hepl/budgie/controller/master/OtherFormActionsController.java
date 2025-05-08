package com.hepl.budgie.controller.master;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.master.OtherFormsActionsService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.*;
import com.hepl.budgie.config.i18n.Translator;

@RestController
@RequestMapping("/otherForms")
@CrossOrigin
@Slf4j
public class OtherFormActionsController {
    private final OtherFormsActionsService otherFormsActionsService;
    private final Translator translator;
    private final MasterFormService masterFormService;

    public OtherFormActionsController(OtherFormsActionsService otherFormsActionsService, Translator translator,
            MasterFormService masterFormService) {
        this.otherFormsActionsService = otherFormsActionsService;
        this.translator = translator;
        this.masterFormService = masterFormService;
    }

    @PostMapping()
    @Operation(summary = "Submit Form")
    public GenericResponse<String> saveData(@RequestBody FormRequest formRequest, @RequestParam String filter,
            @RequestParam String org) {
        log.info("Saving form {} ", formRequest.getFormName());
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        otherFormsActionsService.saveDynamicData(formRequest, filter);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_SAVED));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Form by ID")
    public GenericResponse<String> updateData(
            @PathVariable String id,
            @RequestBody FormRequest formRequest,
            @RequestParam String org) {
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        log.info("Updating form {} with ID: {}", formRequest.getFormName(), id);

        otherFormsActionsService.updateDynamicData(id, formRequest);
        return GenericResponse.success(translator.toLocale(AppMessages.FORM_UPDATE));
    }

}
