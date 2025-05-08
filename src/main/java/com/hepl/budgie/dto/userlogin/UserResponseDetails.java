package com.hepl.budgie.dto.userlogin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hepl.budgie.dto.organization.OrganizationRef;
import com.hepl.budgie.dto.role.PermissionResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDetails {

    private String firstName;
    private String lastName;
    private String officialEmail;
    private String designation;
    private String empId;
    private String idCard;
    private String roleType;
    // private boolean isReviewer;
    // private boolean isReportingManager;
    // private boolean onboradingStatus;
    @JsonIgnore
    private boolean spocStatus;
    @JsonIgnore
    private boolean spocReportingManagerStatus;

    private List<String> roles;
    private OrganizationRef activeOrganization;
    private List<OrganizationRef> organizationAccess;
    private List<PermissionResponse> permission;
    // idCardStatus
    // repManagerStatus
    // SwipeDetails
    private String swipeMethod;

}
