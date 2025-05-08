package com.hepl.budgie.dto.formbuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormBuilderDTO {

    @NotBlank(message = "{validation.error.notBlank}")
    private String formName;
    @NotEmpty(message = "{validation.error.notBeEmpty}")
    private List<@Valid FormBuilderFields> fields;

}
