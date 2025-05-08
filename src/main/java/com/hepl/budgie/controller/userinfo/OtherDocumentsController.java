package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.DocumentsFetchDTO;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.OtherDocumentsService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/doc-info")
public class OtherDocumentsController {
    private final MasterFormService masterFormService;

    private final OtherDocumentsService otherDocumentsService;

    private final Translator translator;

    private final FileService fileService;

    private final JWTHelper jwtHelper;

    @PutMapping()
    public GenericResponse<String> updateDocuments(@ModelAttribute FormRequest file) throws IOException {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(file.getFormName(), org,
                AccessLevelType.EDIT);
        Map<String, Object> fields = masterFormService.formValidate(file, org, AccessLevelType.EDIT, formFields);
        otherDocumentsService.updateOtherDocuments(fields, formFields,jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(translator.toLocale(AppMessages.OTHER_DOCUMENTS_UPDATE));
    }

    @PutMapping("/hr/{empId}")
    public GenericResponse<String> updateHRDocuments(@ModelAttribute FormRequest file,@PathVariable String empId) throws IOException {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(file.getFormName(), org,
                AccessLevelType.EDIT);
        Map<String, Object> fields = masterFormService.formValidate(file, org, AccessLevelType.EDIT, formFields);
        otherDocumentsService.updateOtherDocuments(fields, formFields, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.OTHER_DOCUMENTS_UPDATE));
    }

    @GetMapping()
    public GenericResponse<List<DocumentsFetchDTO>> getFileNameByEmpId() {
        return GenericResponse.success(otherDocumentsService.getFileNameByEmpId(jwtHelper.getUserRefDetail().getEmpId()));
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<List<DocumentsFetchDTO>> getFileNameByHR(@PathVariable String empId) {
        return GenericResponse.success(otherDocumentsService.getFileNameByEmpId(empId));
    }

}
