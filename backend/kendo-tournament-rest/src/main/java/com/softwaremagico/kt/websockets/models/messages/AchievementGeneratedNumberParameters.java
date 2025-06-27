package com.softwaremagico.kt.websockets.models.messages;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

public class AchievementGeneratedNumberParameters {
    private String tournamentName;
    private long achievementsNumber;

    public AchievementGeneratedNumberParameters(String tournamentName, long achievementsNumber) {
        this.tournamentName = tournamentName;
        this.achievementsNumber = achievementsNumber;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public long getAchievementsNumber() {
        return achievementsNumber;
    }

    public void setAchievementsNumber(long achievementsNumber) {
        this.achievementsNumber = achievementsNumber;
    }
}
