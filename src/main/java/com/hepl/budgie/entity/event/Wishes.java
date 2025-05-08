package com.hepl.budgie.entity.event;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "t_wishes")
public class Wishes {
    @Id
    private String id;
    private String from;
    private String to;
    private String type;
    private LocalDateTime time;
}
