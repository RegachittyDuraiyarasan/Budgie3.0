package com.hepl.budgie.dto.event;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    private String eventName;
    private String where;
    private String category;
    private String eventType;
    private MultipartFile eventFile;
    private String allCandidate;
    private String color;
    private String description;
    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$", message = "{validation.error.invalid}")
    private String startDate;
    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$", message = "{validation.error.invalid}")
    private String endDate;
    @Pattern(regexp = "^[0-9]{2}:[0-9]{2}$", message = "{validation.error.invalidTime}")
    private String startTime;
    @Pattern(regexp = "^[0-9]{2}:[0-9]{2}$", message = "{validation.error.invalidTime}")
    private String endTime;
    private String filterType;
    private String filterValue;
    private boolean filterAll;
    private List<String> empIds;
}
