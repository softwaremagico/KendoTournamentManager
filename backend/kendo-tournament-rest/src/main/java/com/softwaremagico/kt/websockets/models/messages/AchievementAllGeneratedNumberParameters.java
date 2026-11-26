package com.softwaremagico.kt.websockets.models.messages;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

public class AchievementAllGeneratedNumberParameters {
    private int tournamentNumber;
    private long achievementsNumber;

    public AchievementAllGeneratedNumberParameters(int tournamentNumber, long achievementsNumber) {
        this.tournamentNumber = tournamentNumber;
        this.achievementsNumber = achievementsNumber;
    }

    public int getTournamentNumber() {
        return tournamentNumber;
    }

    public void setTournamentNumber(int tournamentNumber) {
        this.tournamentNumber = tournamentNumber;
    }

    public long getAchievementsNumber() {
        return achievementsNumber;
    }

    public void setAchievementsNumber(long achievementsNumber) {
        this.achievementsNumber = achievementsNumber;
    }
}
