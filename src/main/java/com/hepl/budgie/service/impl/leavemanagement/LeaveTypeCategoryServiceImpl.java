package com.hepl.budgie.service.impl.leavemanagement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.leavemanagement.LeaveTypeInfoDTO;
import com.hepl.budgie.dto.leavemanagement.LeaveTypeRequestDTO;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.hepl.budgie.mapper.leavemanagement.LeaveTypeCategoryMapper;
import com.hepl.budgie.repository.leavemanagement.LeaveTypeCategoryRepository;
import com.hepl.budgie.service.leavemanagement.LeaveTypeCategoryService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.IdGenerator;
import com.mongodb.client.result.UpdateResult;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Slf4j
public class LeaveTypeCategoryServiceImpl implements LeaveTypeCategoryService {
	private final LeaveTypeCategoryRepository leaveTypeCategoryRepository;
	private final LeaveTypeCategoryMapper leaveTypeCategoryMapper;
	private final MongoTemplate mongoTemplate;
	private final IdGenerator idGenerator;
	private final Translator translator;
	private final JWTHelper jwtHelper;

	@Override
	public List<LeaveTypeCategory> getLeaveTypeCategoryList() {
		List<LeaveTypeCategory> activeLeaveTypeCategory = leaveTypeCategoryRepository
				.findAllByActiveStatus(jwtHelper.getOrganizationCode(), mongoTemplate);
//		List<LeaveTypeCategoryDTO> leaveTypeCategoryDTOs = activeLeaveTypeCategory.stream().map(entity -> {
//			LeaveTypeCategoryDTO dto = new LeaveTypeCategoryDTO();
//			dto.setId(entity.getId());
//			dto.setName(entity.getLeaveTypeName());
//			dto.setCode(entity.getLeaveTypeCode());
//			dto.setDescription(entity.getDescription());
//			dto.setLeaveScheme(entity.getLeaveScheme());
//			dto.setPeriodicityDays(entity.getPeriodicityDays());
//			dto.setEncashmentProcess(entity.getEncashmentProcess());
//			dto.setCarryForward(entity.getCarryForward());
//			dto.setStatus(entity.getStatus());
//			return dto;
//		}).collect(Collectors.toList());

		return activeLeaveTypeCategory;
	}

	@Override
	public void add(LeaveTypeRequestDTO leaveTypeCategory) {
		LeaveTypeCategory leaveType = leaveTypeCategoryMapper.toEntity(leaveTypeCategory);
		log.info("Log Info {}", leaveTypeCategory);
		String org = jwtHelper.getOrganizationCode();
		leaveType.setLeaveUniqueCode(idGenerator.generateLeaveTypeCategoryCode(org));

		leaveTypeCategoryRepository.save(leaveType, org, mongoTemplate);
		log.info("Leave Type Category Successfully added");
	}

	@Override
	public void deleteLeaveTypeCategory(String id) {
		log.info("Removing Leave Type Category");
		UpdateResult result = leaveTypeCategoryRepository.deleteLeaveTypeCategory(id, jwtHelper.getOrganizationCode(),
				mongoTemplate);
	}

	@Override
	public List<LeaveTypeInfoDTO> getLeaveTypeNameList() {
		log.info("fetching leave type category names");
		List<LeaveTypeCategory> leaveTypeNames = leaveTypeCategoryRepository
				.findAllByActiveStatus(jwtHelper.getOrganizationCode(), mongoTemplate);
		return leaveTypeNames.stream()
				.map(leaveTypeCategory -> new LeaveTypeInfoDTO(leaveTypeCategory.getLeaveUniqueCode(),
						leaveTypeCategory.getLeaveTypeName(), leaveTypeCategory.getLeaveTypeCode()))
				.collect(Collectors.toList());
	}

	@Override
	public void update(String id, LeaveTypeRequestDTO leaveTypeCategory) {
		log.info("Updating LeaveType Category For Id", id);

		String org = jwtHelper.getOrganizationCode();
		LeaveTypeCategory existingLeaveType = leaveTypeCategoryRepository
				.findByLeaveTypeCategoryId(id, org, mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		leaveTypeCategoryMapper.updateEntityFromDTO(leaveTypeCategory, existingLeaveType);

		leaveTypeCategoryRepository.save(existingLeaveType, org, mongoTemplate);

		log.info("Leave Type Category with ID {} updated successfully", id);
	}

	public Map<String, String> fetchLeaveTypeCodeMap(String org, MongoTemplate mongoTemplate) {
		return leaveTypeCategoryRepository.findAllByActiveStatus(org, mongoTemplate).stream()
				.collect(Collectors.toMap(LeaveTypeCategory::getLeaveTypeName, LeaveTypeCategory::getLeaveTypeCode,
						(existing, replacement) -> existing));
	}

	public Map<String, String> fetchLeaveTypeNameMap(String org, MongoTemplate mongoTemplate) {
		return leaveTypeCategoryRepository.findAllByActiveStatus(org, mongoTemplate).stream()
				.collect(Collectors.toMap(LeaveTypeCategory::getLeaveTypeCode, LeaveTypeCategory::getLeaveTypeName,
						(existing, replacement) -> existing));
	}
}
