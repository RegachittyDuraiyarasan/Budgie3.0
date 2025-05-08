package com.hepl.budgie.mapper.userinfo;
import com.hepl.budgie.dto.userinfo.DocumentsFetchDTO;
import com.hepl.budgie.entity.userinfo.PassportPhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.hepl.budgie.entity.userinfo.UserOtherDocuments;

import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OtherDocumentsMapper {
    @Mapping(source = "passportPhoto", target = "documents.passportPhoto.fileName")
    UserOtherDocuments toEntity(Map<String, Object> userOtherDocuments);

    default String map(Object value) {
        return value == null ? null : value.toString();
    }

     DocumentsFetchDTO mapToDTO(PassportPhoto passportPhoto);

}
