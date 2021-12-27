package com.softwaremagico.kt.utils;

public class IdCard {
    private static final String NIF_STRING_ASOCIATION = "TRWAGMYFPDXBNJZSQVHLCKE";

    /**
     * Adds the letter of a spanish DNI.
     *
     * @param dni
     * @return
     */
    public static String nifFromDni(Integer dni) {
        if (dni == null) {
            return null;
        }
        return String.valueOf(dni) + NIF_STRING_ASOCIATION.charAt(dni % 23);
    }

}
