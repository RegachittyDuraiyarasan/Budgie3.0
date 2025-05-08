package com.hepl.budgie.entity.menu;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = Include.NON_NULL)
public class CommonMenuFields {
    private String name;
    private String path;
    private String icon;
    private String condition;
    private String status;
    private Boolean hasSubmenu;
    private List<String> permissions;
    @Builder.Default
    private List<Condition> conditionList = new ArrayList<>();

}
