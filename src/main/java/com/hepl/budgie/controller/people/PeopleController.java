package com.hepl.budgie.controller.people;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;
import com.hepl.budgie.dto.employee.TeamResponseDTO;
import com.hepl.budgie.dto.people.PeopleDTO;
import com.hepl.budgie.service.people.PeopleService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Tag(name = "Manage peoples", description = "")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/people")
@SecurityRequirement(name = "Bearer Authentication")
public class PeopleController {

    private final PeopleService peopleService;
    private final Translator translator;

    @GetMapping("/my-teams")
    public GenericResponse<TeamResponseDTO> teamMembers() {
        log.info("Get team members");

        return GenericResponse.success(peopleService.getTeams());
    }

    @GetMapping("/my-teams/{empId}")
    public GenericResponse<List<EmployeeOrgChartDTO>> teamMembersByEmployee(@PathVariable String empId) {
        log.info("Get team members by {}", empId);

        return GenericResponse.success(peopleService.getTeamsByEmployee(empId));
    }

    @GetMapping("/org-chart")
    public GenericResponse<EmployeeOrgChartDTO> getOrgChart() {
        log.info("Fetching org chart");
        peopleService.getEmployeeOrgChart();
        return GenericResponse.success(peopleService.getEmployeeOrgChart());
    }

    @PostMapping("/activePeople")
    public GenericResponse<List<PeopleDTO>> getActivePeople(
            @RequestBody(required = false) Map<String, String> request) {
        String department = request != null ? request.get("department") : null;
        String designation = request != null ? request.get("designation") : null;
        String workLocation = request != null ? request.get("workLocation") : null;
        List<PeopleDTO> activePeople = peopleService.getActivePeople(department, designation, workLocation);
        log.info("activePeople {}", activePeople);
        return GenericResponse.success(activePeople);
    }

    @PostMapping("/starredPeople")
    public GenericResponse<List<PeopleDTO>> getStarredPeople(
            @RequestBody(required = false) Map<String, String> request) {
        String department = request != null ? request.get("department") : null;
        String designation = request != null ? request.get("designation") : null;
        String workLocation = request != null ? request.get("workLocation") : null;
        List<PeopleDTO> starredPeople = peopleService.getStarredPeople(department, designation, workLocation);
        log.info("activePeople {}", starredPeople);
        return GenericResponse.success(starredPeople);
    }

    @GetMapping("/{empId}")
    public GenericResponse<List<PeopleDTO>> getEmployee(@PathVariable String empId) {
        log.info("empId: {}", empId);
        List<PeopleDTO> peopleDTOList = peopleService.getEmployee(empId);
        return GenericResponse.success(peopleDTOList);
    }


    @PostMapping()
    public GenericResponse<String> toggleStarredEmployee(@RequestBody Map<String, String> request) {
        String starredEmpIds = request.get("newEmpId");

        if (starredEmpIds == null || starredEmpIds.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PEOPLE_EMPTY);
        }

        boolean isStarred = peopleService.toggleStarredEmployee(starredEmpIds);

        String message = isStarred ? translator.toLocale(AppMessages.PEOPLE_STARRED)
                : translator.toLocale(AppMessages.PEOPLE_UNSTARRED);

        return GenericResponse.success(message);
    }

}
