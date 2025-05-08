package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.WorkingInformationDTO;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.WorkingInformationService;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/work-info")
@Slf4j
public class WorkingInformationController {

    private final WorkingInformationService workingInformationService;

    private final Translator translator;

    private final MasterFormService masterFormService;

    private final JWTHelper jwtHelper;

    public WorkingInformationController(WorkingInformationService workingInformationService, Translator translator,
            MasterFormService masterFormService, JWTHelper jwtHelper) {
        this.workingInformationService = workingInformationService;
        this.translator = translator;
        this.masterFormService = masterFormService;
        this.jwtHelper = jwtHelper;
    }

    @PutMapping("/hr/{empId}")
    public GenericResponse<String> updateWorkingInformation(@PathVariable String empId, @RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        workingInformationService.updateWorkingInformation(formRequest, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.WORKING_INFORMATION_UPDATE));
    }

    @GetMapping("")
    public GenericResponse<WorkingInformationDTO> getWorkingInformation() {
        WorkingInformationDTO workingInformationDTO = workingInformationService.getWorkingInformation(jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(workingInformationDTO);
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<WorkingInformationDTO> getHrWorkingInformation(@PathVariable String empId) {
        WorkingInformationDTO workingInformationDTO = workingInformationService.getWorkingInformation(empId);
        return GenericResponse.success(workingInformationDTO);
    }

}
