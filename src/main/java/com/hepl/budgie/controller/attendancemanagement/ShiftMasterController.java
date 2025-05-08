package com.hepl.budgie.controller.attendancemanagement;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.attendancemanagement.ShiftMaster;
import com.hepl.budgie.service.attendancemanagement.ShiftMasterService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Shift Master", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/shift-master")
public class ShiftMasterController {

	private final ShiftMasterService shiftMasterService;
	private final MasterFormService masterFormService;
	private final Translator translator;

	@PostMapping("/type")
	@Operation(summary = "Add Shift Type")
	public GenericResponse<String> addShiftType(@Valid @RequestBody FormRequest formRequest, @RequestParam String org) {
//		masterFormService.formValidate(formRequest, org);
		shiftMasterService.addShiftName(formRequest);

		return GenericResponse.success(translator.toLocale(AppMessages.ADDED_SHIFT_TYPE));
	}

	@PutMapping("/type/{id}")
	@Operation(summary = "Update Shift Type")
	public GenericResponse<String> updateShiftType(@PathVariable String id, @Valid @RequestBody FormRequest formRequest,
			@RequestParam String org) {
//		masterFormService.formValidate(formRequest, org);
		shiftMasterService.updateShiftType(id, formRequest);

		return GenericResponse.success(translator.toLocale(AppMessages.UPDATED_SHIFT_TYPE));
	}

	@DeleteMapping("/type/{id}")
	@Operation(summary = "Delete Shift Type")
	public GenericResponse<String> deleteShiftType(@PathVariable String id) {
		shiftMasterService.deleteShiftType(id);

		return GenericResponse.success(translator.toLocale(AppMessages.DELETED_SHIFT_TYPE));
	}

	@GetMapping("/type")
	@Operation(summary = "Get Shift Types")
	public GenericResponse<List<Map<String, String>>> fetchShiftTypes() {
		List<Map<String, String>> shiftTypes = shiftMasterService.fetchShiftTypes();

		return GenericResponse.success(shiftTypes);
	}

	@PostMapping()
	@Operation(summary = "Save Shift Master")
	public GenericResponse<ShiftMaster> save(@Valid @RequestBody FormRequest formRequest, @RequestParam String org) {
//		masterFormService.formValidate(formRequest, org);
		ShiftMaster shiftMaster = shiftMasterService.addShiftMaster(formRequest);

		return GenericResponse.success(translator.toLocale(AppMessages.ADDED_SHIFT_MASTER), shiftMaster);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update Shift Master")
	public GenericResponse<ShiftMaster> update(@PathVariable String id, @Valid @RequestBody FormRequest formRequest,
			@RequestParam String org) {
//		masterFormService.formValidate(formRequest, org);
		ShiftMaster shiftMaster = shiftMasterService.updateShiftMaster(id, formRequest);

		return GenericResponse.success(translator.toLocale(AppMessages.UPDATED_SHIFT_MASTER), shiftMaster);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Change Status ")
	public GenericResponse<String> changeStatus(@PathVariable String id, @RequestParam boolean status) {
		shiftMasterService.changeStatus(id, status);

		return GenericResponse.success(translator.toLocale(AppMessages.STATUS_CHANGED_SHIFT_MASTER));
	}

	@GetMapping()
	@Operation(summary = "Get All Shift Master")
	public GenericResponse<List<ShiftMaster>> fetch() {
		List<ShiftMaster> shiftMaster = shiftMasterService.fetch();

		return GenericResponse.success(shiftMaster);
	}

	@GetMapping("/sample-download")
	public ResponseEntity<byte[]> sampleDownload() throws IOException {
		byte[] excelContent = shiftMasterService.createExcelTemplate();

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Shift_Master.xlsx")
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(excelContent);
	}
}
