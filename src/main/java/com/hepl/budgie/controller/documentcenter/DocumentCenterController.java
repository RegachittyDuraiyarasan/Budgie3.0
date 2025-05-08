package com.hepl.budgie.controller.documentcenter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.documentInfo.DocumentDTO;
import com.hepl.budgie.dto.documentInfo.DocumentDetailsInfoDto;
import com.hepl.budgie.dto.documentInfo.DocumentInfoDto;
import com.hepl.budgie.dto.documentInfo.ResponseDocumentDTO;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.documentinfo.DocumentInfo;
import com.hepl.budgie.entity.master.ModuleMaster;
import com.hepl.budgie.service.documentservice.DocumentService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/document/center")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")

public class DocumentCenterController {

    private final DocumentService documentService;
    private final Translator translator;
    private final MasterFormService masterFormService;
    private final JWTHelper jwtHelper;

    @PostMapping("/category")
    public GenericResponse<String> addDocumentType(@RequestBody FormRequest formRequest,
            @RequestParam String referenceName) {
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(),
                jwtHelper.getOrganizationCode(),
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, jwtHelper.getOrganizationCode(), AccessLevelType.ADD, formFields);

        documentService.addDocumentTYpe(formRequest, referenceName, jwtHelper.getOrganizationCode());

        return GenericResponse.success(translator.toLocale(AppMessages.ADDED_DOCUMENT_TYPE));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> uploadDocument(
            @ModelAttribute @Valid DocumentDTO documentDTO) throws IOException {

        if ("bulk Upload".equalsIgnoreCase(documentDTO.getDocumentsCategory())) {

            documentService.addBulkDocumentInfo(documentDTO);
        } else {
            documentService.addDocumentInfo(documentDTO);
        }

        return GenericResponse.success(translator.toLocale(AppMessages.DOCUMENT_ADDED_SUCCESSFULLY));
    }

    @GetMapping
    public GenericResponse<List<Map<String, Object>>> getDocumentContent(@RequestParam String referenceName) {
        log.info("Fetching document content for referenceName: {}", referenceName);

        List<Map<String, Object>> options = documentService.getOptionsByReferenceNameContent(referenceName,
                jwtHelper.getOrganizationCode());

        return GenericResponse.success(options);
    }

    @GetMapping("/document-type")
    public GenericResponse<List<Map<String, Object>>> getAllDocumentTypes(
            @RequestParam String referenceName) {
        List<Map<String, Object>> documentTypes = documentService.getAllDocumentTypes(referenceName,
                jwtHelper.getOrganizationCode());
        return GenericResponse.success(documentTypes);
    }

    @GetMapping("/document-info")
    public GenericResponse<List<ResponseDocumentDTO>> getAllDocumentInfo() {
        List<ResponseDocumentDTO> documentInfoList = documentService.getAllDocumentInfo();
        return GenericResponse.success(documentInfoList);
    }

    @PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> updateDocument(
            @ModelAttribute @Valid DocumentDTO documentDTO) throws IOException {

        if (documentDTO.getModuleId() == null || documentDTO.getModuleId().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    translator.toLocale(AppMessages.DOCUMENT_ID_REQUIRED));
        }

        documentService.updateDocumentInfo(documentDTO);
        return GenericResponse.success(translator.toLocale(AppMessages.DOCUMENT_UPDATED));
    }

    @PutMapping("/update-status")
    public GenericResponse<String> updateDocumentStatus(
            @RequestParam String moduleId,
            @RequestParam String empId) {
        documentService.updateDocumentStatus(moduleId, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.DOCUMENT_STATUS_UPDATED));
    }

    @DeleteMapping("/documnt-info")
    public GenericResponse<String> softDeleteDocument(
            @RequestParam String moduleId,
            @RequestParam String empId) {
        documentService.DeleteDocumentInfo(moduleId, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.DOCUMENT_DELETED));
    }

}
