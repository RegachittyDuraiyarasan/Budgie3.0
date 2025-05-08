package com.hepl.budgie.service.impl.leavemanagement;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactionType;
import com.hepl.budgie.repository.leavemanagement.LeaveTransactionTypeRepository;
import com.hepl.budgie.service.leavemanagement.LeaveTransactionTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveTransactionTypeServiceImpl implements LeaveTransactionTypeService {

	private final LeaveTransactionTypeRepository leaveTransactionTypeRepository;
	private final MongoTemplate mongoTemplate;
	private final JWTHelper jwtHelper;

	@Override
	public String saveLeaveTransactionType(@Valid LeaveTransactionType leaveTransactionType) {
		log.info("Saving Leave Transaction Type ");

		return leaveTransactionTypeRepository.insertOrUpdate(leaveTransactionType, getAuthUser(),
				jwtHelper.getOrganizationCode(), mongoTemplate);
	}

	@Override
	public void deleteLeaveTransactionType(String transactionTypeId) {
		log.info("Deleting Leave Transaction Type ");

		leaveTransactionTypeRepository.deleteLeaveTransactionType(transactionTypeId, getAuthUser(),
				jwtHelper.getOrganizationCode(), mongoTemplate);
	}

	@Override
	public List<LeaveTransactionType> fetchLeaveTransactionType() {
		log.info("Fetching all Leave Transaction Type's");

		return leaveTransactionTypeRepository.findByActiveStatus(jwtHelper.getOrganizationCode(), mongoTemplate);
	}

	private String getAuthUser() {
		return jwtHelper.getUserRefDetail().getEmpId();
	}

	@Override
	public List<String> fetchLeaveTransactionTypes() {
		log.info("Fetching Leave Transaction Type Names");
		List<LeaveTransactionType> typeNames = leaveTransactionTypeRepository
				.findByActiveStatus(jwtHelper.getOrganizationCode(), mongoTemplate);

		return typeNames.stream().map(LeaveTransactionType::getLeaveTransactionType).toList();
	}

}
