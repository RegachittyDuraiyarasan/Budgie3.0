package com.hepl.budgie.entity.leavemanagement;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "leave_transaction_type")
public class LeaveTransactionType extends AuditInfo {

	@Id
	private String id;
	private String leaveTransactionType;
	private String allowFirstTime;
	private String balanceImpact;
	private String directTransaction;
	private String status;
}
