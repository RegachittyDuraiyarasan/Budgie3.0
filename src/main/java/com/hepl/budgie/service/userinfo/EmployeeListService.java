package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.employee.EmployeeDetailsDTO;
import com.hepl.budgie.dto.employee.EmployeeOrganisationDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.role.EditRoleDTO;
import com.hepl.budgie.dto.userinfo.PasswordDTO;
import com.hepl.budgie.dto.userinfo.ProfileDetailsDTO;
import com.hepl.budgie.entity.userinfo.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface EmployeeListService {
        UserInfo addEmployee(FormRequest formRequest, String org);

        void updateEmployeeRoleType(String employeeId, List<EditRoleDTO> roleDTOList);

        void updateEmployeeOrganizations(String employeeId, String organization, List<String> subOrganization);

        List<EmployeeDetailsDTO> findAllEmployees();

        List<EmployeeActiveDTO> findByStatus();

        List<Map<String, String>> getGender(String referenceName);

        List<EmployeeOrganisationDTO> getOrganisationRoles(String org);

        List<EmployeeOrganisationDTO> getAllOrgansSubOrg();

        List<Map<String, String>> getAllReviewerDetails();

        List<EmployeeDetailsDTO> findEmployeesByFilters(String employeeId,
                        String department,
                        String designation,
                        String gender,
                        String reviewerId,
                        String reportingManagerId,
                        String payRollStatus,
                        String band,
                        String status,
                        String organization,
                        String subOrganization,
                        String image);

        ProfileDetailsDTO getEmployeeBasicDetails(String empId);

        String updateProfileOverallSubmit(String profileOverallSubmit);

        List<Map<String, String>> getRoleOfIntake(String organizationCode);

        public List<Map<String, String>> getEmployeesByPrimaryManager(String primaryManagerId);

        byte[] sampleExcelDownload() throws IOException;

        byte[] excelImport(MultipartFile file) throws IOException, InterruptedException, ExecutionException;

        Page<EmployeeDetailsDTO> findEmployeesBypage(
                        String employeeId, String department, String designation, String gender,
                        String reviewerId, String reportingManagerId, String payRollStatus, String band,
                        String status, String organization, String subOrganization, Pageable pageReq, String query);

        List<UserInfo> findByAll(String all);

        GenericResponse<String> updateIdCardByHr(String empId, String empRelation, String nameOfRelation,
                        String contactNo,
                        String status) throws IOException;

        String updatePassword(String empId, PasswordDTO password);

}
