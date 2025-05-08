package com.hepl.budgie.entity;

import lombok.Getter;

@Getter
public enum LeaveCategory {
	LEAVE_APPLY("Leave Apply"),
	LEAVE_CANCEL("Leave Cancel"),
	PRIVILEGE_LEAVE("Privilege Leave"),
	LEAVE_GRANTER("Leave Granter"),
	CASUAL_LEAVE("Casual Leave");

	public final String label;

	private LeaveCategory(String label) {
		this.label = label;
	}
}
