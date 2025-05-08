package com.hepl.budgie.service.iiy;

import java.util.List;

import com.hepl.budgie.entity.iiy.CourseCategory;

public interface CourseCategoryService {
    void addCourseCategory(CourseCategory request);

    List<CourseCategory> fetchCourseCategoryList();

    void updateCourseCategory(CourseCategory request);

    void deleteCourseCategory(String id);

    void updateStatusCourseCategory(String id);

    List<CourseCategory> fetchCourseCategory();
}
