package com.hepl.budgie.entity.leavemanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;
import com.hepl.budgie.entity.Status;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "leave_scheme")
public class LeaveScheme extends AuditInfo{
	
    @Id
    private String id;
    @Indexed(unique = true)
    private String schemeName;
    private String applicableTo;
    private String probationType;
    private String periodicity;
    private String status = Status.ACTIVE.label;
    
}
