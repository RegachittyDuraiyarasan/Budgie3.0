package com.hepl.budgie.service.impl.people;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;
import com.hepl.budgie.dto.employee.TeamResponseDTO;
import com.hepl.budgie.dto.people.PeopleDTO;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.people.PeopleMapper;
import com.hepl.budgie.repository.userinfo.UserExpEducationRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.people.PeopleService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeopleServiceImplementation implements PeopleService {

        private final UserInfoRepository userInfoRepository;
        private final UserExpEducationRepository userExpEducationRepository;
        private final MongoTemplate mongoTemplate;
        private final PeopleMapper peopleMapper;
        private final JWTHelper jwtHelper;

        @Override
        public List<PeopleDTO> getActivePeople(String department, String designation, String workLocation) {
                String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
                log.info("authenticatedEmpId {}", authenticatedEmpId);
                UserInfo userInfo = userInfoRepository.findByEmpId(authenticatedEmpId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                AppMessages.ID_NOT_FOUND));

                Query query = new Query();
                query.addCriteria(Criteria.where("status").is(Status.ACTIVE.label));

                if (department != null && !department.isEmpty()) {
                        query.addCriteria(Criteria.where("sections.workingInformation.department").is(department));
                }
                if (designation != null && !designation.isEmpty()) {
                        query.addCriteria(Criteria.where("sections.workingInformation.designation").is(designation));
                }
                if (workLocation != null && !workLocation.isEmpty()) {
                        query.addCriteria(Criteria.where("sections.workingInformation.workLocation").is(workLocation));
                }

                List<UserInfo> userInfoList = mongoTemplate.find(query, UserInfo.class, "userinfo");

                Query starredQuery = new Query();
                starredQuery.fields().include("starredEmpDetails");
                List<UserInfo> starredData = mongoTemplate.find(starredQuery, UserInfo.class, "userinfo");

                // Extract starred employee IDs
                Set<String> starredEmpIds = new HashSet<>(
                                Optional.ofNullable(userInfo.getStarredEmpDetails()).orElse(List.of()));
                // Map to DTO and set `isStarred`
                return userInfoList.stream()
                                .map(user -> {
                                        PeopleDTO dto = peopleMapper.mapToDTO(user);
                                        dto.setIsStarred(starredEmpIds.contains(user.getEmpId()));
                                        return dto;
                                }).toList();
        }

        @Override
        public List<PeopleDTO> getStarredPeople(String department, String designation, String workLocation) {
                // Get authenticated user empId
                String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();

                // Step 1: Fetch only the authenticated user from DB
                Query authUserQuery = new Query();
                authUserQuery.addCriteria(Criteria.where("empId").is(authenticatedEmpId)
                                .and("status").is(Status.ACTIVE.label));

                UserInfo authenticatedUser = mongoTemplate.findOne(authUserQuery, UserInfo.class);

                if (authenticatedUser == null || authenticatedUser.getStarredEmpDetails() == null
                                || authenticatedUser.getStarredEmpDetails().isEmpty()) {
                        return Collections.emptyList(); // Return empty list if no starred employees
                }

                // Step 2: Extract the starred employee IDs of the authenticated user
                Set<String> starredEmpIds = new HashSet<>(authenticatedUser.getStarredEmpDetails());

                // Step 3: Create a query to fetch details of starred employees
                Query query1 = new Query();
                query1.addCriteria(Criteria.where("empId").in(starredEmpIds));

                // Apply department, designation, and workLocation filters dynamically
                List.of(
                                Optional.ofNullable(department)
                                                .filter(dep -> !dep.isEmpty())
                                                .map(dep -> Criteria.where("sections.workingInformation.department")
                                                                .is(dep)),
                                Optional.ofNullable(designation)
                                                .filter(des -> !des.isEmpty())
                                                .map(des -> Criteria.where("sections.workingInformation.designation")
                                                                .is(des)),
                                Optional.ofNullable(workLocation)
                                                .filter(loc -> !loc.isEmpty())
                                                .map(loc -> Criteria.where("sections.workingInformation.workLocation")
                                                                .is(loc)))
                                .stream()
                                .flatMap(Optional::stream)
                                .forEach(query1::addCriteria);

                // Step 4: Fetch starred employee details
                List<UserInfo> starredEmployees = mongoTemplate.find(query1, UserInfo.class);

                // Step 5: Convert to DTO and mark them as starred
                return starredEmployees.stream()
                                .map(user -> {
                                        PeopleDTO dto = peopleMapper.mapToDTO(user);
                                        dto.setIsStarred(true);
                                        return dto;
                                }).toList();
        }

        @Override
        public List<PeopleDTO> getEmployee(String empId) {
                // Query to fetch the requested employee's details
                Query query = new Query();
                query.addCriteria(Criteria.where("empId").is(empId));
                List<UserInfo> userInfoList = mongoTemplate.find(query, UserInfo.class);
                String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
                // Query to fetch the logged-in user's starred employees
                Query starredQuery = new Query();
                starredQuery.addCriteria(Criteria.where("empId").is(authenticatedEmpId));
                starredQuery.fields().include("starredEmpDetails");
                UserInfo loggedInUser = mongoTemplate.findOne(starredQuery, UserInfo.class);

                // Get the list of starred employee IDs for quick lookup
                Set<String> starredEmpIds = loggedInUser != null && loggedInUser.getStarredEmpDetails() != null
                                ? new HashSet<>(loggedInUser.getStarredEmpDetails())
                                : Collections.emptySet();

                // Map UserInfo to PeopleDTO and check if the empId is starred by the logged-in
                // user
                return userInfoList.stream()
                                .map(user -> {
                                        PeopleDTO dto = peopleMapper.mapToDTO(user);
                                        dto.setIsStarred(starredEmpIds.contains(empId));
                                        return dto;
                                })
                                .toList();
        }

        @Override
        public boolean toggleStarredEmployee(String newEmpId) {
                String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
                UserInfo user = userInfoRepository.findByEmpId(authenticatedEmpId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                AppMessages.ID_NOT_FOUND));

                boolean starredEmpIdExists = userInfoRepository.existsByEmpId(newEmpId);
                if (!starredEmpIdExists) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.STARRED_ID_NOT_FOUND);
                }

                // Ensure starredEmpDetails is initialized
                if (user.getStarredEmpDetails() == null) {
                        user.setStarredEmpDetails(new ArrayList<>());
                }

                List<String> starredList = user.getStarredEmpDetails();

                // If already starred, unstar it; otherwise, star it
                boolean isStarred;
                if (starredList.contains(newEmpId)) {
                        starredList.remove(newEmpId);
                        isStarred = false;
                } else {
                        starredList.add(newEmpId);
                        isStarred = true;
                }

                userInfoRepository.save(user);
                return isStarred;
        }

        @Override
        public EmployeeOrgChartDTO getEmployeeOrgChart() {
                log.info("Fetching org chart details");
                UserInfo userInfo = userInfoRepository
                                .getOrganizationChart(jwtHelper.getOrganizationCode(), mongoTemplate)
                                .getUniqueMappedResult();

                Map<String, UserInfo> userEmployeeMap = Optional.ofNullable(userInfo)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                AppMessages.NO_BUSINESS_HEAD))
                                .getChildren().stream()
                                .collect(Collectors.toMap(UserInfo::getEmpId, Function.identity()));
                List<UserInfo> removeSubsidiaries = new ArrayList<>();
                for (UserInfo detail : userInfo.getChildren()) {
                        if (userEmployeeMap.containsKey(
                                        detail.getSections().getHrInformation().getPrimary().getManagerId())) {
                                UserInfo tmpEmployee = userEmployeeMap
                                                .get(detail.getSections().getHrInformation().getPrimary()
                                                                .getManagerId());
                                if (tmpEmployee.getChildren() == null) {
                                        tmpEmployee.setChildren(new ArrayList<>());
                                        tmpEmployee.getChildren().add(detail);
                                } else {
                                        tmpEmployee.getChildren().add(detail);
                                }
                        }
                        if (!detail.getSections().getHrInformation().getPrimary().getManagerId().equals(userInfo
                                        .getEmpId())) {
                                removeSubsidiaries.add(detail);
                        }

                }
                userInfo.getChildren().removeAll(removeSubsidiaries);
                userInfo.setDirect(userInfo.getDirectCount());
                userInfo.setSubsidiaries(userInfo.getSubsidiariesCount());
                if(userInfo.getIdCardDetails() != null){
                   userInfo.setIdCardDetails(userInfo.getIdCardDetails());
                }
                calculateCounts(userInfo.getChildren());
                return peopleMapper.toOrgChartDTO(userInfo, LocaleContextHolder.getTimeZone().getID());
        }

        private void calculateCounts(List<UserInfo> childrens) {
                if (childrens != null) {
                        for (UserInfo userDetail : childrens) {
                                userDetail.setDirect(userDetail.getDirectCount());
                                userDetail.setSubsidiaries(userDetail.getSubsidiariesCount());
                                calculateCounts(userDetail.getChildren());
                        }
                }

        }

        @Override
        public TeamResponseDTO getTeams() {
                log.info("Fetching team based on token");
                UserRef loggedInUser = jwtHelper.getUserRefDetail();
                List<EmployeeOrgChartDTO> employeeDetails = userInfoRepository
                                .getTeamListByEmpId(loggedInUser.getEmpId(), loggedInUser.getOrganizationCode(),
                                                LocaleContextHolder.getTimeZone(), mongoTemplate)
                                .getMappedResults();
                List<String> reporteeEnabledUser = employeeDetails.stream().map(EmployeeOrgChartDTO::getEmpId).toList();
                employeeDetails = calculateExperience(employeeDetails, reporteeEnabledUser);

                return TeamResponseDTO.builder()
                                .userWithReportees(userInfoRepository.getEmployeeDetailsIfReportee(
                                                loggedInUser.getOrganizationCode(),
                                                reporteeEnabledUser,
                                                mongoTemplate))
                                .reportees(employeeDetails).build();
        }

        private List<EmployeeOrgChartDTO> calculateExperience(List<EmployeeOrgChartDTO> employeeDetails,
                        List<String> reporteeEnabledUser) {
                List<EmployeeActiveDTO> employeeExperience = userExpEducationRepository.getEmployeeExperienceDetails(
                                reporteeEnabledUser,
                                mongoTemplate);
                Map<String, EmployeeActiveDTO> employeeExperienceMap = employeeExperience.stream()
                                .collect(Collectors.toMap(EmployeeActiveDTO::getEmpId, Function.identity()));
                for (EmployeeOrgChartDTO employeeDetail : employeeDetails) {
                        EmployeeActiveDTO employeeActiveDTO = employeeExperienceMap.getOrDefault(
                                        employeeDetail.getEmpId(),
                                        EmployeeActiveDTO.builder().years(0).months(0).days(0).build());
                        employeeDetail.setExperience(
                                        getExperienceDetailsPerEmployee(employeeDetail.getYears(),
                                                        employeeDetail.getMonths(), employeeDetail.getDays()));
                        employeeDetail.setOtherExperience(
                                        getExperienceDetailsPerEmployee(employeeActiveDTO.getYears(),
                                                        employeeActiveDTO.getMonths(), employeeActiveDTO.getDays()));
                }

                return employeeDetails;
        }

        private String getExperienceDetailsPerEmployee(int years, int month, int days) {
                int remaningDays = days - ((days / 365) * 30);
                if (remaningDays > 30) {
                        remaningDays = remaningDays - ((remaningDays / 30) * 30);
                }
                return String.format("%dY %dM %dD", years,
                                month - (years * 12), remaningDays);
        }

        @Override
        public List<EmployeeOrgChartDTO> getTeamsByEmployee(String empId) {
                log.info("Fetching team by employee {}", empId);
                List<EmployeeOrgChartDTO> employeeDetails = userInfoRepository
                                .getTeamListByEmpId(empId, jwtHelper.getOrganizationCode(),
                                                LocaleContextHolder.getTimeZone(),
                                                mongoTemplate)
                                .getMappedResults();
                List<String> reporteeEnabledUser = employeeDetails.stream().map(EmployeeOrgChartDTO::getEmpId).toList();
                employeeDetails = calculateExperience(employeeDetails, reporteeEnabledUser);
                return employeeDetails;
        }
}
