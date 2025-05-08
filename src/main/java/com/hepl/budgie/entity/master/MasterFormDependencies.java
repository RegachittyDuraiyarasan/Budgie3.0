package com.hepl.budgie.entity.master;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.config.annotation.ValueOfEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MasterFormDependencies {

    private String fieldId;
    private Boolean disabledPast;
    private Boolean disabledFuture;
    private String expectedValue;
    private Boolean disabled;
    private Boolean show;

    @ValueOfEnum(enumClass = DependencyCondType.class, message = "{validation.error.invalid}")
    private String condType;
    private String param;
    private String optionFieldId;
    private String matchOptionFieldId;
    private String value;
    private Boolean setValue;

}
