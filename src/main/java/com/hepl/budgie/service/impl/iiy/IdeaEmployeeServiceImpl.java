package com.hepl.budgie.service.impl.iiy;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.iiy.IIYEmployeeDetails;
import com.hepl.budgie.dto.iiy.IdeaFetchDTO;
import com.hepl.budgie.dto.iiy.IdeaEmployeeRequestDTO;
import com.hepl.budgie.entity.iiy.IdeaDetails;
import com.hepl.budgie.entity.userinfo.HrInformation;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.userinfo.WorkingInformation;
import com.hepl.budgie.repository.iiy.IdeaEmployeeRepository;

import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.iiy.IdeaEmployeeService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdeaEmployeeServiceImpl implements IdeaEmployeeService {
    private final IdeaEmployeeRepository ideaEmployeeRepository;
    private final UserInfoRepository userInfoRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public void addIdea(IdeaEmployeeRequestDTO data) {
        log.info("Adding Idea");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        boolean existInOrg = userInfoRepository.existsByOrganisationAndEmpId(mongoTemplate, organizationCode,
                authUser);
        if (!existInOrg)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.UNAUTHORIZED_ACTIVITY);

        data.setIdeaId(ideaEmployeeRepository.findTopByOrderByIdDesc(organizationCode, mongoTemplate)
                .map(e -> AppUtils.generateUniqueId(e.getIdeaId()))
                .orElse("IDA000001"));
        boolean existStatus = ideaEmployeeRepository.existsByEmpIdAndIdea(mongoTemplate, organizationCode,
                authUser, data.getIdea());
        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.DUPLICATE_COURSE);

        ideaEmployeeRepository.insertOrUpdate(data, mongoTemplate, organizationCode, authUser, "", "insert");

        log.info("Idea Saved Successfully");

    }

    @Override
    public List<IdeaFetchDTO> fetchIdeaList(IdeaEmployeeRequestDTO data) {
        log.info("Fetching Idea List");
        String organizationCode = jwtHelper.getOrganizationCode();
        String fromDate = data.getFromDate();
        String toDate = data.getToDate();
        String empId = jwtHelper.getUserRefDetail().getEmpId();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime from = LocalDateTime.parse(fromDate + " 00:00:00", formatter);
        LocalDateTime to = LocalDateTime.parse(toDate + " 23:59:59", formatter);
        List<IdeaDetails> ideaDetails = ideaEmployeeRepository.findByEmpIdAndFromDateAndToDate(mongoTemplate,
                organizationCode, empId, from, to);
        List<IdeaFetchDTO> ideaFetchDTOs = new ArrayList<>();
        UserInfo info = getUserInfo(data.getEmpId()); // Reusable function call

        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for (IdeaDetails ideaDetail : ideaDetails) {
            IdeaFetchDTO ideaFetchDTO = new IdeaFetchDTO();
            ideaFetchDTO.setId(ideaDetail.getId());
            ideaFetchDTO.setEmpId(ideaDetail.getEmpId());
            // Set employee details
            IIYEmployeeDetails dto = mapUserInfoToEmployeeDetails(info, outputDateFormat);
            ideaFetchDTO.setEmployeeDetails(dto);
            String formattedDate = outputDateFormat.format(ideaDetail.getIdeaDate());
            ideaFetchDTO.setIdeaDate(formattedDate);
            ideaFetchDTO.setIdea(ideaDetail.getIdea());
            ideaFetchDTO.setCourse(ideaDetail.getCourse());
            ideaFetchDTO.setCategory(ideaDetail.getCategory());
            ideaFetchDTO.setWeightage(ideaDetail.getWeightage());
            ideaFetchDTO.setDescription(ideaDetail.getDescription());
            ideaFetchDTO.setRmStatus(ideaDetail.getRmStatus());
            ideaFetchDTO.setRmRemarks(ideaDetail.getRmRemarks());
            ideaFetchDTO.setRmWeightage(ideaDetail.getRmWeightage());

            ideaFetchDTOs.add(ideaFetchDTO);
        }
        return ideaFetchDTOs;
    }

    @Override
    public List<IdeaFetchDTO> fetchTeamIdeaList(IdeaEmployeeRequestDTO data) {
        log.info("Fetching Team Idea List");
        String organizationCode = jwtHelper.getOrganizationCode();
        List<IdeaDetails> ideaDetails = ideaEmployeeRepository.fetchIdeaByFilters(mongoTemplate, organizationCode,
                data);
        List<IdeaFetchDTO> ideaFetchDTOs = new ArrayList<>();
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for (IdeaDetails ideaDetail : ideaDetails) {
            IdeaFetchDTO ideaFetchDTO = new IdeaFetchDTO();
            ideaFetchDTO.setId(ideaDetail.getId());
            ideaFetchDTO.setEmpId(ideaDetail.getEmpId());
            // Fetch and set employee details
            UserInfo info = getUserInfo(data.getEmpId()); // Reusable function call
            // Set employee details
            IIYEmployeeDetails dto = mapUserInfoToEmployeeDetails(info, outputDateFormat);
            ideaFetchDTO.setEmployeeDetails(dto);
            ideaFetchDTO.setIdea(ideaDetail.getIdea());
            String formattedDate = outputDateFormat.format(ideaDetail.getIdeaDate());
            ideaFetchDTO.setIdeaDate(formattedDate);
            ideaFetchDTO.setCourse(ideaDetail.getCourse());
            ideaFetchDTO.setCategory(ideaDetail.getCategory());
            ideaFetchDTO.setWeightage(ideaDetail.getWeightage());
            ideaFetchDTO.setDescription(ideaDetail.getDescription());
            ideaFetchDTO.setRmStatus(ideaDetail.getRmStatus());
            ideaFetchDTO.setRmRemarks(ideaDetail.getRmRemarks());
            ideaFetchDTO.setRmWeightage(ideaDetail.getRmWeightage());

            ideaFetchDTOs.add(ideaFetchDTO);
        }
        return ideaFetchDTOs;
    }

    @Override
    public List<IdeaFetchDTO> fetchTeamIdeaReportList(IdeaEmployeeRequestDTO data) {
        log.info("Fetching Team Idea List");
        String organizationCode = jwtHelper.getOrganizationCode();
        List<IdeaDetails> ideaDetails = ideaEmployeeRepository.fetchIdeaReportByFilters(mongoTemplate, organizationCode,
                data);
        List<IdeaFetchDTO> ideaFetchDTOs = new ArrayList<>();
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for (IdeaDetails ideaDetail : ideaDetails) {
            IdeaFetchDTO ideaFetchDTO = new IdeaFetchDTO();
            ideaFetchDTO.setId(ideaDetail.getId());
            ideaFetchDTO.setEmpId(ideaDetail.getEmpId());
            // Fetch and set employee details
            UserInfo info = getUserInfo(data.getEmpId()); // Reusable function call

            // Set employee details
            IIYEmployeeDetails dto = mapUserInfoToEmployeeDetails(info, outputDateFormat);
            ideaFetchDTO.setEmployeeDetails(dto);
            String formattedDate = outputDateFormat.format(ideaDetail.getIdeaDate());
            ideaFetchDTO.setIdeaDate(formattedDate);
            ideaFetchDTO.setIdea(ideaDetail.getIdea());
            ideaFetchDTO.setCourse(ideaDetail.getCourse());
            ideaFetchDTO.setCategory(ideaDetail.getCategory());
            ideaFetchDTO.setWeightage(ideaDetail.getWeightage());
            ideaFetchDTO.setDescription(ideaDetail.getDescription());
            ideaFetchDTO.setRmStatus(ideaDetail.getRmStatus());
            ideaFetchDTO.setRmRemarks(ideaDetail.getRmRemarks());
            ideaFetchDTO.setRmWeightage(ideaDetail.getRmWeightage());

            ideaFetchDTOs.add(ideaFetchDTO);
        }
        return ideaFetchDTOs;
    }

    @Override
    public void approveTeamIdea(IdeaEmployeeRequestDTO data) {
        log.info("Approving Team Idea");

        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        IdeaDetails ideaDetails = ideaEmployeeRepository.findByIdAndEmpId(mongoTemplate, organizationCode, data.getId(),
                authUser);
        if (ideaDetails == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND);

        }
        ideaEmployeeRepository.insertOrUpdate(data, mongoTemplate, organizationCode, authUser, data.getIdeaId(),
                "approve");

        log.info("Idea Approved Successfully");
    }

    @Override
    public void rejectTeamIdea(IdeaEmployeeRequestDTO data) {

        log.info("Approving Team Idea");

        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        IdeaDetails ideaDetails = ideaEmployeeRepository.findByIdAndEmpId(mongoTemplate, organizationCode, data.getId(),
                authUser);
        if (ideaDetails == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND);

        }
        ideaEmployeeRepository.insertOrUpdate(data, mongoTemplate, organizationCode, authUser, data.getIdeaId(),
                "reject");

        log.info("Idea Rejected Successfully");

    }

    public String getFinancialYear() {
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int startYear, endYear;

        // Check if the current month is before April
        if (currentDate.getMonthValue() < 4) {
            // Financial year starts in April of the previous year and ends in March of
            // the
            // current year
            startYear = currentYear - 1;
            endYear = currentYear;
        } else {
            // Financial year starts in April of the current year and ends in March of
            // next year
            startYear = currentYear;
            endYear = currentYear + 1;
        }
        // Format as "YYYY - YYYY"
        return String.format("%d - %d", startYear, endYear);
    }

    private UserInfo getUserInfo(String empId) {
        return userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.RESOURCE_NOT_FOUND));
    }

    private IIYEmployeeDetails mapUserInfoToEmployeeDetails(UserInfo info, SimpleDateFormat outputDateFormat) {
        IIYEmployeeDetails dto = new IIYEmployeeDetails();

        String empFirstName = info.getSections().getBasicDetails().getFirstName();
        String empLastName = info.getSections().getBasicDetails().getLastName();
        dto.setEmpName(empFirstName + " " + empLastName);

        HrInformation hrInfo = info.getSections().getHrInformation();

        // Fetch manager details
        Map<String, UserInfo> managerInfoMap = Stream.of(
                hrInfo.getPrimary().getManagerId(),
                hrInfo.getReviewer().getManagerId(),
                hrInfo.getDivisionHead().getManagerId()).distinct()
                .collect(Collectors.toMap(Function.identity(), this::getUserInfo));

        String rmId = hrInfo.getPrimary().getManagerId();
        dto.setReportingManagerEmpId(rmId);
        dto.setReportingManagerName(getManagerFullName(managerInfoMap, rmId));

        String reviewerId = hrInfo.getReviewer().getManagerId();
        dto.setReviewerId(reviewerId);
        dto.setReviewerName(getManagerFullName(managerInfoMap, reviewerId));

        String divisionHeadId = hrInfo.getDivisionHead().getManagerId();
        dto.setDivisionHeadId(divisionHeadId);
        dto.setDivisionHeadName(getManagerFullName(managerInfoMap, divisionHeadId));

        WorkingInformation workingInfo = info.getSections().getWorkingInformation();
        dto.setDesignation(workingInfo.getDesignation());
        dto.setDepartment(workingInfo.getDepartment());
        dto.setDateOfJoining(outputDateFormat.format(workingInfo.getDoj()));
        dto.setGroupOfJoining(outputDateFormat.format(workingInfo.getGroupOfDOJ()));

        return dto;
    }

    private String getManagerFullName(Map<String, UserInfo> managerInfoMap, String managerId) {
        UserInfo managerInfo = managerInfoMap.get(managerId);
        return (managerInfo != null)
                ? managerInfo.getSections().getBasicDetails().getFirstName() + " "
                        + managerInfo.getSections().getBasicDetails().getLastName()
                : "";
    }

}
