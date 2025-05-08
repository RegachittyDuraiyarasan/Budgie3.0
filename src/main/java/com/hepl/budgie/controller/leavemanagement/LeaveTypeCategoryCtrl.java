package com.hepl.budgie.controller.leavemanagement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LeaveTypeCategoryDTO;
import com.hepl.budgie.dto.leavemanagement.LeaveTypeInfoDTO;
import com.hepl.budgie.dto.leavemanagement.LeaveTypeRequestDTO;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.hepl.budgie.service.leavemanagement.LeaveTypeCategoryService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Create and Manage Leave Type Category", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/leave/category")
@Slf4j
public class LeaveTypeCategoryCtrl {
	private LeaveTypeCategoryService leaveTypeCategoryService;
	private final Translator translator;
	private final MasterFormService masterFormService;

	public LeaveTypeCategoryCtrl(LeaveTypeCategoryService leaveTypeCategoryService, Translator translator,
			MasterFormService masterFormService) {
		this.leaveTypeCategoryService = leaveTypeCategoryService;
		this.translator = translator;
		this.masterFormService = masterFormService;
	}

	@GetMapping()
	@Operation(summary = "Get Active Leave Type Category")
	public GenericResponse<List<LeaveTypeCategory>> fetch() {
		log.info("Get Leave Type Category List");
		return GenericResponse.success(leaveTypeCategoryService.getLeaveTypeCategoryList());
	}

	@PostMapping()
	@Operation(summary = "Add new Leave Type Category")
	public GenericResponse<String> saveForm(@RequestBody LeaveTypeRequestDTO leaveTypeCategory) {
//        masterFormService.formValidate(formRequest,org);
		leaveTypeCategoryService.add(leaveTypeCategory);
		return GenericResponse.success(translator.toLocale(AppMessages.ADDED_LEAVE_TYPE_CATEGORY));
	}

	@PutMapping("/{id}")
    @Operation(summary = "Update Leave Type Category by ID")
    public GenericResponse<String> updateLeaveTypeCategory(@PathVariable String id,
            @RequestBody LeaveTypeRequestDTO leaveTypeCategory) {
        leaveTypeCategoryService.update(id, leaveTypeCategory);
        return GenericResponse.success(translator.toLocale(AppMessages.UPDATED_LEAVE_TYPE_CATEGORY));
    }

	@DeleteMapping("/{id}")
	public GenericResponse<String> deleteLeaveTypeCategory(@PathVariable String id) {
		log.info("Delete Leave Type Category map {}", id);

		leaveTypeCategoryService.deleteLeaveTypeCategory(id);
		return GenericResponse
				.success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "Leave Type Category" }));
	}

	@GetMapping("/name")
	@Operation(summary = "Get Active Leave Type Names")
	public GenericResponse<List<LeaveTypeInfoDTO>> fetchLeaveCategory() {
		log.info("Get Leave Type Name");
		return GenericResponse.success(leaveTypeCategoryService.getLeaveTypeNameList());
	}

	// @GetMapping("/edit")
	// public Object edit(@RequestParam("id") String id) {
	// return leaveTypeCategoryService.edit(id);
	//
	// }
	// @PutMapping("/update")
	// public Object update(@RequestBody LeaveTypeCategoryDto leaveTypeCategory){
	// return leaveTypeCategoryService.update(leaveTypeCategory);
	// }

}
