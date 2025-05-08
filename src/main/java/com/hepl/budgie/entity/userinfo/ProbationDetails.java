package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProbationDetails {
    private boolean isProbation;
    private String probationStatus;
    private Integer initialDurationMonths;
    private ZonedDateTime probationStartDate;
    private ZonedDateTime probationEndDate;
    private String results;
    private String extendedMonths;
    private List<Integer> extensionOptionsMonths;

}
