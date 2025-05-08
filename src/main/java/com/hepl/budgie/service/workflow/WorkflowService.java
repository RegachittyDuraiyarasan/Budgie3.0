package com.hepl.budgie.service.workflow;

import com.hepl.budgie.entity.workflow.TaskHistory;
import com.hepl.budgie.entity.workflow.WorkFlow;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public interface WorkflowService {

    List<TaskHistory> updateTaskHistory(String role, String status, List<TaskHistory> existingHierarchyLevels);

    void changeStatusBySequence(List<WorkFlow> workflows, String targetAction);

    String addFinalStatus(List<WorkFlow> workflows, String targetAction, String role);

    List<WorkFlow> initializeWorkflow(String empId, List<WorkFlow> workflow, String requestRole, String status, Map<String, Object> formFields);

    void validateShownDays(Integer shownOnDaysBefore, LocalDate givenDate);

    boolean isMatchingDate(String type, int fromDate, int toDate, String fieldId, Map<String, Object> data, ZoneId zoneId);
}
