package com.hepl.budgie.service.impl.workflow;

import com.hepl.budgie.entity.workflow.ConditionCheck;
import com.hepl.budgie.service.workflow.CriteriaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CriteriaServiceImplementation implements CriteriaService {
    @Override
    public boolean checkCondition(String condition, String actualValue, String expectedValue) {
        ConditionCheck conditionType = ConditionCheck.fromString(condition);

        return switch (conditionType) {
            case EQ ->
                    actualValue.equals(expectedValue);
            case GT ->
                    compareAsDouble(actualValue, expectedValue) > 0;
            case LT ->
                    compareAsDouble(actualValue, expectedValue) < 0;
            case GTE ->
                    compareAsDouble(actualValue, expectedValue) >= 0;
            case LTE ->
                    compareAsDouble(actualValue, expectedValue) <= 0;

        };
    }

    private double compareAsDouble(String actualValue, String expectedValue) {
        try {
            return Double.compare(Double.parseDouble(actualValue), Double.parseDouble(expectedValue));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Values must be numeric for comparison: " + actualValue + ", " + expectedValue);
        }
    }
}
