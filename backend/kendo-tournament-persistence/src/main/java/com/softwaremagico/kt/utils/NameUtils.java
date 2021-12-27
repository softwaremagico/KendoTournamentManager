package com.softwaremagico.kt.utils;

public class NameUtils {
    private static final int MAX_NAME_LENGTH = 11;
    private static final int MAX_SHORT_NAME_LENGTH = 8;

    public static String getLastnameName(String lastname, String name) {
        if (lastname.length() > 0 || name.length() > 0) {
            return lastname + ", " + name;
        } else {
            return " --- --- ";

        }
    }

    /**
     * Get an automatic abbreviation of the lastname with the initial letter of
     * the name.
     *
     * @return
     */
    public String getLastnameNameIni(String lastname, String name) {
        return getLastnameNameIni(lastname, name, MAX_NAME_LENGTH);
    }


    /**
     * Get an automatic abbreviation of the lastname with the initial letter or
     * complete name.
     *
     * @param maxLength
     * @param lastname
     * @param name
     * @return
     */
    public static String getLastnameNameIni(String lastname, String name, int maxLength) {
        if (lastname.length() > 0 || name.length() > 0) {
            // Short lastname.
            String lastnameShort = lastname.substring(0, Math.min(maxLength, lastname.length())).toUpperCase();
            if (lastname.length() > maxLength) {
                lastnameShort = lastnameShort.trim() + ".";
            }
            // Short lastname
            if (lastnameShort.length() < maxLength) {
                final String nameShort = name.substring(0, Math.min(maxLength - lastnameShort.length() + 1, name.length()));
                // Name cut.
                if (nameShort.length() < name.length()) {
                    return lastnameShort.trim() + ", " + nameShort.trim() + ".";
                } else {
                    // Full name.
                    return lastnameShort.trim() + ", " + nameShort.trim();
                }
            } else {
                return lastnameShort.trim() + ", " + name.substring(0, 1) + ".";
            }
        } else {
            return " --- --- ";
        }
    }

    /**
     * Get an automatic abbreviation of the lastname of the person.
     *
     * @param length
     * @return
     */
    public static String getShortLastname(String lastname, int length) {
        final String[] shortLastname = lastname.split(" ");
        if (shortLastname[0].length() > 3) {
            return shortLastname[0].substring(0, Math.min(length, shortLastname[0].length()));
        } else {
            return lastname.substring(0, Math.min(length - 1, lastname.length()));
        }
    }

    /**
     * Get an automatic abbreviation of the lastname of the person.
     *
     * @return
     */
    public static String getShortLastname(String lastname) {
        return getShortLastname(lastname, MAX_SHORT_NAME_LENGTH);
    }

    /**
     * Get an automatic abbreviation of the name with the lastname of the person.
     *
     * @return
     */
    public static String getShortLastnameName(String lastname, String name, int maxLength) {
        if (name.length() + lastname.length() == 0) {
            return "";
        }

        final float rateLastname = (name.length() + getShortLastname(lastname, 20).length()) / (float) lastname.length();
        final float rateName = (name.length() + getShortName(name, 20).length()) / (float) name.length();
        final String ret = getShortLastname(lastname, (int) (maxLength / rateLastname)).trim() + ", " + getShortName(name, (int) (maxLength / rateName));
        return ret.trim();
    }


    /**
     * Get an automatic abbreviation of the name of the person.
     *
     * @param length
     * @return
     */
    public static String getShortName(String name, int length) {
        return name.substring(0, Math.min(length, name.length()));
    }

    /**
     * Get an automatic abbreviation of the name of the person.
     *
     * @return
     */
    public static String getShortName(String name) {
        return getShortName(name, MAX_SHORT_NAME_LENGTH);
    }


    public String getAcronym(String lastname, String name) {
        String acronym = "";
        acronym += name.trim().substring(0, 1).toUpperCase();
        final String[] shortLastname = lastname.trim().split(" ");
        if (shortLastname[0].length() < 4 && shortLastname.length > 1) {
            acronym += shortLastname[0].substring(0, 1).toLowerCase();
            acronym += shortLastname[1].substring(0, 1).toUpperCase();
        } else {
            acronym += shortLastname[0].substring(0, 1).toUpperCase();
        }
        return acronym;
    }

}
