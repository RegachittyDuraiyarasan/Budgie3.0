package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicDetails {
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
}
