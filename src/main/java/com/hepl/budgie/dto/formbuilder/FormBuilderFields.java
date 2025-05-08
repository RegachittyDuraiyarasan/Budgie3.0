package com.hepl.budgie.dto.formbuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.dto.form.FieldAccessLevel;
import com.hepl.budgie.dto.form.ValidationDTO;
import com.hepl.budgie.entity.master.MasterFormDependencies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormBuilderFields {

    private String label;
    private String name;
    private String placeholder;
    private String type;
    private Styles styles;
    private List<FieldAccessLevel> accessLevel;
    private ValidationDTO validations;
    private List<MasterFormDependencies> dependencies;
    private Advanced advanced;

}
