package com.hepl.budgie.entity.payroll.payrollEnum;

public enum DataOperations {
    CREATED("Created"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    SAVE("Save"),
    SUBMIT("Submit"),
    PENDING("Pending"),
    PROCESSING("Processing"),
    UPDATE("Update"),
    DRAFT("Draft"),
    DELETE("Delete"),
    CONSIDER("Consider");

    public final String label;
    private DataOperations(String label) {
        this.label = label;
    }
}
