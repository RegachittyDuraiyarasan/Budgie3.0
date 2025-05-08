package com.hepl.budgie.service.impl.master;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.config.exceptions.FieldException;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.dto.formbuilder.FormBuilderDTO;
import com.hepl.budgie.dto.formbuilder.FormBuilderFields;
import com.hepl.budgie.entity.FormFieldType;
import com.hepl.budgie.entity.master.MasterForm;
import com.hepl.budgie.mapper.form.FormMapper;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormDTO;
import com.hepl.budgie.repository.master.MasterFormRepository;
import com.hepl.budgie.repository.master.OptionsRepository;
import com.hepl.budgie.service.form.FormService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.FileExtUtils;
import com.mongodb.client.result.UpdateResult;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j

public class MasterServiceImplementation implements MasterFormService {

    public static final String REF_COLLECTION_NAME = "m_settings";

    private final MasterFormRepository masterFormRepository;
    private final OptionsRepository optionsRepository;
    private final MongoTemplate mongoTemplate;
    private final Map<String, FormService> formService;
    private final FormMapper formMapper;
    private final JWTHelper jwtHelper;

    public MasterServiceImplementation(MasterFormRepository masterFormRepository, OptionsRepository optionsRepository,
                                       MongoTemplate mongoTemplate, List<FormService> formServiceList, FormMapper formMapper, JWTHelper jwtHelper) {
        this.masterFormRepository = masterFormRepository;
        this.optionsRepository = optionsRepository;
        this.mongoTemplate = mongoTemplate;
        this.formMapper = formMapper;
        this.formService = formServiceList.stream()
                .collect(Collectors.toMap(path -> path.getClass().getSimpleName(), Function.identity()));
        this.jwtHelper = jwtHelper;
    }

    @Override
    public void deleteForm(String formId, String org) {
        log.info("Delete form - {}", formId);
        masterFormRepository.deleteForms(formId, org, mongoTemplate);
    }

    @Override
    public void deleteFormByFieldId(String formId, String fieldId, String org) {
        log.info("Delete form - {} by field id - {}", formId, fieldId);
        masterFormRepository.deleteFormByFieldId(formId, fieldId, org, mongoTemplate);
    }

    @Override
    public void buildForm(FormBuilderDTO formBuilderDTO, String org) {
        log.info("Build forms {}", formBuilderDTO.getFormName());
        List<FormFieldsDTO> formFields = formMapper.toFormFields(formBuilderDTO.getFields());

        FormDTO form = FormDTO.builder().formFields(formFields).formName(formBuilderDTO.getFormName()).build();
        masterFormRepository.saveForms(mapToFormFields(form, org), mongoTemplate, org);

    }

    @Override
    public void updateForm(FormBuilderDTO formBuilderDTO, String org, String formId) {
        log.info("Update forms {}", formBuilderDTO.getFormName());

        List<FormFieldsDTO> formFields = formMapper.toFormFields(formBuilderDTO.getFields());

        FormDTO form = FormDTO.builder().formFields(formFields).formName(formBuilderDTO.getFormName()).build();
        masterFormRepository.updateForms(mapToFormFields(form, org), mongoTemplate, formId, org);
    }

    @Override
    public List<MasterForm> allForms(String org) {
        log.info("Fetching all forms");

        return masterFormRepository.getAll(org, mongoTemplate);
    }

    @Override
    public FormDTO getFormByName(String formName, AccessLevelType accessLevelType) {
        log.info("Fetching form by name - {}", formName);
        log.info("Fetching form by org - {}", jwtHelper.getOrganizationCode());

        AggregationResults<FormDTO> formResults = masterFormRepository.fetchByFormName(formName, "HR",
                accessLevelType.label,
                mongoTemplate, jwtHelper.getOrganizationCode());

        if (formResults.getMappedResults().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        return formResults.getUniqueMappedResult();
    }

    @Override
    public void updateFormFields(FormFieldsDTO fields, String formId, String org) {
        log.info("Update form fields - {}", fields.getFieldId());

        UpdateResult result = masterFormRepository.updateFormField(fields, formId, mongoTemplate, org);
        if (result.getMatchedCount() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

    }

    @Override
    public Map<String, FormFieldsDTO> getFormFields(String formName, String org, AccessLevelType accessLevelType) {
        log.info("Get form field for name {}", formName);
        FormDTO masterForm = getFormByName(formName, accessLevelType);

        return masterForm.getFormFields().stream()
                .collect(Collectors.toMap(FormFieldsDTO::getFieldId, Function.identity()));
    }

    @Override
    public Map<String, Object> formValidate(FormRequest formRequest, String org, AccessLevelType accessLevelType,
            Map<String, FormFieldsDTO> masterFieldIds) {

        log.info("Validate forms - {}", formRequest.getFormName());

        Map<String, Object> formFieldMap = Optional.ofNullable(formRequest.getFormFields())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FORM_INVALID));
        Map<String, Object> renderFields = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        Map<String, String[]> errorArgs = new HashMap<>();

        // validate only those fields that are present in the master form
        for (Map.Entry<String, FormFieldsDTO> entry : masterFieldIds.entrySet()) {
            String formId = entry.getKey(); // spoc_name
            FormFieldsDTO masterFormField = entry.getValue();
            log.info("master form field {} ", masterFormField);
            Optional<Object> formValue = Optional.ofNullable(formFieldMap.get(formId));

            if (formValue.isPresent()) {
                log.info("form field {}, field type {} ", masterFormField.getFieldId(), masterFormField.getType());
                renderFields.put(formId, formFieldMap.get(formId));

                Optional<FormFieldType> fieldType = Optional
                        .ofNullable(FormFieldType.valueOfLabel(masterFormField.getType()));

                formService.getOrDefault(fieldType.orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                AppMessages.INVALID_FORM_FIELD)).service,
                        null)
                        .validateForm(formValue.get(), masterFormField, errors, errorArgs);
            } else {
                errors.put(formId, AppMessages.FORM_FIELD_NOT_FOUND);
            }
        }

        if (!errors.isEmpty()) {
            throw new FieldException(AppMessages.FORM_VALIDATION_FAILED, errors, errorArgs);
        }

        log.info("Render Fields : {}", renderFields);
        return renderFields;
    }

    @Override
    public Map<String, Object> formValidateMulti(FormRequest formRequest, String org, AccessLevelType accessLevelType) {
        log.info("Validate forms - {}", formRequest.getFormName());
        FormDTO masterForm = getFormByName(formRequest.getFormName(), accessLevelType);

        // Check if formFieldList is null or empty
        List<Map<String, Object>> formFieldList = Optional.ofNullable(formRequest.getFormFieldList())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FORM_INVALID));

        Map<String, Object> renderFields = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        Map<String, String[]> errorArgs = new HashMap<>();

        // Extract field IDs from the master form
        Map<String, FormFieldsDTO> masterFieldIds = masterForm.getFormFields().stream()
                .collect(Collectors.toMap(FormFieldsDTO::getFieldId, Function.identity()));

        // Validate only those fields that are present in the master form
        for (Map.Entry<String, FormFieldsDTO> entry : masterFieldIds.entrySet()) {
            String formId = entry.getKey(); // spoc_name
            FormFieldsDTO masterFormField = entry.getValue();
            log.info("master form field {} ", masterFormField);

            // Now, iterate over the formFieldList which is a List of Maps
            boolean fieldFound = false;
            for (Map<String, Object> fieldMap : formFieldList) {
                if (fieldMap.containsKey(formId)) {
                    fieldFound = true;
                    Object formValue = fieldMap.get(formId);
                    log.info("form value {} ", formValue);
                    renderFields.put(formId, formValue);
                    formService.getOrDefault(FormFieldType.valueOfLabel(masterFormField.getType()).service, null)
                            .validateForm(formValue, masterFormField, errors, errorArgs);
                }
            }

            if (!fieldFound) {
                errors.put(formId, AppMessages.FORM_FIELD_NOT_FOUND);
            }
        }

        if (!errors.isEmpty()) {
            throw new FieldException(AppMessages.FORM_VALIDATION_FAILED, errors, errorArgs);
        }

        log.info("Render Fields : {}", renderFields);
        return renderFields;
    }

    @Override
    public List<OptionsResponseDTO> fetchField(String name, String value, String collectionName, String filter) {
        log.info("Fetching options from - {}", collectionName);
        return optionsRepository.getOptions(name, value, collectionName, filter, mongoTemplate);
    }

    @Override
    public void saveForm(FormDTO formRequestDTO, String org) {
        log.info("Save forms - {}", formRequestDTO.getFormName());
        log.info("Form for Org - {}", org);

        masterFormRepository.saveForms(mapToFormFields(formRequestDTO, org), mongoTemplate, org);
    }

    @Override
    public void updateForms(FormDTO formRequestDTO, String formId, String org) {
        log.info("Update forms - {}", formRequestDTO.getFormName());

        masterFormRepository.updateForms(mapToFormFields(formRequestDTO, org), mongoTemplate, formId, org);
    }

    @Override
    public List<FormBuilderFields> getBuilderFormByOrgAndId(String org, String formId) {
        log.info("Get builder forms - {}", formId);
        MasterForm form = masterFormRepository.findById(formId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
        return formMapper.toFormBuilderFields(form.getFormFields());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, Object> mapToFormFields(FormDTO formRequestDTO, String org) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> formMap = objectMapper.convertValue(formRequestDTO,
                new TypeReference<Map<String, Object>>() {
                });
        List<Map<String, Object>> formFields = (List<Map<String, Object>>) formMap.get("formFields");
        Map<String, Object> initialValueMap = new HashMap<>();

        for (Map<String, Object> field : formFields) {
            Optional<String> optionsReference = Optional.ofNullable((String) field.get("optionsReferenceId"));
            Optional<Object> initialValue = Optional.ofNullable(field.get("initialValue"));
            initialValueMap.put((String) field.get("fieldId"), initialValue.isPresent() ? initialValue.get() : "");

            if (optionsReference.isPresent()) {
                field.put("optionsReference", new com.mongodb.DBRef(
                        REF_COLLECTION_NAME + org, new ObjectId(optionsReference.get())));
                field.remove("optionsReferenceId");
            } else {
                field.put("optionsReference", null);
            }

            String type = (String) field.get("type");
            if (type.equals(FormFieldType.FILE.label)) {
                Map<String, Object> validationDTO = (Map<String, Object>) field.get("validation");
                validationDTO.put("fileType",
                        FileExtUtils.getHumanReadableFormatsByExt((List) validationDTO.get("fileType")));
            }
        }

        formMap.put("initialValue", initialValueMap);
        return formMap;
    }

    @Override
    public void initIndexingFormsForOrganisation(String organisation) {
        log.info("Initialize indexing for org {}", organisation);
        masterFormRepository.initMasterForms(organisation, mongoTemplate);
    }

}