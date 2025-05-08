package com.hepl.budgie.service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.hepl.budgie.entity.attendancemanagement.AttendanceRegularization;

public interface TemplateService {

    String getForgotPasswordTemplate(String email, String recepientName, String hashed);

    String getCustomPdfData(String paragraph);

    String getComposeMailTemplate(String content);

    String getAccountSetupTemlate(String email, String password, String from, String recepientName);

    String getIdCardInfo(String candidateImage, String firstName, String lastName,String secondaryContactNumber,String empId,String officialEmailID,String dateOfJoining,String bloodGroup,String qrCode);

    String getWelcomeOnboard(String firstName, String designation, String department,
                             ZonedDateTime doj, List<Map<String, Object>> educationDetails,
                             List<Map<String, Object>> experienceDetails,String achievementEducation,String achievementExperience,
                             String favPastime, String favHobbies, String threePlaces,
                             String threeFood, String favSports, String favMovie,
                             String extracurricularActivities, String careerInspiration, String languageKnown,
                             String interestingFact, String myMotto, String favBook);

    String regularizationApply(AttendanceRegularization regularization,String employeeName, String managerName);

    String regularizationApprove(AttendanceRegularization regularization,String userName);

    String absentMail(String empName, String empId, String date);
    
    String getRelievingLetter(String date, String empId, String designation,
                             String empname, String dol, String dateOfJoining, String hrSignPath)throws IOException;                         
}