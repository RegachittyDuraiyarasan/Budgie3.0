package com.hepl.budgie.mapper.userinfo;

import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.dto.userinfo.BasicDetailsDTO;
import com.hepl.budgie.entity.probation.ProbationProcess;
import com.hepl.budgie.entity.userinfo.BasicDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProbationDetailsMapper {

    @Mapping(source = "userInfo.sections.probationDetails.probationStartDate", target = "probationStartDate")
    @Mapping(source = "userInfo.sections.probationDetails.probationEndDate", target = "probationEndDate")
    @Mapping(source = "userInfo.sections.probationDetails.results", target = "results")
    @Mapping(source = "userInfo.sections.probationDetails.extendedMonths", target = "extendedMonths")
    @Mapping(target = "empName", expression = "java(userInfo.getSections().getBasicDetails().getFirstName() + \" \" + userInfo.getSections().getBasicDetails().getLastName() + \" - \" + userInfo.getEmpId())")
    @Mapping(target = "reportingManagerName", source = "userInfo.sections.hrInformation.primary.managerId", qualifiedByName = "getNameByEmpId")
    @Mapping(source = "probationProcess.status", target = "status")
    @Mapping(source = "probationProcess.extendedStatus", target = "extendedStatus")
    @Mapping(source = "probationProcess.hrVerifyStatus", target = "hrVerifyStatus")
    ProbationFetchDTO mapToDTO(UserInfo userInfo, ProbationProcess probationProcess, @Context UserInfoRepository userInfoRepository);

    @Named("getNameByEmpId")
    default String getEmployeeName(String empId, @Context UserInfoRepository userInfoRepository) {
        if (empId == null || empId.trim().isEmpty()) {
            return null;
        }
        return userInfoRepository.findByEmpId(empId)
                .map(user -> {
                    String firstName = user.getSections().getBasicDetails().getFirstName();
                    String lastName = user.getSections().getBasicDetails().getLastName();
                    return firstName + " " + lastName + " - " + empId;
                })
                .orElse(empId);
    }
}
