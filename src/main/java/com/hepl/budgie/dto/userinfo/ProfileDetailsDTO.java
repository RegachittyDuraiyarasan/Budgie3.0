package com.hepl.budgie.dto.userinfo;

import lombok.Data;

@Data
public class ProfileDetailsDTO {
    private String employeeName;
    private String personalEmailId;
    private String dob;
    private String designation;
    private String contactNumber;
    private String workLocation;
    private ImageDTO profileImage;
    private ImageDTO bannerImage;
    private String profileOverallSubmit;
}

