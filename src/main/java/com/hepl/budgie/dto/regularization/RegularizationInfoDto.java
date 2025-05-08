package com.hepl.budgie.dto.regularization;
import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegularizationInfoDto {

    private LocalDate date;
    private String reason;
    private String inTime;
    private String outTime;
}
