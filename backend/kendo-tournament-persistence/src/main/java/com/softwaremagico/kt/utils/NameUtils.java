package com.softwaremagico.kt.utils;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

public final class NameUtils {

    private static final String DEFAULT_NAME = " --- --- ";

    private static final int MAX_NAME_LENGTH = 11;
    private static final int MAX_SHORT_NAME_LENGTH = 8;
    private static final int NAME_PREFIX = 3;
    private static final int MAX_ALLOWED_NAME_LENGTH = 20;

    private static final String COPY_SUFFIX = " - Copy";

    private NameUtils() {

    }

    public static String getLastnameName(IParticipantName participant) {
        if (participant == null) {
            return DEFAULT_NAME;
        }
        return getLastnameName(participant.getLastname(), participant.getName());
    }

    public static String getLastnameName(String lastname, String name) {
        if (!lastname.isEmpty() || !name.isEmpty()) {
            return lastname + ", " + name;
        } else {
            return DEFAULT_NAME;
        }
    }

    public static String getLastnameNameIni(IParticipantName participant) {
        if (participant == null) {
            return DEFAULT_NAME;
        }
        return getLastnameNameIni(participant.getLastname(), participant.getName());
    }

    /**
     * Get an automatic abbreviation of the lastname with the initial letter of
     * the name.
     *
     * @return
     */
    public static String getLastnameNameIni(String lastname, String name) {
        return getLastnameNameIni(lastname, name, MAX_NAME_LENGTH);
    }

    public static String getLastnameNameIni(IParticipantName participant, int maxLength) {
        if (participant == null) {
            return DEFAULT_NAME;
        }
        return getLastnameNameIni(participant.getLastname(), participant.getName(), maxLength);
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
        if (!lastname.isEmpty() || !name.isEmpty()) {
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
                return lastnameShort.trim() + ", " + name.charAt(0) + ".";
            }
        } else {
            return DEFAULT_NAME;
        }
    }

    public static String getShortLastname(IParticipantName participant, int length) {
        if (participant == null) {
            return DEFAULT_NAME;
        }
        return getShortLastname(participant.getLastname(), length);
    }

    /**
     * Get an automatic abbreviation of the lastname of the person.
     *
     * @param length
     * @return
     */
    public static String getShortLastname(String lastname, int length) {
        final String[] shortLastname = lastname.split(" ");
        if (shortLastname[0].length() > NAME_PREFIX) {
            return shortLastname[0].substring(0, Math.min(length, shortLastname[0].length()));
        } else {
            return lastname.substring(0, Math.min(length - 1, lastname.length()));
        }
    }

    public static String getShortLastname(IParticipantName participant) {
        if (participant == null) {
            return DEFAULT_NAME;
        }
        return getShortLastname(participant.getLastname());
    }

    /**
     * Get an automatic abbreviation of the lastname of the person.
     *
     * @return
     */
    public static String getShortLastname(String lastname) {
        return getShortLastname(lastname, MAX_SHORT_NAME_LENGTH);
    }

    public static String getShortLastnameName(IParticipantName participant, int maxLength) {
        if (participant == null) {
            return DEFAULT_NAME;
        }
        return getShortLastnameName(participant.getLastname(), participant.getName(), maxLength);
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

        final float rateLastname = (name.length() + getShortLastname(lastname, MAX_ALLOWED_NAME_LENGTH).length())
                / ((float) lastname.length() > 0 ? (float) lastname.length() : 1);
        final float rateName = (name.length() + getShortName(name, MAX_ALLOWED_NAME_LENGTH).length()) / ((float) name.length() > 0 ? (float) name.length() : 1);
        final String ret = getShortLastname(lastname, (int) (maxLength / rateLastname)).trim() + ", " + getShortName(name, (int) (maxLength / rateName));
        return ret.trim();
    }

    public static String getShortName(IParticipantName participant, int length) {
        if (participant == null) {
            return " --- ";
        }
        return getShortName(participant.getName(), length);
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

    public static String getShortName(IParticipantName participant) {
        if (participant == null) {
            return " --- ";
        }
        return getShortName(participant.getName());
    }

    /**
     * Get an automatic abbreviation of the name of the person.
     *
     * @return
     */
    public static String getShortName(String name) {
        return getShortName(name, MAX_SHORT_NAME_LENGTH);
    }

    public static String getAcronym(IParticipantName participant) {
        if (participant == null) {
            return "";
        }
        return getAcronym(participant.getLastname(), participant.getName());
    }


    public static String getAcronym(String lastname, String name) {
        String acronym = "";
        acronym += name.trim().substring(0, 1).toUpperCase();
        final String[] shortLastname = lastname.trim().split(" ");
        if (shortLastname[0].length() <= NAME_PREFIX && shortLastname.length > 1) {
            acronym += shortLastname[0].substring(0, 1).toLowerCase();
            acronym += shortLastname[1].substring(0, 1).toUpperCase();
        } else {
            acronym += shortLastname[0].substring(0, 1).toUpperCase();
        }
        return acronym;
    }

    public static String getShortName(IName nameItem) {
        return getShortName(nameItem, MAX_ALLOWED_NAME_LENGTH);
    }

    public static String getShortName(IName nameItem, int length) {
        if (nameItem.getName().length() <= length) {
            return nameItem.getName();
        } else {
            return nameItem.getName().substring(0, length - NAME_PREFIX).trim() + ". "
                    + nameItem.getName().substring(nameItem.getName().length() - 2).trim();
        }
    }

    public static String getNameCopy(IName nameItem) {
        if (nameItem.getName() != null) {
            if (nameItem.getName().contains(COPY_SUFFIX)) {
                final String index = nameItem.getName().substring(nameItem.getName().indexOf(COPY_SUFFIX + " #") + (COPY_SUFFIX + " #").length()).trim();
                try {
                    return nameItem.getName().substring(0, nameItem.getName().indexOf(COPY_SUFFIX)).trim() + COPY_SUFFIX + " #" + (Integer.parseInt(index) + 1);
                } catch (Exception e) {
                    return nameItem.getName() + " #2";
                }
            } else {
                return nameItem.getName() + COPY_SUFFIX;
            }
        }
        return nameItem.getName();
    }

}
