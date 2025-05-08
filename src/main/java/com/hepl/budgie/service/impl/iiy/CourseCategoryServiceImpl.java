package com.hepl.budgie.service.impl.iiy;

import java.util.List;

import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.iiy.CourseCategory;
import com.hepl.budgie.repository.iiy.CourseCategoryRepository;
import com.hepl.budgie.service.iiy.CourseCategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hepl.budgie.config.security.JWTHelper;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl implements CourseCategoryService {
    private final CourseCategoryRepository courseCategoryRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public void addCourseCategory(CourseCategory request) {
        log.info("Adding Course Category");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        boolean existStatus = courseCategoryRepository.existsByCategoryName(mongoTemplate, organizationCode,
                request.getCategoryName());
        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.DUPLICATE_COURSE_CATEGORY);

        request.setCategoryId(courseCategoryRepository.findTopByOrderByIdDesc(organizationCode, mongoTemplate)
                .map(e -> AppUtils.generateUniqueId(e.getCategoryId()))
                .orElse("CC000001"));
        request.setStatus(Status.ACTIVE.label);
        courseCategoryRepository.insertOrUpdate(request, mongoTemplate, organizationCode, authUser);
        log.info("Course Category Saved Successfully");
    }

    @Override
    public List<CourseCategory> fetchCourseCategoryList() {
        log.info("Fetch Course Category List");
        String organizationCode = jwtHelper.getOrganizationCode();
        return courseCategoryRepository.findByAll(mongoTemplate, organizationCode);

    }

    @Override
    public void updateCourseCategory(CourseCategory request) {
        log.info("Updating Course Category");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        boolean existStatus = courseCategoryRepository.existsByCategoryNameAndCategoryIdNot(mongoTemplate,
                organizationCode, request.getCategoryName(), request.getCategoryId());
        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.DUPLICATE_COURSE_CATEGORY);

        courseCategoryRepository.findByCategoryId(mongoTemplate, organizationCode, request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        courseCategoryRepository.insertOrUpdate(request, mongoTemplate, organizationCode, authUser);
        log.info("Course Category Updated Successfully");

    }

    @Override
    public void deleteCourseCategory(String id) {
        log.info("Deleting Course Category");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        courseCategoryRepository.findByCategoryId(mongoTemplate, organizationCode, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        courseCategoryRepository.deleteByCategoryId(id, mongoTemplate, organizationCode, authUser);
        log.info("Course Category Deleted Successfully");

    }

    @Override
    public void updateStatusCourseCategory(String id) {
        log.info("Changing Course Category Status");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();

        CourseCategory courseCategory = (CourseCategory) courseCategoryRepository
                .findByCategoryId(mongoTemplate, organizationCode, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        String status = courseCategory.getStatus().equalsIgnoreCase(Status.ACTIVE.label) ? Status.INACTIVE.label
                : Status.ACTIVE.label;
        courseCategoryRepository.updateByCategoryId(id, mongoTemplate, organizationCode, status, authUser);
        log.info("Course Category Status Changed");

    }

    @Override
    public List<CourseCategory> fetchCourseCategory() {
        log.info("Fetch Active Course Category List");
        String organizationCode = jwtHelper.getOrganizationCode();
        return courseCategoryRepository.findAllByActiveStatus(mongoTemplate, organizationCode);
    }
}
