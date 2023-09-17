package com.softwaremagico.kt.utils;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

public final class StringUtils {

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
}
