package com.hepl.budgie.mapper.payroll;

import com.hepl.budgie.dto.payroll.PayrollVpfDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollVpf;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollVpfMapper {
    @Mapping(target = "status", source = "status", qualifiedByName = "setStatus")
    PayrollVpf toEntity(PayrollVpfDTO payrollVpfDTO, Status status);

    @Named("setStatus")
    default String setStatus(Status status) {
        return status.label;
    }

}
