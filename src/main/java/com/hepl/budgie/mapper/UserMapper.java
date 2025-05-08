package com.hepl.budgie.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.hepl.budgie.dto.UserDTO;
import com.hepl.budgie.dto.userlogin.UserResponseDetails;
import com.hepl.budgie.entity.Users;
import com.hepl.budgie.entity.userinfo.UserInfo;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "deletedDate", ignore = true)
    Users toUserEntity(UserDTO userReqDTO);

    @Mapping(target = "firstName", source = "userInfo.sections.basicDetails.firstName")
    @Mapping(target = "lastName", source = "userInfo.sections.basicDetails.lastName")
    @Mapping(target = "officialEmail", source = "userInfo.sections.workingInformation.officialEmail")
    @Mapping(target = "designation", source = "userInfo.sections.workingInformation.designation")
    @Mapping(target = "spocStatus", source = "userInfo.sections.hrInformation.spocStatus")
    @Mapping(target = "spocReportingManagerStatus", source = "userInfo.sections.hrInformation.spocReportingManagerStatus")
    @Mapping(target = "organizationAccess", source = "userInfo.subOrganization")
    @Mapping(target = "roles", source = "roleDetails")
    @Mapping(target = "roleType", source = "activeRole")
    @Mapping(target = "swipeMethod", source = "userInfo.sections.workingInformation.swipeMethod")
    UserResponseDetails toResponseDetails(UserInfo userInfo, String activeRole, List<String> roleDetails);

}
