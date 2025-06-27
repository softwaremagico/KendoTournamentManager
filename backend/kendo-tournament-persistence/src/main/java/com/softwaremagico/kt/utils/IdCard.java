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

public final class IdCard {
    private static final String NIF_STRING_ASSOCIATION = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final int NIF_CRC = 23;

    private IdCard() {

    }

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
        return String.valueOf(dni) + NIF_STRING_ASSOCIATION.charAt(dni % NIF_CRC);
    }

}
