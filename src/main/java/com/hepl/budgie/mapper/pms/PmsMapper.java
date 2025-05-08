package com.hepl.budgie.mapper.pms;

import com.hepl.budgie.dto.pms.PmsDTO;
import com.hepl.budgie.dto.pms.ReportingManagerFetchDTO;
import com.hepl.budgie.dto.pms.ReviewerTabFetchDTO;
import com.hepl.budgie.entity.pms.EmployeeTabFetchDTO;
import com.hepl.budgie.entity.pms.Pms;
import com.hepl.budgie.entity.pms.PmsEmployeeDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PmsMapper {
    PmsMapper INSTANCE = Mappers.getMapper(PmsMapper.class);

    @Mapping(source = "finalRating", target = "finalRating", qualifiedByName = "stringToList")
    @Mapping(source = "finalRatingValue", target = "finalRatingValue", qualifiedByName = "stringToList")
    @Mapping(source = "hierarchyLevel", target = "hierarchyLevel", qualifiedByName = "stringToList")
    @Mapping(source = "recommendation", target = "recommendation", qualifiedByName = "stringToList")
    @Mapping(source = "recommendationValue", target = "recommendationValue", qualifiedByName = "stringToList")
    @Mapping(source = "consolidatedSelfRating", target = "consolidatedSelfRating") // Ensure this field exists in both
                                                                                   // DTO and Entity
    Pms toEntity(PmsDTO dto);

    List<EmployeeTabFetchDTO> toEmployeeTabFetchDTOList(List<Pms> pmsList);

    List<ReportingManagerFetchDTO> toReportingManagerTabFetchDTOList(List<Pms> pmsList);

    List<ReviewerTabFetchDTO> toReviewerTabFetchDTOList(List<Pms> pmsList);

    @Mapping(target = "empName", source = "empId", qualifiedByName = "getNameByEmpId")
    @Mapping(target = "repManagerId", source = "sections.hrInformation.primary.managerId")
    @Mapping(target = "repManagerName", source = "sections.hrInformation.primary.managerId", qualifiedByName = "getNameByEmpId")
    @Mapping(target = "reviewerId", source = "sections.hrInformation.reviewer.managerId")
    @Mapping(target = "reviewerName", source = "sections.hrInformation.reviewer.managerId", qualifiedByName = "getNameByEmpId")
    @Mapping(target = "divisionHeadId", source = "sections.hrInformation.divisionHead.managerId")
    @Mapping(target = "divisionHeadName", source = "sections.hrInformation.divisionHead.managerId", qualifiedByName = "getNameByEmpId")
    @Mapping(target = "designation", source = "sections.workingInformation.designation")
    @Mapping(target = "department", source = "sections.workingInformation.department")
    @Mapping(target = "grade", source = "sections.workingInformation.grade")
    @Mapping(target = "roleOfIntake", source = "sections.workingInformation.roleOfIntake")
    @Mapping(target = "dateOfJoining", source = "sections.workingInformation.doj")
    @Mapping(target = "ctc", source = "sections.workingInformation.ctc")
    @Mapping(target = "manPower", source = "sections.workingInformation.manpowerOutsourcing")
    PmsEmployeeDetails toPmsEmployeeDetails(UserInfo userInfo, @Context UserInfoRepository userInfoRepository);

    @Named("getNameByEmpId")
    default String getEmployeeName(String empId, @Context UserInfoRepository userInfoRepository) {
        if (empId == null || empId.trim().isEmpty()) {
            return null;
        }
        return userInfoRepository.findByEmpId(empId)
                .map(user -> user.getSections().getBasicDetails().getFirstName() + " "
                        + user.getSections().getBasicDetails().getLastName())
                .orElse(null);
    }

    @Named("stringToList")
    static List<String> stringToList(String value) {
        return (value == null || value.isEmpty()) ? Collections.emptyList() : Collections.singletonList(value);
    }

    @Named("listToString")
    static String listToString(List<String> values) {
        return (values == null || values.isEmpty()) ? "" : String.join(", ", values);
    }
}
