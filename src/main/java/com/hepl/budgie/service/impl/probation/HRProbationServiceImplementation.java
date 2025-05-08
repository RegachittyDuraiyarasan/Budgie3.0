package com.hepl.budgie.service.impl.probation;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.entity.probation.ProbationProcess;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.probationProcess.ProbationProcessMapper;
import com.hepl.budgie.mapper.userinfo.ProbationDetailsMapper;
import com.hepl.budgie.repository.probationProcess.ProbationProcessRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.probation.HRProbationService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HRProbationServiceImplementation implements HRProbationService {

    private final UserInfoRepository userInfoRepository;

    private final ProbationProcessRepository probationProcessRepository;

    private final ProbationDetailsMapper probationDetailsMapper;

    private final ProbationProcessMapper probationProcessMapper;

    private final MongoTemplate mongoTemplate;

    private final JWTHelper jwtHelper;

    @Override
    public List<ProbationFetchDTO> getCurrentHRProbation() {
        Query query = new Query();
        query.addCriteria(Criteria.where("sections.probationDetails.isProbation").is(true));

        List<UserInfo> probationUsers = mongoTemplate.find(query, UserInfo.class);

        if (probationUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PROBATION);
        }

        // Today's date for comparison
        LocalDate today = LocalDate.now();
        LocalDate oneMonthLater = today.plusMonths(1);
        log.info("oneMonthLater {}",oneMonthLater);
        return probationUsers.stream()
                .filter(user -> {
                    ZonedDateTime probationEndDate = user.getSections().getProbationDetails().getProbationEndDate();
                    LocalDate probationEndLocalDate = probationEndDate.toLocalDate();
                    log.info("probationEndLocalDate {}",probationEndLocalDate);
                    log.info("today {}",today);
                    return (probationEndLocalDate.isEqual(today) || probationEndLocalDate.isAfter(today)) && probationEndLocalDate.isBefore(oneMonthLater);
                })
                .map(user -> {
                    ProbationProcess probationProcess = probationProcessRepository.findByEmpId(
                            user.getEmpId(),
                            jwtHelper.getOrganizationCode(),
                            mongoTemplate
                    );

                    if (probationProcess == null) {
                        probationProcess = new ProbationProcess();
                    }

                    return probationDetailsMapper.mapToDTO(user, probationProcess, userInfoRepository);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProbationFetchDTO> getExtendedHRProbation() {
        Query query = new Query();
        query.addCriteria(Criteria.where("sections.probationDetails.results").is("Extended"));

        List<UserInfo> probationUsers = mongoTemplate.find(query, UserInfo.class);

        if (probationUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PROBATION);
        }

        LocalDate today = LocalDate.now();
        LocalDate oneMonthLater = today.plusMonths(1);
        return probationUsers.stream()
                .filter(user -> {
                    ZonedDateTime probationEndDate = user.getSections().getProbationDetails().getProbationEndDate();
                    LocalDate probationEndLocalDate = probationEndDate.toLocalDate();
                    return (probationEndLocalDate.isEqual(today) || probationEndLocalDate.isAfter(today)) && probationEndLocalDate.isBefore(oneMonthLater);
                })
                .map(user -> {
                    ProbationProcess probationProcess = probationProcessRepository.findByEmpId(
                            user.getEmpId(),
                            jwtHelper.getOrganizationCode(),
                            mongoTemplate
                    );

                    if (probationProcess == null) {
                        probationProcess = new ProbationProcess();
                    }

                    return probationDetailsMapper.mapToDTO(user, probationProcess, userInfoRepository);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProbationFetchDTO> getConfirmedHRProbation() {
        Query query = new Query();
        query.addCriteria(Criteria.where("sections.probationDetails.results").is("Confirmed"));

        List<UserInfo> probationUsers = mongoTemplate.find(query, UserInfo.class);

        if (probationUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PROBATION);
        }

        LocalDate today = LocalDate.now();
        LocalDate oneMonthLater = today.plusMonths(1);
        return probationUsers.stream()
                .filter(user -> {
                    ZonedDateTime probationEndDate = user.getSections().getProbationDetails().getProbationEndDate();
                    LocalDate probationEndLocalDate = probationEndDate.toLocalDate();
                    return (probationEndLocalDate.isEqual(today) || probationEndLocalDate.isAfter(today)) && probationEndLocalDate.isBefore(oneMonthLater);
                })
                .map(user -> {
                    ProbationProcess probationProcess = probationProcessRepository.findByEmpId(
                            user.getEmpId(),
                            jwtHelper.getOrganizationCode(),
                            mongoTemplate
                    );

                    if (probationProcess == null) {
                        probationProcess = new ProbationProcess();
                    }

                    return probationDetailsMapper.mapToDTO(user, probationProcess, userInfoRepository);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProbationFetchDTO> getDeemedProbation(){
        Query query = new Query();
        query.addCriteria(Criteria.where("sections.probationDetails.isProbation").is(true));

        List<UserInfo> probationUsers = mongoTemplate.find(query, UserInfo.class);

        if (probationUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PROBATION);
        }

        LocalDate today = LocalDate.now();
        LocalDate oneMonthLater = today.plusMonths(1);
        return probationUsers.stream()
                .filter(user -> {
                    ZonedDateTime probationEndDate = user.getSections().getProbationDetails().getProbationEndDate();
                    LocalDate probationEndLocalDate = probationEndDate.toLocalDate();
                    return (probationEndLocalDate.isEqual(today) || probationEndLocalDate.isAfter(today)) && probationEndLocalDate.isBefore(oneMonthLater);
                })
                .map(user -> {
                    ProbationProcess probationProcess = probationProcessRepository.findByEmpId(
                            user.getEmpId(),
                            jwtHelper.getOrganizationCode(),
                            mongoTemplate
                    );

                    if (probationProcess == null) {
                        probationProcess = new ProbationProcess();
                    }

                    return probationDetailsMapper.mapToDTO(user, probationProcess, userInfoRepository);
                })
                .collect(Collectors.toList());
    }


    @Override
    public void updateHRVerifyStatuses(String empId, ProbationProcess request) {
        String org = jwtHelper.getOrganizationCode();

        Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmpId(empId);
        if (userInfoOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee not found");
        }

        ProbationProcess probationProcess = probationProcessRepository.findByEmpId(empId, org, mongoTemplate);

        if (probationProcess == null) {
            probationProcess = new ProbationProcess();
            probationProcess.setEmpId(empId);
            probationProcess.setHrVerifyStatus(request.getHrVerifyStatus());
            probationProcess.setExtendedHrVerifyStatus(request.getExtendedHrVerifyStatus());
        }

         probationProcessRepository.updateField(mongoTemplate, "ORG00001" , empId, request);

    }



}
