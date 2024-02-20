package com.softwaremagico.kt.persistence.values;

/*
 * #%L
 * KendoTournamentGenerator
 * %%
 * Copyright (C) 2008 - 2012 Softwaremagico
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

public enum TournamentType {

    CHAMPIONSHIP,

    TREE,

    LEAGUE,

    LOOP,

    CUSTOM_CHAMPIONSHIP,

    KING_OF_THE_MOUNTAIN,

    CUSTOMIZED;

    public static TournamentType getType(String name) {
        for (final TournamentType type : TournamentType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public String getCode() {
        return name().replace("-", ".").toLowerCase();
    }
}
