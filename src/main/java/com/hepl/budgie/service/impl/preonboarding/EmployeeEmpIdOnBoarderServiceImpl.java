package com.hepl.budgie.service.impl.preonboarding;

import com.hepl.budgie.dto.employee.EmployeeCreateDTO;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.Sequence;
import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;
import com.hepl.budgie.entity.userinfo.UserExpEducation;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.userinfo.UserOtherDocuments;
import com.hepl.budgie.repository.organization.OrganizationRepository;
import com.hepl.budgie.repository.preOnboardingRepository.OnboardingInfoRepository;
import com.hepl.budgie.repository.userinfo.OtherDocumentsRepository;
import com.hepl.budgie.repository.userinfo.UserExpEducationRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.preonboarding.EmployeeEmpIdOnBoarderService;
import com.hepl.budgie.utils.AppMessages;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeEmpIdOnBoarderServiceImpl implements EmployeeEmpIdOnBoarderService {

    private final UserInfoRepository userInfoRepository;
    private final OrganizationRepository organizationRepository;
    private final OnboardingInfoRepository onboardingInfoRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final OtherDocumentsRepository otherDocumentsRepository;
    private final UserExpEducationRepository userExpEducationRepository;

    @Override
    public List<String> updateMultipleEmployeeEmpIds(List<String> empIds) {
        List<String> updatedEmpIds = new ArrayList<>();
        Set<String> generatedEmpIds = new HashSet<>();

        for (String empId : empIds) {
            Optional<UserInfo> existingEmployeeOpt = userInfoRepository.findByEmpId(empId);
            if (existingEmployeeOpt.isEmpty()) {
                log.error(AppMessages.EMPLOYEEID_NOT_FOUND, empId);
                continue;
            }

            UserInfo existingEmployee = existingEmployeeOpt.get();
            String roleOfIntake = existingEmployee.getSections().getWorkingInformation().getRoleOfIntake();
            if (roleOfIntake == null || roleOfIntake.isBlank()) {
                log.error(AppMessages.ROLE_OF_INTAKE, empId);
                continue;
            }

            Optional<Organization> organizationOpt = organizationRepository
                    .findByOrganizationDetail(existingEmployee.getOrganization().getOrganizationDetail());
            if (organizationOpt.isEmpty()) {
                log.error(AppMessages.ORGANIZATION_DETAILS_NOT_FOUND, existingEmployee.getOrganization());
                continue;
            }

            Organization organization = organizationOpt.get();
            Optional<Sequence> matchingSequenceOpt = organization.getSequence().stream()
                    .filter(seq -> seq.getRoleType().equals(roleOfIntake))
                    .filter(seq-> "Yes".equalsIgnoreCase(seq.getAutoGenerationStatus()))
                    .findFirst();
            if (matchingSequenceOpt.isEmpty()) {
                log.error(AppMessages.ROLE_NOT_FOUND_IN_SEQUENCE, roleOfIntake, organization.getOrganizationCode());
                continue;
            }

            Sequence matchingSequence = matchingSequenceOpt.get();
            String baseSequence = matchingSequence.getRoleSequence();
            String newEmpId;
            int sequenceNumber;

            String prefix = baseSequence.replaceAll("\\d", "");
            String numericPart = baseSequence.replaceAll("\\D", "");
            sequenceNumber = Integer.parseInt(numericPart);

            do {
                sequenceNumber++; 
                String formattedSequence = String.format("%0" + numericPart.length() + "d", sequenceNumber);
                newEmpId = prefix + formattedSequence;

            } while (generatedEmpIds.contains(newEmpId) || userInfoRepository.existsByEmpId(newEmpId));

            generatedEmpIds.add(newEmpId);
            existingEmployee.setEmpId(newEmpId);
            existingEmployee.setEmpIdGenerateStatus(true);

            updateAssociatedRecords(empId, newEmpId);
            log.info("Updating empId for employee {} to {}", empId, newEmpId);
            userInfoRepository.save(existingEmployee);

            updatedEmpIds.add(newEmpId);
        }

        return updatedEmpIds;
    }

    private void updateAssociatedRecords(String oldEmpId, String newEmpId) {
        Optional<UserOtherDocuments> otherDocuments = otherDocumentsRepository.findByEmpId(oldEmpId);
        otherDocuments.ifPresent(userOtherDocuments -> {
            userOtherDocuments.setEmpId(newEmpId);
            otherDocumentsRepository.save(userOtherDocuments);
        });

        Optional<UserExpEducation> userExpEducation = userExpEducationRepository.findByEmpId(oldEmpId);
        userExpEducation.ifPresent(userExpEducation1 -> {
            userExpEducation1.setEmpId(newEmpId);
            userExpEducationRepository.save(userExpEducation1);
        });

        Optional<OnBoardingInfo> onBoardingInfo = onboardingInfoRepository.findByEmpId(oldEmpId);
        onBoardingInfo.ifPresent(onBoardingInfo1 -> {
            onBoardingInfo1.setEmpId(newEmpId);
            onboardingInfoRepository.save(onBoardingInfo1);
        });
    }

    @Override
    public String updateNapsAndNatsEmpId(EmployeeCreateDTO employeeCreateDTO) {
        String empId = employeeCreateDTO.getEmpId();
        String newEmpId = employeeCreateDTO.getNewEmpId();

        if (userInfoRepository.findByEmpId(newEmpId).isPresent()) {
            log.error("Duplicate Employee ID: {}", newEmpId);
            return "Employee ID already exists";
        }

        Optional<UserInfo> existingEmployeeOpt = userInfoRepository.findByEmpId(empId);
        if (existingEmployeeOpt.isEmpty()) {
            log.error(AppMessages.EMPLOYEEID_NOT_FOUND, empId);
            return "Employee ID not found";
        }

        UserInfo existingEmployee = existingEmployeeOpt.get();
        String roleOfIntake = existingEmployee.getSections().getWorkingInformation().getRoleOfIntake();

        if (roleOfIntake == null || roleOfIntake.isBlank()) {
            log.error(AppMessages.ROLE_OF_INTAKE, empId);
            return "Role of intake not found";
        }

        Optional<Organization> organizationOpt = organizationRepository
                .findByOrganizationDetail(existingEmployee.getOrganization().getOrganizationDetail());

        if (organizationOpt.isEmpty()) {
            log.error(AppMessages.ORGANIZATION_DETAILS_NOT_FOUND, existingEmployee.getOrganization());
            return "Organization details not found";
        }

        Organization organization = organizationOpt.get();
        Optional<Sequence> matchingSequenceOpt = organization.getSequence().stream()
                .filter(seq -> seq.getRoleType().equals(roleOfIntake))
                .findFirst();

        if (matchingSequenceOpt.isEmpty()) {
            log.error(AppMessages.ROLE_NOT_FOUND_IN_SEQUENCE, roleOfIntake, organization.getOrganizationCode());
            return "Role not found in sequence";
        }

        Sequence matchingSequence = matchingSequenceOpt.get();

        if ("No".equalsIgnoreCase(matchingSequence.getAutoGenerationStatus())) {
            existingEmployee.setEmpId(newEmpId);
            existingEmployee.setEmpIdGenerateStatus(true);
            userInfoRepository.save(existingEmployee);

            updateDependentEntities(empId, newEmpId);
            log.info("Updated empId for employee {} to {}", empId, newEmpId);
            return "Employee ID updated successfully to ";
        }

        log.info("Auto-generation is enabled. Employee ID not updated for {}", empId);
        return "Auto-generation is enabled";
    }

    private void updateDependentEntities(String oldEmpId, String newEmpId) {
        otherDocumentsRepository.findByEmpId(oldEmpId).ifPresent(doc -> {
            doc.setEmpId(newEmpId);
            otherDocumentsRepository.save(doc);
        });

        userExpEducationRepository.findByEmpId(oldEmpId).ifPresent(expEdu -> {
            expEdu.setEmpId(newEmpId);
            userExpEducationRepository.save(expEdu);
        });

        onboardingInfoRepository.findByEmpId(oldEmpId).ifPresent(onBoarding -> {
            onBoarding.setEmpId(newEmpId);
            onboardingInfoRepository.save(onBoarding);
        });
    }

    private void sendWelcomeEmail(UserInfo employee, String password) {
        String toEmail = employee.getSections().getContact().getPersonalEmailId();
        if (toEmail == null || toEmail.isBlank()) {
            log.warn(AppMessages.PERSONAL_EMAIL, employee.getEmpId());
            return;
        }

        String subject = "Welcome Onboard || Budgie";
        Context context = new Context();
        context.setVariable("firstname", employee.getSections().getBasicDetails().getFirstName());
        context.setVariable("empId", employee.getEmpId());
        context.setVariable("password", password);
        String htmlContent = templateEngine.process("EmpIdGenerated", context);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public OnBoardingInfo updateOnboardingStatus(String empId) {
        Optional<OnBoardingInfo> onboarding = onboardingInfoRepository.findByEmpId(empId);
        if (onboarding.isPresent()) {
            OnBoardingInfo onboardingInfo = onboarding.get();
            onboardingInfo.setOnboardingStatus(true);
            return onboardingInfoRepository.save(onboardingInfo);
        } else {
            OnBoardingInfo newOnboardingInfo = new OnBoardingInfo();
            newOnboardingInfo.setEmpId(empId);
            newOnboardingInfo.setOnboardingStatus(true);
            return onboardingInfoRepository.save(newOnboardingInfo);
        }
    }

}