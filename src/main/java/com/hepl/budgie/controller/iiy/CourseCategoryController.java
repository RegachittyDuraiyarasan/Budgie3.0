package com.hepl.budgie.controller.iiy;

import com.hepl.budgie.entity.iiy.CourseCategory;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.service.iiy.CourseCategoryService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Tag(name = "Create and Manage Course Category", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/iiy/category")
@RequiredArgsConstructor
public class CourseCategoryController {
    private final CourseCategoryService courseCategoryService;
    private final Translator translator;

    @PostMapping()
    @Operation(summary = "Add Course Category")
    public GenericResponse<String> addCourseCategory(@Valid @RequestBody CourseCategory request) {
        log.info("Add Course Category - {}", request);
        courseCategoryService.addCourseCategory(request);
        return GenericResponse.success(translator.toLocale(AppMessages.ADDED_COURSE_CATEGORY));

    }

    @GetMapping
    @Operation(summary = "Fetch Course Category list")
    public GenericResponse<List<CourseCategory>> fetchCourseCategoryList() {
        log.info("Fetch Course Category List - {}");
        List<CourseCategory> result = courseCategoryService.fetchCourseCategoryList();
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_COURSE_CATEGORY), result);

    }

    @PutMapping()
    @Operation(summary = "Update Course Category")
    public GenericResponse<String> updateCourseCategory(@Valid @RequestBody CourseCategory request) {
        log.info("Update Course Category - {}", request);
        courseCategoryService.updateCourseCategory(request);
        return GenericResponse.success(translator.toLocale(AppMessages.UPDATED_COURSE_CATEGORY));

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Course Category")
    public GenericResponse<String> deleteCourseCategory(@PathVariable String id) {
        log.info("Delete Course Category - {}", id);
        courseCategoryService.deleteCourseCategory(id);
        return GenericResponse.success(translator.toLocale(AppMessages.DELETED_COURSE_CATEGORY));

    }

    @PutMapping("/status/{id}")
    @Operation(summary = "Update Status Course Category")
    public GenericResponse<String> updateStatusCourseCategory(@PathVariable String id) {
        log.info("Update Course Category Status - {}", id);
        courseCategoryService.updateStatusCourseCategory(id);
        return GenericResponse.success(translator.toLocale(AppMessages.STATUS_CHANGED_COURSE_CATEGORY));

    }

    @GetMapping("/fetch")
    @Operation(summary = "Fetch Course Category")
    public GenericResponse<List<CourseCategory>> fetchCourseCategory() {
        log.info("Fetch Course Category - {}");
        List<CourseCategory> result = courseCategoryService.fetchCourseCategory();
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_COURSE_CATEGORY), result);

    }

}
