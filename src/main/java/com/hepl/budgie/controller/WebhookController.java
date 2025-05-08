package com.hepl.budgie.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.dto.GenericResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Manage webhooks", description = "")
@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    @PostMapping()
    public GenericResponse<String> listenWebhook(@RequestBody Map<String, Object> data) {
        log.info("Listening to callback");

        return GenericResponse.success("");
    }

}
