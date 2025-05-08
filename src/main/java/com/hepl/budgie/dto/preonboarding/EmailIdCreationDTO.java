package com.hepl.budgie.dto.preonboarding;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmailIdCreationDTO {
    private String employeeId;
//    private String name;
    private String email;
    private boolean status;
    private String hrSuggestedEmail;
}
