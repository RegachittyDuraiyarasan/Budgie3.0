package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProbationSettings {
    private boolean isProbationRequired;
    private Integer defaultDurationMonths;
    private List<Integer> extensionOptionsMonths;
}
