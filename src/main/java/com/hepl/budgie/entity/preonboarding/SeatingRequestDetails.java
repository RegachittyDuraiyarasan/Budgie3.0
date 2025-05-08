package com.hepl.budgie.entity.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
 public class SeatingRequestDetails {
    private Boolean isSeatingRequestInitiated;
    private String seatingRequestApprovedBy;
    private LocalDateTime seatingRequestApprovedAt;
    private Boolean isIdCardRequestInitiated;
    private String IdCardApprovedBy;
    private LocalDateTime IdCardApprovedAt;

}
