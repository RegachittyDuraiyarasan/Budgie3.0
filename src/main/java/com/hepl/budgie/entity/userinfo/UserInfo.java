package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.hepl.budgie.dto.organization.OrganizationRef;
import com.hepl.budgie.entity.menu.Condition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Optional;

@Data
@Document(collection = "userinfo")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {
    @Id
    private String id;
    private String tempId;
    private String empId;
    private List<String> roleDetails;
    private String password;
    private boolean empIdGenerateStatus;
    private OrganizationRef organization;
    private List<OrganizationRef> subOrganization;
    private String status;
    private Sections sections;
    private IdCardDetails idCardDetails;
    private PayrollDetails payrollDetails;
    private List<String> starredEmpDetails;
    private List<ProbationDetails> probationDetails;
    private List<Condition> conditions;

    private List<UserInfo> children;
    @Transient
    private int direct;
    @Transient
    private int subsidiaries;

    public int getSubsidiariesCount() {
        return Optional.ofNullable(children)
                .orElse(List.of())
                .stream()
                .mapToInt(child -> 1 + child.getSubsidiariesCount())
                .sum();
    }

    public int getDirectCount() {
        return Optional.ofNullable(children).orElse(List.of()).size();
    }

}
