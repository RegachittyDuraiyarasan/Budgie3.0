package com.hepl.budgie.controller.iiy;

import com.hepl.budgie.entity.iiy.IdeaCategory;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.service.iiy.IdeaCategoryService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Tag(name = "Create and Manage Idea Category", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/idea/category")
@RequiredArgsConstructor
public class IdeaCategoryController {
    private final IdeaCategoryService ideaCategoryService;
    private final Translator translator;

    @PostMapping()
    @Operation(summary = "Add Idea Category")
    public GenericResponse<String> addIdeaCategory(@Valid @RequestBody IdeaCategory request) {
        log.info("Add Idea Category - {}", request);
        ideaCategoryService.addIdeaCategory(request);
        return GenericResponse.success(translator.toLocale(AppMessages.ADDED_IDEA_CATEGORY));

    }

    @GetMapping
    @Operation(summary = "Fetch Idea Category list")
    public GenericResponse<List<IdeaCategory>> fetchIdeaCategoryList() {
        log.info("Fetch Idea Category List - {}");
        List<IdeaCategory> result = ideaCategoryService.fetchIdeaCategoryList();
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_IDEA_CATEGORY), result);

    }

    @PutMapping()
    @Operation(summary = "Update Idea Category")
    public GenericResponse<String> updateIdeaCategory(@Valid @RequestBody IdeaCategory request) {
        log.info("Update Idea Category - {}", request);
        ideaCategoryService.updateIdeaCategory(request);
        return GenericResponse.success(translator.toLocale(AppMessages.UPDATED_IDEA_CATEGORY));

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Idea Category")
    public GenericResponse<String> deleteIdeaCategory(@PathVariable String id) {
        log.info("Delete Idea Category - {}", id);
        ideaCategoryService.deleteIdeaCategory(id);
        return GenericResponse.success(translator.toLocale(AppMessages.DELETED_IDEA_CATEGORY));

    }

    @PutMapping("/status/{id}")
    @Operation(summary = "Update Status Idea Category")
    public GenericResponse<String> updateStatusIdeaCategory(@PathVariable String id) {
        log.info("Update Idea Category Status - {}", id);
        ideaCategoryService.updateStatusIdeaCategory(id);
        return GenericResponse.success(translator.toLocale(AppMessages.STATUS_CHANGED_IDEA_CATEGORY));

    }

    @GetMapping("/fetch")
    @Operation(summary = "Fetch Idea Category")
    public GenericResponse<List<IdeaCategory>> fetchIdeaCategory() {
        log.info("Fetch Idea Category - {}");
        List<IdeaCategory> result = ideaCategoryService.fetchIdeaCategory();
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_IDEA_CATEGORY), result);

    }

}
