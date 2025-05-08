package com.hepl.budgie.entity.master;

public enum DocumentTypeEnum {

    NORMALUPLOAD("NormalUpload"),
    BULKUPLOAD("BulkUpload");

    public final String label;

    private DocumentTypeEnum(String label) {
        this.label = label;
    }

}
