package com.hepl.budgie.dto.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormRequest {

    @JsonView({ FormView.Single.class, FormView.Multi.class })
    @NotBlank(message = "{validation.form.formTypeCannotBeEmpty}")
    private String formName;

    @JsonView(FormView.Single.class)
    private Map<String, Object> formFields;

    @JsonView(FormView.Multi.class)
    private List<Map<String, Object>> formFieldList;

    @JsonView({ FormView.Single.class, FormView.Multi.class })
    private String tempId;
    private Object lockAttendanceDate;

}
