package com.hepl.budgie.controller.iiy;

import com.hepl.budgie.dto.iiy.OptionDTO;
import com.hepl.budgie.entity.iiy.Course;
import com.hepl.budgie.service.iiy.CourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Tag(name = "Create and Manage Course", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/iiy/course")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final Translator translator;

    @PostMapping()
    @Operation(summary = "Add Course")
    public GenericResponse<String> addCourse(@Valid @RequestBody Course request) {
        log.info("Add Course - {}", request);
        courseService.addCourse(request);
        return GenericResponse.success(translator.toLocale(AppMessages.ADDED_COURSE));

    }

    @GetMapping
    @Operation(summary = "Fetch Course list")
    public GenericResponse<List<Course>> fetchCourseList() {
        log.info("Fetch Course List - {}");
        List<Course> result = courseService.fetchCourseList();
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_COURSE), result);

    }

    @PutMapping()
    @Operation(summary = "Update Course ")
    public GenericResponse<String> updateCourse(@Valid @RequestBody Course request) {
        log.info("Update Course - {}", request);
        courseService.updateCourse(request);
        return GenericResponse.success(translator.toLocale(AppMessages.UPDATED_COURSE));

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Course")
    public GenericResponse<String> deleteCourse(@PathVariable String id) {
        log.info("Delete Course - {}", id);
        courseService.deleteCourse(id);
        return GenericResponse.success(translator.toLocale(AppMessages.DELETED_COURSE));

    }

    @PutMapping("/status/{id}")
    @Operation(summary = "Update Status Course")
    public GenericResponse<String> updateStatusCourse(@PathVariable String id) {
        log.info("Update Course Status - {}", id);
        courseService.updateStatusCourse(id);
        return GenericResponse.success(translator.toLocale(AppMessages.STATUS_CHANGED_COURSE));

    }

    @GetMapping("/fetch")
    @Operation(summary = "Fetch Course Category")
    public GenericResponse<List<OptionDTO>> fetchCourseByDepartmentAndEmpId() {
        log.info("Fetch Course by department and empid - {}");
        List<OptionDTO> result = courseService.fetchCourseByDepartmentAndEmpId();
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_COURSE), result);

    }

}
