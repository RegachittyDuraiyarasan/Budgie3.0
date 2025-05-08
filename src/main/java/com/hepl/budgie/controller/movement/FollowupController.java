package com.hepl.budgie.controller.movement;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.movement.FollowupDTO;
import com.hepl.budgie.service.movement.FollowupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("users/followup-info")
@Slf4j
public class FollowupController {

    private final FollowupService followupService;

    private final JWTHelper jwtHelper;

    public FollowupController(FollowupService followupService, JWTHelper jwtHelper) {
        this.followupService = followupService;
        this.jwtHelper = jwtHelper;
    }

    @GetMapping("")
    public GenericResponse<FollowupDTO> getFollowup(){
        FollowupDTO followupDTO = followupService.getFollowup(jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(followupDTO);
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<FollowupDTO> getHrFollowup(@PathVariable String empId){
        FollowupDTO followupDTO = followupService.getFollowup(empId);
        return GenericResponse.success(followupDTO);
    }

}
