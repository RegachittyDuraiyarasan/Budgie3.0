package com.hepl.budgie.dto.people;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hepl.budgie.entity.FilePathStruct;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class PeopleDTO {
    private String empId;
    private String firstName;
    private String lastName;
    private String designation;
    private String workLocation;
    private String department;
    private String officialEmail;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private ZonedDateTime doj;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM")
    private ZonedDateTime dob;
    private Boolean isStarred;
    private FilePathStruct profilePicture;
}
