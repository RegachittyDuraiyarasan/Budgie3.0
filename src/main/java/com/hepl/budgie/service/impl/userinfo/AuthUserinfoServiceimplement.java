package com.hepl.budgie.service.impl.userinfo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.organization.OrganizationRef;
import com.hepl.budgie.dto.role.PermissionResponse;
import com.hepl.budgie.dto.role.SubmenuPermissionResponse;
import com.hepl.budgie.dto.userinfo.ForgotPasswordDTO;
import com.hepl.budgie.dto.userlogin.AuthSwitch;
import com.hepl.budgie.dto.userlogin.LoginResponse;
import com.hepl.budgie.dto.userlogin.UserLogin;
import com.hepl.budgie.dto.userlogin.UserResponseDetails;
import com.hepl.budgie.dto.userlogin.UserSwitchAuth;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.menu.Condition;
import com.hepl.budgie.entity.role.Permissions;
import com.hepl.budgie.entity.role.Roles;
import com.hepl.budgie.entity.role.SubmenuPermission;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.UserMapper;
import com.hepl.budgie.repository.master.RolesRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.repository.userinfo.UserOnboardingInfoRepository;
import com.hepl.budgie.service.userinfo.UserAuthService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthUserinfoServiceimplement implements UserAuthService {

        private final UserInfoRepository userInfoRepository;
        private final UserOnboardingInfoRepository onboardingInfoRepository;
        private final BCryptPasswordEncoder bcryptPasswordEncoder;
        private final JWTHelper jwtHelper;
        private final UserMapper userMapper;
        private final MongoTemplate mongoTemplate;
        private final RolesRepository rolesRepository;

        @Override
        public LoginResponse authUser(UserLogin userLogin) {
                log.info("Validating user details");
                UserInfo user = userInfoRepository.findByEmpId(userLogin.getEmpId()).orElseThrow(
                                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                                AppMessages.EMP_ID_INCORRECT));

                if (!bcryptPasswordEncoder.matches(userLogin.getPassword(), user.getPassword())) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                        AppMessages.EMP_PASSWORD_INCORRECT);
                }

                return getLoginResponseDetails(
                                getUserResponseDetails(user, user.getSubOrganization().get(0).getRoleDetails().get(0),
                                                user.getSubOrganization().get(0)),
                                UserRef.builder().empId(user.getEmpId())
                                                .organizationCode(
                                                                user.getSubOrganization().get(0).getOrganizationCode())
                                                .organizationGroupCode(user.getOrganization().getGroupId())
                                                .activeRole(user.getSubOrganization().get(0).getRoleDetails().get(0))
                                                .build(),
                                user.getSubOrganization().get(0).getRoleDetails());

        }

        private List<PermissionResponse> buildPermissionResponse(String groupCode, String roleName,
                        Map<String, Object> userDefaultConditionValue) {
                Roles role = rolesRepository.findByNameAndGrp(roleName, groupCode, mongoTemplate)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                AppMessages.ACCESS_DENIED));

                List<PermissionResponse> permissionResponses = new ArrayList<>();
                for (Permissions permissions : role.getPermissions()) {
                        if (checkMenuOrSubmenuCondt(permissions.getMenu().getConditionList(), userDefaultConditionValue,
                                        permissions.getMenu().getCondition(), permissions.getMenu().getStatus(),
                                        Optional.ofNullable(permissions.getMenu().getPermissions())
                                                        .orElse(List.of()))) {
                                List<SubmenuPermissionResponse> submenuPermissions = new ArrayList<>();
                                for (SubmenuPermission submenuPermission : permissions.getSubMenuPermissions()) {
                                        if (checkMenuOrSubmenuCondt(submenuPermission.getSubmenu().getConditionList(),
                                                        userDefaultConditionValue,
                                                        submenuPermission.getSubmenu().getCondition(),
                                                        submenuPermission.getSubmenu().getStatus(),
                                                        submenuPermission.getPermissions())) {
                                                submenuPermissions.add(SubmenuPermissionResponse.builder()
                                                                .icon(submenuPermission.getSubmenu().getIcon())
                                                                .path(submenuPermission.getSubmenu().getPath())
                                                                .text(submenuPermission.getSubmenu().getName())
                                                                .permission(submenuPermission.getPermissions())
                                                                .build());
                                        }

                                }
                                permissionResponses
                                                .add(PermissionResponse.builder().text(permissions.getMenu().getName())
                                                                .path(submenuPermissions.isEmpty()
                                                                                ? permissions.getMenu().getPath()
                                                                                : submenuPermissions.get(0).getPath())
                                                                .icon(permissions.getMenu().getIcon())
                                                                .submenu(submenuPermissions)
                                                                .build());
                        }

                }

                return permissionResponses;
        }

        private boolean checkMenuOrSubmenuCondt(List<Condition> conditions,
                        Map<String, Object> userDefaultConditionValue, String condition, String status,
                        List<String> permissions) {
                if (Status.ACTIVE.label.equals(status) || permissions.contains("View")) {
                        if (condition.equals(YesOrNoEnum.NO.label)) {
                                return true;
                        } else {
                                return conditions.stream()
                                                .anyMatch(condt -> userDefaultConditionValue
                                                                .getOrDefault(condt.getKey(), "")
                                                                .toString()
                                                                .equals(condt.getValue().toString()))
                                                && condition.equals(YesOrNoEnum.YES.label);
                        }
                }
                return false;
        }

        private LoginResponse getLoginResponseDetails(UserResponseDetails userResponseDetails, UserRef userRef,
                        List<String> roleDetails) {
                return LoginResponse.builder().userDetails(userResponseDetails)
                                .token(jwtHelper.createJwtForClaims("budgie",
                                                getClaims(userRef), roleDetails))
                                .build();
        }

        @Override
        public LoginResponse switchUser(UserSwitchAuth switchAuth) {
                log.info("Switch login");
                UserRef userRef = jwtHelper.getUserRefDetail();
                UserInfo user = userInfoRepository.getUsersByEmpIdBySwitchAuth(switchAuth, userRef.getEmpId(),
                                mongoTemplate, userRef.getOrganizationCode()).orElseThrow(
                                                () -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                                AppMessages.ACCESS_DENIED));
                if (switchAuth.getType().equals(AuthSwitch.ORGANIZATION.label)) {
                        Optional<OrganizationRef> organizationRef = getOrganizationByRef(user.getSubOrganization(),
                                        switchAuth.getValue());

                        return getLoginResponseDetails(
                                        getUserResponseDetails(user, organizationRef.get().getRoleDetails().get(0),
                                                        organizationRef.get()),
                                        UserRef.builder().empId(user.getEmpId())
                                                        .organizationCode(switchAuth.getValue())
                                                        .organizationGroupCode(user.getOrganization().getGroupId())
                                                        .activeRole(organizationRef.get().getRoleDetails().get(0))
                                                        .build(),
                                        organizationRef.get().getRoleDetails());
                } else {
                        Optional<OrganizationRef> organizationRef = getOrganizationByRef(user.getSubOrganization(),
                                        userRef.getOrganizationCode());

                        return getLoginResponseDetails(
                                        getUserResponseDetails(user, switchAuth.getValue(), organizationRef.get()),
                                        UserRef.builder().empId(user.getEmpId())
                                                        .organizationCode(userRef.getOrganizationCode())
                                                        .organizationGroupCode(user.getOrganization().getGroupId())
                                                        .activeRole(switchAuth.getValue()).build(),
                                        organizationRef.get().getRoleDetails());
                }
        }

        private Optional<OrganizationRef> getOrganizationByRef(List<OrganizationRef> organizationRefs, String orgCode) {
                return organizationRefs.stream()
                                .filter(org -> org.getOrganizationCode().equals(orgCode))
                                .findFirst();
        }

        private UserResponseDetails getUserResponseDetails(UserInfo user, String role,
                        OrganizationRef activeOrganization) {
                UserResponseDetails responseDetails = userMapper.toResponseDetails(user,
                                role, activeOrganization.getRoleDetails());
                responseDetails.setActiveOrganization(activeOrganization);

                responseDetails.setPermission(buildPermissionResponse(user.getOrganization().getGroupId(),
                                role,
                                Map.of("onboardingStatus",
                                                onboardingInfoRepository.getOnboardingStatus(user.getEmpId(),
                                                                mongoTemplate),
                                                "reviewer",
                                                userInfoRepository.getReviewerStatus(user.getEmpId(), mongoTemplate),
                                                "reportingManager",
                                                userInfoRepository.getReportingManagerStatus(user.getEmpId(),
                                                                mongoTemplate),
                                                "spocStatus",
                                                false, "spocReportingManagerStatus",
                                                false)));

                return responseDetails;
        }

        private Map<String, Object> getClaims(UserRef userRef) {

                return Map.of("empId", userRef.getEmpId(),
                                "organizationCode", userRef.getOrganizationCode(),
                                "groupId", userRef.getOrganizationGroupCode(), "activeRole", userRef.getActiveRole());
        }

       
        @Override
        public ForgotPasswordDTO getUserEmailByEmpId(String empId) {
            UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
        
            Sections sections = userInfo.getSections();
        
            if (sections == null || sections.getContact() == null) {
                throw new ResponseStatusException(HttpStatus.ACCEPTED, "Contact details not available");
            }
        
            String contactDetails = sections.getContact().getPersonalEmailId();
        
            if (contactDetails == null || contactDetails.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.ACCEPTED, "Email ID not available");
            }
             // Set the email in ForgotPasswordDTO
        ForgotPasswordDTO forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setEmailId(contactDetails);

        return forgotPasswordDTO;

        } 
}