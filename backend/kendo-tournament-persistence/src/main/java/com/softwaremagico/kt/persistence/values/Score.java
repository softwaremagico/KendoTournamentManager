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

import java.util.ArrayList;
import java.util.Objects;

public enum Score {

    MEN("Men", 'M', 'M'),

    KOTE("Kote", 'K', 'K'),

    DO("Do", 'D', 'D'),

    TSUKI("Tsuki", 'T', 'T'),

    IPPON("Ippon", 'I', 'I'),

    HANSOKU("Hansoku", 'H', 'H'),

    EMPTY("ClearMenuItem", ' ', ' '),

    FAULT("FaultMenuItem", '^', '\u25B2'),

    DRAW("Draw", 'X', 'X');

    private final char abbreviation;
    private final char enhancedAbbreviation;
    private final String name;

    Score(String name, char abbreviation, char enhancedAbbreviation) {
        this.abbreviation = abbreviation;
        this.name = name;
        this.enhancedAbbreviation = enhancedAbbreviation;
    }

    public static Score getScore(char abbreviation) {
        for (final Score score : Score.values()) {
            if (score.abbreviation == abbreviation) {
                return score;
            }
        }
        return EMPTY;
    }

    public static Score getScore(String name) {
        for (final Score score : Score.values()) {
            if (Objects.equals(score.name, name)) {
                return score;
            }
        }
        return EMPTY;
    }

    public static boolean isValidPoint(Score sc) {
        return getValidPoints().contains(sc);
    }

    public static ArrayList<Score> getValidPoints() {
        final ArrayList<Score> points = new ArrayList<>();
        points.add(MEN);
        points.add(KOTE);
        points.add(DO);
        points.add(TSUKI);
        points.add(IPPON);
        points.add(HANSOKU);
        return points;
    }

    /**
     * Abbreviation for simple fonts.
     *
     * @return
     */
    public char getAbbreviation() {
        return abbreviation;
    }

    /**
     * Abbreviation for fonts with complex symbols.
     *
     * @return
     */
    public char getEnhancedAbbreviation() {
        return enhancedAbbreviation;
    }

    public String getName() {
        return name;
    }
}
