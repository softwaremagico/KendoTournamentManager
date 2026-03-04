package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.statistics.TournamentStatistics;
import com.softwaremagico.kt.core.statistics.TournamentStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.stereotype.Service;

@Service
public class TournamentStatisticsProvider extends CrudProvider<TournamentStatistics, Integer, TournamentStatisticsRepository> {

    private final TournamentFightStatisticsProvider fightStatisticsProvider;

    private final TeamProvider teamProvider;

    private final RoleProvider roleProvider;

    protected TournamentStatisticsProvider(TournamentStatisticsRepository repository, TournamentFightStatisticsProvider fightStatisticsProvider,
                                           TeamProvider teamProvider, RoleProvider roleProvider) {
        super(repository);
        this.fightStatisticsProvider = fightStatisticsProvider;
        this.teamProvider = teamProvider;
        this.roleProvider = roleProvider;
    }


    public TournamentStatistics get(Tournament tournament) {
        final TournamentStatistics tournamentStatistics = new TournamentStatistics();
        tournamentStatistics.setFightStatistics(fightStatisticsProvider.get(tournament));
        tournamentStatistics.setTournamentId(tournament.getId());
        tournamentStatistics.setTournamentName(tournament.getName());
        tournamentStatistics.setNumberOfTeams(teamProvider.count(tournament));
        tournamentStatistics.setTournamentCreatedAt(tournament.getCreatedAt());
        tournamentStatistics.setTournamentLockedAt(tournament.getLockedAt());
        tournamentStatistics.setTournamentFinishedAt(tournament.getFinishedAt());
        tournamentStatistics.setTeamSize(tournament.getTeamSize());
        tournamentStatistics.setFightSize(tournament.getFightSize());
        for (final RoleType roleType : RoleType.values()) {
            tournamentStatistics.addNumberOfParticipants(roleType, roleProvider.count(tournament, roleType));
        }
        return tournamentStatistics;
    }
}
