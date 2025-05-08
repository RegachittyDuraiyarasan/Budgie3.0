package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.WorkingInformationDTO;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.userinfo.WorkingInformation;
import com.hepl.budgie.mapper.userinfo.WorkingInformationMapper;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.userinfo.WorkingInformationService;

import com.hepl.budgie.utils.Base64Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkingInformationServiceImplementation implements WorkingInformationService {

    private final WorkingInformationMapper workingInformationMapper;
    private final UserInfoRepository userInfoRepository;
    private final JWTHelper jwtHelper;

    @Override
    public UserInfo updateWorkingInformation(FormRequest formRequest, String empId) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);
        if (userInfoOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        UserInfo userInfo = userInfoOptional.get();

        // Extract form fields
        Map<String, Object> formFields = formRequest.getFormFields();
        if (formFields == null || formFields.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Form fields are empty");
        }

        Sections sections = userInfo.getSections();
        if (sections == null) {
            sections = new Sections();
        }

        WorkingInformation workingInformation = sections.getWorkingInformation();
        if (workingInformation == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Working Information not found for the user");
        }

        // Convert date strings to ZonedDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Map<String, Object> formFields1 = formRequest.getFormFields();
        Object ctcObject = formFields1.get("ctc");
        long ctcLong;
        ctcLong = ((Integer) ctcObject).longValue();
        String ctcString = String.valueOf(ctcLong);
        String encryptedCtc = Base64Util.encode(ctcString);
        workingInformation.setCtc(encryptedCtc);
        double monthlyAmount = Math.ceil(ctcLong / 12.0);
        String encryptedCmt = Base64Util.encode(String.valueOf((int) monthlyAmount));
        workingInformation.setCmt(encryptedCmt);


        workingInformation.setDepartment((String) formFields.getOrDefault("department", workingInformation.getDepartment()));
        workingInformation.setDesignation((String) formFields.getOrDefault("designation", workingInformation.getDesignation()));
        String dojString = (String) formFields.get("doj");
        if (dojString != null && !dojString.isEmpty()) {
            LocalDate localDate = LocalDate.parse(dojString, formatter);
            ZonedDateTime doj = localDate.atStartOfDay(ZoneId.systemDefault());
            workingInformation.setDoj(doj);
        }
        workingInformation.setWorkLocation((String) formFields.getOrDefault("workLocation", workingInformation.getWorkLocation()));
        workingInformation.setRoleOfIntake((String) formFields.getOrDefault("roleOfIntake", workingInformation.getRoleOfIntake()));
        workingInformation.setGrade((String) formFields.getOrDefault("grade", workingInformation.getGrade()));
        workingInformation.setRfh((String) formFields.getOrDefault("rfh", workingInformation.getRfh()));
        workingInformation.setOfficialEmail((String) formFields.getOrDefault("officialEmail", workingInformation.getOfficialEmail()));
        userInfo.setStatus((String) formFields.getOrDefault("candidateStatus", workingInformation.getCandidateStatus()));
        workingInformation.setAccessCardId((String) formFields.getOrDefault("accessCardId", workingInformation.getAccessCardId()));
        workingInformation.setEsiNo((String) formFields.getOrDefault("esiNo", workingInformation.getEsiNo()));
        String groupOfDOJString = (String) formFields.get("groupOfDOJ");
        if (groupOfDOJString != null && !groupOfDOJString.isEmpty()){
            LocalDate localDate = LocalDate.parse(groupOfDOJString, formatter);
            ZonedDateTime groupOfDOJ = localDate.atStartOfDay(ZoneId.systemDefault()); // Convert LocalDate to ZonedDateTime
            workingInformation.setGroupOfDOJ(groupOfDOJ);
        }
        workingInformation.setSwipeMethod((String) formFields.getOrDefault("swipeMethod", workingInformation.getSwipeMethod()));
        String dateOfRelievingString = (String) formFields.get("dateOfRelieving");
        if (dateOfRelievingString != null && !dateOfRelievingString.isEmpty()){
            LocalDate localDate = LocalDate.parse(dateOfRelievingString, formatter);
            ZonedDateTime dateOfRelieving = localDate.atStartOfDay(ZoneId.systemDefault());
            workingInformation.setDateOfRelieving(dateOfRelieving);
        }
        workingInformation.setShift((String) formFields.getOrDefault("shift", workingInformation.getShift()));
        workingInformation.setWeekOff((String) formFields.getOrDefault("weekOff", workingInformation.getWeekOff()));
        workingInformation.setLeaveScheme((String) formFields.getOrDefault("leaveScheme", workingInformation.getLeaveScheme()));
        workingInformation.setMarketFacingTitle((String) formFields.getOrDefault("marketFacingTitle", workingInformation.getMarketFacingTitle()));

        sections.setWorkingInformation(workingInformation);
        userInfo.setSections(sections);

        userInfoRepository.save(userInfo);
        return userInfo;
    }

    @Override
    public WorkingInformationDTO getWorkingInformation(String empId) {
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Sections sections = userInfo.getSections();

        if (sections == null || sections.getWorkingInformation() == null) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "");
        }

        WorkingInformation workingInformation = sections.getWorkingInformation();
        workingInformation.setCandidateStatus(userInfo.getStatus());
        ZonedDateTime doj = workingInformation.getDoj();
        String experience = "0y 0m 0d";

        if (doj != null) {
            LocalDate dojDate = doj.toLocalDate();
            LocalDate currentDate = LocalDate.now();

            Period period = Period.between(dojDate, currentDate);

            experience = String.format("%dY %dM %dD", period.getYears(), period.getMonths(), period.getDays());
        }
        workingInformation.setExperience(experience);
        return workingInformationMapper.mapToDTO(workingInformation);
    }
    
}
