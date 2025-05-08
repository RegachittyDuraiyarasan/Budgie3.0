package com.hepl.budgie.controller;

import java.util.Optional;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.notification.ComposeMailDTO;
import com.hepl.budgie.dto.notification.Notification;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.repository.organization.OrganizationRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Test notifications", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/test/notification")
@Slf4j
public class NotificationController {

    private final StreamBridge streamBridge;
    private final OrganizationRepository organizationRepository;

    public NotificationController(StreamBridge streamBridge, OrganizationRepository organizationRepository) {
        this.streamBridge = streamBridge;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> getMethodName(@ModelAttribute ComposeMailDTO mailDto) {
        log.info("Mail topic");
        String orgId = mailDto.getOrganisationId();
        Optional<Organization> organization = organizationRepository.findByOrganizationCode(orgId);
        Organization mailConfig = organization.get();
        log.info("organization details", mailConfig);
        mailDto.setHost(mailConfig.getSmtpProvider());
        mailDto.setPort(mailConfig.getSmtpPort());
        mailDto.setUserName(mailConfig.getUserName());
        mailDto.setPassword(mailConfig.getPassword());
        mailDto.setFrom(mailConfig.getEmail());
        mailDto.setOrganisationDetail(mailConfig.getOrganizationDetail());
        streamBridge.send("mail-topic", mailDto);
        return GenericResponse.success("");
    }

    
    // @MessageMapping("/sendNotification")
    // @SendTo("/topic/notifications")
    // public String sendNotificationd(String message) {
    // return "🔔 New Notification: " + message;
    // }
    public ResponseEntity<String> sendNotification() {
        Notification notification = new Notification();
        notification.setRecepient("I191750");
        notification.setClientId("sop");
        notification.setMessage("Ticket bnjkl");
        notification.setType("notificatiion");
        notification.setWebHookUrl("http://localhost:8080/webhook/callbackNotification");
        streamBridge.send("notification-topic", notification);
        return ResponseEntity.ok("Success");
    }
}
