package com.hepl.budgie.service.impl.scheduler;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.hepl.budgie.service.scheduler.JobAction;

import lombok.extern.slf4j.Slf4j;

@Component("sendEmail")
@Slf4j
public class SendEmailJobAction implements JobAction {

    @Override
    public void execute(String jobId, Map<String, Object> parameters) {
        log.info("Sending email...");
    }

}
