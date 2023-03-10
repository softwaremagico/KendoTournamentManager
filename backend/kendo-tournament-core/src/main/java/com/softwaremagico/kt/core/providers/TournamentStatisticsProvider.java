package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.statistics.TournamentStatistics;
import com.softwaremagico.kt.core.statistics.TournamentStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.stereotype.Service;

@Service
public class TournamentStatisticsProvider extends CrudProvider<TournamentStatistics, Integer, TournamentStatisticsRepository> {

    private final FightStatisticsProvider fightStatisticsProvider;

    private final DuelProvider duelProvider;

    protected TournamentStatisticsProvider(TournamentStatisticsRepository repository, FightStatisticsProvider fightStatisticsProvider,
                                           DuelProvider duelProvider) {
        super(repository);
        this.fightStatisticsProvider = fightStatisticsProvider;
        this.duelProvider = duelProvider;
    }


    public TournamentStatistics get(Tournament tournament) {
        final TournamentStatistics tournamentStatistics = new TournamentStatistics();
        tournamentStatistics.setMenNumber(duelProvider.countScore(tournament, Score.MEN));
        tournamentStatistics.setDoNumber(duelProvider.countScore(tournament, Score.DO));
        tournamentStatistics.setKoteNumber(duelProvider.countScore(tournament, Score.KOTE));
        tournamentStatistics.setHansokuNumber(duelProvider.countScore(tournament, Score.HANSOKU));
        tournamentStatistics.setTsukiNumber(duelProvider.countScore(tournament, Score.TSUKI));
        tournamentStatistics.setIpponNumber(duelProvider.countScore(tournament, Score.IPPON));
        tournamentStatistics.setFightStatistics(fightStatisticsProvider.get(tournament));
        return tournamentStatistics;
    }
}
