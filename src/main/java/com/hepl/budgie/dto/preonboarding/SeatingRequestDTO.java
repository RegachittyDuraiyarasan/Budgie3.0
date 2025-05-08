package com.hepl.budgie.dto.preonboarding;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SeatingRequestDTO {
    private String empId;
    private String email;
    private String mobileNumber;
    private boolean seatingStatus;
    private boolean idCardStatus;
}
