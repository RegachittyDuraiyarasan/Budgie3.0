package com.hepl.budgie.service.impl.excel;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.hepl.budgie.repository.master.MasterSettingsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.excel.ExcelBuilder;
import com.hepl.budgie.service.excel.ExcelExport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Component("AddEmployeeSample")
public class AddEmployeeSampleExcelExport implements ExcelExport {
    private final UserInfoRepository userInfoRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private final MasterSettingsRepository masterSettingsRepository;

    @Override
    public List<HeaderList> prepareHeaders() {
        String login = jwtHelper.getUserRefDetail().getActiveRole();
        boolean hrOps = login.equalsIgnoreCase("HR Ops") || login.equalsIgnoreCase("HR Ops Admin");
        boolean payrollAdmin = login.equalsIgnoreCase("Payroll Admin") || login.equalsIgnoreCase("Payroll Ops") ;
        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", payrollAdmin, "String"),
                new HeaderList("First_Name", true, "String"),
                new HeaderList("Middle_Name", false, "String"),
                new HeaderList("Last_Name", true, "String"),
                new HeaderList("DOB", true, "String"),
                new HeaderList("Age", false, "Date"),
                new HeaderList("Gender", false, "String"),
                new HeaderList("Personal_Email", false, "String"),
                new HeaderList("Official_Email", false, "String"),
                new HeaderList("Department", true, "String"),
                new HeaderList("Designation", true, "String"),
                new HeaderList("DOJ", true, "Date"),
                new HeaderList("Grade", true, "String"),
                new HeaderList("Contact_Number", false, "String"),
                new HeaderList("Alternative_Number", false, "String"),
                new HeaderList("Blood_Group", false, "String"),
                new HeaderList("Annual_CTC", true, "int"),
                new HeaderList("Emergency_Contact_Relation", false, "String"),
                new HeaderList("Emergency_Contact_Relation_Name", false, "String"),
                new HeaderList("Emergency_Contact_Number", false, "String"),
                new HeaderList("Aadhaar_Number", false, "int"),
                new HeaderList("PAN_Number", false, "String"),
                new HeaderList("Supervisor_Employee_ID", true, "String"),
                new HeaderList("Supervisor_Name", false, "String"),
                new HeaderList("Reviewer_Employee_ID", true, "String"),
                new HeaderList("Reviewer_Name", false, "String"),
                new HeaderList("HRBP_Employee_ID", false, "String"),
                new HeaderList("HRBP_Name", false, "String"),
                new HeaderList("Recruiter_ID", false, "String"),
                new HeaderList("On_Boarder_Employee_ID", false, "String"),
                new HeaderList("Buddy_Employee_ID", false, "String"),
                new HeaderList("Work_Location", true, "String"),
                new HeaderList("Role_Type", false, "String"),
                new HeaderList("Active", true, "String"),
                new HeaderList("Vertical", false, "String"),
                new HeaderList("Business", false, "String"),
                new HeaderList("PF_Logic", true, "String"),
                new HeaderList("Payroll_State", true, "String"),
                new HeaderList("Account_Holder_Name", false, "String"),
                new HeaderList("Account_Number", hrOps, "String"),
                new HeaderList("Bank_Name", false, "String"),
                new HeaderList("IFSC_Code", hrOps, "String"),
                new HeaderList("Account_Mobile_Number", false, "String"),
                new HeaderList("Branch_Name", false, "String"),
                new HeaderList("UPI_ID", false, "String"),
                new HeaderList("UAN_Number", false, "String"),
                new HeaderList("PF_Number", false, "String"),
                new HeaderList("ESI_Number", false, "String"),
                new HeaderList("Probation_Status", false, "String"),
                new HeaderList("Swipe_Method", false, "String"),
                new HeaderList("Access_Card_ID", false, "String"),
                new HeaderList("Image_Path", false, "String"),
                new HeaderList("Notice_Period", false, "String"),
                new HeaderList("DOL", false, "Date"),
                new HeaderList("Group_DOJ", false, "Date"),
                new HeaderList("Pre_onboarding", false, "String"),
                new HeaderList("Probation_Confirmed_Date", false, "Date"),
                new HeaderList("PMS_Eligible_Status", false, "String"),
                new HeaderList("PMS_Status", false, "String"),
                new HeaderList("Division_Head_ID", true, "String"),
                new HeaderList("Division_Head_Name", false, "String"),
                new HeaderList("Pitl", false, "String"),
                new HeaderList("Manpower_Outsourcing", true, "String"),
                new HeaderList("Experience", false, "String"),
                new HeaderList("Attendance_Format", false, "String"),
                new HeaderList("Week_off", false, "String"),
                new HeaderList("Role_Of_Intake", true, "String"),
                new HeaderList("Mail_ID_Required", false, "String"),
                new HeaderList("Domain_Name", false, "String"),
                new HeaderList("Marital_Status", false, "String"),
                new HeaderList("Shift", false, "String"),
                new HeaderList("Cost_Center", payrollAdmin, "String"),
                new HeaderList("Resource_Type", payrollAdmin, "String"),
                new HeaderList("Billing_Type", payrollAdmin, "String"),
                new HeaderList("Division", true, "String"),
                new HeaderList("Old_Regime_Status", false, "String"),
                new HeaderList("Is_Metro_User", false, "String"),
                new HeaderList("Market_Facing_Title", true, "String"),
                new HeaderList("Training Start Date", false, "Date"),
                new HeaderList("Training End Date", false, "Date")

                ).toList());
        return  headerList;
    }

    @Override
    public List<ExcelBuilder.DropdownConfig> prepareDropdowns(){
        List<String> headers =  prepareHeaders().stream().map(HeaderList::getHeader).toList();
        log.info("Gender Dropdowns -{}", getOptionNames("Gender", org()));
        Map<String, List<String>> dropdownMappings = new HashMap<>();
        dropdownMappings.put("Gender", getOptionNames("Gender", org()));
//        dropdownMappings.put("Attendance_Format", getOptionNames("Attendance Format", org()));
//        dropdownMappings.put("Access_Type", getOptionNames("Attendance Format", org()));
//        dropdownMappings.put("Week_Off", getOptionNames("Week off", org()));
//        dropdownMappings.put("Experience", getOptionNames("Experience", org()));
//        dropdownMappings.put("Marital_Status",  getOptionNames("Marital Status", org()));
//        dropdownMappings.put("Blood_Group", getOptionNames("Blood Group", org()));
//        dropdownMappings.put("PF",  getOptionNames("PF", org()));
//        dropdownMappings.put("Shift",  getOptionNames("Shift", org()));
//        dropdownMappings.put("Division",  getOptionNames("Division", org()));
//        dropdownMappings.put("WorkLocation",  getOptionNames("WorkLocation", org()));
//        dropdownMappings.put("Role_Of_Intake", getOptionNames("Role Of Intake", org()));
//        dropdownMappings.put("Designation",  getOptionNames("Designation", org()));
//        dropdownMappings.put("Department",  getOptionNames("Department", org()));
//        dropdownMappings.put("Business",  getOptionNames("Business", org()));

        List<ExcelBuilder.DropdownConfig> validation = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : dropdownMappings.entrySet()) {
            int columnIndex = headers.indexOf(entry.getKey());
            if (columnIndex != -1) {
                validation.add(new ExcelBuilder.DropdownConfig(columnIndex, entry.getValue()));
            }
        }
        return validation;
    }
    private String org(){
//        return jwtHelper.getOrganizationCode();
        return "ORG00001";
    }
    private List<String> getOptionNames(String referenceName, String org) {
        MasterFormSettings masterForm = masterSettingsRepository.fetchOptions(referenceName, org, mongoTemplate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<String> optionNames = new ArrayList<>();
        if (masterForm.getOptions() != null) {
            for (MasterFormOptions option : masterForm.getOptions()) {
                optionNames.add(option.getName());
            }
        }
        return optionNames;
    }
}
