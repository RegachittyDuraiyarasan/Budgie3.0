package com.hepl.budgie.mapper.attendancemanagement;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.hepl.budgie.dto.attendancemanagement.AttendanceWeekendDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WeekendPolicyMapper {

    WeekendPolicyMapper INSTANCE = Mappers.getMapper(WeekendPolicyMapper.class);

    // AttendanceWeekendPolicy toEntity(AttendanceWeekendDTO weekend);
    
}
