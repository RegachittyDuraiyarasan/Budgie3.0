package com.hepl.budgie.service.impl.leavemanagement;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.leavemanagement.LeaveScheme;
import com.hepl.budgie.mapper.LeaveSchemeMapper;
import com.hepl.budgie.repository.leavemanagement.LeaveSchemeRepository;
import com.hepl.budgie.service.leavemanagement.LeaveSchemeService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveSchemeServiceImpl implements LeaveSchemeService {

	private final LeaveSchemeRepository leaveSchemeRepository;
	private final LeaveSchemeMapper leaveSchemeMapper;
	private final MongoTemplate mongoTemplate;
	private final JWTHelper jwtHelper;

	@Override
	public List<LeaveScheme> getLeaveSchemeList() {
		List<LeaveScheme> activeLeaveSchemes = leaveSchemeRepository.findByStatus(Status.ACTIVE.label, mongoTemplate,
				jwtHelper.getOrganizationCode());
		return activeLeaveSchemes;
	}

	@Override
	public String saveForm(FormRequest formRequest) {
		LeaveScheme leaveScheme = leaveSchemeMapper.toEntity(formRequest.getFormFields());
		log.info("Log Info {}", leaveScheme);
		String scheme = leaveSchemeRepository.insertOrUpdate(leaveScheme, mongoTemplate,
				jwtHelper.getOrganizationCode(), jwtHelper.getUserRefDetail().getEmpId());

		return scheme;
	}

	@Override
	public void deleteLeaveScheme(String id) {
		log.info("Removing Leave Scheme");

		UpdateResult result = leaveSchemeRepository.deleteLeaveScheme(id, mongoTemplate,
				jwtHelper.getOrganizationCode(), jwtHelper.getUserRefDetail().getEmpId());
		if (result.getMatchedCount() == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
		}
	}

	@Override
	public List<String> getLeaveSchemeNames() {
		List<LeaveScheme> activeLeaveSchemes = leaveSchemeRepository.findByStatus(Status.ACTIVE.label, mongoTemplate,
				jwtHelper.getOrganizationCode());
		return activeLeaveSchemes.stream().map(LeaveScheme::getSchemeName).collect(Collectors.toList());
	}
}
