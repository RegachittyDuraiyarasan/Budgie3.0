package com.hepl.budgie.service.impl.movement;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.movement.FollowupDTO;
import com.hepl.budgie.dto.movement.FollowupDetails;
import com.hepl.budgie.entity.movement.Movement;
import com.hepl.budgie.entity.movement.MovementDetails;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.movement.MovementRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.movement.FollowupService;
import com.hepl.budgie.utils.AppMessages;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowupServiceImplementation implements FollowupService {

    private final MovementRepository movementRepository;

    private final UserInfoRepository userInfoRepository;

    private final MongoTemplate mongoTemplate;

    private final JWTHelper jwtHelper;



    public FollowupServiceImplementation(MovementRepository movementRepository, UserInfoRepository userInfoRepository, MongoTemplate mongoTemplate, JWTHelper jwtHelper) {
        this.movementRepository = movementRepository;
        this.userInfoRepository = userInfoRepository;
        this.mongoTemplate = mongoTemplate;
        this.jwtHelper = jwtHelper;
    }

    @Override
    public FollowupDTO getFollowup(String empId){

        String org = jwtHelper.getOrganizationCode();
        Movement movement = movementRepository.findByEmpId(empId, org, mongoTemplate);

        if (movement == null || movement.getMovementDetails() == null){
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "");
        }

        List<MovementDetails> movementDetails = movement.getMovementDetails()
                .stream()
                .filter(details -> details.getMovementType() != null)
                .toList();

        FollowupDTO followupDTO = new FollowupDTO();
        String supervisorEmpId = null;
        String reviewerEmpId = null;

        for (MovementDetails details : movementDetails) {
            FollowupDetails followupDetails = new FollowupDetails();

            // Set department
            if (details.getMovementType().getDepartment() && details.getDepartment() != null) {
                followupDetails.setDepartment(details.getDepartment().getNow());
            }

            // Set designation
            if (details.getMovementType().getDesignation() && details.getDesignation() != null) {
                followupDetails.setDesignation(details.getDesignation().getNow());
            }

            // Set supervisor
            if (details.getMovementType().getSupervisor() && details.getSupervisor() != null) {
                supervisorEmpId = details.getSupervisor().getNow();
                UserInfo userInfo = userInfoRepository.findByEmpId(supervisorEmpId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

                Sections sections = userInfo.getSections();
                if (sections == null || sections.getBasicDetails() == null){
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
                }
                String firstName = sections.getBasicDetails().getFirstName();
                String lastName =  sections.getBasicDetails().getLastName();
                String userName = "" + firstName + " " + lastName + " - " + supervisorEmpId +"";
                followupDetails.setSupervisor(userName);
            }

            // Set reviewer
            if (details.getMovementType().getReviewer() && details.getReviewer() != null) {
                reviewerEmpId = details.getReviewer().getNow();
                UserInfo userInfo = userInfoRepository.findByEmpId(reviewerEmpId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

                Sections sections = userInfo.getSections();
                if (sections == null || sections.getBasicDetails() == null){
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
                }
                String firstName = sections.getBasicDetails().getFirstName();
                String lastName = sections.getBasicDetails().getLastName();
                String userName = "" + firstName + " " + lastName + " - " + reviewerEmpId + "";
                followupDetails.setReviewer(userName);
            }

            // Set HR Approved At
            if ("Approved".equalsIgnoreCase(details.getHrStatus().getStatus()) && details.getHrStatus().getStatus() != null){
                followupDetails.setHrApprovedAt(details.getHrStatus().getHrApprovedAt());
            }

            // Add to the list
            followupDTO.getFollowupDetails().add(followupDetails);
        }

        return followupDTO;
    }
}
