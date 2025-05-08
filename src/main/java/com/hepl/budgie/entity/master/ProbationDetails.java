package com.hepl.budgie.entity.master;

import lombok.Data;

import java.util.List;

@Data
public class ProbationDetails {
    private Boolean isProbationRequired;
    private Integer defaultDurationMonths;
    private List<String> extensionOptionsMonths;
}
