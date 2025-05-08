package com.hepl.budgie.entity.leavemanagement;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "leave_granter")
public class LeaveGranter extends AuditInfo{

	@Id
	private String id;
	private String empId;
	private String processedType;
	private String fromDate;
	private String toDate;
	private String postedOn;
	private List<Details> details;
}
