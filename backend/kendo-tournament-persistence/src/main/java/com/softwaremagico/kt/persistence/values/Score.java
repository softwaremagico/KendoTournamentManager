package com.softwaremagico.kt.persistence.values;

/*
 * #%L
 * KendoTournamentGenerator
 * %%
 * Copyright (C) 2008 - 2012 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero.
 * Jorge Hortelano Otero <softwaremagico@gmail.com>
 * C/Quart 89, 3. Valencia CP:46008 (Spain).
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static final HashMap<String, Image> existingScore = new HashMap<>();

    Score(String name, char abbreviation, char enhancedAbbreviation) {
        this.abbreviation = abbreviation;
        this.name = name;
        this.enhancedAbbreviation = enhancedAbbreviation;
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

    public static Score getScore(char abbreviation) {
        for (Score s : Score.values()) {
            if (s.abbreviation == abbreviation) {
                return s;
            }
        }
        return EMPTY;
    }

    public static Score getScore(String name) {
        for (Score s : Score.values()) {
            if (Objects.equals(s.name, name)) {
                return s;
            }
        }
        return EMPTY;
    }

    public static boolean isValidPoint(Score sc) {
        return getValidPoints().contains(sc);
    }

    public static ArrayList<Score> getValidPoints() {
        ArrayList<Score> points = new ArrayList<>();
        points.add(MEN);
        points.add(KOTE);
        points.add(DO);
        points.add(TSUKI);
        points.add(IPPON);
        points.add(HANSOKU);
        return points;
    }
}
