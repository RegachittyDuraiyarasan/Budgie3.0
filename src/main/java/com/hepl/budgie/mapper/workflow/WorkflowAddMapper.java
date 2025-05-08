package com.hepl.budgie.mapper.workflow;

import com.hepl.budgie.dto.workflow.WorkflowAddDTO;
import com.hepl.budgie.entity.workflow.WorkFlow;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WorkflowAddMapper {
    @Mapping(target = "empDetails", source = "empDetails")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "sequence", source = "sequence")
    @Mapping(target = "actions", source = "actions")
    @Mapping(target = "status", source = "status")
    WorkflowAddDTO toDTO(WorkFlow workFlow);

    @Mapping(target = "empDetails", source = "empDetails")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "sequence", source = "sequence")
    @Mapping(target = "actions", source = "actions")
    @Mapping(target = "status", source = "status")
    WorkFlow toEntity(WorkflowAddDTO workflowAddDTO);

    List<WorkflowAddDTO> toDTOList(List<WorkFlow> workFlows);

    List<WorkFlow> toEntityList(List<WorkflowAddDTO> workflowAddDTOs);}
