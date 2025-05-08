package com.hepl.budgie.entity.leavemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.entity.workflow.TaskHistory;
import com.hepl.budgie.entity.workflow.WorkFlow;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document()
public class LeaveApply {
    @Id
    private String id;
    private String leaveCode;
    private String empId;
    private String appliedTo;
    private String leaveType;
    private String leaveCategory;
    private List<WorkFlow> workFlow;
    private List<TaskHistory> taskHistory;
    private String finalStatus;
    private String role;
    private String status;
    private ZonedDateTime relievingDate;
    private Integer noticePeriodDays;

}
