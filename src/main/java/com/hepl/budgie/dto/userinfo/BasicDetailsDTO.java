package com.hepl.budgie.dto.userinfo;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

import com.hepl.budgie.entity.userinfo.Skills;

@Data
public class BasicDetailsDTO {
    private String firstName;
    private String middleName;
    private String lastName;
    private ZonedDateTime dob;
    private String bloodGroup;
    private String placeOfBirth;
    private ZonedDateTime preferredDob;
    private String religion;
    private String gender;
    private String maritalStatus;
    private String identificationMarks;
    private String aadhaarCardNo;
    private String panNo;
    private Skills skills;
    private List<String> languages;
    private Integer age;

}
