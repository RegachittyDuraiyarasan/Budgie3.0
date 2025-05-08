package com.hepl.budgie.entity.master;

public enum AcknowledgementTypeEnum {
    DEFAULT("Default"),
    CUSTOM("Custom");

    public final String label;

    private AcknowledgementTypeEnum(String label) {
        this.label = label;
    }

}
