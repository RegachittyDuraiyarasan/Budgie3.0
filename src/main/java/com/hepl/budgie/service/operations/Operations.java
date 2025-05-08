package com.hepl.budgie.service.operations;

public interface Operations {
    Object invokeMethod(Object target, String methodName, Object... args);
}
