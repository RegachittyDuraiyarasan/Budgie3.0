package com.hepl.budgie.dto.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attribute {

    private String timeFormat;
    private String startFrom;
    private String endOn;
    private String format;
    private List<String> views;
    private Boolean disableFuture;
    private Boolean disablePast;
    private String disableSpecificDates;

    private String color;
    private String size;
    private String justifyContent;

}
