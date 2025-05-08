package com.hepl.budgie.entity.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequestDetails {
    private Boolean isEmailCreationInitiated;
    private LocalDateTime emailCreationInitiatedAt;
    private String hrSuggestedMail;
    private String suggestedBy;
    private Boolean isEmailIdCreated;
    private String emailIDCreatedBy;
    private LocalDateTime emailIdCreatedAt;
    private String confirmedMail;
}
