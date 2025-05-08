package com.hepl.budgie.dto.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.entity.InputType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ValidationDTO {

    private String pattern;

    @ValueOfEnum(enumClass = InputType.class, message = "{validation.error.invalid}")
    private String inputType;
    private Long maxFileSize; // in bytes

    private List<String> fileType;

    private String folderName;
    private Integer maxFiles;
    private Integer maxLength;
    private Integer minLength;
    private Boolean disabledPast;
    private Boolean disabledFuture;
    private Integer ageVerification;

    private String disabledSpecificDates;
    private String disabledDateRangeStart;
    private String disabledDateRangeEnd;

    private List<String> views;
    private String format;

}
