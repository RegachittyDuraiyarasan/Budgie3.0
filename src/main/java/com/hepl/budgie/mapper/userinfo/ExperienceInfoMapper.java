package com.hepl.budgie.mapper.userinfo;

import com.hepl.budgie.dto.userinfo.ExperienceDTO;
import com.hepl.budgie.entity.userinfo.ExperienceDetails;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExperienceInfoMapper {

    default String map(Object value) {
        return value == null ? null : value.toString();
    }

    default List<String> mapToListOfString(Object value) {
        if (value instanceof List) {
            return (List<String>) value;
        }
        return null;
    }

    ExperienceDTO mapToDTO(ExperienceDetails experienceDetails);

    List<ExperienceDTO> mapToDTO(List<ExperienceDetails> experienceDetails);

}
