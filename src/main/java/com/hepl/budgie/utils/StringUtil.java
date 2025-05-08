package com.hepl.budgie.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtil {

    /**
     * Abbreviates a string by taking the first letter of each word.
     *
     * @param input the input string
     * @return the abbreviated string
     */
    public static String abbreviate(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return null or empty string as is
        }

        return Arrays.stream(input.split(" "))
                .map(word -> word.isEmpty() ? "" : word.substring(0, 1)) // Take the first character of each word
                .collect(Collectors.joining()); // Join the characters into a single string
    }
}