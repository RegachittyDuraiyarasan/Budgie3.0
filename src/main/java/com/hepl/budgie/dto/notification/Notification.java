package com.hepl.budgie.dto.notification;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    private String recepient;
    private String message;
    private String type;
    private String clientId;
    private Map<String, Object> payload;
    private String webHookUrl;

}
