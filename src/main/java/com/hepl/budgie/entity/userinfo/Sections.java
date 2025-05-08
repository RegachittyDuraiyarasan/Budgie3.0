package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.entity.FilePathStruct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sections {
    private BasicDetails basicDetails;
    private Contact contact;
    private WorkingInformation workingInformation;
    private HrInformation hrInformation;
    private ProbationDetails probationDetails;
    private AccountInformation accountInformation;
    private Family family;
    private FilePathStruct profilePicture;
    private FilePathStruct bannerImage;
    private String profileOverallSubmit;
}
