package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "user_exp_education")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserExpEducation {
    @Id
    private String id;
    private String empId;
    private List<ExperienceDetails> experienceDetails = new ArrayList<>();
    private List<EducationDetails> educationDetails = new ArrayList<>() ;

    public UserExpEducation(String empId, List<EducationDetails> educationDetails, List<ExperienceDetails> experienceDetails) {
        this.empId = empId;
        this.educationDetails = educationDetails;
        this.experienceDetails = experienceDetails;
    }
}
