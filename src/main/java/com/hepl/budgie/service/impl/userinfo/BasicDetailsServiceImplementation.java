package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.BasicDetailsDTO;
import com.hepl.budgie.entity.userinfo.BasicDetails;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.userinfo.BasicDetailsMapper;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.userinfo.BasicDetailsService;

import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicDetailsServiceImplementation implements BasicDetailsService {

    private final UserInfoRepository userInfoRepository;

    private final BasicDetailsMapper basicDetailsMapper;

    private final JWTHelper jwtHelper;

    @Override
    public UserInfo updateBasicDetails(FormRequest formRequest,String empId) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);

        if (userInfoOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        UserInfo userInfo = userInfoOptional.get();

        BasicDetails updatedBasicDetails = basicDetailsMapper.toEntity(formRequest.getFormFields());
        log.info("updatedBasicDetails {}",updatedBasicDetails);
        Sections sections = userInfo.getSections();
        if (sections == null) {
            sections = new Sections();
        }
        sections.setBasicDetails(updatedBasicDetails);
        userInfo.setSections(sections);

        userInfoRepository.save(userInfo);
        return userInfo;
    }

    @Override
    public BasicDetailsDTO getBasicDetails(String empId) {
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

        Sections sections = userInfo.getSections();
        if (sections == null || sections.getBasicDetails() == null) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "");
        }

        BasicDetails basicDetails = sections.getBasicDetails();
        log.info("Skills in BasicDetails:{}",basicDetails.getSkills());

        ZonedDateTime dob = basicDetails.getDob();
        int age = dob != null ? Period.between(dob.toLocalDate(), LocalDate.now()).getYears() : 0;

        BasicDetailsDTO basicDetailsDTO = basicDetailsMapper.mapToDTO(basicDetails);
        basicDetailsDTO.setAge(age);

        return basicDetailsDTO;
    }
}
