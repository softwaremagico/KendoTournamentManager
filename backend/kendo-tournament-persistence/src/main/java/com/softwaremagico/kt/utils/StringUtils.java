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

import java.security.SecureRandom;

public final class StringUtils {

    private static final int RANDOM_MINIMUM_ASCII_CODE = 33;
    private static final int RANDOM_MAXIMUM_ASCII_CODE = 90;

    private static final SecureRandom RANDOM = new SecureRandom();

    private StringUtils() {

    }

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

    public static String generateRandomToken(int targetStringLength) {

        return RANDOM.ints(RANDOM_MINIMUM_ASCII_CODE, RANDOM_MAXIMUM_ASCII_CODE + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
