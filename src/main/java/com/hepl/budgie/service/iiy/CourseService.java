package com.hepl.budgie.service.iiy;

import java.util.List;

import com.hepl.budgie.dto.iiy.OptionDTO;
import com.hepl.budgie.entity.iiy.Course;

public interface CourseService {
    void addCourse(Course request);

    List<Course> fetchCourseList();

    void updateCourse(Course request);

    void deleteCourse(String id);

    void updateStatusCourse(String id);

    List<OptionDTO> fetchCourseByDepartmentAndEmpId();
}
