package com.softwaremagico.kt.persistence.values;

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

public enum ScoreType {

    CLASSIC("classic"),

    WIN_OVER_DRAWS("winOverDraws"),

    EUROPEAN("european"),

    CUSTOM("custom"),

    INTERNATIONAL("international");


    public static final ScoreType DEFAULT = ScoreType.INTERNATIONAL;
    private final String tag;

    ScoreType(String tag) {
        this.tag = tag;
    }

    public static ScoreType getScoreType(String tag) {
        for (final ScoreType scoreType : ScoreType.values()) {
            if (scoreType.getTag().equals(tag.toLowerCase())) {
                return scoreType;
            }
        }
        return DEFAULT;
    }

    public String getTag() {
        return tag;
    }
}
