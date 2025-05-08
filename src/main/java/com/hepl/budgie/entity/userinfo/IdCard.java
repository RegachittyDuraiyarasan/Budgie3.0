package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdCard {
    private String emergencyRelationship;
    private String relationshipName;
    private String emergencyContactNo;
    private String fileName;
    private String folderName;
    private String idCardStatusByHr;
    private String createdBy;
    private String submittedOn;
}
