package com.hepl.budgie.mapper.probationProcess;

import com.hepl.budgie.dto.probation.AddProbationDTO;
import com.hepl.budgie.dto.probation.FeedbackFormDTO;
import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.entity.probation.ProbationProcess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProbationProcessMapper {
    @Mapping(target = "empId", ignore = true)
    void updateEntity(AddProbationDTO addProbationDTO, @MappingTarget ProbationProcess probationProcess);

    FeedbackFormDTO mapToDTO(ProbationProcess probationProcess);
}
