package com.hepl.budgie.entity.event;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("m_events")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    private String id;
    private String eventName;
    private String where;
    private String category;
    private String eventType;
    private String eventFile;
    private String allCandidate;
    private String color;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String filterType;
    private String filterValue;
    private boolean filterAll;
    private List<String> empIds;
    private String status;
}
