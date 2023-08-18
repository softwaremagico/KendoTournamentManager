package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

public enum DrawResolution {

    OLDEST_ELIMINATED,

    BOTH_ELIMINATED,

    NEWEST_ELIMINATED;

    public static DrawResolution getFromTag(String tag) {
        for (final DrawResolution drawResolution : DrawResolution.values()) {
            if (drawResolution.name().equalsIgnoreCase(tag)) {
                return drawResolution;
            }
        }
        return BOTH_ELIMINATED;
    }
}
