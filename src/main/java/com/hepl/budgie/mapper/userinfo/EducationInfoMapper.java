package com.hepl.budgie.mapper.userinfo;

import com.hepl.budgie.dto.userinfo.EducationDTO;
import com.hepl.budgie.entity.userinfo.EducationDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EducationInfoMapper {
    EducationDTO mapToDTO(EducationDetails educationDetails);

    List<EducationDTO> mapToDTOList(List<EducationDetails> educationDetailsList);
}
