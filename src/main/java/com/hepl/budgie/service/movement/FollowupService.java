package com.hepl.budgie.service.movement;

import com.hepl.budgie.dto.movement.FollowupDTO;

public interface FollowupService {
    FollowupDTO getFollowup(String empId);
}
