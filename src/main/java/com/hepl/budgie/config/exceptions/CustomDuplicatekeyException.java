package com.hepl.budgie.config.exceptions;

import java.util.Map;
import java.util.List;

public class CustomDuplicatekeyException extends RuntimeException {

    private final String message;
    private final Map<String, String> errors;
    private final List<FileInfo> fileInfo;

    public CustomDuplicatekeyException(String message, Map<String, String> errors, List<FileInfo> fileInfo) {
        super(message);
        this.message = message;
        this.errors = errors;
        this.fileInfo = fileInfo;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public Map<String, String> getErrors() {
        return this.errors;
    }

    public List<FileInfo> getFileInfo() {
        return this.fileInfo;
    }

}
