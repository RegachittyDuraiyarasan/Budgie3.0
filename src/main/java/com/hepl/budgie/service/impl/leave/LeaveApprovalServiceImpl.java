package com.hepl.budgie.service.impl.leave;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.leave.AdminLeaveApproveDto;
import com.hepl.budgie.dto.leave.LeaveApprovalDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leavemanagement.LeaveBalanceSummary;
import com.hepl.budgie.entity.leavemanagement.LeaveMaster;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.leave.LeaveApprovalRepository;
import com.hepl.budgie.repository.leavemanagement.LeaveMasterRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.leave.LeaveApprovalService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.IdGenerator;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveApprovalServiceImpl implements LeaveApprovalService {

	private final Translator translator;
	private final JWTHelper jwtHelper;
	private final LeaveMasterRepository leaveMasterRepository;
	private final UserInfoRepository userInfoRepository;
	private final LeaveApprovalRepository leaveApprovalRepository;
	private final IdGenerator idGenerator;
	private final MongoTemplate mongoTemplate;

	@Override
	public List<LeaveApprovalDTO> getLeaveDatas(String status, String roleType) {

		UserRef authenticatedUser = getAuthenticatedUser();

		log.info("Fetching leave data for empId: {}, status: {}, roleType: {}", authenticatedUser.getEmpId(), status,
				roleType);

		List<LeaveApply> leaveList;

		switch (roleType.toUpperCase()) {
		case "TL":
			leaveList = fetchLeaveDataForTL(authenticatedUser.getEmpId(), status);
			break;
		case "EMPLOYEE":
			leaveList = fetchLeaveDataForEmployee(authenticatedUser.getEmpId(), status);
			break;
		default:
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid roleType provided: " + roleType);
		}

		return mapToLeaveApprovalDTO(leaveList);
	}

	private List<LeaveApply> fetchLeaveDataForTL(String empId, String status) {
		if (Status.ACTIVE.label.equalsIgnoreCase(status)) {
			return leaveApprovalRepository.findByAppliedToAndStatus(empId, Status.PENDING.label, org(), mongoTemplate);
		} else {
			return leaveApprovalRepository.findByAppliedToAndStatusIn(empId,
					List.of(Status.APPROVED.label, Status.REJECTED.label), org(), mongoTemplate);
		}
	}

	private List<LeaveApply> fetchLeaveDataForEmployee(String empId, String status) {
		if (Status.ACTIVE.label.equalsIgnoreCase(status)) {
			return leaveApprovalRepository.findByEmpIdAndStatus(empId, Status.PENDING.label, org(), mongoTemplate);
		} else {
			List<String> validStatuses = List.of(Status.APPROVED.label, Status.REJECTED.label, Status.WITHDRAWN.label);
			List<LeaveApply> leaveList = leaveApprovalRepository.findByEmpIdAndStatusIn(empId, validStatuses, org(), mongoTemplate);
			if (!status.isEmpty() && validStatuses.contains(status)) {
				return leaveList.stream().filter(leave -> leave.getStatus().equalsIgnoreCase(status))
						.collect(Collectors.toList());
			}
			return leaveList;
		}
	}

	private List<LeaveApprovalDTO> mapToLeaveApprovalDTO(List<LeaveApply> leaveList) {
		return leaveList.stream().map(leave -> {
			LeaveApprovalDTO dto = new LeaveApprovalDTO();
			dto.setId(leave.getId());
			dto.setEmpId(leave.getEmpId());
			dto.setAppliedTo(leave.getAppliedTo());
			dto.setStatus(leave.getStatus());
			dto.setLeaveType(leave.getLeaveType());
			dto.setDays(leave.getNumOfDays());
			dto.setBalanceDay(leave.getBalance());
			dto.setContactNo(leave.getContactNo());
			dto.setLeaveCode(leave.getLeaveCode());
			dto.setCategory(leave.getLeaveCategory());
			dto.setFromDate(leave.getFromDate());
			dto.setToDate(leave.getToDate());
			dto.setFromSession(leave.getFromSession());
			dto.setToSession(leave.getToSession());
			dto.setAppliedCC(leave.getAppliedCC());
			dto.setEmpReason(leave.getEmpReason());
			dto.setApproverReason(leave.getApproverReason());
			dto.setApproveOrRejectDate(leave.getApproveOrRejectDate());
			dto.setFiles(leave.getFileNames());
			dto.setCreatedAt(leave.getCreatedDate());
			dto.setUpdatedAt(leave.getLastModifiedDate());

			Optional<UserInfo> appliedToUserOptional = userInfoRepository.findByEmpId(leave.getAppliedTo());
			appliedToUserOptional.ifPresent(user -> {
				dto.setAppliedToName(user.getSections().getBasicDetails().getFirstName() + " "
						+ user.getSections().getBasicDetails().getLastName());
				dto.setAssigned(user.getSections().getBasicDetails().getFirstName() + " "
						+ user.getSections().getBasicDetails().getLastName());
			});

			Optional<UserInfo> userToUserOptional = userInfoRepository.findByEmpId(leave.getEmpId());
			userToUserOptional.ifPresent(userdata -> {
				dto.setEmpName(userdata.getSections().getBasicDetails().getFirstName() + " "
						+ userdata.getSections().getBasicDetails().getLastName());
			});

			return dto;
		}).collect(Collectors.toList());
	}

	private UserRef getAuthenticatedUser() {
		UserRef user = jwtHelper.getUserRefDetail();
		if (user == null || user.getEmpId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User details not found");
		}
		return user;
	}

	private UserInfo getUserInfromations(String empId) {

		UserInfo userInfo = userInfoRepository.findByEmpId(empId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND));
		return userInfo;
	}

	@Override
	public String approveOrRejectLeave(String leaveCode, String type, String rejectReason) {
		if ("approve".equalsIgnoreCase(type)) {
			return approveLeave(leaveCode);
		} else if ("reject".equalsIgnoreCase(type)) {
			return rejectLeave(leaveCode, rejectReason);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid type provided. Must be 'approve' or 'reject'.");
		}
	}

	private String approveLeave(String leaveCode) {

		LeaveApply leaveApply = leaveApprovalRepository.findByLeaveCode(leaveCode, org(), mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		if (Status.APPROVED.label.equalsIgnoreCase(leaveApply.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave already approved");
		}

		updateLeaveBalanceAndStatus(leaveApply, Status.APPROVED.label);

//      sendLeaveStatusEmail(leaveApply, "Approved");

		return translator.toLocale(AppMessages.LEAVE_APPROVE);
	}

	private String rejectLeave(String leaveCode, String rejectReason) {
		LeaveApply leaveApply = leaveApprovalRepository.findByLeaveCode(leaveCode, org(), mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		if (Status.REJECTED.label.equalsIgnoreCase(leaveApply.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave already rejected");
		}
		if (leaveApply.getLeaveApply() != null) {
			leaveApply.getLeaveApply().forEach(leaveDate -> {
				leaveDate.setStatus(Status.REJECTED.label);
				leaveDate.setApproverId(getAuthenticatedUser().getEmpId());
				leaveDate.setApproverStatus(Status.REJECTED.label);
				leaveDate.setApproverAt(LocalDateTime.now().toString());
			});
		}
		leaveApply.setStatus(Status.REJECTED.label);
		leaveApply.setApproverReason(rejectReason);
		leaveApply.setApproveOrRejectDate(LocalDateTime.now());
		leaveApprovalRepository.saveLeaveApproval(leaveApply , org(), mongoTemplate);

		Set<String> years = leaveApply.getDateList().stream()
				.map(date -> String.valueOf(LocalDate.parse(date).getYear())).collect(Collectors.toSet());

		years.forEach(year -> {
			leaveMasterRepository
					.findByEmpIdAndYear(leaveApply.getEmpId(), year, org(), mongoTemplate)
					.ifPresent(leaveMaster -> {
						LeaveTransactions transaction = createLeaveTransaction(leaveApply, Status.REJECTED.label, leaveMaster);
						leaveMaster.getLeaveTransactions().add(transaction);
						leaveMasterRepository.saveLeaveMaster(leaveMaster, org(), mongoTemplate);
					});
		});
//		sendLeaveStatusEmail(leaveApply, "Rejected");

		return translator.toLocale(AppMessages.LEAVE_REJECT);
	}

	private void updateLeaveBalanceAndStatus(LeaveApply leaveApply, String status) {
		Set<String> years = leaveApply.getDateList().stream()
				.map(date -> String.valueOf(LocalDate.parse(date).getYear())).collect(Collectors.toSet());

		years.forEach(year -> {
			leaveMasterRepository
					.findByEmpIdAndYear(leaveApply.getEmpId(), year, org(), mongoTemplate)
					.ifPresent(leaveMaster -> {
						leaveMaster.getLeaveBalanceSummary().stream().filter(
								summary -> summary.getLeaveTypeName().equalsIgnoreCase(leaveApply.getLeaveType()))
								.forEach(summary -> {
									if ("Approved".equals(status)) {
										summary.setBalance(summary.getBalance() - leaveApply.getNumOfDays());
										summary.setAvailed(summary.getAvailed() + leaveApply.getNumOfDays());
									}
								});

						LeaveTransactions transaction = createLeaveTransaction(leaveApply, status, leaveMaster);
						leaveMaster.getLeaveTransactions().add(transaction);

						leaveMasterRepository.saveLeaveMaster(leaveMaster, org(), mongoTemplate);
					});
		});

		if (leaveApply.getLeaveApply() != null) {
			leaveApply.getLeaveApply().forEach(leaveDate -> {
				leaveDate.setStatus(status);
				leaveDate.setApproverId(getAuthenticatedUser().getEmpId());
				leaveDate.setApproverStatus(status);
				leaveDate.setApproverAt(LocalDateTime.now().toString());
			});
		}
		leaveApply.setStatus(status);
		leaveApply.setApproveOrRejectDate(LocalDateTime.now());
		leaveApply.getLeaveApply();
		leaveApprovalRepository.saveLeaveApproval(leaveApply, org(), mongoTemplate);
	}

	private LeaveTransactions createLeaveTransaction(LeaveApply leaveApply, String status, LeaveMaster leaveMaster) {
		LeaveTransactions transaction = new LeaveTransactions();
		transaction.setTransactionId(idGenerator.generateTransactionId(leaveMaster));
		transaction.setLeaveTypeName(leaveApply.getLeaveType());
		if (Status.WITHDRAWN.label.equalsIgnoreCase(status)) {
			transaction.setTransactionType(Status.WITHDRAWN.label);
		} else {
			transaction.setTransactionType(Status.APPROVED.label.equalsIgnoreCase(status) ? "Availed" : "Rejected");
		}
		transaction.setProcessedBy(getAuthenticatedUser().getEmpId());
		transaction.setPostedOn(LocalDate.now().toString());
		transaction.setFromDate(leaveApply.getFromDate());
		transaction.setToDate(leaveApply.getToDate());
		transaction.setFromSession(leaveApply.getFromSession());
		transaction.setToSession(leaveApply.getToSession());
		transaction.setNoOfDays(leaveApply.getNumOfDays());

		return transaction;
	}

	@Override
	public Object getLeaveData(String leaveCode) throws IllegalAccessException, InvocationTargetException {

		LeaveApply previewData = leaveApprovalRepository.findByLeaveCode(leaveCode, org(), mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		UserInfo user = getUserInfromations(previewData.getEmpId());

		LeaveApprovalDTO pendingListRecord = mapToLeaveApprovalDto(previewData, user);

		return pendingListRecord;
	}

	private LeaveApprovalDTO mapToLeaveApprovalDto(LeaveApply leaveApply, UserInfo user)
			throws IllegalAccessException, InvocationTargetException {
		LeaveApprovalDTO dto = new LeaveApprovalDTO();
		BeanUtils.copyProperties(dto, leaveApply);

		UserInfo userData = getUserInfromations(leaveApply.getAppliedTo());

		dto.setEmpName(user.getSections().getBasicDetails().getFirstName());
		dto.setCategory(leaveApply.getLeaveCategory());
		dto.setAssigned(userData.getSections().getBasicDetails().getFirstName());
		dto.setDays(leaveApply.getNumOfDays());
		dto.setBalanceDay(leaveApply.getBalance());

		List<String> files = Optional.ofNullable(leaveApply.getFileNames()).orElse(Collections.emptyList()).stream()
				.map(file -> leaveApply.getLeaveCode() + "/" + file).collect(Collectors.toList());
		dto.setFiles(files);

		return dto;
	}

	@Override
	public String withdrawLeave(String leaveCode) {

		LeaveApply leaveApply = leaveApprovalRepository.findByLeaveCode(leaveCode, org(), mongoTemplate)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

		if (Status.WITHDRAWN.label.equalsIgnoreCase(leaveApply.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave already Withdrawn");
		}
		if (leaveApply.getLeaveApply() != null) {
			leaveApply.getLeaveApply().forEach(leaveDate -> {
				leaveDate.setStatus(Status.WITHDRAWN.label);
			});
		}
		leaveApply.setStatus(Status.WITHDRAWN.label);
		leaveApprovalRepository.saveLeaveApproval(leaveApply, org(), mongoTemplate);

		Set<String> years = leaveApply.getDateList().stream()
				.map(date -> String.valueOf(LocalDate.parse(date).getYear())).collect(Collectors.toSet());

		years.forEach(year -> {
			leaveMasterRepository
					.findByEmpIdAndYear(leaveApply.getEmpId(), year, org(), mongoTemplate)
					.ifPresent(leaveMaster -> {
						LeaveTransactions transaction = createLeaveTransaction(leaveApply, Status.WITHDRAWN.label, leaveMaster);
						leaveMaster.getLeaveTransactions().add(transaction);
						leaveMasterRepository.saveLeaveMaster(leaveMaster, org(), mongoTemplate);
					});
		});

		return translator.toLocale(AppMessages.LEAVE_WITHDRAWN);
	}

	@Override
	public String adminApproveOrRejectLeave(List<AdminLeaveApproveDto> leaveRequests) {

		String org = jwtHelper.getOrganizationCode();
		for (AdminLeaveApproveDto request : leaveRequests) {
			if ("approve".equalsIgnoreCase(request.getType())) {
				approveLeaves(request.getLeaveCode(), org);
			} else if ("reject".equalsIgnoreCase(request.getType())) {
				rejectLeaves(request.getLeaveCode(), request.getRemark(), org);
			}
		}
		return "Success";
	}

	private void approveLeaves(String leaveCode, String org) {

		String collection = leaveApprovalRepository.getCollectionName(org);

		Query query = new Query(Criteria.where("leaveCode").is(leaveCode));
		LeaveApply leaveApply = mongoTemplate.findOne(query, LeaveApply.class, collection);

		if ("Approved".equalsIgnoreCase(leaveApply.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave already approved");
		}

		updateLeaveBalanceAndStatuss(leaveApply, Status.APPROVED.label, org);
		leaveApply.setStatus(Status.APPROVED.label);
		leaveApply.setApproveOrRejectDate(LocalDateTime.now());

		mongoTemplate.save(leaveApply, collection);
	}

	private void rejectLeaves(String leaveCode, String rejectReason, String org) {
		String collection = leaveApprovalRepository.getCollectionName(org);

		Query query = new Query(Criteria.where("leaveCode").is(leaveCode));
		LeaveApply leaveApply = mongoTemplate.findOne(query, LeaveApply.class, collection);

		if (Status.REJECTED.label.equalsIgnoreCase(leaveApply.getStatus())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave already rejected");
		}

		leaveApply.setStatus(Status.REJECTED.label);
		leaveApply.setApproverReason(rejectReason);
		leaveApply.setApproveOrRejectDate(LocalDateTime.now());

		mongoTemplate.save(leaveApply, collection);
	}

	private void updateLeaveBalanceAndStatuss(LeaveApply leaveApply, String status, String org) {

		String collection = leaveMasterRepository.getCollectionName(org);

		Set<String> years = leaveApply.getDateList().stream()
				.map(date -> String.valueOf(LocalDate.parse(date).getYear())).collect(Collectors.toSet());

		for (String year : years) {
			Query query = new Query(Criteria.where("empId").is(leaveApply.getEmpId()).and("year").is(year));
			Update update = new Update();

			LeaveMaster leaveMaster = mongoTemplate.findOne(query, LeaveMaster.class, collection);
			if (leaveMaster != null) {
				for (LeaveBalanceSummary summary : leaveMaster.getLeaveBalanceSummary()) {
					if (summary.getLeaveTypeName().equalsIgnoreCase(leaveApply.getLeaveType())) {
						if (Status.APPROVED.label.equals(status)) {
							summary.setBalance(summary.getBalance() - leaveApply.getNumOfDays());
							summary.setAvailed(summary.getAvailed() + leaveApply.getNumOfDays());
						}
					}
				}

				LeaveTransactions transaction = createLeaveTransactions(leaveApply, status, leaveMaster);
				leaveMaster.getLeaveTransactions().add(transaction);
				update.set("leaveTransactions", leaveMaster.getLeaveTransactions());

				mongoTemplate.updateFirst(query, update, LeaveMaster.class, collection);
			}
		}

		if (leaveApply.getLeaveApply() != null) {
			leaveApply.getLeaveApply().forEach(leaveDate -> {
				leaveDate.setStatus(status);
				leaveDate.setApproverId(getAuthenticatedUser().getEmpId());
				leaveDate.setApproverStatus(status);
				leaveDate.setApproverAt(LocalDateTime.now().toString());
			});
		}
		leaveApply.setStatus(status);
		leaveApply.setApproveOrRejectDate(LocalDateTime.now());
		mongoTemplate.save(leaveApply, collection);
	}

	private LeaveTransactions createLeaveTransactions(LeaveApply leaveApply, String status, LeaveMaster leaveMaster) {
		LeaveTransactions transaction = new LeaveTransactions();
		transaction.setTransactionId(idGenerator.generateTransactionId(leaveMaster));
		transaction.setLeaveTypeName(leaveApply.getLeaveType());
		if (Status.WITHDRAWN.label.equalsIgnoreCase(status)) {
			transaction.setTransactionType(Status.WITHDRAWN.label);
		} else {
			transaction.setTransactionType(Status.APPROVED.label.equalsIgnoreCase(status) ? "Availed" : "Rejected");
		}
		transaction.setProcessedBy(getAuthenticatedUser().getEmpId());
		transaction.setPostedOn(LocalDate.now().toString());
		transaction.setFromDate(leaveApply.getFromDate());
		transaction.setToDate(leaveApply.getToDate());
		transaction.setFromSession(leaveApply.getFromSession());
		transaction.setToSession(leaveApply.getToSession());
		transaction.setNoOfDays(leaveApply.getNumOfDays());

		return transaction;
	}
	
	private String org() {
		return jwtHelper.getOrganizationCode();
	}
}