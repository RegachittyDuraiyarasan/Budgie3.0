package com.hepl.budgie.service.people;

import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;
import com.hepl.budgie.dto.employee.TeamResponseDTO;
import com.hepl.budgie.dto.people.PeopleDTO;

import java.util.List;

public interface PeopleService {

    List<PeopleDTO> getActivePeople(String department, String designation, String workLocation);

    List<PeopleDTO> getStarredPeople(String department, String designation, String workLocation);

    List<PeopleDTO> getEmployee(String empId);

    boolean toggleStarredEmployee(String newEmpId);

    EmployeeOrgChartDTO getEmployeeOrgChart();

    TeamResponseDTO getTeams();

    List<EmployeeOrgChartDTO> getTeamsByEmployee(String empId);

}
