package com.hepl.budgie.dto.notification;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComposeMailDTO {
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String content;
    private List<MultipartFile> attachment;
    private String host;
    private long port;
    private String userName;
    private String password;
    private String from;
    private String organisationId;
    private String type;
    private String templateName;
    private Map<String, Object> templateVariables;
    private String logo;
    private String organisationDetail;
    private String token;
    private String webHookUrl;

}
