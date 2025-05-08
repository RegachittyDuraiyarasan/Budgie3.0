package com.hepl.budgie.config.exceptions;

public enum ErrorTypes {

    OPERATIONAL("OPERATIONAL"),
    FATAL("FATAL"),
    FIELD("FIELD");

    public final String label;

    private ErrorTypes(String label) {
        this.label = label;
    }

}
