package com.hepl.budgie.dto.preonboarding;

import lombok.Data;

import java.util.List;
@Data
public class WelcomeAboardDTO {
    private UserDTO userDTO;
    private List<EducationInterestingDTO> educationDetails;
    private List<ExperienceInterestingDTO> experienceDetails;
    private Achievement achievement;
    private boolean status;

    public WelcomeAboardDTO() {
    }

    public WelcomeAboardDTO(UserDTO userDTO, List<EducationInterestingDTO> educationDetails, List<ExperienceInterestingDTO> experienceDetails,Achievement achievement) {
        this.userDTO = userDTO;
        this.educationDetails = educationDetails;
        this.experienceDetails = experienceDetails;
        this.achievement = achievement;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }
     public void setInterestingFactsDTO(InterestingFactsDTO interestingFactsDTO) {
        this.achievement = achievement;
    }

    public List<EducationInterestingDTO> getEducationDetails() {
        return educationDetails;
    }

    public void setEducationDetails(List<EducationInterestingDTO> educationDetails) {
        this.educationDetails = educationDetails;
    }

    public List<ExperienceInterestingDTO> getExperienceDetails() {
        return experienceDetails;
    }

    public void setExperienceDetails(List<ExperienceInterestingDTO> experienceDetails) {
        this.experienceDetails = experienceDetails;
    }

    public Achievement getAchievement() {
        return achievement;
    }
}
