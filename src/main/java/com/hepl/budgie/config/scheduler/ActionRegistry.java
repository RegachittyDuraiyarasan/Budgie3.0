package com.hepl.budgie.config.scheduler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hepl.budgie.service.scheduler.JobAction;

@Component
public class ActionRegistry {

    private final Map<String, JobAction> actions;

    @Autowired
    public ActionRegistry(List<JobAction> jobActions) {
        this.actions = jobActions.stream().collect(Collectors.toMap(
                action -> action.getClass().getAnnotation(Component.class).value(),
                Function.identity()));
    }

    public JobAction getAction(String actionName) {
        return actions.get(actionName);
    }

}
