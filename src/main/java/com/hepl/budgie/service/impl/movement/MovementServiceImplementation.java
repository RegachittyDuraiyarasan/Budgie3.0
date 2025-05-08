package com.hepl.budgie.service.impl.movement;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.movement.*;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.movement.*;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.movement.EmployeeDetailsMapper;
import com.hepl.budgie.mapper.movement.MovementMapper;
import com.hepl.budgie.repository.movement.MovementRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.movement.MovementService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementServiceImplementation implements MovementService {
    private final UserInfoRepository userInfoRepository;
    private final MovementRepository movementRepository;
    private final MovementMapper movementMapper;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private final EmployeeDetailsMapper employeeDetailsMapper;

    @Override
    public List<EmpCodeValueDTO> getEmployeeCodeUnderRM(String empId) {
        return userInfoRepository.fetchEmployeeUnderRM(empId);
    }

    @Override
    public void initiateMovementByRM(MovementInitiateDTO request) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(request.getEmpId());
        if (userInfoOptional.isEmpty()) {
            throw new CustomResponseStatusException(AppMessages.EMPLOYEEID_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
        }
        UserInfo userInfo = userInfoOptional.get();

        // Validate old Reviewer and Supervisor
        String currentReviewer = userInfo.getSections().getHrInformation().getReviewer().getManagerId();
        String currentSupervisor = userInfo.getSections().getHrInformation().getPrimary().getManagerId();
        String currentDepartment = userInfo.getSections().getWorkingInformation().getDepartment();
        String currentDesignation = userInfo.getSections().getWorkingInformation().getDesignation();

        if (!request.getReviewerOld().equals(currentReviewer) || !request.getSupervisorOld().equals(currentSupervisor)) {
            throw new CustomResponseStatusException(AppMessages.SUPERVISOR_OR_REVIEWER_NOT_MATCH, HttpStatus.BAD_REQUEST, null);
        }

        // Create a new MovementDetails object
        MovementDetails movementDetails = new MovementDetails();
        boolean typeFound = false;

        MovementType type = request.getMovementType(); // Directly assign the value

        if (type != null) {
            typeFound = true;
            movementDetails.setMovementTeamId(0); // Default for department & designation

            if (type.getDepartment()) {
                movementDetails.setMovementTypeId(0);
                movementDetails.setDepartment(new MovementTransferDetails(
                        userInfo.getSections().getWorkingInformation().getDepartment(),
                        request.getDepartmentNow()
                ));
            }
            if (type.getDesignation()) {
                movementDetails.setMovementTypeId(0);
                movementDetails.setDesignation(new MovementTransferDetails(
                        userInfo.getSections().getWorkingInformation().getDesignation(),
                        request.getDesignationNow()
                ));
            }
            if (type.getSupervisor() || type.getReviewer()) {
                movementDetails.setMovementTypeId(1);
                int movementTeamId = processSupervisorOrReviewer(request, jwtHelper.getUserRefDetail().getEmpId());
                movementDetails.setMovementTeamId(movementTeamId);
                movementDetails.setSupervisor(new MovementTransferDetails(currentSupervisor, request.getSupervisorNow()));

                if (movementTeamId == 1) {
                    movementDetails.setReviewer(new MovementTransferDetails(currentReviewer, request.getReviewerNow()));
                }

                log.info("movementTeamId: {}", movementTeamId);

                if (movementTeamId == 1 && movementDetails.getAssignedReviewerStatus() == null) {
                    movementDetails.setAssignedReviewerStatus(new ApprovalStatus());
                    movementDetails.getAssignedReviewerStatus().setStatus(Status.PENDING.label);
                }
            }

            // Initialize old values if not set
            if (movementDetails.getSupervisor() == null) {
                movementDetails.setSupervisor(new MovementTransferDetails(currentSupervisor, null));
            }
            if (movementDetails.getReviewer() == null) {
                movementDetails.setReviewer(new MovementTransferDetails(currentReviewer, null));
            }
            if (movementDetails.getDepartment() == null) {
                movementDetails.setDepartment(new MovementTransferDetails(currentDepartment, null));
            }
            if (movementDetails.getDesignation() == null) {
                movementDetails.setDesignation(new MovementTransferDetails(currentDesignation, null));
            }

            // Ensure OfficialReviewerStatus is initialized
            if (movementDetails.getOfficialReviewerStatus() == null) {
                movementDetails.setOfficialReviewerStatus(new ApprovalStatus());
            }
            movementDetails.getOfficialReviewerStatus().setStatus(Status.PENDING.label);
        }

        if (!typeFound) {
            throw new CustomResponseStatusException("No valid movement type found", HttpStatus.BAD_REQUEST, null);
        }

        // Find the existing movement document and append the new MovementDetails
        Query query = new Query(Criteria.where("empId").is(request.getEmpId()));

        // Generate next movement ID
        Movement existingMovement = mongoTemplate.findOne(query, Movement.class, mongoTemplate.getCollectionName(Movement.class)+(jwtHelper.getOrganizationCode().isEmpty() ? "" : "_" + jwtHelper.getOrganizationCode()));
        int nextMovementId = 1;
        if (existingMovement != null && existingMovement.getMovementDetails() != null) {
            nextMovementId = existingMovement.getMovementDetails().size() + 1;
        }
        movementDetails.setMovementId("MV" + String.format("%03d", nextMovementId));

        movementDetails.setMovementInitializer(jwtHelper.getUserRefDetail().getEmpId());
        movementDetails.setEffectiveFrom(request.getEffectiveFrom());
        movementDetails.setSupervisorRemarks(request.getSupervisorRemarks());
        movementDetails.setIsWithdrawn(Boolean.FALSE);
        movementDetails.setMovementType(request.getMovementType());
        if (movementDetails.getHrStatus() == null) {
            movementDetails.setHrStatus(new HRStatus());
        }
        movementDetails.getHrStatus().setStatus(Status.PENDING.label);
        movementDetails.setCreatedBy(jwtHelper.getUserRefDetail().getEmpId());
        movementDetails.setUpdatedBy(jwtHelper.getUserRefDetail().getEmpId());
        movementDetails.setCreatedAt(ZonedDateTime.now());
        movementDetails.setUpdatedAt(ZonedDateTime.now());


        movementRepository.updateMovementDetails(request.getEmpId(), movementDetails, jwtHelper.getOrganizationCode(), mongoTemplate);
    }

    private int processSupervisorOrReviewer(MovementInitiateDTO request, String authenticatedEmpId) {
        List<UserInfo> initializerInfo = userInfoRepository.findAllByEmpId(authenticatedEmpId);
        Set<String> initializerSupId = initializerInfo.stream()
                .map(userInfo -> userInfo.getSections().getHrInformation().getPrimary().getManagerId())
                .collect(Collectors.toSet());
        Set<String> initializerReviewerId = initializerInfo.stream()
                .map(userInfo -> userInfo.getSections().getHrInformation().getReviewer().getManagerId())
                .collect(Collectors.toSet());

        String newReviewer = request.getReviewerNow();

        String newReportingManager = request.getSupervisorNow();

        int movementTeamId = -1; // Default value indicating no match found

        if (request.getMovementType().getReviewer()) {
            if (initializerSupId.contains(newReviewer) || initializerReviewerId.contains(newReviewer)) {
                movementTeamId = 0;
            } else {
                movementTeamId = 1;
            }
        } else if (request.getMovementType().getSupervisor()) {
            if (initializerSupId.contains(newReportingManager) || initializerReviewerId.contains(newReportingManager)) {
                movementTeamId = 0;
            } else {
                movementTeamId = 1;
            }
        }
        return movementTeamId;
    }

    @Override
    public List<MovementFetchDTO> getEmployeesForPrimaryReportingManager(String[] hrStatus, Boolean initializerWithdraw) {
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        return movementRepository.getEmployeesForPrimaryReportingManager(mongoTemplate, hrStatus, initializerWithdraw, empId, jwtHelper.getOrganizationCode());
    }

    @Override
    public Map<String, Object> extractHrAndWithdrawStatus(String HRStatus, String WithdrawStatus) {
        String[] hrStatus;
        if (HRStatus != null) {
            hrStatus = switch (HRStatus) {
                case "Pending" -> new String[]{"Pending", "Hold"};
                case "Completed" -> new String[]{"Approved"};
                default -> throw new IllegalArgumentException("Invalid HRStatus value: " + HRStatus);
            };
        } else {
            hrStatus = new String[]{"Pending", "Approved", "Hold"};
        }

        Boolean initializerWithdraw = null;
        if (WithdrawStatus != null) {
            initializerWithdraw = switch (WithdrawStatus) {
                case "Pending" -> false;
                case "Completed" -> true;
                default -> throw new IllegalArgumentException("Invalid WithdrawStatus value: " + WithdrawStatus);
            };
        }

        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("hrStatus", hrStatus);
        statusMap.put("initializerWithdraw", initializerWithdraw);

        return statusMap;
    }

    @Override
    public List<MovementFetchDTO> getEmployeesUnderReviewer(String teamType) {

        if (jwtHelper.getUserRefDetail().getEmpId() == null) {
            return Collections.emptyList();
        }

        final Integer movementTeamId;
        if (teamType == null) {
            movementTeamId = null;
        } else if ("myTeam".equalsIgnoreCase(teamType)) {
            movementTeamId = 0;
        } else if ("otherTeam".equalsIgnoreCase(teamType)) {
            movementTeamId = 1;
        } else {
            throw new IllegalArgumentException("Invalid teamType value: " + teamType);
        }

        String org = jwtHelper.getOrganizationCode();

        // Get users under reviewer
        List<UserInfo> usersUnderReviewer = userInfoRepository.findEmpIdsUnderReviewer(jwtHelper.getUserRefDetail().getEmpId(), mongoTemplate);
        Set<String> empIds = usersUnderReviewer.stream()
                .map(UserInfo::getEmpId)
                .collect(Collectors.toSet());

        List<Movement> movements = movementRepository.findByEmpIds(empIds, org, mongoTemplate);

        Map<String, String> userNameMap = movementRepository.getUserNames(mongoTemplate, empIds);

        return movements.stream()
                .flatMap(movement -> movement.getMovementDetails().stream()
                        .filter(detail -> movementTeamId == null || detail.getMovementTeamId().equals(movementTeamId))
                        .map(detail -> {
                            MovementFetchDTO dto = movementMapper.toDto(detail, movement);
                            dto.setUserName(userNameMap.getOrDefault(movement.getEmpId(), " "));
                            dto.setCreatedAt(detail.getCreatedAt() != null ? detail.getCreatedAt() : ZonedDateTime.parse(" "));
                            return dto;
                        }))
                .collect(Collectors.toList());

    }


    private boolean anyMatch(String[] hrStatus, String value) {
        for (String status : hrStatus) {
            if (Objects.equals(status, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateReviewerStatus(List<ReviewerUpdateDTO> requestList) {
        movementRepository.updateReviewerStatus(requestList, jwtHelper.getOrganizationCode(), mongoTemplate, jwtHelper.getUserRefDetail().getEmpId());
    }

    @Override
    public void updateHRStatus(List<HrUpdateDTO> request) {
        movementRepository.updateHRStatus(request, jwtHelper.getOrganizationCode(),mongoTemplate, jwtHelper.getUserRefDetail().getEmpId());
    }
    @Override
    public List<MovementFetchDTO> getMovementInfoByHrStatus(String HRStatus, String InitializerWithdraw) {
        String [] hrStatus = null;
        Boolean initializerWithdraw = null;

        if (HRStatus != null) {
            hrStatus = switch (HRStatus) {
                case "Pending" -> new String[]{"Pending", "Hold"};
                case "Completed" -> new String[]{"Approved"};
                default -> throw new IllegalArgumentException("Invalid HR status value: " + HRStatus);
            };
        }

        if (InitializerWithdraw != null) {
            initializerWithdraw = switch (InitializerWithdraw) {
                case "Pending" -> false;
                case "Completed" -> true;
                default ->
                        throw new IllegalArgumentException("Invalid InitializerWithdraw value: " + InitializerWithdraw);
            };
        }

        return movementRepository.getMovementInfoByHrStatus(hrStatus, initializerWithdraw, mongoTemplate, movementMapper, jwtHelper.getOrganizationCode());
    }

    @Override
    public void updateWithdrawStatus(String empId, String movementId) {
        movementRepository.updateInitializerWithdraw(empId, movementId, jwtHelper.getOrganizationCode(),mongoTemplate);
    }

    @Override
    public EmployeeCurrentDetail getEmployeeOldDetails(String empId) {
        Document document = userInfoRepository.getEmployeeCurrentDetails(mongoTemplate, empId);
        return document != null ? employeeDetailsMapper.toEmployeeDetailsDTO(document) : null;
    }




}
