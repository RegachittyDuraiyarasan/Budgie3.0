package com.hepl.budgie.service.scheduler;

import java.util.Map;

public interface JobAction {
    void execute(String jobId, Map<String, Object> parameters);
}
