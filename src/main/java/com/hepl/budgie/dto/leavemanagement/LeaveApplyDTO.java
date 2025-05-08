package com.hepl.budgie.dto.leavemanagement;

import lombok.Data;

import java.util.List;

import com.hepl.budgie.entity.workflow.TaskHistory;
import com.hepl.budgie.entity.workflow.WorkFlow;

@Data
public class LeaveApplyDTO {
    private String leaveCode;
    private String empId;
    private String appliedTo;
    private String leaveType;
    private String leaveCategory;
    private String role;
    private String status;
    private String finalStatus;
    private List<WorkFlow> workFlow;
    private List<TaskHistory> taskHistory;

}
