package com.hepl.budgie.mapper.attendancemanagement;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import com.hepl.budgie.dto.NameDTO;
import com.hepl.budgie.entity.attendancemanagement.ShiftType;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShiftTypeMapper {
	
	ShiftType toEntity(NameDTO nameDTO);
}   
