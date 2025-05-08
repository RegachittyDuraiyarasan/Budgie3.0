package com.hepl.budgie.entity.iiy;

import com.hepl.budgie.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import com.hepl.budgie.config.auditing.AuditInfo;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

@Data
@Document(collection = "iiy_details")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IIYDetails extends AuditInfo {
    @Id
    private String id;
    private String activityId;
    private String empId;
    private String financialYear;
    private ZonedDateTime iiyDate;
    private String courseCategory;
    private String course;
    private String duration;
    private String remarks;
    private String description;
    // private String status;
    @Builder.Default
    private String rmStatus = Status.PENDING.label;
    @Builder.Default
    private String rmRemarks = "";
    private String certification;
    private String fileName;

}
