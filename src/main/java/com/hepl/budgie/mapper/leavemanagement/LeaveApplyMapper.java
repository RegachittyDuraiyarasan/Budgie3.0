package com.hepl.budgie.mapper.leavemanagement;

import com.hepl.budgie.dto.leavemanagement.LeaveApplyDTO;
import com.hepl.budgie.entity.leavemanagement.LeaveApply;

import com.hepl.budgie.entity.master.Actions;
import com.hepl.budgie.entity.workflow.Criteria;
import com.hepl.budgie.entity.workflow.EmployeeDetails;
import com.hepl.budgie.entity.workflow.TaskHistory;
import com.hepl.budgie.entity.workflow.WorkFlow;
import java.util.Collections;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeaveApplyMapper {

    @Mapping(target = "workFlow", expression = "java(mapWorkFlow((java.util.List<Map<String, Object>>) leaveApply.get(\"workFlow\")))")
    @Mapping(target = "taskHistory", expression = "java(mapTaskHistory((java.util.List<Map<String, Object>>) leaveApply.get(\"taskHistory\")))")
    LeaveApply toEntity(Map<String, Object> leaveApply);

    default List<WorkFlow> mapWorkFlow(List<Map<String, Object>> workflows) {
        if (workflows == null)
            return Collections.emptyList();
        return workflows.stream().map(workflowMap -> {
            WorkFlow workflow = new WorkFlow();
            workflow.setEmpDetails((EmployeeDetails) workflowMap.get("empDetails"));
            workflow.setRole((List<String>) workflowMap.get("role"));
            workflow.setSequence((Integer) workflowMap.get("sequence"));
            workflow.setActions((List<Actions>) workflowMap.get("actions"));
            workflow.setStatus((String) workflowMap.get("status"));
            workflow.setRoleSpecific((String) workflowMap.get("roleSpecific"));
            workflow.setCriteria((List<Criteria>) workflowMap.get("criteria"));
            return workflow;
        }).toList();
    }

    default List<TaskHistory> mapTaskHistory(List<Map<String, Object>> taskHistories) {
        if (taskHistories == null)
            return Collections.emptyList();
        return taskHistories.stream().map(taskMap -> {
            TaskHistory taskHistory = new TaskHistory();
            taskHistory.setRole((String) taskMap.get("role"));
            taskHistory.setStatus((String) taskMap.get("status"));
            return taskHistory;
        }).toList();
    }

    // Custom method to map Object to String
    default String map(Object value) {
        return value == null ? null : value.toString();
    }

    LeaveApplyDTO updateEntityFromDTO(LeaveApply entity);
}
