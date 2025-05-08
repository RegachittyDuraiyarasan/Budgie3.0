package com.hepl.budgie.service.impl.iiy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hepl.budgie.dto.iiy.OptionDTO;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.iiy.Course;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.userinfo.WorkingInformation;
import com.hepl.budgie.repository.iiy.CourseRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.iiy.CourseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hepl.budgie.config.security.JWTHelper;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private final UserInfoRepository userInfoRepository;

    @Override
    public void addCourse(Course request) {
        log.info("Adding Course Category");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        boolean existStatus = courseRepository.existsByCourseName(mongoTemplate, organizationCode,
                request.getCourseName());
        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.DUPLICATE_COURSE);

        request.setCourseId(courseRepository.findTopByOrderByIdDesc(organizationCode, mongoTemplate)
                .map(e -> AppUtils.generateUniqueId(e.getCourseId()))
                .orElse("C000001"));
        request.setStatus(Status.ACTIVE.label);
        courseRepository.insertOrUpdate(request, mongoTemplate, organizationCode, authUser);
        log.info("Course Saved Successfully");
    }

    @Override
    public List<Course> fetchCourseList() {
        log.info("Fetch Course  List");
        String organizationCode = jwtHelper.getOrganizationCode();
        return courseRepository.findByAll(mongoTemplate, organizationCode);

    }

    @Override
    public void updateCourse(Course request) {
        log.info("Updating Course ");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        boolean existStatus = courseRepository.existsByCourseNameAndCourseIdNot(mongoTemplate, organizationCode,
                request.getCourseName(), request.getCourseId());
        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.DUPLICATE_COURSE);

        courseRepository.findByCourseId(mongoTemplate, organizationCode, request.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        courseRepository.insertOrUpdate(request, mongoTemplate, organizationCode, authUser);
        log.info("Course  Updated Successfully");

    }

    @Override
    public void deleteCourse(String id) {
        log.info("Deleting Course ");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        courseRepository.findByCourseId(mongoTemplate, organizationCode, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        courseRepository.deleteCourseId(id, mongoTemplate, organizationCode, authUser);
        log.info("Course  Deleted Successfully");

    }

    @Override
    public void updateStatusCourse(String id) {
        log.info("Changing Course  Status");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();

        Course course = (Course) courseRepository.findByCourseId(mongoTemplate, organizationCode, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        String status = course.getStatus().equalsIgnoreCase(Status.ACTIVE.label) ? Status.INACTIVE.label
                : Status.ACTIVE.label;
        courseRepository.updateCourseId(id, mongoTemplate, organizationCode, status, authUser);
        log.info("Course  Status Changed");

    }

    @Override
    public List<OptionDTO> fetchCourseByDepartmentAndEmpId() {
        log.info("Fetch Employee Active Course List");
        String organizationCode = jwtHelper.getOrganizationCode();

        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        String department = getUserInfo(authUser);

        List<Course> iiyCourses = courseRepository.findAllByActiveStatus(mongoTemplate, organizationCode);
        List<OptionDTO> CourseMasterFetchDTOs = new ArrayList<>();
      
            for (Course iiyCourse : iiyCourses) {
                boolean departmentMatches = false;
                boolean empIdMatches = false;

                if (iiyCourse.getDepartment() != null) {
                    departmentMatches = Arrays.asList(iiyCourse.getDepartment()).contains(department);
                }

                if (iiyCourse.getEmployee() != null) {
                    empIdMatches = Arrays.asList(iiyCourse.getEmployee()).contains(authUser);
                }

                boolean allDepartmentYes = "Yes".equalsIgnoreCase(iiyCourse.getAllDepartment());
                boolean allEmployeeYes = "Yes".equalsIgnoreCase(iiyCourse.getAllEmployee());

                if (departmentMatches || empIdMatches || allDepartmentYes) {
                    OptionDTO dto = new OptionDTO();
                    dto.setLabel(iiyCourse.getCourseName());
                    dto.setValue(iiyCourse.getCourseName());

                    CourseMasterFetchDTOs.add(dto);
                }
            }
           

        return CourseMasterFetchDTOs;
    }

    private String getUserInfo(String empId) {
        UserInfo info = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResourceNotFoundException(AppMessages.RESOURCE_NOT_FOUND));

        WorkingInformation workingInfo = info.getSections().getWorkingInformation();
        return workingInfo.getDepartment();

    }
}
