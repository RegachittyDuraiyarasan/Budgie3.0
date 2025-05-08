
package com.hepl.budgie.service.impl.iiy;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.iiy.*;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.iiy.Action;
import com.hepl.budgie.entity.iiy.Course;
import com.hepl.budgie.entity.iiy.CourseCategory;
import com.hepl.budgie.entity.iiy.IIYDetails;
import com.hepl.budgie.entity.userinfo.HrInformation;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.userinfo.WorkingInformation;
import com.hepl.budgie.repository.iiy.CourseRepository;
import com.hepl.budgie.repository.iiy.EmployeeRepository;

import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.iiy.EmployeeService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

        private final EmployeeRepository employeeRepository;
        private final UserInfoRepository userInfoRepository;
        private final CourseRepository courseRepository;
        private final MongoTemplate mongoTemplate;
        private final CourseCategoryServiceImpl courseCategoryServiceImpl;
        private final JWTHelper jwtHelper;
        private final FileService fileService;

        @Override
        public void addActivityWithCertificate(ActivityRequestDTO data, MultipartFile file) throws IOException {
                log.info("Adding Activity");
                String organizationCode = jwtHelper.getOrganizationCode();
                String authUser = jwtHelper.getUserRefDetail().getEmpId();
                boolean existInOrg = userInfoRepository.existsByOrganisationAndEmpId(mongoTemplate, organizationCode,
                                authUser);
                if (!existInOrg)
                        throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.UNAUTHORIZED_ACTIVITY);

                data.setActivityId(employeeRepository.findTopByOrderByIdDesc(organizationCode, mongoTemplate)
                                .map(e -> AppUtils.generateUniqueId(e.getActivityId()))
                                .orElse("IA000001"));
                log.info("Fetching course for Org: {}, Course: {}", organizationCode, data.getCourse());
                Course courseMaster = courseRepository.findByCourseName(mongoTemplate, organizationCode,
                                data.getCourse());

                String courseCategory = Action.OTHERS.label;
                if (courseMaster != null) {
                        courseCategory = courseMaster.getCategory();
                } else {
                        log.warn("Course not found: {}", data.getCourse());
                }

                data.setEmpId(authUser);
                data.setCourseCategory(courseCategory);
                data.setFinancialYear(getFinancialYear());

                log.info("Certification: {}, File: {}", data.getCertification(),
                                file != null ? file.getOriginalFilename() : "No file uploaded");

                if ("Yes".equalsIgnoreCase(data.getCertification()) && file != null && !file.isEmpty()) {
                        String fileName = fileService.uploadFile(file, FileType.IIY_CERTIFICATE, "");
                        data.setFileName(fileName);
                } else {
                        data.setFileName("");
                }

                employeeRepository.insertOrUpdate(data, mongoTemplate, organizationCode, authUser, Action.INSERT.label,
                                LocaleContextHolder.getTimeZone().getID());
                log.info("Activity Saved Successfully");
        }

        public List<ActivityFetchDTO> fetchActivityList(IIYEmployeeRequestDTO data) {
                String authUser = jwtHelper.getUserRefDetail().getEmpId();
                log.info("Fetching Activity List for Emp ID: {}", authUser);

                // Extract organization code
                String organizationCode = jwtHelper.getOrganizationCode();

                log.info("Organization Code: {}", organizationCode);

                // Parse date range
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                LocalDateTime from = LocalDateTime.parse(data.getFromDate() + " 00:00:00", formatter);
                LocalDateTime to = LocalDateTime.parse(data.getToDate() + " 23:59:59", formatter);
                log.info("Fetching data from {} to {}", from, to);

                // Fetch details from repository
                log.info("Querying employeeRepository for records...");
                List<IIYDetails> iiyDetails = employeeRepository.findByEmpIdAndFromDateAndToDate(mongoTemplate,
                                organizationCode, authUser, from, to);
                log.info("Fetched {} records from repository", iiyDetails.size());

                // Prepare DTO list
                List<ActivityFetchDTO> fetchDTOs = new ArrayList<>();
                log.info("Fetching user info for Emp ID: {}", authUser);
                UserInfo info = getUserInfo(authUser);

                SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");

                for (IIYDetails iiyDetail : iiyDetails) {
                        log.info("Processing record ID: {}", iiyDetail.getId());

                        ActivityFetchDTO fetchDTO = new ActivityFetchDTO();
                        fetchDTO.setId(iiyDetail.getId());
                        fetchDTO.setEmpId(iiyDetail.getEmpId());

                        // Map employee details
                        log.info("Mapping user info to employee details...");
                        IIYEmployeeDetails dto = mapUserInfoToEmployeeDetails(info, outputDateFormat);
                        fetchDTO.setEmployeeDetails(dto);

                        // Set IIY details
                        fetchDTO.setIiyDate(AppUtils.formatZonedDate(
                                        "dd-MM-yyyy",
                                        iiyDetail.getIiyDate(),
                                        LocaleContextHolder.getTimeZone().getID()));

                        fetchDTO.setCourseCategory(iiyDetail.getCourseCategory());
                        fetchDTO.setCourse(iiyDetail.getCourse());
                        fetchDTO.setDuration(iiyDetail.getDuration());
                        fetchDTO.setDescription(iiyDetail.getDescription());
                        fetchDTO.setRemarks(iiyDetail.getRemarks());
                        fetchDTO.setRmStatus(iiyDetail.getRmStatus());
                        fetchDTO.setRmRemarks(iiyDetail.getRmRemarks());
                        fetchDTO.setCertification(iiyDetail.getCertification());
                        fetchDTO.setFileName(iiyDetail.getFileName());

                        fetchDTOs.add(fetchDTO);
                        log.info("Successfully processed record ID: {}", iiyDetail.getId());
                }

                log.info("Successfully fetched {} activity records for Emp ID: {}", fetchDTOs.size(), authUser);
                return fetchDTOs;
        }

        @Override
        public List<ActivityFetchDTO> fetchTeamActivityList(IIYEmployeeRequestDTO data) {
                String authUser = jwtHelper.getUserRefDetail().getEmpId();
                log.info("Fetching Team Activity List for Emp ID: {}", authUser);
                data.setReportingManagerEmpId(authUser);
                String organizationCode = jwtHelper.getOrganizationCode();
                List<IIYDetails> iiyDetails = employeeRepository.fetchActivityByFilters(mongoTemplate, organizationCode,
                                data);
                List<ActivityFetchDTO> fetchDTOs = new ArrayList<>();
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");

                for (IIYDetails iiyDetail : iiyDetails) {
                        ActivityFetchDTO fetchDTO = new ActivityFetchDTO();
                        fetchDTO.setId(iiyDetail.getId());
                        fetchDTO.setEmpId(iiyDetail.getEmpId());

                        // Fetch and set employee details
                        UserInfo info = getUserInfo(iiyDetail.getEmpId());

                        // Set employee details
                        IIYEmployeeDetails dto = mapUserInfoToEmployeeDetails(info, outputDateFormat);
                        fetchDTO.setEmployeeDetails(dto);
                        // Set IIY details
                        fetchDTO.setIiyDate(AppUtils.formatZonedDate(
                                        "dd-MM-yyyy",
                                        iiyDetail.getIiyDate(),
                                        LocaleContextHolder.getTimeZone().getID()));
                        fetchDTO.setCourseCategory(iiyDetail.getCourseCategory());
                        fetchDTO.setCourse(iiyDetail.getCourse());
                        fetchDTO.setDuration(iiyDetail.getDuration());
                        fetchDTO.setDescription(iiyDetail.getDescription());
                        fetchDTO.setRemarks(iiyDetail.getRemarks());
                        fetchDTO.setCertification(iiyDetail.getCertification());
                        // fetchDTO.setStatus(iiyDetail.getStatus());
                        fetchDTO.setRmStatus(iiyDetail.getRmStatus());
                        fetchDTO.setRmRemarks(iiyDetail.getRmRemarks());
                        fetchDTO.setFileName(iiyDetail.getFileName());
                        fetchDTOs.add(fetchDTO);
                }
                return fetchDTOs;
        }

        @Override
        public List<ActivityFetchDTO> fetchTeamActivityReportList(IIYEmployeeRequestDTO data, String type) {
                String authUser = jwtHelper.getUserRefDetail().getEmpId();
                if (type.equals(Action.TEAM.label)) {
                        data.setReportingManagerEmpId(authUser);
                        log.info("Fetching Team Activity List for Emp ID: {}", authUser);
                } else {
                        log.info("Fetching Overall Activity Report List");
                }

                String organizationCode = jwtHelper.getOrganizationCode();
                List<IIYDetails> iiyDetails = employeeRepository.fetchActivityReportByFilters(mongoTemplate,
                                organizationCode,
                                data);

                List<ActivityFetchDTO> fetchDTOs = new ArrayList<>();
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");

                for (IIYDetails iiyDetail : iiyDetails) {
                        ActivityFetchDTO fetchDTO = new ActivityFetchDTO();
                        fetchDTO.setId(iiyDetail.getId());
                        fetchDTO.setEmpId(iiyDetail.getEmpId());
                        // Fetch and set employee details
                        UserInfo info = getUserInfo(iiyDetail.getEmpId());

                        // Set employee details
                        IIYEmployeeDetails dto = mapUserInfoToEmployeeDetails(info, outputDateFormat);
                        fetchDTO.setEmployeeDetails(dto);

                        // Set IIY details

                        fetchDTO.setIiyDate(AppUtils.formatZonedDate(
                                        "dd-MM-yyyy",
                                        iiyDetail.getIiyDate(),
                                        LocaleContextHolder.getTimeZone().getID()));

                        fetchDTO.setCourseCategory(iiyDetail.getCourseCategory());
                        fetchDTO.setCourse(iiyDetail.getCourse());
                        fetchDTO.setDuration(iiyDetail.getDuration());
                        fetchDTO.setDescription(iiyDetail.getDescription());
                        fetchDTO.setRemarks(iiyDetail.getRemarks());
                        fetchDTO.setCertification(iiyDetail.getCertification());
                        // fetchDTO.setStatus(iiyDetail.getStatus());
                        fetchDTO.setRmStatus(iiyDetail.getRmStatus());
                        fetchDTO.setRmRemarks(iiyDetail.getRmRemarks());
                        fetchDTO.setFileName(iiyDetail.getFileName());
                        fetchDTOs.add(fetchDTO);
                }
                return fetchDTOs;
        }

        @Override
        public Map<String, List<String>> approveTeamActivity(List<ActivityRequestDTO> requests) {
                log.info("Approving Team Activity");

                String organizationCode = jwtHelper.getOrganizationCode();
                String authUser = jwtHelper.getUserRefDetail().getEmpId();
                List<String> successEmpIds = new ArrayList<>();
                List<String> errorMessages = new ArrayList<>();

                for (ActivityRequestDTO request : requests) {
                        IIYDetails iiyDetails = employeeRepository.findByActivityIdAndEmpId(
                                        mongoTemplate, organizationCode, request.getActivityId(), request.getEmpId());

                        if (iiyDetails == null) {
                                errorMessages
                                                .add("No Data found for EmpId: " + request.getEmpId() + " on date: "
                                                                + request.getActivityId());
                                continue;
                        }

                        employeeRepository.insertOrUpdate(request, mongoTemplate, organizationCode, authUser,
                                        Action.APPROVE.label,
                                        LocaleContextHolder.getTimeZone().getID());

                        successEmpIds.add(request.getEmpId());
                }

                Map<String, List<String>> response = new HashMap<>();
                response.put("success", successEmpIds);
                response.put("errors", errorMessages);

                return response;
        }

        @Override
        public Map<String, List<String>> rejectTeamActivity(List<ActivityRequestDTO> requests) {
                log.info("Rejecting Team Activity");
                String organizationCode = jwtHelper.getOrganizationCode();
                String authUser = jwtHelper.getUserRefDetail().getEmpId();
                List<String> successEmpIds = new ArrayList<>();
                List<String> errorMessages = new ArrayList<>();

                for (ActivityRequestDTO request : requests) {
                        IIYDetails iiyDetails = employeeRepository.findByActivityIdAndEmpId(mongoTemplate,
                                        organizationCode,
                                        request.getActivityId(), request.getEmpId());

                        if (iiyDetails == null) {
                                errorMessages
                                                .add("No Data found for EmpId: " + request.getEmpId() + " on date: " +
                                                                request.getActivityId());
                                continue;
                        }

                        employeeRepository.insertOrUpdate(request, mongoTemplate, organizationCode, authUser,
                                        Action.REJECT.label,
                                        LocaleContextHolder.getTimeZone().getID());

                        successEmpIds.add(request.getEmpId());

                }
                Map<String, List<String>> response = new HashMap<>();
                response.put("success", successEmpIds);
                response.put("errors", errorMessages);

                return response;

        }

        @Override
        public List<IIYReportFetchDTO> fetchIiyReportList(IIYEmployeeRequestDTO data, String type) {
                String organizationCode = jwtHelper.getOrganizationCode();
                String authUser = jwtHelper.getUserRefDetail().getEmpId();
                if (type.equals(Action.EMPLOYEE.label)) {
                        data.setEmpId(authUser);
                        log.info("Fetching IIY Employee Dashboard for Emp ID: {}", authUser);
                } else {
                        log.info("Fetching IIY Report List");
                }

                List<IIYDetails> iiyDetails = employeeRepository.fetchActivityReportByFilters(mongoTemplate,
                                organizationCode,
                                data);

                // Apply courseCategory filter if provided
                if (data.getCourseCategory() != null && !data.getCourseCategory().isEmpty()) {
                        iiyDetails = iiyDetails.stream()
                                        .filter(detail -> data.getCourseCategory()
                                                        .equalsIgnoreCase(detail.getCourseCategory()))
                                        .toList();
                }
                // Apply financial Year filter if provided
                if (data.getFinancialYear() != null && !data.getFinancialYear().isEmpty()) {
                        iiyDetails = iiyDetails.stream()
                                        .filter(detail -> data.getFinancialYear()
                                                        .equalsIgnoreCase(detail.getFinancialYear()))
                                        .toList();
                }

                List<CourseCategory> courseCategoryList = courseCategoryServiceImpl.fetchCourseCategory();
                List<String> courseCategoryNames = courseCategoryList.stream()
                                .map(CourseCategory::getCategoryName).toList();

                Map<String, Map<String, Long>> durationByEmpIdAndCategory = iiyDetails.stream()
                                .collect(Collectors.groupingBy(
                                                IIYDetails::getEmpId,
                                                Collectors.groupingBy(
                                                                detail -> {
                                                                        if (detail.getCourse().toLowerCase()
                                                                                        .contains(Action.COURSES_AND_CERTIFICATE.label)) {
                                                                                return Action.OTHERS.label;
                                                                        }
                                                                        return detail.getCourseCategory();
                                                                },
                                                                Collectors.summingLong(detail -> {
                                                                        String durationString = detail.getDuration();
                                                                        String[] timeParts = durationString.split(":");
                                                                        long hours = Long.parseLong(timeParts[0]);
                                                                        long minutes = Long.parseLong(timeParts[1]);
                                                                        return (hours * 60) + minutes;
                                                                })

                                                )));

                durationByEmpIdAndCategory.forEach((empId, categoryDurations) -> {
                        long othersDuration = categoryDurations.getOrDefault(Action.OTHERS.label, 0L);

                        categoryDurations.forEach((category, duration) -> {
                                if (category.toLowerCase().contains(Action.COURSES_AND_CERTIFICATE.label)) {
                                        categoryDurations.put(category, duration + othersDuration);
                                }
                        });

                });

                Map<String, Set<String>> coursesByEmpId = iiyDetails.stream()
                                .collect(Collectors.groupingBy(
                                                IIYDetails::getEmpId,
                                                Collectors.mapping(IIYDetails::getCourse, Collectors.toSet())));
                Map<String, Set<String>> coursesCategoryByEmpId = iiyDetails.stream()
                                .collect(Collectors.groupingBy(
                                                IIYDetails::getEmpId,
                                                Collectors.mapping(IIYDetails::getCourseCategory, Collectors.toSet())));

                List<IIYReportFetchDTO> reportDTOs = new ArrayList<>();

                for (Map.Entry<String, Map<String, Long>> entry : durationByEmpIdAndCategory.entrySet()) {
                        String empId = entry.getKey();
                        Map<String, Long> categoryDurations = entry.getValue();
                        long totalMinutes = categoryDurations.values().stream().mapToLong(Long::longValue).sum();
                        long totalHours = totalMinutes / 60;
                        long totalMinutesRemainder = totalMinutes % 60;
                        String totalDuration = String.format("%02d:%02d", totalHours,
                                        totalMinutesRemainder);
                        // Calculate remaining hours and minutes
                        long defaultMinutes = 400; // Default is 6.40 hrs per month in minutes

                        long monthsBetween = 0;
                        String startCutOffDate = "2024-04-01";
                        String endCutOffDate = "2025-03-31";
                        String financialYear = data.getFinancialYear();
                        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                        // Fetch and set employee details
                        Optional<UserInfo> existingEmployee = userInfoRepository.findByEmpId(empId);
                        UserInfo info = getUserInfo(empId);

                        if (existingEmployee.isPresent()) {
                                // String dateOfJoining = outputDateFormat
                                // .format(existingEmployee.get().getSections().getWorkingInformation().getDoj());
                                // String groupOfJoining = outputDateFormat
                                // .format(existingEmployee.get().getSections().getWorkingInformation()
                                // .getGroupOfDOJ());
                                String dateOfJoining = AppUtils.parseZonedDate(
                                                "yyyy-MM-dd",
                                                existingEmployee.get().getSections().getWorkingInformation().getDoj()
                                                                .withZoneSameInstant(ZoneId.of(LocaleContextHolder
                                                                                .getTimeZone().getID())),
                                                LocaleContextHolder.getTimeZone().getID());
                                String groupOfJoining = AppUtils.parseZonedDate(
                                                "yyyy-MM-dd",
                                                existingEmployee.get().getSections().getWorkingInformation()
                                                                .getGroupOfDOJ()
                                                                .withZoneSameInstant(ZoneId.of(LocaleContextHolder
                                                                                .getTimeZone().getID())),
                                                LocaleContextHolder.getTimeZone().getID());
                                log.info("EmpId: {}, Total dateOfJoining: {},groupOfJoining: {}", empId,
                                                dateOfJoining, groupOfJoining);

                                if (groupOfJoining != null && !groupOfJoining.isEmpty()) {
                                        monthsBetween = calculateMonthsBetween(groupOfJoining, startCutOffDate,
                                                        endCutOffDate);

                                        log.info("EmpId: {}, groupOfJoining", empId, groupOfJoining);
                                        log.info("EmpId: {}, monthsBetween: {}", empId, monthsBetween);

                                } else {
                                        monthsBetween = calculateMonthsBetween(dateOfJoining, startCutOffDate,
                                                        endCutOffDate);

                                        log.info("EmpId: {}, dateOfJoining", empId, dateOfJoining);
                                        log.info("EmpId: {}, monthsBetween: {}", empId, monthsBetween);

                                }
                        }

                        long incrementMonthOld = Math.min(monthsBetween, 12);
                        log.info("EmpId: {}, incrementMonthOld: {}", empId, incrementMonthOld);
                        long targetMonthsBetween = calculateMonths(startCutOffDate, endCutOffDate);
                        log.info("EmpId: {}, targetMonthsBetween: {}", empId, targetMonthsBetween);
                        long targetMinutes = 400 * monthsBetween; // Default is 6.40 hrs per month in
                        // minutes
                        long targetHours = targetMinutes / 60;
                        long targetMinutesRemainder = targetMinutes % 60;
                        String targetDuration = String.format("%02d:%02d", targetHours,
                                        targetMinutesRemainder);

                        // Cap totalMinutes to defaultMinutes if it exceeds month targetMinutes
                        long cappedMinutes = Math.min(totalMinutes, targetMinutes);

                        long remainingMinutes = Math.max(targetMinutes - cappedMinutes, 0);
                        long remainingHours = remainingMinutes / 60;
                        long remainingMinutesRemainder = remainingMinutes % 60;

                        String remainingDuration = String.format("%02d:%02d", remainingHours,
                                        remainingMinutesRemainder);

                        long totalprorate = Math.max(cappedMinutes / defaultMinutes, 2) *
                                        incrementMonthOld;

                        log.info("EmpId: {}, cappedMinutes: {}, totalprorate: {}", empId,
                                        cappedMinutes, totalprorate);
                        // Log the remaining hours in "HH:mm" format
                        log.info("EmpId: {}, Total Duration: {}, Remaining Duration: {}", empId,
                                        totalDuration, remainingDuration);

                        Map<String, String> formattedCategoryDurations = new HashMap<>();
                        for (String categoryName : courseCategoryNames) {
                                long categoryMinutes = categoryDurations.getOrDefault(categoryName, 0L);
                                if (categoryName.equalsIgnoreCase(Action.COURSES_AND_CERTIFICATE.label)) {
                                        long othersCategoryMinutes = categoryDurations.getOrDefault(Action.OTHERS.label,
                                                        0L);

                                        long hours = (categoryMinutes + othersCategoryMinutes) / 60;
                                        long minutes = (categoryMinutes + othersCategoryMinutes) % 60;
                                        formattedCategoryDurations.put(categoryName, String.format("%02d:%02d",
                                                        hours, minutes));

                                } else {
                                        long hours = categoryMinutes / 60;
                                        long minutes = categoryMinutes % 60;
                                        formattedCategoryDurations.put(categoryName, String.format("%02d:%02d",
                                                        hours, minutes));
                                }

                        }

                        Set<String> coursesSet = coursesByEmpId.getOrDefault(empId, new HashSet<>());
                        String[] distinctCourses = coursesSet.toArray(new String[0]);
                        Set<String> coursesCategorySet = coursesCategoryByEmpId.getOrDefault(empId,
                                        new HashSet<>());
                        String[] distinctCoursesCategory = coursesCategorySet.toArray(new String[0]);

                        // Create and populate the DTO
                        IIYReportFetchDTO iiYReportFetchDTO = new IIYReportFetchDTO();
                        iiYReportFetchDTO.setEmpId(empId);
                        iiYReportFetchDTO.setFinancialYear(financialYear);

                        // Set employee details
                        IIYEmployeeDetails dto = mapUserInfoToEmployeeDetails(info, outputDateFormat);
                        iiYReportFetchDTO.setEmployeeDetails(dto);

                        iiYReportFetchDTO.setTotalDuration(totalDuration); // total duration (before formatting)
                        iiYReportFetchDTO.setRemainingDuration(remainingDuration); // remaining duration
                        iiYReportFetchDTO.setProrateDuration(targetDuration); // prorate Duration
                        iiYReportFetchDTO.setFinancialYear(financialYear);

                        iiYReportFetchDTO.setCourses(distinctCourses); // distinct courses
                        iiYReportFetchDTO.setCoursesCategory(distinctCoursesCategory); // distinct courses category
                        iiYReportFetchDTO.setCoursesCategoryDurations(formattedCategoryDurations); // individual course
                                                                                                   // category
                        reportDTOs.add(iiYReportFetchDTO);
                }

                return reportDTOs;
        }

        @Override
        public List<CourseFetchDTO> fetchIiyProcessingCourseListByEmpId(IIYEmployeeRequestDTO data) {
                String OrganizationCode = jwtHelper.getOrganizationCode();
                String empId = jwtHelper.getUserRefDetail().getEmpId();
                List<IIYDetails> iiyDetails = employeeRepository.findByEmpId(mongoTemplate, OrganizationCode, empId);

                // Filter the details for the specified empId and collect distinct courses
                List<String> distinctCourses = iiyDetails.stream()
                                .filter(detail -> detail.getEmpId().equals(empId))
                                .map(IIYDetails::getCourse)
                                .distinct() // Ensure uniqueness while maintaining order
                                .collect(Collectors.toList());

                // Convert the distinct courses to CourseFetchDTO with index
                AtomicInteger indexCounter = new AtomicInteger(1); // Start index from 1
                return distinctCourses.stream()
                                .map(course -> {
                                        CourseFetchDTO dto = new CourseFetchDTO();
                                        dto.setCourseName(course); // Assuming CourseFetchDTO has setCourseName method
                                        dto.setId(String.valueOf(indexCounter.getAndIncrement())); // Set index for each
                                                                                                   // course
                                        return dto;
                                }).toList();
        }

        public String getFinancialYear() {
                LocalDate currentDate = LocalDate.now();
                int currentYear = currentDate.getYear();
                int startYear, endYear;

                // Check if the current month is before April
                if (currentDate.getMonthValue() < 4) {
                        // Financial year starts in April of the previous year and ends in March of
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

        private static long calculateMonthsBetween(String dateOfJoining, String startCutOffDate, String endCutOffDate) {
                LocalDate joiningDate = LocalDate.parse(dateOfJoining);
                LocalDate startCutoffDate = LocalDate.parse(startCutOffDate);
                LocalDate endCutoffDate = LocalDate.parse(endCutOffDate);

                // Check if dateOfJoining exceeds endCutOffDate
                if (joiningDate.isAfter(endCutoffDate)) {
                        return 0;
                }

                LocalDate currentMonthLastDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
                LocalDate startDate;
                LocalDate endDate = currentMonthLastDate;

                // Case 1: dateOfJoining is before startCutOffDate
                if (joiningDate.isBefore(startCutoffDate)) {
                        startDate = startCutoffDate;
                }
                // Case 2: dateOfJoining is after or on startCutOffDate
                else {
                        startDate = joiningDate;
                }

                // Calculate months between startDate and endDate
                return ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1),
                                endDate.plusDays(1).withDayOfMonth(1));
        }

        private static long calculateMonths(String startCutOffDate, String endCutOffDate) {
                LocalDate startDate = LocalDate.parse(startCutOffDate);
                LocalDate cutoffEndDate = LocalDate.parse(endCutOffDate);

                // Get the current month's last date
                LocalDate currentMonthLastDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
                LocalDate endDate = cutoffEndDate.isAfter(currentMonthLastDate) ? currentMonthLastDate : cutoffEndDate;

                // If startDate is after the cutoffEndDate, return 0
                if (startDate.isAfter(cutoffEndDate)) {
                        return 0;
                }

                // Add one month to ensure the last month is included
                return ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1),
                                endDate.plusDays(1).withDayOfMonth(1));
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
                String rmId = (hrInfo.getPrimary() != null && hrInfo.getPrimary().getManagerId() != null)
                                ? hrInfo.getPrimary().getManagerId()
                                : "";
                String reviewerId = (hrInfo.getReviewer() != null && hrInfo.getReviewer().getManagerId() != null)
                                ? hrInfo.getReviewer().getManagerId()
                                : "";
                String divisionHeadId = (hrInfo.getDivisionHead() != null
                                && hrInfo.getDivisionHead().getManagerId() != null)
                                                ? hrInfo.getDivisionHead().getManagerId()
                                                : "";

                // Map manager details only if IDs are present
                Map<String, UserInfo> managerInfoMap = Stream.of(rmId, reviewerId, divisionHeadId)
                                .filter(id -> !id.isEmpty())
                                .distinct()
                                .collect(Collectors.toMap(Function.identity(), this::getUserInfo));

                dto.setReportingManagerEmpId(rmId);
                dto.setReportingManagerName(!rmId.isEmpty() ? getManagerFullName(managerInfoMap, rmId) : "");

                dto.setReviewerId(reviewerId);
                dto.setReviewerName(!reviewerId.isEmpty() ? getManagerFullName(managerInfoMap, reviewerId) : "");

                dto.setDivisionHeadId(divisionHeadId);
                dto.setDivisionHeadName(
                                !divisionHeadId.isEmpty() ? getManagerFullName(managerInfoMap, divisionHeadId) : "");

                WorkingInformation workingInfo = info.getSections().getWorkingInformation();
                dto.setDesignation(workingInfo.getDesignation());
                dto.setDepartment(workingInfo.getDepartment());
                dto.setDateOfJoining(AppUtils.parseZonedDate(
                                "dd-MM-yyyy",
                                workingInfo.getDoj().withZoneSameInstant(
                                                ZoneId.of(LocaleContextHolder.getTimeZone().getID())),
                                LocaleContextHolder.getTimeZone().getID()));
                dto.setGroupOfJoining(AppUtils.parseZonedDate(
                                "dd-MM-yyyy",
                                workingInfo.getGroupOfDOJ().withZoneSameInstant(
                                                ZoneId.of(LocaleContextHolder.getTimeZone().getID())),
                                LocaleContextHolder.getTimeZone().getID()));

                dto.setStatus(info.getStatus());

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
