package com.hepl.budgie.dto.idCard;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.mail.Multipart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdCardGenerationDto {
    // private Multipart fileName;
    private String firstName;
    private String middleName;
    private String lastName;
    private String workLocation;
    private String primaryContactNumber;
    private String secondaryContactNumber;
    private String emergencyContactOfRelationship;
    private String nameOfRelationship;
    private String emergencyContactNo;
    private String status;
    private String graphicsImage;
    private String graphicsImageFolderName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfJoining;
    private String bloodGroup;
    private String employeeCode;
    private String officialEmailID;
    private String personalEmailID;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate groupDOJ;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate preferredDateOfBirth;  
}
