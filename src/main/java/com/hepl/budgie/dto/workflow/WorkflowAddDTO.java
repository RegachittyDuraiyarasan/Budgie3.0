package com.hepl.budgie.dto.workflow;

import com.hepl.budgie.entity.master.Actions;
import com.hepl.budgie.entity.workflow.EmployeeDetails;
import lombok.Data;

import java.util.List;

@Data
public class WorkflowAddDTO {
    private EmployeeDetails empDetails;
    private List<String> role;
    private Integer sequence;
    private List<Actions> actions;
    private String status;
}
