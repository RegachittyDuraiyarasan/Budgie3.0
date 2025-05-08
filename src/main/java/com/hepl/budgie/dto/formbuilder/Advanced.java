package com.hepl.budgie.dto.formbuilder;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Advanced {

    private String options;
    private String optionsReferenceLink;
    private String optionsReferenceId;

    private Boolean multiple;

    private String dateFormat;
    private List<String> views;

}
