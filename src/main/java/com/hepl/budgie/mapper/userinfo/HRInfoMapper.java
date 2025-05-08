package com.hepl.budgie.mapper.userinfo;

import com.hepl.budgie.dto.userinfo.BasicDetailsDTO;
import com.hepl.budgie.dto.userinfo.HRInfoDTO;
import com.hepl.budgie.entity.userinfo.BasicDetails;
import com.hepl.budgie.entity.userinfo.HrInformation;

import com.hepl.budgie.entity.userinfo.ReporteeDetail;
import com.hepl.budgie.entity.userinfo.UserInfo;
import org.mapstruct.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HRInfoMapper {

    // Primary Manager
    @Mapping(source = "primaryReportingManager", target = "primary.managerId")
    @Mapping(target = "primary.effectiveFrom", expression = "java(getCurrentDate())")

    // Recruiter Manager
    @Mapping(source = "recruiter", target = "recruiter.managerId")
    @Mapping(target = "recruiter.effectiveFrom", expression = "java(getCurrentDate())")

    // Secondary Manager
    @Mapping(source = "additionalReportingManager", target = "secondary.managerId")
    @Mapping(target = "secondary.effectiveFrom", expression = "java(getCurrentDate())")

    // Reviewer Manager
    @Mapping(source = "reviewer", target = "reviewer.managerId")
    @Mapping(target = "reviewer.effectiveFrom", expression = "java(getCurrentDate())")


    @Mapping(source = "divisionHeadId",target = "divisionHead.managerId")
    @Mapping(target = "divisionHead.effectiveFrom", expression = "java(getCurrentDate())")

    // Onboarder Manager
    @Mapping(source = "onBoarder", target = "onboarder.managerId")
    @Mapping(target = "onboarder.effectiveFrom", expression = "java(getCurrentDate())")

    // Buddy
    @Mapping(source = "buddy", target = "buddy.managerId")
    @Mapping(target = "buddy.effectiveFrom", expression = "java(getCurrentDate())")

    // Notice Period
    @Mapping(source = "noticePeriod", target = "noticePeriod")
    HrInformation toEntity(Map<String, Object> formFields);

    default ZonedDateTime getCurrentDate() {
        return ZonedDateTime.now();
    }

    default Date mapStringToDate(String value) {
        try {
            return value == null ? null : new SimpleDateFormat("dd/MM/yyyy").parse(value);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }
    }

    default String map(Object value) {
        return value == null ? null : value.toString();
    }

    BasicDetailsDTO mapToDTO(BasicDetails basicDetails);

    @Mapping(source = "sections.basicDetails.firstName", target = "firstName")
    @Mapping(source = "sections.basicDetails.lastName", target = "lastName")
    @Mapping(source = "sections.hrInformation.primary.managerId", target = "primaryManagerId")
    @Mapping(source = "sections.hrInformation.reviewer.managerId", target = "reviewerId")
    @Mapping(source = "sections.hrInformation.divisionHead.managerId", target = "divisionHeadId")
    @Mapping(source = "sections.hrInformation.onboarder.managerId", target = "onboarderId")
    @Mapping(source = "sections.hrInformation.recruiter.managerId", target = "recruiterId")

    // Safe mapping of manager names
    @Mapping(target = "primaryManagerName", expression = "java(getSafeFullName(managerMap, userInfo.getSections().getHrInformation().getPrimary()))")
    @Mapping(target = "reviewerName", expression = "java(getSafeFullName(managerMap, userInfo.getSections().getHrInformation().getReviewer()))")
    @Mapping(target = "divisionHeadName", expression = "java(getSafeFullName(managerMap, userInfo.getSections().getHrInformation().getDivisionHead()))")
    @Mapping(target = "onboarderName", expression = "java(getSafeFullName(managerMap, userInfo.getSections().getHrInformation().getOnboarder()))")
    @Mapping(target = "recruiterName", expression = "java(getSafeFullName(managerMap, userInfo.getSections().getHrInformation().getRecruiter()))")

    HRInfoDTO mapToDTO(UserInfo userInfo, @Context Map<String, BasicDetails> managerMap);

    default String getSafeFullName(Map<String, BasicDetails> managerMap, ReporteeDetail hrInfoField) {
        if (hrInfoField == null || hrInfoField.getManagerId() == null) {
            return ""; // Handle missing manager IDs safely
        }

        String managerId = hrInfoField.getManagerId();
        BasicDetails manager = managerMap.get(managerId);

        System.out.println(manager);
        System.out.println(managerId);


        // If manager ID exists in the map, return full name, else return "ID Not Found"
        return (manager != null)
                ? manager.getFirstName() + " " + manager.getLastName() + " - " + managerId
                : "ID Not Found (" + managerId + ")";
    }

}

