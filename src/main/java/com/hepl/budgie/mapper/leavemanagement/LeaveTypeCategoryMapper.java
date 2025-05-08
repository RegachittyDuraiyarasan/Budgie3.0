package com.hepl.budgie.mapper.leavemanagement;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.leavemanagement.LeaveTypeRequestDTO;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.hepl.budgie.repository.leavemanagement.LeaveSchemeRepository;

import lombok.RequiredArgsConstructor;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Component
@RequiredArgsConstructor
public abstract class LeaveTypeCategoryMapper {

    @Autowired
    private LeaveSchemeRepository leaveSchemeRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private JWTHelper jwtHelper;

    @Mapping(target = "id", ignore = true) 
    @Mapping(target = "leaveSchemeId", source = "leaveScheme", qualifiedByName = "mapLeaveScheme")
    public abstract LeaveTypeCategory toEntity(LeaveTypeRequestDTO leaveTypeCategory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leaveUniqueCode", ignore = true)
    @Mapping(target = "leaveSchemeId", source = "leaveScheme", qualifiedByName = "mapLeaveScheme")
    public abstract void updateEntityFromDTO(LeaveTypeRequestDTO dto, @MappingTarget LeaveTypeCategory entity);
    
    @Named("mapLeaveScheme")
    protected List<LeaveTypeCategory.LeaveScheme> mapLeaveScheme(List<String> leaveSchemeIds) {
        if (leaveSchemeIds == null) {
            return null;
        }
        return leaveSchemeIds.stream()
            .map(schemeName -> leaveSchemeRepository.findBySchemeName(schemeName, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new RuntimeException("LeaveScheme not found with Name: " + schemeName)))
            .map(scheme -> {
                LeaveTypeCategory.LeaveScheme leaveScheme = new LeaveTypeCategory.LeaveScheme();
                leaveScheme.setSchemeId(scheme.getId());
                leaveScheme.setSchemeName(scheme.getSchemeName());
                leaveScheme.setStatus(scheme.getStatus());
                return leaveScheme;
            })
            .collect(Collectors.toList());
    }
}