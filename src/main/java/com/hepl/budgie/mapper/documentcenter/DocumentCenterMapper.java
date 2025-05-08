package com.hepl.budgie.mapper.documentcenter;

import com.hepl.budgie.dto.documentInfo.DocumentInfoDto;
import com.hepl.budgie.dto.documentInfo.DocumentDetailsInfoDto;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.documentinfo.DocumentDetailsInfo;
import com.hepl.budgie.entity.documentinfo.DocumentInfo;
import com.hepl.budgie.entity.documentinfo.FileDetails;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentCenterMapper {

    @Mapping(target = "docdetails", source = "documentInfoDto.docdetails", qualifiedByName = "mapDocDetails")
    DocumentInfo toEntity(DocumentInfoDto documentInfoDto);

    @Named("mapDocDetails")
    default List<DocumentDetailsInfo> mapDocDetails(List<DocumentDetailsInfoDto> docDetailsDtoList) {
        if (docDetailsDtoList == null || docDetailsDtoList.isEmpty()) {
            return Collections.emptyList();
        }
        return docDetailsDtoList.stream()
                .map(dto -> {
                    DocumentDetailsInfo docDetails = new DocumentDetailsInfo();
                    docDetails.setModuleId(dto.getModuleId());
                    docDetails.setDocumentCategory(dto.getDocumentCategory());
                    // docDetails.setDocumentType(dto.getDocumentType());
                    docDetails.setModuleId(dto.getModuleId());
                    docDetails.setTitle(dto.getTitle());

                    // if (dto.getFileDetailsDto() != null) {
                    //     FileDetails fileDetail = new FileDetails();
                    //     fileDetail.setFolderName(dto.getFileDetailsDto().getFolderName());
                    //     fileDetail.setFileName(dto.getFileDetailsDto().getFileName());
                    //     docDetails.setFileDetails(fileDetail);
                    // }
                    docDetails.setDescription(dto.getDescription());
                    docDetails.setAcknowledgedType(dto.getAcknowledgedType());
                    docDetails.setAcknowledgementHeading(dto.getAcknowledgementHeading());
                    docDetails.setAcknowledgementDescription(dto.getAcknowledgementDescription());
                    docDetails.setStatus(Status.ACTIVE.label);
                    return docDetails;
                })
                .collect(Collectors.toList());
    }

    DocumentInfoDto toDto(DocumentInfo documentInfo);
}
