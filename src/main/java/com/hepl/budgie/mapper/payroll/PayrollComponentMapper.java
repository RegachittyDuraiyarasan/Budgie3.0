package com.hepl.budgie.mapper.payroll;

import com.hepl.budgie.dto.payroll.PayrollComponentDTO;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollComponentMapper {
    PayrollComponent toEntity(PayrollComponentDTO payrollComponentDTO);
}
