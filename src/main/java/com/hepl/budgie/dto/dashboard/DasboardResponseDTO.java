package com.hepl.budgie.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DasboardResponseDTO {

    private List<EmployeeOrgChartDTO> birthdays;
    private List<EmployeeOrgChartDTO> workAnniversary;

}
