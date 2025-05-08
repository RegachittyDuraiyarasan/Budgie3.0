package com.hepl.budgie.service.impl.leavemanagement;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.form.FormDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.workflow.WorkflowAddDTO;
import com.hepl.budgie.entity.leavemanagement.LeaveApply;
import com.hepl.budgie.entity.master.Actions;
import com.hepl.budgie.entity.workflow.*;
import com.hepl.budgie.mapper.leavemanagement.LeaveApplyMapper;
import com.hepl.budgie.mapper.workflow.WorkflowAddMapper;
import com.hepl.budgie.repository.leavemanagement.LeaveApplyRepository;
import com.hepl.budgie.repository.master.MasterFormRepository;
import com.hepl.budgie.service.leavemanagement.LeaveApplyService;
import com.hepl.budgie.service.operations.Operations;
import com.hepl.budgie.service.workflow.WorkflowService;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaveApplyServiceImplementation implements LeaveApplyService {

    private final LeaveApplyRepository leaveApplyRepository;
    private final LeaveApplyMapper leaveApplyMapper;
    private final MasterFormRepository masterFormRepository;
    private final MongoTemplate mongoTemplate;
    private final WorkflowService workflowService;
    private final Translator translator;
    private final WorkflowAddMapper workflowMapper;
    private final Operations operations;

    public LeaveApplyServiceImplementation(LeaveApplyRepository leaveApplyRepository, LeaveApplyMapper leaveApplyMapper,
            MasterFormRepository masterFormRepository, MongoTemplate mongoTemplate, WorkflowService workflowService,
            Translator translator, WorkflowAddMapper workflowMapper, Operations operations) {
        this.leaveApplyRepository = leaveApplyRepository;
        this.leaveApplyMapper = leaveApplyMapper;
        this.masterFormRepository = masterFormRepository;
        this.mongoTemplate = mongoTemplate;
        this.workflowService = workflowService;
        this.translator = translator;
        this.workflowMapper = workflowMapper;
        this.operations = operations;
    }

    @Override
    public void add(FormRequest request) {
        AggregationResults<FormDTO> formResults = masterFormRepository.fetchByFormName("Leave Apply", "HR", "Add",
                mongoTemplate, "");
        log.info("Mapped result --{}", formResults.getMappedResults());
        LeaveApply leaveApply = leaveApplyMapper.toEntity(request.getFormFields());

        if (!formResults.getMappedResults().isEmpty()) {
            FormDTO form = formResults.getMappedResults().get(0);

            if (form.getWorkflow() != null && !form.getWorkflow().isEmpty()) {
                String requestRole = leaveApply.getRole();
                List<WorkFlow> workflows = workflowService.initializeWorkflow(leaveApply.getEmpId(), form.getWorkflow(),
                        requestRole, leaveApply.getStatus(), request.getFormFields());
                List<TaskHistory> taskHistories = workflowService.updateTaskHistory(leaveApply.getRole(),
                        leaveApply.getStatus(), null);
                leaveApply.setTaskHistory(taskHistories);

                String finalStatus = workflowService.addFinalStatus(form.getWorkflow(), leaveApply.getStatus(),
                        requestRole);
                leaveApply.setFinalStatus(String.valueOf(finalStatus));

                if (leaveApply.getWorkFlow() == null || !(leaveApply.getWorkFlow() instanceof ArrayList)) {
                    leaveApply.setWorkFlow(new ArrayList<>());
                }

                // Convert WorkFlow -> WorkflowAddDTO -> WorkFlow
                List<WorkflowAddDTO> workflowAddDTOs = workflowMapper.toDTOList(workflows);
                List<WorkFlow> updatedWorkFlows = workflowMapper.toEntityList(workflowAddDTOs);

                log.info("Adding workflows: {}", updatedWorkFlows);
                leaveApply.getWorkFlow().addAll(updatedWorkFlows);
            }
        }

        leaveApplyRepository.save(leaveApply);
    }

    @Override
    public void update(FormRequest request) {
        // Validate and fetch the leave code
        String leaveCode = String.valueOf(request.getFormFields().get("leaveCode"));
        LeaveApply existLeaveApply = leaveApplyRepository.findByLeaveCode(leaveCode);

        if (existLeaveApply == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    AppMessages.LEAVE_CODE_NOT_EXIST);
        }

        // Initialize TaskHistory if null
        List<TaskHistory> existingTaskHistories = Optional.ofNullable(existLeaveApply.getTaskHistory())
                .orElseGet(() -> {
                    List<TaskHistory> newTaskHistories = new ArrayList<>();
                    existLeaveApply.setTaskHistory(newTaskHistories);
                    return newTaskHistories;
                });

        // Extract role and status from request
        String role = request.getFormFields().get("role").toString();
        String status = request.getFormFields().get("status").toString();

        // Update Task History
        List<TaskHistory> updatedTaskHistory = workflowService.updateTaskHistory(
                role,
                status,
                existingTaskHistories);

        log.info("Processing leave application: {}", existLeaveApply);

        // Get the matching InvokeMethod based on role and action status
        Optional<InvokeMethod> optionalInvokeMethod = existLeaveApply.getWorkFlow().stream()
                .filter(workflow -> workflow.getRole() != null && workflow.getRole().contains(role))
                .flatMap(workflow -> workflow.getActions().stream())
                .filter(action -> action.getAction() != null && action.getAction().equals(status))
                .map(Actions::getInvokeMethod)
                .filter(Objects::nonNull)
                .findFirst();

        if (optionalInvokeMethod.isPresent()) {
            InvokeMethod invokeMethod = optionalInvokeMethod.get();
            String methodName = invokeMethod.getFunction();
            List<String> argumentIds = invokeMethod.getArgumentId();
            String updateToId = invokeMethod.getUpdateToId();

            if (methodName == null || methodName.isEmpty()) {
                log.warn("InvokeMethod function is empty for role: {} and status: {}", role, status);
                throw new CustomResponseStatusException(AppMessages.FUNCTION_EMPTY, HttpStatus.NOT_FOUND,
                        new Object[] { role, status });

            } else if (isMethodPresent(operations, methodName)) {
                try {
                    log.info("Invoking method: {}", methodName);

                    // Prepare method arguments
                    Map<String, Object> arguments = new HashMap<>();
                    arguments.put("workflow", existLeaveApply.getWorkFlow());
                    arguments.put("role", role);

                    for (String argumentId : argumentIds) {
                        Object argumentValue = request.getFormFields().get(argumentId);
                        if (argumentValue == null) {
                            throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    AppMessages.ARGUEMENT_ID_REQUIRED + argumentId);
                        }
                        arguments.put(argumentId, argumentValue);
                    }

                    // invoke the method
                    Object calculatedValue = operations.invokeMethod(operations, methodName, arguments);

                    // Dynamically update the field using updateToId
                    if (updateToId != null && !updateToId.isEmpty()) {
                        updateFieldDynamically(existLeaveApply, updateToId, calculatedValue);
                    }

                } catch (Exception e) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            AppMessages.ERROR_PROCESS_ACTION
                    );
                }
            } else {
                throw new CustomResponseStatusException(AppMessages.MATCHING_METHOD_NOT_FOUND, HttpStatus.NOT_FOUND,
                        new Object[] { role });
            }
        } else {
            throw new CustomResponseStatusException(AppMessages.INVOKE_METHOD_NOT_FOUND, HttpStatus.NOT_FOUND,
                    new Object[] { role });
        }

        // Update workflow status
        workflowService.changeStatusBySequence(existLeaveApply.getWorkFlow(), status);
        String finalStatus = workflowService.addFinalStatus(existLeaveApply.getWorkFlow(), status, role);
        existLeaveApply.setFinalStatus(finalStatus);

        // Update task history and save
        existLeaveApply.setTaskHistory(updatedTaskHistory);
        leaveApplyRepository.save(existLeaveApply);

        log.info("Leave application updated successfully: {}", leaveCode);
    }

    @Override
    public List<LeaveApply> getFilteredLeaveApplications(String role) {
        log.info("Fetching all leave applications...");
        List<LeaveApply> allLeaves = leaveApplyRepository.findAll();
        log.info("Total leave applications found: {}", allLeaves.size());

        List<LeaveApply> filteredLeaves = allLeaves.stream()
                .filter(leave -> matchesDateCriteria(leave, role))
                .collect(Collectors.toList());

        log.info("Total leave applications after filtering for role '{}': {}", role, filteredLeaves.size());
        return filteredLeaves;
    }

    private boolean matchesDateCriteria(LeaveApply leave, String role) {
        log.debug("Checking leave ID: {} for role: {}", leave.getId(), role);

        for (WorkFlow workFlow : leave.getWorkFlow()) {
            log.debug("Checking workflow with role: {}", workFlow.getRole());

            if (workFlow.getRole() != null && workFlow.getRole().contains(role)) {
                ShowOn showOn = workFlow.getShowOn();
                if (showOn != null && "days".equalsIgnoreCase(showOn.getType())) {
                    int from = Integer.parseInt(showOn.getFrom());
                    int to = Integer.parseInt(showOn.getTo());
                    String fieldId = showOn.getFieldId();
                    ZoneId zone = ZoneId.systemDefault();

                    log.debug("Processing ShowOn criteria - Type: {}, From: {}, To: {}, FieldId: {}",
                            showOn.getType(), from, to, fieldId);

                    Map<String, Object> data = convertToMap(leave);

                    boolean isMatching = workflowService.isMatchingDate(showOn.getType(), from, to, fieldId, data,
                            zone);
                    log.debug("isMatchingDate result for leave ID {}: {}", leave.getId(), isMatching);

                    if (isMatching) {
                        log.info("Leave ID {} matches criteria for role: {}", leave.getId(), role);
                        return true;
                    }
                }
            }
        }
        log.debug("Leave ID {} does not match criteria for role: {}", leave.getId(), role);
        return false;
    }

    private Map<String, Object> convertToMap(LeaveApply leave) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("relievingDate", leave.getRelievingDate().toLocalDate().toString());
        return dataMap;
    }

    private void updateFieldDynamically(LeaveApply leaveApply, String fieldName, Object value) {
        try {
            Field field = LeaveApply.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(leaveApply, value);
            log.info("Updated field '{}' with value: {}", fieldName, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Error updating field '{}': {}", fieldName, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update field: " + fieldName);
        }
    }

    private boolean isMethodPresent(Object target, String methodName) {
        try {
            log.info("target{}", target.toString());
            log.info("methodName{}", methodName);
            Method[] methods = target.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking method presence: {}", e.getMessage(), e);
            return false;
        }
    }

}
