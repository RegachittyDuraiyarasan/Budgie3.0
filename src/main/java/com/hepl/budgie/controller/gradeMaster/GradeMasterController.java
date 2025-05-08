package com.hepl.budgie.controller.gradeMaster;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.grade.GradeFetchDTO;
import com.hepl.budgie.service.gradeMaster.GradeMasterService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grade-master")
@RequiredArgsConstructor
@Slf4j
public class GradeMasterController {

    private final GradeMasterService gradeMasterService;

    private final MasterFormService masterFormService;

    private final JWTHelper jwtHelper;

    private final Translator translator;

    @PostMapping()
    public GenericResponse<String> addGrade(@RequestBody FormRequest formRequest){
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        gradeMasterService.addGrade(formRequest,org);
        return GenericResponse.success(translator.toLocale(AppMessages.GRADE_ADDED));
    }

    @GetMapping()
    public GenericResponse<List<GradeFetchDTO>> getGrade(){
        List<GradeFetchDTO> gradeFetchDTO = gradeMasterService.getAllGrades(jwtHelper.getOrganizationCode());
        log.info("gradeFetchDTO {}",gradeFetchDTO);
        return GenericResponse.success(gradeFetchDTO);
    }

    @PutMapping("/{gradeId}")
    public GenericResponse<String> updateGrade(@PathVariable String gradeId,@RequestBody FormRequest formRequest){
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(),
                jwtHelper.getOrganizationCode(),
                AccessLevelType.EDIT);
        masterFormService.formValidate(formRequest, jwtHelper.getOrganizationCode(), AccessLevelType.EDIT, formFields);
        gradeMasterService.updateGrade(formRequest, jwtHelper.getOrganizationCode(), gradeId);
        return GenericResponse.success(translator.toLocale(AppMessages.GRADE_UPDATED));
    }

    @DeleteMapping("/{gradeId}")
    public GenericResponse<String> deleteGrade(@PathVariable String gradeId){
        gradeMasterService.deleteGrade(gradeId, jwtHelper.getOrganizationCode());
        return GenericResponse.success(translator.toLocale(AppMessages.GRADE_DELETED));
    }

    @PutMapping("/status/{gradeId}")
    public GenericResponse<String> toggleStatus(@PathVariable String gradeId){
        gradeMasterService.toggleStatus(gradeId, jwtHelper.getOrganizationCode());
        return GenericResponse.success(translator.toLocale(AppMessages.GRADE_STATUS));
    }


}
