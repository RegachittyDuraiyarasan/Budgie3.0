package com.hepl.budgie.entity.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
class DocumentVerification {
    private LocalDateTime verifiedAt;
    private String status;
}
