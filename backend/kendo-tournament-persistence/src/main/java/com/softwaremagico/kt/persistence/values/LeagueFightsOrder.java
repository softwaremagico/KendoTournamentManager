package com.softwaremagico.kt.persistence.values;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

public enum LeagueFightsOrder {

    //For 3 teams, the league order will be:

    // 1 - 2
    // 3 - 2
    // 3 - 1
    FIFO,

    // 1 - 2
    // 1 - 3
    // 2 - 3
    LIFO;

    public static LeagueFightsOrder get(String tag) {
        for (LeagueFightsOrder leagueFightsOrder : LeagueFightsOrder.values()) {
            if (leagueFightsOrder.name().equalsIgnoreCase(tag)) {
                return leagueFightsOrder;
            }
        }
        return LeagueFightsOrder.FIFO;
    }
}
