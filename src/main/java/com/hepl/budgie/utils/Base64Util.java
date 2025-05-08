package com.hepl.budgie.utils;

import java.util.Base64;

public class Base64Util {
    public static String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static String decode(String base64Input) {
        return new String(Base64.getDecoder().decode(base64Input));
    }
}
