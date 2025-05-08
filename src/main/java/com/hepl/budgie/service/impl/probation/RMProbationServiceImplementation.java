package com.hepl.budgie.service.impl.probation;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.probation.AddProbationDTO;
import com.hepl.budgie.dto.probation.FeedbackFormDTO;
import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.entity.probation.FollowUp;
import com.hepl.budgie.entity.probation.ProbationProcess;
import com.hepl.budgie.entity.userinfo.ProbationDetails;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.probationProcess.ProbationProcessMapper;
import com.hepl.budgie.mapper.userinfo.ProbationDetailsMapper;
import com.hepl.budgie.repository.probationProcess.ProbationProcessRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.probation.RMProbationService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.DateUtil;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RMProbationServiceImplementation implements RMProbationService {

    private final UserInfoRepository userInfoRepository;

    private final ProbationProcessRepository probationProcessRepository;

    private final ProbationDetailsMapper probationDetailsMapper;

    private final ProbationProcessMapper probationProcessMapper;

    private final MongoTemplate mongoTemplate;

    private final JWTHelper jwtHelper;

    @Override
    public List<ProbationFetchDTO> getCurrentProbation() {

        List<UserInfo> probationUsers = userInfoRepository.fetchUserDetails(jwtHelper.getOrganizationCode(),jwtHelper.getUserRefDetail().getEmpId(),mongoTemplate);
        log.info("probationUser{}",probationUsers);
        LocalDate today = LocalDate.now();
        log.info("today {}",today);
        LocalDate oneMonthLater = today.plusMonths(1);
        log.info("oneMonthLater {}", oneMonthLater);

        return probationUsers.stream()
                .filter(user -> {
                    ZonedDateTime probationEndDate = user.getSections().getProbationDetails().getProbationEndDate();
                    LocalDate probationEndLocalDate = probationEndDate.toLocalDate();
                    log.info("probationEndLocalDate {}", probationEndLocalDate);
                    log.info("today {}", today);
                    return (probationEndLocalDate.isEqual(today) || probationEndLocalDate.isAfter(today)) && probationEndLocalDate.isBefore(oneMonthLater);
                })
                .map(user -> {
                    // Fetch ProbationProcess separately using the custom method
                    ProbationProcess probationProcess = probationProcessRepository.findByEmpId(
                            user.getEmpId(),
                            jwtHelper.getOrganizationCode(),
                            mongoTemplate
                    );

                    // Handle null case if no record is found
                    if (probationProcess == null) {
                        probationProcess = new ProbationProcess();
                    }

                    return probationDetailsMapper.mapToDTO(user, probationProcess,userInfoRepository);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProbationProcess addFeedbackForm(String empId, AddProbationDTO addProbationDTO) {
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        log.info("Received DTO: {}", addProbationDTO);

        // Validate results
        String result = addProbationDTO.getResults();
        if (!"Extended".equalsIgnoreCase(result) && !"Confirmed".equalsIgnoreCase(result)) {
            log.warn("Invalid result: {}. Only 'Extended' or 'Confirmed' allowed.", result);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.INVALID_RESULTS);
        }

        if ("Extended".equalsIgnoreCase(result) && (addProbationDTO.getExtendedMonths() == null || addProbationDTO.getExtendedMonths().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EXTENDED_MONTHS);
        }

        if ("Extended".equalsIgnoreCase(result) && "Submitted".equalsIgnoreCase(addProbationDTO.getStatus())) {
            if (addProbationDTO.getExtendedStatus() == null || addProbationDTO.getExtendedStatus().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,AppMessages.EXTENDED_STATUS);
            }
        }

        // Validate status
        String status = addProbationDTO.getStatus();
        if (!"Saved".equalsIgnoreCase(status) && !"Submitted".equalsIgnoreCase(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be 'Saved' or 'Submitted'.");
        }

        ProbationProcess probationProcess = probationProcessRepository.findByEmpId(empId, jwtHelper.getOrganizationCode(),mongoTemplate);

        if (probationProcess == null) {
            probationProcess = new ProbationProcess();
            probationProcess.setEmpId(empId);
            probationProcess.setFollowUps(new ArrayList<>());
        }

        probationProcessMapper.updateEntity(addProbationDTO, probationProcess);
        probationProcess.setStatus(status);

        if ("Extended".equalsIgnoreCase(result)) {
            probationProcess.setExtendedMonths(addProbationDTO.getExtendedMonths());
            probationProcess.setExtendedStatus(status);
        } else {
            probationProcess.setStatus(status);
        }

        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        probationProcess.setReportingManagerId(authenticatedEmpId);

        // Handle Follow-up logic if status is "Submitted"
        if ("Submitted".equalsIgnoreCase(status)) {
            FollowUp followUp = new FollowUp();
            followUp.setResults(result);
            followUp.setMailStatus(false);
            followUp.setMailSentDate(ZonedDateTime.now());

            if ("Extended".equalsIgnoreCase(result)) {
                followUp.setExtendedMonths(addProbationDTO.getExtendedMonths());
            }

            if (probationProcess.getFollowUps() == null) {
                probationProcess.setFollowUps(new ArrayList<>());
            }

            probationProcess.getFollowUps().add(followUp);
        }

        probationProcessRepository.saveProbation(probationProcess, jwtHelper.getOrganizationCode(), mongoTemplate);
        updateUserInfoProbationResults(empId, result, addProbationDTO.getExtendedMonths());

        return probationProcess;
    }

    private void updateUserInfoProbationResults(String empId, String result, String extendedMonths) {
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        ProbationDetails probationDetails = Optional.ofNullable(userInfo.getSections())
                .map(Sections::getProbationDetails)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PROBATION));

        probationDetails.setResults(result);

        // Extract only numeric value from extendedMonths (e.g., "3 months" → "3")
        String normalizedMonths = extendedMonths.replaceAll("\\D", ""); // Remove non-numeric characters

        List<String> allowedMonths = Arrays.asList("3", "6");

        if ("Extended".equalsIgnoreCase(result) && allowedMonths.contains(normalizedMonths)) {
            LocalDate probationEndDate = DateUtil.parseDate(probationDetails.getProbationEndDate()).toLocalDate();
            LocalDate updatedEndDate = probationEndDate.plusMonths(Integer.parseInt(normalizedMonths));

            probationDetails.setProbationEndDate(DateUtil.parseDate(updatedEndDate));
            probationDetails.setExtendedMonths(normalizedMonths + " months");

            log.info("Updated probationEndDate & extendedMonths for empId {} to {} & {}", empId, updatedEndDate, normalizedMonths);
        } else {
            log.warn("Invalid extendedMonths value: {} for empId {}", extendedMonths, empId);
        }

        userInfoRepository.save(userInfo);
    }

    @Override
    public List<ProbationFetchDTO> getExtendedProbation() {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        Query query = new Query();
        query.addCriteria(Criteria.where("sections.hrInformation.primary.managerId").is(authenticatedEmpId));
        query.addCriteria(Criteria.where("sections.probationDetails.results").is("Extended"));

        List<UserInfo> probationUsers = mongoTemplate.find(query, UserInfo.class);

        if (probationUsers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PROBATION);
        }

        // Today's date for comparison
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
    public List<ProbationFetchDTO> getConfirmedProbation() {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        Query query = new Query();
        query.addCriteria(Criteria.where("sections.hrInformation.primary.managerId").is(authenticatedEmpId));
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
    public FeedbackFormDTO getFeedbackForm(String empId) {
        ProbationProcess probationProcess = probationProcessRepository.findByEmpId(empId,jwtHelper.getOrganizationCode(),mongoTemplate);
        return probationProcessMapper.mapToDTO(probationProcess);
    }


}


