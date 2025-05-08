package com.hepl.budgie.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hepl.budgie.entity.attendancemanagement.AttendanceRegularization;
import com.hepl.budgie.service.TemplateService;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class TemplateServiceImpl implements TemplateService {

    @Value("${com.custom.reset-link}")
    private String resetPasswordLink;
    private final TemplateEngine templateEngine;

    public TemplateServiceImpl(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public String getForgotPasswordTemplate(String email, String recepientName, String hashed) {
        log.info("Generating forgot mail using html ...");

        Context context = new Context();
        context.setVariable("to", email);
        setDefaultContext(context);
        context.setVariable("recepient", recepientName);
        context.setVariable("resetLink", resetPasswordLink + "?token=" + hashed);

        return templateEngine.process("reset-mail", context);
    }

    @Override
    public String getAccountSetupTemlate(String email, String password, String from, String recepientName) {
        log.info("Generating account setup mail using html ...");
        Context context = new Context();
        context.setVariable("email", email);
        setDefaultContext(context);
        context.setVariable("recepient", recepientName);
        context.setVariable("from", from);
        context.setVariable("password", password);

        return templateEngine.process("login-password", context);
    }

    @Override
    public String getIdCardInfo(String candidateImage, String firstName, String lastName, String secondaryContactNumber,
            String empId, String officialEmailID, String dateOfJoining, String bloodGroup, String qrCode) {
        Context context = new Context();
        context.setVariable("image", candidateImage);
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("secondaryContactNumber", secondaryContactNumber);
        context.setVariable("empId", empId);
        context.setVariable("officialEmailID", officialEmailID);
        context.setVariable("dateOfJoining", dateOfJoining);
        context.setVariable("bloodGroup", bloodGroup);
        context.setVariable("qrCode", qrCode);

        return templateEngine.process("idCardTemplate", context);
    }

    @Override
    public String getWelcomeOnboard(String firstName, String designation, String department,
            ZonedDateTime doj, List<Map<String, Object>> educationDetails,
            List<Map<String, Object>> experienceDetails,String achievementEducation,String achievementExperience,
                                    String favPastime, String favHobbies, String threePlaces,
                                    String threeFood, String favSports, String favMovie,
                                    String extracurricularActivities, String careerInspiration, String languageKnown,
                                    String interestingFact, String myMotto, String favBook) {
        Context context = new Context();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String formattedDoj = doj.toLocalDate().format(formatter);
        context.setVariable("firstName", firstName);
        context.setVariable("designation", designation);
        context.setVariable("department", department);
        context.setVariable("doj", formattedDoj);
        context.setVariable("achievementEducation", achievementEducation);
        context.setVariable("achievementExperience", achievementExperience);
        context.setVariable("favPastime",favPastime);
        context.setVariable("favHobbies",favHobbies);
        context.setVariable("threePlaces",threePlaces);
        context.setVariable("threeFood",threeFood);
        context.setVariable("favSports",favSports);
        context.setVariable("favMovie",favMovie);
        context.setVariable("extracurricularActivities",extracurricularActivities);
        context.setVariable("careerInspiration",careerInspiration);
        context.setVariable("languageKnown",languageKnown);
        context.setVariable("interestingFact",interestingFact);
        context.setVariable("myMotto",myMotto);
        context.setVariable("favBook",favBook);


        for (Map<String, Object> edu : educationDetails) {
            if (edu.get("endOn") instanceof ZonedDateTime) {
                edu.put("formattedEndOn", ((ZonedDateTime) edu.get("endOn")).toLocalDate().format(formatter));
            }
        }
        context.setVariable("educationDetails", educationDetails);
        for (Map<String, Object> exp : experienceDetails) {
            if (exp.get("year") instanceof ZonedDateTime) {
                exp.put("formattedYear", ((ZonedDateTime) exp.get("year")).toLocalDate().format(formatter));
            }
        }
        context.setVariable("experienceDetails", experienceDetails);

        return templateEngine.process("welcomeOnboard", context);
    }

    @Override
    public String getCustomPdfData(String paragraph) {
        log.info("Generating forgot mail using html ...");

        Context context = new Context();
        context.setVariable("data", paragraph);

        return templateEngine.process("custom-pdf", context);
    }

    @Override
    public String getComposeMailTemplate(String content) {
        log.info("generating compose mail using html...");

        Context context = new Context();
        context.setVariable("content", content);
        setDefaultContext(context);

        return templateEngine.process("custom-mail", context);
    }

    @Override
    public String regularizationApply(AttendanceRegularization regularization, String employeeName,
            String managerName) {
        log.info("Generating forgot mail using html ...");

        Context context = new Context();
        context.setVariable("managerName", managerName); // Assuming appliedTo is the manager
        context.setVariable("userName", employeeName);
        context.setVariable("employeeId", regularization.getEmployeeId());
        context.setVariable("appliedRegularizations", regularization.getAppliedRegularizations()); // Pass list
        context.setVariable("remarks", regularization.getRemarks());

        return templateEngine.process("RegularizationMail", context);
    }

    private void setDefaultContext(Context context) {
        context.setVariable("logo", "logo");
        context.setVariable("header", "header");
        context.setVariable("background", "background");
    }

    @Override
    public String regularizationApprove(AttendanceRegularization regularization, String userName) {
        log.info("Generating forgot mail using html ...");

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("ApprovedDatesAndTimes", regularization.getApprovedDates());

        return templateEngine.process("RegularizationApproveMail", context);
    }

    @Override
    public String absentMail(String empName, String empId, String date) {
        log.info("Generating forgot mail using html ...");

        Context context = new Context();
        context.setVariable("userName", empName);
        context.setVariable("empId", empId);
        context.setVariable("date", date);
    
        return templateEngine.process("AttendanceShortFallMail", context);
    }
    @Override
    public String getRelievingLetter(String date, String empId, String designation,
            String empname, String dol, String dateOfJoining, String hrSignPath) throws IOException {
        log.info("Generating Relieving Letter using html ...");
        Resource resource = new ClassPathResource("static/images/hepl-logo.png");
        InputStream logoStream = resource.getInputStream();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedDate = LocalDate.parse(date, inputFormatter).format(outputFormatter);
        Context context = new Context();
        context.setVariable("logoPath", logoStream);
        context.setVariable("date", formattedDate);
        context.setVariable("empId", empId);
        context.setVariable("designation", designation);
        context.setVariable("empname", empname);
        context.setVariable("dol", dol);
        context.setVariable("doj", dateOfJoining);
        context.setVariable("hrSignPath", hrSignPath);

        return templateEngine.process("RelievingLetter", context);
    }

}
