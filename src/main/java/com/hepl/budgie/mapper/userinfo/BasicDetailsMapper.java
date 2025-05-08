package com.hepl.budgie.mapper.userinfo;

import com.hepl.budgie.dto.userinfo.BasicDetailsDTO;
import com.hepl.budgie.entity.userinfo.BasicDetails;
import com.hepl.budgie.entity.userinfo.Skills;

import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BasicDetailsMapper {

    @Mapping(target = "skills", expression = "java(mapSkills(basicDetails))")
    @Mapping(target = "dob", expression = "java(parseDate(basicDetails.get(\"dob\"), \"dd/MM/yyyy HH:mm:ss\"))")
    @Mapping(target = "preferredDob", expression = "java(parseDate(basicDetails.get(\"preferredDob\"), \"dd/MM/yyyy HH:mm:ss\"))")
    BasicDetails toEntity(Map<String, Object> basicDetails);

    default String map(Object value) {
        return value == null ? null : value.toString();
    }

    default List<String> mapToListOfString(Object value) {
        if (value instanceof List) {
            return (List<String>) value;
        }
        return null;
    }

    @Mapping(target = "skills.primary", source = "basicDetails.skills.primary")
    @Mapping(target = "skills.secondary", source = "basicDetails.skills.secondary")
    BasicDetailsDTO mapToDTO(BasicDetails basicDetails);

    default Skills mapSkills(Object skills) {
        if (skills instanceof Map) {
            Map<String, Object> skillsMap = (Map<String, Object>) skills;
            List<String> primarySkills = mapToListOfString(skillsMap.get("primary"));
            List<String> secondarySkills = mapToListOfString(skillsMap.get("secondary"));
            return new Skills(primarySkills, secondarySkills);
        }
        return null;
    }

    default ZonedDateTime parseDate(Object value, String format) {
        if (value == null) {
            return ZonedDateTime.now();
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime localDate = LocalDateTime.parse(value.toString() + " 00:00:00", formatter);
            return ZonedDateTime.of(localDate, ZoneId.systemDefault());
        }

    }
}
