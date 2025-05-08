package com.hepl.budgie.service.impl.workflow;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.entity.master.Actions;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.workflow.Criteria;
import com.hepl.budgie.entity.workflow.EmployeeDetails;
import com.hepl.budgie.entity.workflow.TaskHistory;
import com.hepl.budgie.entity.workflow.WorkFlow;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.workflow.CriteriaService;
import com.hepl.budgie.service.workflow.UnseenRoleTypeService;
import com.hepl.budgie.service.workflow.WorkflowService;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class WorkflowServiceImplementation implements WorkflowService {
    private final UserInfoRepository userInfoRepository;
    private final Translator translator;
    private final CriteriaService criteriaService;
    private final UnseenRoleTypeService unseenRoleTypeService;

    public WorkflowServiceImplementation(UserInfoRepository userInfoRepository, Translator translator, CriteriaService criteriaService, UnseenRoleTypeService unseenRoleTypeService) {
        this.userInfoRepository = userInfoRepository;
        this.translator = translator;
        this.criteriaService = criteriaService;
        this.unseenRoleTypeService = unseenRoleTypeService;
    }
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<WorkFlow> initializeWorkflow(String empId, List<WorkFlow> workflows, String requestRole,
                                             String status, Map<String, Object> formFields) {
        WorkFlow workflow = workflows.stream()
                .filter(workFlow -> workFlow.getRole() != null && workFlow.getRole().contains(requestRole)) // Check if the role matches
                .filter(wf -> wf.getActions() != null && wf.getActions().stream()
                        .anyMatch(action -> action.getAction().equalsIgnoreCase(status))) // Check if an action matches the status
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,translator.toLocale(AppMessages.WORKFLOW_NOT_FOUND)));

        // Get sequence order based on criteria for the first workflow
        List<Integer> sequenceOrder = getSequenceOrder(workflow, formFields, workflows);

        // Filter and sort workflows based on sequenceOrder
        List<WorkFlow> filteredWorkflows = workflows.stream()
                .filter(wf -> sequenceOrder.contains(wf.getSequence())) // Only process workflows in the sequence order
                .sorted(Comparator.comparingInt(wf -> sequenceOrder.indexOf(wf.getSequence())))
                .collect(Collectors.toList());

        // Default fallback: Process all workflows in natural order if no criteria are present
        if (filteredWorkflows.isEmpty()) {
            filteredWorkflows = workflows.stream()
                    .sorted(Comparator.comparingInt(WorkFlow::getSequence))
                    .collect(Collectors.toList());
        }

        // Process workflows
        AtomicReference<WorkFlow> previousWorkflowRef = new AtomicReference<>();
        filteredWorkflows.forEach(wf -> {
            WorkFlow processedWorkflow = assembleWorkFlow(empId, wf, status, formFields);
            WorkFlow previousWorkflow = previousWorkflowRef.get();

            // Handle status transitions based on the previous workflow
            if (previousWorkflow != null && "Closed".equalsIgnoreCase(previousWorkflow.getStatus())) {
                processedWorkflow.setStatus("Open");
            }

            previousWorkflowRef.set(processedWorkflow);
        });

        log.info("Final workflow list: {}", filteredWorkflows);
        return filteredWorkflows;
    }

    private WorkFlow assembleWorkFlow(String empId, WorkFlow workflow, String status, Map<String, Object> formFields) {
        String managerId = null;
        List<String> role = workflow.getRole();

        // Determine reporting manager or role based on hierarchy
        if (workflow.getRoleSpecific() != null && workflow.getRoleSpecific().contains("Hierarchy")) {
            Optional<UserInfo> reportingManagerUser = userInfoRepository.findByEmpId(empId);
            if (reportingManagerUser.isPresent()) {
                if (workflow.getRole().contains("Reporting Manager")) {
                    managerId = reportingManagerUser.get().getSections()
                            .getHrInformation().getPrimary().getManagerId();
                } else if (workflow.getRole().contains("Reviewer")) {
                    managerId = reportingManagerUser.get().getSections()
                            .getHrInformation().getReviewer().getManagerId();
                }
            }
        }

        // Create EmployeeDetails object and set empId and roleType
        EmployeeDetails empDetails = new EmployeeDetails();
        if (workflow.getRole() != null && workflow.getRole().contains("Employee")) {
            empDetails.setEmpId(empId);
            empDetails.setRoleType(workflow.getRole());
        } else if (managerId != null) {
            empDetails.setEmpId(managerId);
            empDetails.setRoleType(workflow.getRole());
        } else {
            empDetails.setRoleType(role);
        }

        workflow.setEmpDetails(empDetails);

        // Handle status for current sequence
        String updatedStatus = "Pending";
        if (workflow.getSequence() == 1) {
            Actions actionsList = workflow.getActions().stream()
                    .filter(al -> al.getAction().equalsIgnoreCase(status))
                    .findFirst().orElse(null);
            if (actionsList != null) {
                updatedStatus = actionsList.getSequence() == 0 ? "Open" : "Closed";
            }
        }
        workflow.setStatus(updatedStatus);

        return workflow;
    }

    private List<Integer> getSequenceOrder(WorkFlow workflow, Map<String, Object> formFields, List<WorkFlow> allWorkflows) {
        List<Integer> sequenceOrder = new ArrayList<>();

        int hittingSequence = workflow.getSequence(); // Initial sequence
        sequenceOrder.add(hittingSequence);

        Integer nextSequence = null;

        // Check criteria for determining next sequence
        if (workflow.getCriteria() != null && !workflow.getCriteria().isEmpty()) {
            for (Criteria criteria : workflow.getCriteria()) {
                String fieldId = criteria.getFieldId();
                String expectedValue = criteria.getValue();
                String condition = criteria.getCondition();

                if (fieldId != null && expectedValue != null && condition != null) {
                    Object value = formFields.get(fieldId);

                    if ("roleType".equalsIgnoreCase(criteria.getType())) {
                        log.info("roleType is present here");
                        String matchableFieldValue = formFields.get(fieldId).toString();
                        String empId = formFields.get("empId").toString();
                        value = unseenRoleTypeService.getReporterDetail(empId, matchableFieldValue);
                    }

                    log.info("Evaluating condition for field: {}", fieldId);

                    // Determine next sequence based on criteria match
                    if (value != null && criteriaService.checkCondition(condition, value.toString(), expectedValue)) {
                        log.info("Condition met, selecting trueSequence: {}", criteria.getTrueSequence());
                        nextSequence = criteria.getTrueSequence();
                    } else {
                        log.info("Condition not met, selecting falseSequence: {}", criteria.getFalseSequence());
                        nextSequence = criteria.getFalseSequence();
                    }
                }
            }
        }

        // Add determined next sequence if it's valid
        if (nextSequence != null && nextSequence > hittingSequence) {
            sequenceOrder.add(nextSequence);
        }

        // **Update action sequences based on selected sequence**
        updateActionSequences(workflow, nextSequence != null ? nextSequence : hittingSequence);

        // Append remaining sequences in natural order
        int finalNextSequence = (nextSequence != null) ? nextSequence : hittingSequence;
        allWorkflows.stream()
                .map(WorkFlow::getSequence)
                .sorted()
                .filter(seq -> !sequenceOrder.contains(seq)) // Avoid duplicates
                .filter(seq -> seq > finalNextSequence) // Ensure correct order
                .forEach(sequenceOrder::add);

        log.info("Final calculated sequence order: {}", sequenceOrder);
        return sequenceOrder;
    }

    private void updateActionSequences(WorkFlow workflow, int selectedSequence) {
        if (workflow.getActions() != null) {
            workflow.getActions().forEach(action -> {
                if (Boolean.TRUE.equals(action.getIsCriteriaSequence())) { // Check if action needs criteria sequence
                    action.setSequence(selectedSequence);
                    log.info("Updated action '{}' with sequence '{}'", action.getAction(), selectedSequence);
                }
            });
        }
    }

    @Override
    public List<TaskHistory> updateTaskHistory(String role, String action, List<TaskHistory> existingTaskHistories) {
        if (existingTaskHistories == null) {
            existingTaskHistories = new ArrayList<>();
        }
        TaskHistory newTaskHistory = new TaskHistory();
        newTaskHistory.setRole(role);
        newTaskHistory.setStatus(action);
        existingTaskHistories.add(newTaskHistory);
        return existingTaskHistories;
    }

    @Override
    public void changeStatusBySequence(List<WorkFlow> workflows, String targetAction) {
        if (workflows == null || workflows.isEmpty()) {
            return;
        }

        workflows.stream()
                .filter(workflow -> workflow.getActions() != null && workflow.getActions().stream()
                        .anyMatch(action -> action.getAction().equalsIgnoreCase(targetAction)))
                .findFirst()
                .ifPresent(matchedWorkflow -> {
                    if ("reject".equalsIgnoreCase(targetAction)) {
                        matchedWorkflow.getActions().stream()
                                .filter(action -> action.getAction().equalsIgnoreCase(targetAction))
                                .findFirst()
                                .ifPresent(matchedAction -> {
                                    Integer rejectSequence = matchedAction.getSequence();

                                    if (rejectSequence != null) {
                                        workflows.stream()
                                                .filter(workflow -> workflow.getSequence() != null && workflow.getSequence().equals(rejectSequence))
                                                .findFirst()
                                                .ifPresent(workflow -> workflow.setStatus("Open"));

                                        Integer nextSequence = rejectSequence + 1;
                                        workflows.stream()
                                                .filter(workflow -> workflow.getSequence() != null && workflow.getSequence().equals(nextSequence))
                                                .findFirst()
                                                .ifPresent(nextWorkflow -> nextWorkflow.setStatus("Pending"));
                                    }
                                });
                    } else {
                        matchedWorkflow.setStatus("Closed");
                        Integer nextSequence = matchedWorkflow.getSequence() + 1;

                        workflows.stream()
                                .filter(wf -> wf.getSequence() != null && wf.getSequence().equals(nextSequence))
                                .findFirst()
                                .ifPresent(nextWorkflow -> nextWorkflow.setStatus("Open"));
                    }
                });
    }

    @Override
    public String addFinalStatus(List<WorkFlow> workflows, String targetAction, String role) {
        return workflows.stream()
                .filter(wf -> (wf.getRole() != null && wf.getRole().contains(role)) ||
                        (wf.getEmpDetails() != null && wf.getEmpDetails().getRoleType() != null
                                && wf.getEmpDetails().getRoleType().contains(role)))
                .flatMap(wf -> wf.getActions() != null ? wf.getActions().stream() : Stream.empty())
                .filter(action -> targetAction.equals(action.getAction()))
                .findFirst()
                .map(Actions::getStatus)
                .orElse(null);
    }
    @Override
    public void validateShownDays(Integer shownOnDaysBefore, LocalDate givenDate) {
        if (shownOnDaysBefore == null || givenDate == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Invalid input: 'shownOnDaysBefore' or 'givenDate' cannot be null.");
        }

        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = givenDate.minusDays(shownOnDaysBefore);

        if (currentDate.isAfter(givenDate) || currentDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("This action is not open for this date. Current date: %s, valid range: %s to %s",
                            currentDate, startDate, givenDate));
        }
    }
    @Override
    public boolean isMatchingDate(String type, int from, int to, String fieldId, Map<String, Object> data, ZoneId zoneId) {
        if (!"days".equalsIgnoreCase(type) || !data.containsKey(fieldId)) {
            return false;
        }

        String relievingDateStr = data.get(fieldId).toString();
        ZonedDateTime relievingDate = ZonedDateTime.of(LocalDate.parse(relievingDateStr, DATE_FORMAT).atStartOfDay(), zoneId);

        ZonedDateTime fromDateTime = relievingDate.withDayOfMonth(from);

        ZonedDateTime toDateTime = (to == 0) ? fromDateTime : relievingDate.withDayOfMonth(to);

        return (relievingDate.isEqual(fromDateTime) || relievingDate.isEqual(toDateTime))
                || (relievingDate.isAfter(fromDateTime) && relievingDate.isBefore(toDateTime));
    }



}