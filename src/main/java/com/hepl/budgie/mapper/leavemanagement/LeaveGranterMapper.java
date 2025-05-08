package com.hepl.budgie.mapper.leavemanagement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.hepl.budgie.entity.leavemanagement.Details;
import com.hepl.budgie.entity.leavemanagement.LeaveGranter;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeaveGranterMapper {

    @Mapping(target = "details", expression = "java(mapDetails((Map<String, Object>) formRequest.get(\"details\")))")
    @Mapping(target = "postedOn", source = "postedOn")
    @Mapping(target = "fromDate", source = "fromDate")
    @Mapping(target = "toDate", source = "toDate")
    LeaveGranter toEntity(Map<String, Object> formRequest);

    default List<Details> mapDetails(Map<String, Object> detailsMap) {
        if (detailsMap == null) {
            return Collections.emptyList();
        }
        Details details = new Details();
        details.setType((String) detailsMap.get("type"));
        details.setTransaction((String) detailsMap.get("transaction"));
        details.setDays((String) detailsMap.get("days"));
        return List.of(details);
    }

    // Custom method to map Object to String
    default String map(Object value) {
        return value == null ? null : value.toString();
    }
}