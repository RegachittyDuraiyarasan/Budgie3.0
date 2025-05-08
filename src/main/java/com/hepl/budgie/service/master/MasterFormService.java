package com.hepl.budgie.service.master;

import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.dto.formbuilder.FormBuilderDTO;
import com.hepl.budgie.dto.formbuilder.FormBuilderFields;
import com.hepl.budgie.entity.master.MasterForm;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormDTO;

import java.util.List;
import java.util.Map;

public interface MasterFormService {

    FormDTO getFormByName(String formName, AccessLevelType accessLevelType);

    List<MasterForm> allForms(String org);

    Map<String, FormFieldsDTO> getFormFields(String formName, String org, AccessLevelType accessLevelType);

    Map<String, Object> formValidate(FormRequest formRequest, String org, AccessLevelType accessLevelType,
            Map<String, FormFieldsDTO> masterFieldIds);

    Map<String, Object> formValidateMulti(FormRequest formRequest, String org, AccessLevelType accessLevelType);

    void updateFormFields(FormFieldsDTO fields, String formId, String org);

    List<OptionsResponseDTO> fetchField(String name, String value, String collectionName, String filter);

    void saveForm(FormDTO formRequestDTO, String org);

    void updateForms(FormDTO formRequestDTO, String formId, String org);

    void deleteForm(String formId, String org);

    void deleteFormByFieldId(String formId, String fieldId, String org);

    void initIndexingFormsForOrganisation(String organisation);

    void buildForm(FormBuilderDTO formBuilderDTO, String org);

    void updateForm(FormBuilderDTO formBuilderDTO, String org, String formId);

    List<FormBuilderFields> getBuilderFormByOrgAndId(String org, String formId);

}
