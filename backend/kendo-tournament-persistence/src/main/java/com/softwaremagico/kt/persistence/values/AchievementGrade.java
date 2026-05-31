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

import java.util.ArrayList;
import java.util.List;

public enum AchievementGrade {

    NORMAL(0),
    BRONZE(5),
    SILVER(10),
    GOLD(20);

    private final int grade;

    AchievementGrade(int grade) {
        this.grade = grade;
    }


    public static AchievementGrade getType(String name) {
        for (final AchievementGrade achievementGrade : AchievementGrade.values()) {
            if (achievementGrade.name().equalsIgnoreCase(name)) {
                return achievementGrade;
            }
        }
        return null;
    }

    public List<AchievementGrade> getLessThan() {
        final List<AchievementGrade> achievementGrades = new ArrayList<>();
        for (final AchievementGrade achievementGrade : AchievementGrade.values()) {
            if (achievementGrade.grade < this.grade) {
                achievementGrades.add(achievementGrade);
            }
        }
        return achievementGrades;
    }

    public List<AchievementGrade> getGreaterThan() {
        final List<AchievementGrade> achievementGrades = new ArrayList<>();
        for (final AchievementGrade achievementGrade : AchievementGrade.values()) {
            if (achievementGrade.grade > this.grade) {
                achievementGrades.add(achievementGrade);
            }
        }
        return achievementGrades;
    }

    public List<AchievementGrade> getGreaterEqualsThan() {
        final List<AchievementGrade> achievementGrades = getGreaterThan();
        achievementGrades.add(0, this);
        return achievementGrades;
    }
}
