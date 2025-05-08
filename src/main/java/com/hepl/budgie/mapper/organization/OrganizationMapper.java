package com.hepl.budgie.mapper.organization;

import com.hepl.budgie.dto.organization.OrganizationAddDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.organization.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {
    
    @Mapping(target = "status", source = "status", qualifiedByName = "setStatus")
    Organization toEntity(OrganizationAddDTO organizationAddDTO, Status status);
    
    @Named("setStatus")
    default String setStatus(Status status) {
        return status.label;
    }

    OrganizationAddDTO toDto(Organization org);
    
}
