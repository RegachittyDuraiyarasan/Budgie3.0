package com.hepl.budgie.entity.iiy;

import com.hepl.budgie.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import com.hepl.budgie.config.auditing.AuditInfo;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "idea_details")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdeaDetails extends AuditInfo {
    @Id
    private String id;
    private String ideaId;
    private String empId;
    private String financialYear;
    private Date ideaDate;
    private String idea;
    private String course;
    private String category;
    private String weightage;
    private String description;
    @Builder.Default
    private String rmStatus = Status.PENDING.label;
    @Builder.Default
    private String rmWeightage = "";
    @Builder.Default
    private String rmRemarks = "";
}
