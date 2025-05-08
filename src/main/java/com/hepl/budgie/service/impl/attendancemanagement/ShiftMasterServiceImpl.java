package com.hepl.budgie.service.impl.attendancemanagement;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.dto.NameDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.attendancemanagement.ShiftMaster;
import com.hepl.budgie.entity.attendancemanagement.ShiftType;
import com.hepl.budgie.enums.ShiftMasterHeader;
import com.hepl.budgie.repository.attendancemanagement.ShiftMasterRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftTypeRepository;
import com.hepl.budgie.service.attendancemanagement.ShiftMasterService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.ExcelTemplateHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftMasterServiceImpl implements ShiftMasterService {

	private final ObjectMapper objectMapper;
	private final ShiftTypeRepository shiftTypeRepository;
	private final ShiftMasterRepository shiftMasterRepository;

	@Override
	public void addShiftName(FormRequest formRequest) {
		log.info("Adding Shift Name ");

		NameDTO NameDTO = objectMapper.convertValue(formRequest.getFormFields(), NameDTO.class);
//		ShiftType shiftName = ShiftTypeMapper.toEntity(NameDTO);
		ShiftType shiftType = new ShiftType();
		shiftType.setShiftType(NameDTO.getName());
		shiftType.setStatus(true);
		shiftType.setCreatedBy("user");
		shiftType.setUpdatedBy("user");
		shiftType.setCreatedAt(LocalDateTime.now());
		shiftType.setUpdatedAt(LocalDateTime.now());

		shiftTypeRepository.save(shiftType);
		log.info("Shift Type saved successfully ");

	}

	@Override
	public void updateShiftType(String id, FormRequest formRequest) {
		log.info("Updating Shift Type with ID: {}", id);

		ShiftType existingShiftType = shiftTypeRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Shift Type not found with ID: " + id));

		NameDTO nameDTO = objectMapper.convertValue(formRequest.getFormFields(), NameDTO.class);

		existingShiftType.setShiftType(nameDTO.getName());
		existingShiftType.setUpdatedAt(LocalDateTime.now());
		existingShiftType.setUpdatedBy("User");

		shiftTypeRepository.save(existingShiftType);
		log.info("Shift Type updated successfully with ID: {}", id);
	}

	@Override
	public void deleteShiftType(String id) {
		log.info("Soft deleting Shift Type with ID: {}", id);

		ShiftType existingShiftType = shiftTypeRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Shift Type not found with ID: " + id));

		existingShiftType.setStatus(false);
		existingShiftType.setUpdatedAt(LocalDateTime.now());
		existingShiftType.setUpdatedBy("user");

		shiftTypeRepository.save(existingShiftType);
		log.info("Shift Type soft deleted successfully with ID: {}", id);

	}

	@Override
	public List<Map<String, String>> fetchShiftTypes() {
		log.info("Fetching Shift Types ");

		return shiftTypeRepository.findAllByStatusTrue().stream().map(shiftType -> {
			Map<String, String> shiftMap = new HashMap<>();
			shiftMap.put("shiftType", shiftType.getShiftType());
			return shiftMap;
		}).collect(Collectors.toList());
	}

	@Override
	public ShiftMaster addShiftMaster(FormRequest formRequest) {
		log.info("Adding Shift Master ");

		ShiftMaster shiftMaster = objectMapper.convertValue(formRequest.getFormFields(), ShiftMaster.class);
		shiftMaster.setCreatedAt(LocalDateTime.now());
		shiftMaster.setCreatedBy("user");
		shiftMaster.setUpdatedAt(LocalDateTime.now());
		shiftMaster.setUpdatedBy("user");
		shiftMaster.setStatus(true);

		return shiftMasterRepository.save(shiftMaster);
	}

	@Override
	public ShiftMaster updateShiftMaster(String id, FormRequest formRequest) {
		log.info("Updating Shift Master with ID: {}", id);
		ShiftMaster existingShiftMaster = shiftMasterRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND + id));

		ShiftMaster shiftMaster = objectMapper.convertValue(formRequest.getFormFields(), ShiftMaster.class);
		updateFieldsManually(existingShiftMaster, shiftMaster);

		existingShiftMaster.setUpdatedAt(LocalDateTime.now());
		existingShiftMaster.setUpdatedBy("user");

		return shiftMasterRepository.save(existingShiftMaster);
	}

	private void updateFieldsManually(ShiftMaster existing, ShiftMaster updatedFields) {
		if (updatedFields.getShiftCode() != null) {
			existing.setShiftCode(updatedFields.getShiftCode());
		}
		if (updatedFields.getShiftName() != null) {
			existing.setShiftName(updatedFields.getShiftName());
		}
		if (updatedFields.getInTime() != null) {
			existing.setInTime(updatedFields.getInTime());
		}
		if (updatedFields.getOutTime() != null) {
			existing.setOutTime(updatedFields.getOutTime());
		}
		if (updatedFields.getBreakTime() != null) {
			existing.setBreakTime(updatedFields.getBreakTime());
		}
	}

	@Override
	public void changeStatus(String id, boolean status) {
		log.info("Status Changing Shift Master with ID: {}", id);

		ShiftMaster existingShiftMaster = shiftMasterRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND + id));

		existingShiftMaster.setStatus(status);
		existingShiftMaster.setUpdatedAt(LocalDateTime.now());
		existingShiftMaster.setUpdatedBy("user");

		shiftMasterRepository.save(existingShiftMaster);
	}

	@Override
	public List<ShiftMaster> fetch() {
		log.info("Fetching Shift Master");

		return shiftMasterRepository.findAll();
	}

	@Override
	public byte[] createExcelTemplate() {
		log.info("Creating Excel Template for Shift Master");
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("ShiftMaster");

			ExcelTemplateHelper.createHeadersFromEnum(sheet, ShiftMasterHeader.class);

			List<ShiftMaster> shift = shiftMasterRepository.findByStatusTrue();

			ExcelTemplateHelper.createDropDownValidationForList(sheet, shift, ShiftMaster::getShiftName, "Shift Name");

			sheet.createFreezePane(0, 1);

			log.info("Finished createExcelTemplate method");
			return ExcelTemplateHelper.writeWorkbookToByteArray(workbook);
		} catch (Exception e) {
			log.error("Error in createExcelTemplate: ", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					AppMessages.EXCEL_TEMPLATE_CREATION_FAILED);
		}
	}

}
