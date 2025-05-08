package com.hepl.budgie.dto.preonboarding;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Achievement {
    private String achievementsEducation;
    private String achievementsExperience;
    private String favPastime;
    private String favHobbies;
    private String threePlaces;
    private String threeFood;
    private String favSports;
    private String favMovie;
    private String extracurricularActivities;
    private String careerInspiration;
    private String languageKnown;
    private String interestingFact;
    private String myMotto;
    private String favBook;
}
