package com.hepl.budgie.controller.employeelist;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.employee.EmployeeDetailsDTO;
import com.hepl.budgie.dto.employee.EmployeeOrganisationDTO;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.role.EditRoleDTO;
import com.hepl.budgie.dto.userinfo.PasswordDTO;
import com.hepl.budgie.dto.userinfo.ProfileDetailsDTO;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.workflow.OrganizationDetails;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.EmployeeListService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.Multipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/employee")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeListController {

    private final EmployeeListService employeeListService;
    private final Translator translator;
    private final MasterFormService masterFormService;
    private final JWTHelper jwtHelper;

    @PostMapping
    public GenericResponse<String> addEmployee(@RequestBody FormRequest formRequest) {
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(),
                jwtHelper.getOrganizationCode(),
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, jwtHelper.getOrganizationCode(), AccessLevelType.ADD, formFields);
        employeeListService.addEmployee(formRequest, jwtHelper.getOrganizationCode());
        return GenericResponse.success(translator.toLocale(AppMessages.ADDED));
    }

    @GetMapping("/downloadSampleFile")
    @Operation(summary = "Sample Excel File")
    public ResponseEntity<byte[]> fetch() throws IOException {
        byte[] excelContent = employeeListService.sampleExcelDownload();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "CTC Breakups.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);

    }

    @PutMapping("/edit-role-type")
    @Operation(summary = "Update the employee RoleType")
    public GenericResponse<String> updateEmployeeRoleType(
            @RequestParam String employeeId,
            @RequestBody List<EditRoleDTO> roleDTOList) {
        employeeListService.updateEmployeeRoleType(employeeId, roleDTOList);
        return GenericResponse.success(translator.toLocale(AppMessages.EMPLOYEE_ROLE_UPDATED));
    }

    @PutMapping("/{employeeId}/editorgsAndSubOrgs")
    @Operation(summary = "Update the employee organization and sub-organization")
    public GenericResponse<String> updateEmployeeOrgs(
            @PathVariable String employeeId,
            @RequestBody OrganizationDetails request) {
        employeeListService.updateEmployeeOrganizations(employeeId, request.getOrganizationName(),
                request.getSubOrganization());
        return GenericResponse.success(translator.toLocale(AppMessages.EMPLOYEE_ORG_UPDATED));
    }

    // @PostMapping("/upload")
    // public ResponseEntity<byte[]> uploadEmployeeExcel(
    // @RequestParam("file") MultipartFile file,
    // @RequestParam("type") String type) {
    // try {
    // log.info("Uploading Excel file for type: {}", type);
    // return employeeService.uploadExcel(file, type);
    // } catch (Exception e) {
    // log.error("Error while processing Excel upload: {}", e.getMessage(), e);
    // return ResponseEntity.internalServerError()
    // .body(("Error while processing file: " + e.getMessage()).getBytes());
    // }
    // }

    @GetMapping("/all")
    public GenericResponse<List<EmployeeDetailsDTO>> getAllEmployees() {
        List<EmployeeDetailsDTO> employees = employeeListService.findAllEmployees();
        return employees.isEmpty()
                ? GenericResponse.error("NO_DATA", AppMessages.EMPLOYEES_RETRIEVED_SUCCESS)
                : GenericResponse.success(employees);
    }

    @GetMapping("/getActiveEmployees")
    public GenericResponse<List<EmployeeActiveDTO>> getActiveEmployees() {
        List<EmployeeActiveDTO> employees = employeeListService.findByStatus();
        return employees.isEmpty()
                ? GenericResponse.error("NO_DATA", AppMessages.EMPLOYEES_RETRIEVED_SUCCESS)
                : GenericResponse.success(employees);
    }

    @GetMapping("/filter")
    public GenericResponse<List<EmployeeDetailsDTO>> getEmployeesByFilters(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String reviewerId,
            @RequestParam(required = false) String reportingManagerId,
            @RequestParam(required = false) String payRollStatus,
            @RequestParam(required = false) String band,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String organization,
            @RequestParam(required = false) String subOrganization,
            @RequestParam(required = false) String image) {
        List<EmployeeDetailsDTO> employees = employeeListService.findEmployeesByFilters(
                employeeId, department, designation, gender, reviewerId, reportingManagerId, payRollStatus, band,
                status, organization, subOrganization, image);
        if (employees.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
        return GenericResponse.success(employees);
    }

    @GetMapping("/getOrganisationRoles")
    public GenericResponse<List<EmployeeOrganisationDTO>> getOrganisationRoles(@RequestParam String org) {
        List<EmployeeOrganisationDTO> roles = employeeListService.getOrganisationRoles(org);

        return roles.isEmpty()
                ? GenericResponse.error("NO_DATA", "No roles found for the given organization.")
                : GenericResponse.success(roles);
    }

    @GetMapping("/getOrg")
    public GenericResponse<List<EmployeeOrganisationDTO>> getOrg() {
        List<EmployeeOrganisationDTO> roles = employeeListService.getAllOrgansSubOrg();

        return roles.isEmpty()
                ? GenericResponse.error("NO_DATA", "No roles found for the given organization.")
                : GenericResponse.success(roles);
    }

    @GetMapping("/getReviewer")
    public GenericResponse<List<Map<String, String>>> getAllReviewers() {
        log.info("Fetching all reviewers");

        List<Map<String, String>> reviewers = employeeListService.getAllReviewerDetails();

        if (reviewers.isEmpty()) {
            log.warn("No reviewers found");
            return GenericResponse.error("NO_REVIEWERS_FOUND", "No reviewers available.");
        }

        return GenericResponse.success(reviewers);
    }

    @GetMapping("/getEmployeeProfile")
    public GenericResponse<ProfileDetailsDTO> getEmployeeProfile() {
        String empid = jwtHelper.getUserRefDetail().getEmpId();

        log.info("Fetching profile details for employee ID: {}", empid);

        ProfileDetailsDTO profileDetails = employeeListService.getEmployeeBasicDetails(empid);
        return GenericResponse.success(profileDetails);
    }

    @GetMapping("/getEmployeeProfileByHR/{empId}")
    public GenericResponse<ProfileDetailsDTO> getEmployeeProfileByHr(@PathVariable String empId) {
        log.info("Fetching profile details for employee ID: {}", empId);

        ProfileDetailsDTO profileDetails = employeeListService.getEmployeeBasicDetails(empId);
        return GenericResponse.success(profileDetails);
    }

    @PostMapping("/updateProfileOverallSubmit")
    public GenericResponse<String> updateProfileOverallSubmit(
            @RequestBody Map<String, String> request) {
        String empid = jwtHelper.getUserRefDetail().getEmpId();

        log.info("Updating profileOverallSubmit for empId: {}", empid);

        String profileOverallSubmit = request.get("updateProfileOverallSubmit");
        if (profileOverallSubmit == null || profileOverallSubmit.isEmpty()) {
            return GenericResponse.error("INVALID_REQUEST", "updateProfileOverallSubmit field is required.");
        }

        String message = employeeListService.updateProfileOverallSubmit(profileOverallSubmit);

        return GenericResponse.success(message, AppMessages.USER_ONBOARDING);
    }

    @GetMapping("/getRoleOfIntake")
    public GenericResponse<List<Map<String, String>>> getRoleOfIntake(
            @RequestParam(defaultValue = "", required = false) String org) {
        if (org.isEmpty()) {
            return GenericResponse.success(Collections.emptyList());
        }
        List<Map<String, String>> roles = employeeListService.getRoleOfIntake(org);

        if (roles.isEmpty()) {
            return GenericResponse.error("NO_DATA", "No roles found for the given organization.");
        }

        return GenericResponse.success(roles);
    }

    @GetMapping("/by-primary-manager/{primaryManagerId}")
    public GenericResponse<List<Map<String, String>>> getEmployeesByPrimaryManager(
            @PathVariable String primaryManagerId) {
        List<Map<String, String>> employees = employeeListService.getEmployeesByPrimaryManager(primaryManagerId);

        if (employees.isEmpty()) {
            log.warn("No employees found reporting to primary manager with ID: {}", primaryManagerId);
            return GenericResponse.error("NO_EMPLOYEES_FOUND", "No employees available for the given primary manager.");
        }

        return GenericResponse.success(employees);
    }

    @GetMapping("/getGender")
    public GenericResponse<List<Map<String, String>>> getGender(@RequestParam String referenceName) {
        List<Map<String, String>> genderList = employeeListService.getGender(referenceName);

        return GenericResponse.success(genderList);

    }

    @GetMapping("/filterPage")
    public GenericResponse<Page<EmployeeDetailsDTO>> getEmployeesByFilters(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String reviewerId,
            @RequestParam(required = false) String reportingManagerId,
            @RequestParam(required = false) String payRollStatus,
            @RequestParam(required = false) String band,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String organization,
            @RequestParam(required = false) String subOrganization, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int sizePerPage,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "") String query) {
        Pageable pageable = PageRequest.of(page, sizePerPage, sortDirection, "_id");

        Page<EmployeeDetailsDTO> employees = employeeListService.findEmployeesBypage(
                employeeId, department, designation, gender, reviewerId, reportingManagerId, payRollStatus, band,
                status, organization, subOrganization, pageable, query);
        if (employees.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
        return GenericResponse.success(employees);
    }

    @GetMapping("/{all}")
    public GenericResponse<List<UserInfo>> getByAll(@PathVariable String all) {
        List<UserInfo> userInfos = employeeListService.findByAll(all);
        userInfos.forEach(System.out::println);
        return GenericResponse.success(userInfos);
    }

    @PutMapping("/update-idCardByHr")
    public GenericResponse<String> updateIdCardByHr(@RequestParam(required = false) String empId,
            @RequestParam(required = false) String empRelation, @RequestParam(required = false) String nameOfRelation,
            @RequestParam(required = false) String contactNo, @RequestParam String status) throws IOException {
        return employeeListService.updateIdCardByHr(empId, empRelation, nameOfRelation, contactNo, status);
    }

    @PutMapping("/update-password")
    public GenericResponse<String> updatePassword(@RequestBody PasswordDTO passwordDTO) {
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        String response = employeeListService.updatePassword(empId, passwordDTO);

        return GenericResponse.success(response);
    }

}