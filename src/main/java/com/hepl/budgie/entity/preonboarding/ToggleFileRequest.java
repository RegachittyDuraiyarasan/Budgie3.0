package com.hepl.budgie.entity.preonboarding;

import lombok.Data;

import java.util.Map;
@Data
public class ToggleFileRequest {
    private Map<String, String> document;
    private String status;
}
