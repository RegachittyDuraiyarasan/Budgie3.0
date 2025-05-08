package com.hepl.budgie.service.impl.operations;

import com.hepl.budgie.entity.workflow.WorkFlow;
import com.hepl.budgie.service.operations.Operations;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class OperationsServiceImplementation implements Operations {

    public ZonedDateTime calculateDate(Map<String, Object> args) {
        // Filter the workflows based on the request role
        List<WorkFlow> workFlows = (List<WorkFlow>) args.get("workflow");
        Optional<WorkFlow> matchingWorkflow = workFlows.stream()
                .filter(workFlow -> workFlow.getRole() != null && workFlow.getRole().contains(args.get("role")))
                .findFirst();

        if (matchingWorkflow.isPresent()) {
            ZonedDateTime today = ZonedDateTime.now();
            return today.plusDays((Integer)args.get("noticePeriodDays"));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.WORKFLOW_NOT_FOUND_FOR_ROLE);
        }
    }

    @Override
    public Object invokeMethod(Object target, String methodName, Object... args) {

        try {
            // Get the method by name and parameter types
            Method method = target.getClass().getDeclaredMethod(methodName, Map.class);
            method.setAccessible(true);

            return method.invoke(target, args);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    AppMessages.INVOKE_METHOD_ERROR
            );
        }
    }


}
