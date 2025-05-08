package com.hepl.budgie.mapper.payroll;

import com.hepl.budgie.dto.payroll.PayrollPfDTO;
import com.hepl.budgie.entity.payroll.PayrollPf;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollPfMapper {

    PayrollPf toEntity(PayrollPfDTO dto);

    // Mapping Entity to DTO
    PayrollPfDTO toDto(PayrollPf entity);
}
