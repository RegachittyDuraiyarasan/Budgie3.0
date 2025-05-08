package com.hepl.budgie.mapper.event;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.dto.event.EventDto;
import com.hepl.budgie.entity.event.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    @Mappings({
        @Mapping(target = "eventFile", source = "eventFile", qualifiedByName = "mapMultipartFileToString")
    })
    Event eventDtoToEvent(EventDto event);

    @Mappings({
        @Mapping(target = "eventFile", source = "event.eventFile", qualifiedByName = "mapMultipartFileToString")
    })
    Event toUpEvents(EventDto event,@MappingTarget Event existingEvent); 

    @Named("mapMultipartFileToString")
    default String mapMultipartFileToString(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        try {
            return new String(multipartFile.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
