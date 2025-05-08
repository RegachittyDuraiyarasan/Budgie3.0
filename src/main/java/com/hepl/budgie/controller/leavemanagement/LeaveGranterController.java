package com.hepl.budgie.controller.leavemanagement;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LeaveGranterTableDTO;
import com.hepl.budgie.service.leavemanagement.LeaveGranterService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Leave Granter", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/leave/granter")
public class LeaveGranterController {

	private final LeaveGranterService leaveGranterService;
	private final Translator translator;
	private final MasterFormService masterFormService;

	@PostMapping
	@Operation(summary = "Employee Leave Granter")
	public GenericResponse<String> leaveGrant(@RequestBody FormRequest formRequest) {
		log.info("Request received for form " + formRequest.getFormName());
//		masterFormService.formValidate(formRequest);
		leaveGranterService.leaveGranter(formRequest);
		return GenericResponse.success(translator.toLocale(AppMessages.LEAVE_GRANT));
	}

	@GetMapping
	@Operation(summary = "Get Leave Scheme")
	public GenericResponse<List<String>> fetchLeaveScheme() {
		List<String> leaveScheme = leaveGranterService.fetchLeaveScheme();
		
		return GenericResponse.success(leaveScheme);
	}
	
	@GetMapping("/{leaveScheme}")
	@Operation(summary = "Get Periodicity and days")
	public GenericResponse<Map<String, Object>> getPeriodicity(@PathVariable String leaveScheme){
		Map<String , Object> periodicity = leaveGranterService.fetchPeriodicity(leaveScheme);
		
		return GenericResponse.success(periodicity);
	}
	
	@GetMapping("/fetchTable/{processedType}")
	@Operation(summary = "Get Leave Granter Table")
	public GenericResponse<List<LeaveGranterTableDTO>> fetchTable(@PathVariable String processedType){
		List<LeaveGranterTableDTO> data = leaveGranterService.fetchHistory(processedType);
		
		return GenericResponse.success(data);
	}
}
