package com.hepl.budgie.facade;

import java.util.List;
import java.util.Map;

import com.hepl.budgie.service.payroll.CTCBreakupsService;
import org.springframework.stereotype.Service;

import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.master.MasterSettingsService;
import com.hepl.budgie.service.master.ModuleMasterSettingsService;
import com.hepl.budgie.service.role.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataInitFacade {

    private static List<String> masters = List.of("Designation", "Department", "Business", "Attendance Format",
            "Week Off", "Access Type", "Experience", "Grade", "Marital Status", "Blood Group", "PF", "Shift",
            "Swipe Method", "Add venue", "Client", "Band", "Division", "Function", "Work Location", "Role Of Intake",
            "State", "Zone", "PreOnboarding", "Gender","pmsYear","IndustryType","Pms RatingMaster","Eligible Master");
    private static Map<String, List<String>> mastersPreDefined = Map.of("Yes or No", List.of("Yes", "No"));

    private final MasterSettingsService masterSettingsService;
    private final MasterFormService masterFormService;
    private final RoleService roleService;
    private final CTCBreakupsService breakupsService;
    private final ModuleMasterSettingsService moduleMasterSettingsService;

    public void initOrganisationData(String organisation) {
        log.info("Initializing database for org {}", organisation);
        masterSettingsService.initIndexingSettingForOrganisation(organisation);
        // masterSettingsService.initializeMasterSettings(masters, mastersPreDefined, organisation);
        masterFormService.initIndexingFormsForOrganisation(organisation);
        breakupsService.initCTCIndexingForOrg(organisation);
        moduleMasterSettingsService.initIndexingModuleMasterSettings(organisation);

    }

    public void groupOrganizationData(String orgGroupCode) {
        log.info("initializing database for orgMap {}", orgGroupCode);
        roleService.initIndexingRoleForOrganisation(orgGroupCode);
    }

}
