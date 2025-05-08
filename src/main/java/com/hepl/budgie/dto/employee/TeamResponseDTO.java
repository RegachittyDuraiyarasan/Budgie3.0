package com.hepl.budgie.dto.employee;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamResponseDTO {

    private List<EmployeeActiveDTO> userWithReportees;
    private List<EmployeeOrgChartDTO> reportees;

}
