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

import com.softwaremagico.kt.core.statistics.ParticipantFightStatistics;
import com.softwaremagico.kt.core.statistics.ParticipantFightStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ParticipantFightStatisticsProvider extends CrudProvider<ParticipantFightStatistics, Integer, ParticipantFightStatisticsRepository> {

    private final DuelProvider duelProvider;

    private final FightProvider fightProvider;
    private final TeamProvider teamProvider;
    private final RoleProvider roleProvider;

    public ParticipantFightStatisticsProvider(ParticipantFightStatisticsRepository participantFightStatisticsRepository, DuelProvider duelProvider,
                                              FightProvider fightProvider, TeamProvider teamProvider, RoleProvider roleProvider) {
        super(participantFightStatisticsRepository);
        this.duelProvider = duelProvider;
        this.fightProvider = fightProvider;
        this.teamProvider = teamProvider;
        this.roleProvider = roleProvider;
    }


    public ParticipantFightStatistics get(Participant participant) {
        final ParticipantFightStatistics participantFightStatistics = new ParticipantFightStatistics();
        final List<Duel> duels = duelProvider.get(participant);
        long totalDuration = 0L;
        long totalDuelsWithDuration = 0L;
        for (final Duel duel : duels) {
            if (Objects.equals(duel.getCompetitor1(), participant)) {
                populateScores(participantFightStatistics, duel.getCompetitor1Score());
                populateReceivedScores(participantFightStatistics, duel.getCompetitor2Score());
                participantFightStatistics.setFaults(participantFightStatistics.getFaults() + (duel.getCompetitor1Fault() ? 1 : 0));
                participantFightStatistics.setReceivedFaults(participantFightStatistics.getReceivedFaults() + (duel.getCompetitor2Fault() ? 1 : 0));
            } else if (Objects.equals(duel.getCompetitor2(), participant)) {
                populateScores(participantFightStatistics, duel.getCompetitor2Score());
                populateReceivedScores(participantFightStatistics, duel.getCompetitor1Score());
                participantFightStatistics.setFaults(participantFightStatistics.getFaults() + (duel.getCompetitor2Fault() ? 1 : 0));
                participantFightStatistics.setReceivedFaults(participantFightStatistics.getReceivedFaults() + (duel.getCompetitor1Fault() ? 1 : 0));
            }
            totalDuration += duel.getDuration() != null && duel.getDuration() > Duel.DEFAULT_DURATION ? duel.getDuration() : 0;
            totalDuelsWithDuration += duel.getDuration() != null && duel.getDuration() > Duel.DEFAULT_DURATION ? 1 : 0;
        }
        if (totalDuelsWithDuration > 0) {
            participantFightStatistics.setAverageTime(totalDuration / totalDuelsWithDuration);
        } else {
            participantFightStatistics.setAverageTime(0L);
        }
        participantFightStatistics.setTotalDuelsTime(totalDuration);
        participantFightStatistics.setDuelsNumber((long) duels.size());
        return participantFightStatistics;
    }

    private void populateScores(ParticipantFightStatistics participantFightStatistics, List<Score> scores) {
        for (final Score score : scores) {
            switch (score) {
                case MEN:
                    participantFightStatistics.setMenNumber(participantFightStatistics.getMenNumber() + 1);
                    break;
                case KOTE:
                    participantFightStatistics.setKoteNumber(participantFightStatistics.getKoteNumber() + 1);
                    break;
                case DO:
                    participantFightStatistics.setDoNumber(participantFightStatistics.getDoNumber() + 1);
                    break;
                case TSUKI:
                    participantFightStatistics.setTsukiNumber(participantFightStatistics.getTsukiNumber() + 1);
                    break;
                case HANSOKU:
                    participantFightStatistics.setHansokuNumber(participantFightStatistics.getHansokuNumber() + 1);
                    break;
                case IPPON:
                    participantFightStatistics.setIpponNumber(participantFightStatistics.getIpponNumber() + 1);
                    break;
            }
        }
    }

    private void populateReceivedScores(ParticipantFightStatistics participantFightStatistics, List<Score> scores) {
        for (final Score score : scores) {
            switch (score) {
                case MEN:
                    participantFightStatistics.setReceivedMenNumber(participantFightStatistics.getReceivedMenNumber() + 1);
                    break;
                case KOTE:
                    participantFightStatistics.setReceivedKoteNumber(participantFightStatistics.getReceivedKoteNumber() + 1);
                    break;
                case DO:
                    participantFightStatistics.setReceivedDoNumber(participantFightStatistics.getReceivedDoNumber() + 1);
                    break;
                case TSUKI:
                    participantFightStatistics.setReceivedTsukiNumber(participantFightStatistics.getReceivedTsukiNumber() + 1);
                    break;
                case HANSOKU:
                    participantFightStatistics.setReceivedHansokuNumber(participantFightStatistics.getReceivedHansokuNumber() + 1);
                    break;
                case IPPON:
                    participantFightStatistics.setReceivedIpponNumber(participantFightStatistics.getReceivedIpponNumber() + 1);
                    break;
            }
        }
    }
}
