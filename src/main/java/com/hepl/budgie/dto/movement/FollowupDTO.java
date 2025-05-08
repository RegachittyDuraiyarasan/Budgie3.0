package com.hepl.budgie.dto.movement;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FollowupDTO {
    private List<FollowupDetails> followupDetails = new ArrayList<>();
}
