package com.hepl.budgie.dto.payroll;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceDateFetchDTO {
    private String standardStartDate;
    private String standardEndDate;
}
