package com.softwaremagico.kt.utils;

public class StringUtils {

    public static String setCase(String value) {
        final StringBuilder caseString = new StringBuilder();
        final String[] data = value.split(" ");
        for (final String datum : data) {
            if (datum.length() > 2) {
                caseString.append(datum.substring(0, 1).toUpperCase()).append(datum.substring(1).toLowerCase()).append(" ");
            } else {
                caseString.append(datum).append(" ");
            }
        }
        return caseString.toString().trim().replace(";", ",");
    }
}
