package com.hepl.budgie.entity.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProbationDetail {
    private Integer defaultDurationMonth;
    private List<String> extendedOptionMonth;
}
