package com.hepl.budgie.entity.workflow;

import com.hepl.budgie.entity.master.Actions;
import lombok.Data;

import java.util.List;

@Data
public class WorkFlow {
    private EmployeeDetails empDetails;
    private List<String> role;
    private Integer sequence;
    private ShowOn showOn;
    private List<Actions> actions;
    private String status;
    private String roleSpecific;
    private List<Criteria> criteria;
    private Integer shownOnDaysBefore;
}
