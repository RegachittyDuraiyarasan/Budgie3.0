package com.hepl.budgie.entity.master;

import com.hepl.budgie.entity.workflow.InvokeMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Actions {

    private String action;
    private Integer sequence;
    private Trigger triggers;
    private String status;
    private Boolean isCriteriaSequence;
    private InvokeMethod invokeMethod;
}
