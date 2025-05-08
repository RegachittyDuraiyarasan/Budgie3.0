package com.hepl.budgie.utils;

import java.util.Map;
import java.util.Collections;
import java.util.List;

public class FileExtUtils {

    private FileExtUtils() {
        throw new IllegalStateException("File extension utils");
    }

    private static final Map<String, String> allowedExtensions = Map.of("application/msword", "doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", "image/jpeg", "jpeg",
            "application/vnd.ms-excel", "xls", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "xlsx", "image/png", "png", "application/pdf", "pdf");

    private static final Map<String, String> allowedExtensionsByDotExt = Map.of("doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "jpeg", "image/jpeg",
            "jpg", "image/jpeg", "xls", "application/vnd.ms-excel", "xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "png", "image/png", "pdf",
            "application/pdf");

    public static String getHumanReadableFormats(List<String> extensions) {
        return String.join(", ", extensions.stream().map(allowedExtensions::get).toList());
    }

    public static List<String> getExtensionsByMimeType(List<String> extensions) {
        if (extensions != null) {
            return extensions.stream().map(allowedExtensions::get).toList();
        } else {
            return Collections.emptyList();
        }
    }

    public static List<String> getHumanReadableFormatsByExt(List<String> extensions) {
        return extensions.stream().map(allowedExtensionsByDotExt::get).toList();
    }

}
