package com.hepl.budgie.entity.workflow;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum ConditionCheck {
    EQ,
    GT,
    LT,
    GTE,
    LTE;

    public static ConditionCheck fromString(String condition) {
        try {
            return ConditionCheck.valueOf(condition.toUpperCase());
        } catch (ResponseStatusException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unsupported condition: " + condition);
        }
    }
}
