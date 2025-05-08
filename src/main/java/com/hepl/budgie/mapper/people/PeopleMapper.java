package com.hepl.budgie.mapper.people;

import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;
import com.hepl.budgie.dto.people.PeopleDTO;
import com.hepl.budgie.dto.userinfo.BasicDetailsDTO;
import com.hepl.budgie.dto.userinfo.WorkingInformationDTO;
import com.hepl.budgie.entity.userinfo.*;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PeopleMapper {
    BasicDetailsDTO mapToDTO(BasicDetails basicDetails);

    WorkingInformationDTO mapToDTO(WorkingInformation workingInformation);

    @Mapping(source = "sections.basicDetails.firstName", target = "firstName")
    @Mapping(source = "sections.basicDetails.lastName", target = "lastName")
    @Mapping(source = "sections.basicDetails.dob", target = "dob")
    @Mapping(source = "sections.workingInformation.department", target = "department")
    @Mapping(source = "sections.workingInformation.designation", target = "designation")
    @Mapping(source = "sections.workingInformation.workLocation", target = "workLocation")
    @Mapping(source = "sections.workingInformation.officialEmail", target = "officialEmail")
    @Mapping(source = "sections.workingInformation.doj", target = "doj")
    @Mapping(source = "sections.profilePicture.fileName", target = "profilePicture.fileName")
    @Mapping(source = "sections.profilePicture.folderName", target = "profilePicture.folderName")
    PeopleDTO mapToDTO(UserInfo userInfo);

    @Mapping(target = "designation", source = "sections.workingInformation.designation")
    @Mapping(target = "department", source = "sections.workingInformation.department")
    @Mapping(target = "email", source = "sections.workingInformation.officialEmail")
    @Mapping(target = "workLocation", source = "sections.workingInformation.workLocation")
    @Mapping(target = "dateOfJoining", source = "sections.workingInformation.doj", qualifiedByName = "formatDate")
    @Mapping(target = "dateOfBirth", source = "sections.basicDetails.dob", qualifiedByName = "formatDate")
    @Mapping(target = "name", expression = "java(userInfo.getSections().getBasicDetails().getFirstName() + \" \"+userInfo.getSections().getBasicDetails().getLastName())")
    @Mapping(target = "organization", source = "sections.workingInformation.payrollStatusName")
    @Mapping(target = "profile", source = "sections.profilePicture")
    @Mapping(target = "banner", source = "sections.bannerImage")
    @Mapping(target = "idcarddetails", source = "idCardDetails")    
    EmployeeOrgChartDTO toOrgChartDTO(UserInfo userInfo, @Context String offsetId);

    @Named("formatDate")
    public static String formatDate(String date, @Context String offsetId) {
        Instant instant = Instant.parse(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
                .withZone(ZoneId.of(offsetId));
        return formatter.format(instant);
    }

    List<EmployeeOrgChartDTO> toOrgChartDTOList(List<UserInfo> userInfo, @Context String offsetId);

}
