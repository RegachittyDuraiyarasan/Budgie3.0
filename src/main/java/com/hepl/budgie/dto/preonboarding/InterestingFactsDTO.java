package com.hepl.budgie.dto.preonboarding;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestingFactsDTO {
    @NotBlank(message = "Achievements Experience is required")
    private String achievementsExperience;

    @NotBlank(message = "Achievements Education is required")
    private String achievementsEducation;

    @NotBlank(message = "Favorite Pastime is required")
    private String favPastime;

    @NotBlank(message = "Favorite Hobbies are required")
    private String favHobbies;

    @NotBlank(message = "Three Places is required")
    private String threePlaces;

    @NotBlank(message = "Three Food is required")
    private String threeFood;

    @NotBlank(message = "Favorite Sports is required")
    private String favSports;

    @NotBlank(message = "Favorite Movie is required")
    private String favMovie;

    @NotBlank(message = "Extracurricular Activities are required")
    private String extracurricularActivities;

    @NotBlank(message = "Career Inspiration is required")
    private String careerInspiration;

    @NotBlank(message = "Language Known is required")
    private String languageKnown;

    @NotBlank(message = "Interesting Fact is required")
    private String interestingFact;

    @NotBlank(message = "My Motto is required")
    private String myMotto;

    @NotBlank(message = "Favorite Book is required")
    private String favBook;

    public InterestingFactsDTO(String favPastime, String favHobbies, String threePlaces, String favSports, String favMovie, String extracurricularActivities, String careerInspiration, String languageKnown, String interestingFact, String myMotto, String favBook, String threeFood) {
    }
}
