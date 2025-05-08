package com.hepl.budgie.repository.movement;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.dto.movement.HrUpdateDTO;
import com.hepl.budgie.dto.movement.MovementFetchDTO;
import com.hepl.budgie.dto.movement.ReviewerUpdateDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.movement.Movement;
import com.hepl.budgie.entity.movement.MovementDetails;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.movement.MovementMapper;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;
import io.micrometer.common.util.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public interface MovementRepository extends MongoRepository<Movement, String> {

    public static final String COLLECTION_NAME = "movementInfo";

//    Optional<Movement> findByEmpId(String empId);

    default void updateMovementDetails(String empId, MovementDetails newDetail, String org,
                                       MongoTemplate mongoTemplate) {
        String collectionName = "movementInfo" + (org.isEmpty() ? "" : "_" + org); // Dynamic collection based on org

        Query query = new Query(Criteria.where("empId").is(empId));
        Update update = new Update().push("movementDetails", newDetail);

        UpdateResult result = mongoTemplate.updateFirst(query, update, collectionName);

        // If no document was modified (empId not found), insert a new record
        if (result.getMatchedCount() == 0) {
            Movement newMovement = new Movement();
            newMovement.setEmpId(empId);
            newMovement.setMovementDetails(new ArrayList<>(List.of(newDetail)));

            mongoTemplate.insert(newMovement, collectionName);
        }
    }

    default Movement findByEmpId(String empId, String org, MongoTemplate mongoTemplate) {
        if (StringUtils.isBlank(empId)) { // Ensures null and empty check
            return null;
        }
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);

        Query query = new Query(Criteria.where("empId").in(empId));
        return mongoTemplate.findOne(query, Movement.class, collectionName);
    }

    default List<Movement> findByEmpIds(Set<String> empIds, String org, MongoTemplate mongoTemplate) {
        if (empIds.isEmpty())
            return Collections.emptyList();
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org); // Dynamic collection based on org

        Query query = new Query(Criteria.where("empId").in(empIds));
        return mongoTemplate.find(query, Movement.class, collectionName);
    }

    default void updateReviewerStatus(List<ReviewerUpdateDTO> requestList, String org, MongoTemplate mongoTemplate, String authenticatedEmpId) {
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org); // Dynamic collection based on org

        for (ReviewerUpdateDTO request : requestList) {
            Query query = new Query(Criteria.where("empId").is(request.getEmpId())
                    .and("movementDetails.movementId").is(request.getMovementId())); // Ensure movementId matches

            Movement movement = mongoTemplate.findOne(query, Movement.class, collectionName);
            if (movement == null) {
                throw new CustomResponseStatusException(
                        AppMessages.MOVEMENT_NOT_FOUND,
                        HttpStatus.NOT_FOUND, null);
            }
            for (MovementDetails detail : movement.getMovementDetails()) {
                if (!detail.getMovementId().equals(request.getMovementId())) {
                    continue;
                }

                Update update = new Update();

                if (detail.getMovementTeamId() == 0) {
                    update.set("movementDetails.$.officialReviewerStatus.status", request.getOfficialReviewerStatus());
                    update.set("movementDetails.$.officialReviewerStatus.approvedAt", ZonedDateTime.now());
                    update.set("movementDetails.$.officialReviewerStatus.approvedBy", authenticatedEmpId);

                } else if (detail.getMovementTeamId() == 1) {
                    if ("pending".equalsIgnoreCase(detail.getOfficialReviewerStatus().getStatus()) &&
                            request.getAssignedReviewerStatus() != null) {
                        throw new CustomResponseStatusException(
                                AppMessages.ASS_REV_STATUS_CANT_UPDATE,
                                HttpStatus.BAD_REQUEST, null);
                    }

                    if (request.getOfficialReviewerStatus() != null) {
                        update.set("movementDetails.$.officialReviewerStatus.status",
                                request.getOfficialReviewerStatus());
                        update.set("movementDetails.$.officialReviewerStatus.approvedAt", ZonedDateTime.now());
                        update.set("movementDetails.$.officialReviewerStatus.approvedBy", authenticatedEmpId);

                    }
                    if ("approved".equalsIgnoreCase(detail.getOfficialReviewerStatus().getStatus()) &&
                            request.getAssignedReviewerStatus() != null) {
                        update.set("movementDetails.$.assignedReviewerStatus.status",
                                request.getAssignedReviewerStatus());
                        update.set("movementDetails.$.assignedReviewerStatus.approvedAt",
                                ZonedDateTime.now());
                        update.set("movementDetails.$.assignedReviewerStatus.approvedBy",
                                authenticatedEmpId);
                    }
                }

                mongoTemplate.updateFirst(query, update, Movement.class, collectionName);
            }
        }
    }

    default void updateHRStatus(List<HrUpdateDTO> requestList, String org, MongoTemplate mongoTemplate, String authenticatedEmpId) {
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);

        for (HrUpdateDTO request : requestList) {
            // Match both empId and movementDetails.movementId
            Query query = new Query(Criteria.where("empId").is(request.getEmpId())
                    .and("movementDetails.movementId").is(request.getMovementId()));

            Movement movement = mongoTemplate.findOne(query, Movement.class, collectionName);
            if (movement == null) {
                throw new CustomResponseStatusException(AppMessages.MOVEMENT_NOT_FOUND, HttpStatus.BAD_REQUEST, null);
            }

            // Find the specific movementDetails that matches the movementId
            MovementDetails detail = movement.getMovementDetails().stream()
                    .filter(movementDetails -> movementDetails.getMovementId().equalsIgnoreCase(request.getMovementId()))
                    .findFirst()
                    .orElseThrow(() -> new CustomResponseStatusException(AppMessages.MOVEMENT_NOT_FOUND, HttpStatus.NOT_FOUND, null));

            // Validate approval status before updating
            if (detail.getMovementTeamId() == 0 && !Status.APPROVED.label.equalsIgnoreCase(detail.getOfficialReviewerStatus().getStatus())) {
                throw new CustomResponseStatusException(AppMessages.OFF_REV_MUST_APPROVE, HttpStatus.BAD_REQUEST, null);
            }
            if (detail.getMovementTeamId() == 1 && (!Status.APPROVED.label.equalsIgnoreCase(detail.getOfficialReviewerStatus().getStatus()) ||
                    !Status.APPROVED.label.equalsIgnoreCase(detail.getAssignedReviewerStatus().getStatus()))) {
                throw new CustomResponseStatusException(AppMessages.BOTH_REV_MUST_APPROVE, HttpStatus.BAD_REQUEST, null);
            }

            // Use positional operator ($) to update the correct element in movementDetails array
            Update update = new Update();
            update.set("movementDetails.$.hrStatus.status", request.getHrStatus());
            update.set("movementDetails.$.hrStatus.hrApprovedAt", ZonedDateTime.now());
            update.set("movementDetails.$.hrStatus.hrApprovedBy", authenticatedEmpId);

            // Ensure userInfo exists before updating department, designation, supervisor, and reviewer
            if (detail.getMovementType() != null) {
                if (detail.getMovementType().getDepartment()) {
                    update.set("userInfo.sections.workingInformation.department", request.getDepartmentNow());
                }
                if (detail.getMovementType().getDesignation()) {
                    update.set("userInfo.sections.workingInformation.designation", request.getDesignationNow());
                }
                if (detail.getMovementType().getSupervisor()) {
                    update.set("userInfo.sections.hrInformation.primary.managerId", request.getSupervisorNow());
                }
                if (detail.getMovementType().getReviewer()) {
                    update.set("userInfo.sections.hrInformation.reviewer.managerId", request.getReviewerNow());
                }
            }

            // Apply the update
            mongoTemplate.updateFirst(query, update, Movement.class, collectionName);
        }
    }

    default List<MovementFetchDTO> getMovementInfoByHrStatus(String[] hrStatus, Boolean initializerWithdraw,
                                                             MongoTemplate mongoTemplate, MovementMapper movementMapper, String org) {
        Criteria criteria = new Criteria();

        if (hrStatus != null) {
            criteria.and("movementDetails.hrStatus.status").in(Arrays.asList(hrStatus));
        }

        if (initializerWithdraw != null) {
            criteria.and("movementDetails.isWithdrawn").is(initializerWithdraw);
        }

        Query query = new Query(criteria);
        List<Movement> movements = mongoTemplate.find(query, Movement.class, getCollectionName(org));

        // Extract unique empIds
        Set<String> empIds = movements.stream()
                .map(Movement::getEmpId)
                .collect(Collectors.toSet());

        // Get user names map
        Map<String, String> userNameMap = getUserNames(mongoTemplate, empIds);

        // Convert to DTO
        return movements.stream()
                .flatMap(movement -> movement.getMovementDetails().stream()
                        .filter(detail -> (hrStatus == null || Arrays.asList(hrStatus).contains(detail.getHrStatus().getStatus())) &&
                                (initializerWithdraw == null || initializerWithdraw.equals(detail.getIsWithdrawn())))
                        .map(detail -> {
                            MovementFetchDTO dto = movementMapper.toDto(detail, movement);
                            dto.setUserName(userNameMap.getOrDefault(movement.getEmpId(), " "));
                            dto.setCreatedAt((detail.getCreatedAt()));
                            return dto;
                        }))
                .collect(Collectors.toList());
    }

    default void updateInitializerWithdraw(String empId, String movementId, String org, MongoTemplate mongoTemplate) {
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);

        Query query = new Query(Criteria
                .where("empId").is(empId)
                .and("movementDetails.movementId").is(movementId));

        Movement movement = mongoTemplate.findOne(query, Movement.class, collectionName);

        if (movement == null) {
            throw new CustomResponseStatusException(AppMessages.MOVEMENT_NOT_FOUND, HttpStatus.NOT_FOUND, null);
        }

        MovementDetails detail = movement.getMovementDetails().stream()
                .filter(movementDetails -> movementDetails.getMovementId().equalsIgnoreCase(movementId))
                .findFirst()
                .orElseThrow(() -> new CustomResponseStatusException(AppMessages.MOVEMENT_NOT_FOUND, HttpStatus.NOT_FOUND, null));

        if (detail.getHrStatus() != null && Status.APPROVED.label.equalsIgnoreCase(detail.getHrStatus().getStatus())) {
            throw new CustomResponseStatusException(AppMessages.NOT_ALLOWED_TO_WITHDRAW, HttpStatus.BAD_REQUEST, null);
        }

        Update update = new Update().set("movementDetails.$.isWithdrawn", true);

        UpdateResult result = mongoTemplate.updateFirst(query, update, Movement.class, collectionName);

        if (result.getMatchedCount() == 0) {
            throw new CustomResponseStatusException(AppMessages.MOVEMENT_NOT_FOUND, HttpStatus.NOT_FOUND, null);
        }
    }
    default List<MovementFetchDTO> getEmployeesForPrimaryReportingManager(
            MongoTemplate mongoTemplate, String[] hrStatus, Boolean initializerWithdraw, String empId, String org) {

        Query query = new Query();
        Criteria criteria = Criteria.where("movementDetails.movementInitializer").is(empId);

        if (hrStatus != null && hrStatus.length > 0) {
            criteria = criteria.and("movementDetails.hrStatus.status").in((Object[]) hrStatus);
        }

        if (initializerWithdraw != null) {
            criteria = criteria.and("movementDetails.isWithdrawn").is(initializerWithdraw);
        }

        query.addCriteria(criteria);

        List<Movement> movementInformation = mongoTemplate.find(query, Movement.class, getCollectionName(org));

        // Collect only matching movementDetails
        List<MovementFetchDTO> result = new ArrayList<>();

        for (Movement movement : movementInformation) {
            for (MovementDetails detail : movement.getMovementDetails()) {
                boolean matches = (hrStatus == null || Arrays.asList(hrStatus).contains(detail.getHrStatus().getStatus())) &&
                        (initializerWithdraw == null || initializerWithdraw.equals(detail.getIsWithdrawn())) &&
                        empId.equals(detail.getMovementInitializer());

                if (matches) {
                    MovementFetchDTO dto = MovementMapper.INSTANCE.toDto(detail, movement);
                    dto.setCreatedAt(detail.getCreatedAt());
                    result.add(dto);
                }
            }
        }

        Set<String> empIds = result.stream()
                .map(MovementFetchDTO::getEmpId)
                .collect(Collectors.toSet());

        Map<String, String> userNameMap = getUserNames(mongoTemplate, empIds);

        result.forEach(dto -> dto.setUserName(userNameMap.getOrDefault(dto.getEmpId(), " ")));

        return result;
    }
 default Map<String, String> getUserNames(MongoTemplate mongoTemplate, Set<String> empIds) {
        if (empIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Query userQuery = new Query(Criteria.where("empId").in(empIds));
        List<UserInfo> userInfoList = mongoTemplate.find(userQuery, UserInfo.class, "userinfo");

        return userInfoList.stream()
                .collect(Collectors.toMap(UserInfo::getEmpId, userInfo -> userInfo.getSections().getBasicDetails().getFirstName()+" "+userInfo.getSections().getBasicDetails().getLastName()));
    }

    default String getCollectionName(String org){
        return COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);

    }




}
