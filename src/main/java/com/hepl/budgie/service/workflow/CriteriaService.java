package com.hepl.budgie.service.workflow;

public interface CriteriaService {
    boolean checkCondition(String condition, String actualValue, String expectedValue) ;
}
