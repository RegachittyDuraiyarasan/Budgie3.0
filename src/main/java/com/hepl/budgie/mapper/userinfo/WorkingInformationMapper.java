
package com.hepl.budgie.mapper.userinfo;

import com.hepl.budgie.dto.userinfo.WorkingInformationDTO;
import com.hepl.budgie.entity.userinfo.WorkingInformation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WorkingInformationMapper {

    @Mapping(target = "groupOfDOJ", expression = "java(parseDate(workingInformation.get(\"groupOfDOJ\"), \"dd/MM/yyyy HH:mm:ss\"))")
    @Mapping(target = "doj", expression = "java(parseDate(workingInformation.get(\"doj\"), \"dd/MM/yyyy HH:mm:ss\"))")
    @Mapping(target = "dateOfRelieving", expression = "java(parseDate(workingInformation.get(\"dateOfRelieving\"), \"dd/MM/yyyy HH:mm:ss\"))")

    WorkingInformation toEntity(Map<String, Object> workingInformation);

    default String map(Object value) {
        return value == null ? null : value.toString();
    }

    default ZonedDateTime parseDate(Object value, String format) {
        if(value == null){ 
            return ZonedDateTime.now();
        }else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime localDate = LocalDateTime.parse(value.toString() + " 00:00:00", formatter);
            return ZonedDateTime.of(localDate, ZoneId.systemDefault());
        }
    }

    WorkingInformationDTO mapToDTO(WorkingInformation workingInformation);
}
