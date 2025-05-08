package com.hepl.budgie.entity.workflow;

import lombok.Data;

@Data
public class Criteria {
    private String fieldId;
    private String condition;
    private String value;
    private String type;
    private Integer trueSequence;
    private Integer falseSequence;
}
