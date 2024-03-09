package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.statistics.ParticipantFightStatistics;
import com.softwaremagico.kt.core.statistics.ParticipantFightStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ParticipantFightStatisticsProvider extends CrudProvider<ParticipantFightStatistics, Integer, ParticipantFightStatisticsRepository> {

    private final DuelProvider duelProvider;


    public ParticipantFightStatisticsProvider(ParticipantFightStatisticsRepository participantFightStatisticsRepository,
                                              DuelProvider duelProvider) {
        super(participantFightStatisticsRepository);
        this.duelProvider = duelProvider;
    }


    public ParticipantFightStatistics get(Participant participant) {
        final ParticipantFightStatistics participantFightStatistics = new ParticipantFightStatistics();
        final List<Duel> duels = duelProvider.get(participant);
        long totalDuration = 0L;
        long totalDuelsWithDuration = 0L;
        long quickestHit = Integer.MAX_VALUE;
        long quickestReceivedHit = Integer.MAX_VALUE;
        long wonDuels = 0L;
        long lostDuels = 0L;
        long drawDuels = 0L;
        for (final Duel duel : duels) {
            final int winner = duel.getWinner();
            if (Objects.equals(duel.getCompetitor1(), participant)) {
                populateScores(participantFightStatistics, duel.getCompetitor1Score());
                populateReceivedScores(participantFightStatistics, duel.getCompetitor2Score());
                participantFightStatistics.setFaults(participantFightStatistics.getFaults()
                        + (duel.getCompetitor1Fault() != null && duel.getCompetitor1Fault() ? 1 : 0));
                participantFightStatistics.setReceivedFaults(participantFightStatistics.getReceivedFaults()
                        + (duel.getCompetitor2Fault() != null && duel.getCompetitor2Fault() ? 1 : 0));
                for (final Integer scoreTime : duel.getCompetitor1ScoreTime()) {
                    if (scoreTime < quickestHit) {
                        quickestHit = scoreTime;
                    }
                }
                for (final Integer scoreTime : duel.getCompetitor2ScoreTime()) {
                    if (scoreTime < quickestReceivedHit) {
                        quickestReceivedHit = scoreTime;
                    }
                }
                if (winner < 0) {
                    wonDuels++;
                } else if (winner == 0) {
                    drawDuels++;
                } else {
                    lostDuels++;
                }
            } else if (Objects.equals(duel.getCompetitor2(), participant)) {
                populateScores(participantFightStatistics, duel.getCompetitor2Score());
                populateReceivedScores(participantFightStatistics, duel.getCompetitor1Score());
                participantFightStatistics.setFaults(participantFightStatistics.getFaults()
                        + (duel.getCompetitor2Fault() != null && duel.getCompetitor2Fault() ? 1 : 0));
                participantFightStatistics.setReceivedFaults(participantFightStatistics.getReceivedFaults()
                        + (duel.getCompetitor2Fault() != null && duel.getCompetitor1Fault() ? 1 : 0));
                for (final Integer scoreTime : duel.getCompetitor2ScoreTime()) {
                    if (scoreTime < quickestHit) {
                        quickestHit = scoreTime;
                    }
                }
                for (final Integer scoreTime : duel.getCompetitor1ScoreTime()) {
                    if (scoreTime < quickestReceivedHit) {
                        quickestReceivedHit = scoreTime;
                    }
                }
                if (winner > 0) {
                    wonDuels++;
                } else if (winner == 0) {
                    drawDuels++;
                } else {
                    lostDuels++;
                }
            }
            totalDuration += duel.getDuration() != null && duel.getDuration() > Duel.DEFAULT_DURATION ? duel.getDuration() : 0;
            totalDuelsWithDuration += duel.getDuration() != null && duel.getDuration() > Duel.DEFAULT_DURATION ? 1 : 0;
        }
        if (totalDuelsWithDuration > 0) {
            participantFightStatistics.setAverageTime(totalDuration / totalDuelsWithDuration);
        } else {
            participantFightStatistics.setAverageTime(0L);
        }
        if (quickestHit < Integer.MAX_VALUE) {
            participantFightStatistics.setQuickestHit(quickestHit);
        }
        if (quickestReceivedHit < Integer.MAX_VALUE) {
            participantFightStatistics.setQuickestReceivedHit(quickestReceivedHit);
        }
        participantFightStatistics.setTotalDuelsTime(totalDuration);
        participantFightStatistics.setDuelsNumber((long) duels.size());
        participantFightStatistics.setWonDuels(wonDuels);
        participantFightStatistics.setDrawDuels(drawDuels);
        participantFightStatistics.setLostDuels(lostDuels);
        return participantFightStatistics;
    }

    private void populateScores(ParticipantFightStatistics participantFightStatistics, List<Score> scores) {
        //Remove null values
        scores = scores.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
        for (final Score score : scores) {
            switch (score) {
                case MEN -> participantFightStatistics.setMenNumber(participantFightStatistics.getMenNumber() + 1);
                case KOTE -> participantFightStatistics.setKoteNumber(participantFightStatistics.getKoteNumber() + 1);
                case DO -> participantFightStatistics.setDoNumber(participantFightStatistics.getDoNumber() + 1);
                case TSUKI ->
                        participantFightStatistics.setTsukiNumber(participantFightStatistics.getTsukiNumber() + 1);
                case HANSOKU ->
                        participantFightStatistics.setHansokuNumber(participantFightStatistics.getHansokuNumber() + 1);
                case IPPON ->
                        participantFightStatistics.setIpponNumber(participantFightStatistics.getIpponNumber() + 1);
                default -> {
                }
            }
        }
    }

    private void populateReceivedScores(ParticipantFightStatistics participantFightStatistics, List<Score> scores) {
        for (final Score score : scores) {
            switch (score) {
                case MEN ->
                        participantFightStatistics.setReceivedMenNumber(participantFightStatistics.getReceivedMenNumber() + 1);
                case KOTE ->
                        participantFightStatistics.setReceivedKoteNumber(participantFightStatistics.getReceivedKoteNumber() + 1);
                case DO ->
                        participantFightStatistics.setReceivedDoNumber(participantFightStatistics.getReceivedDoNumber() + 1);
                case TSUKI ->
                        participantFightStatistics.setReceivedTsukiNumber(participantFightStatistics.getReceivedTsukiNumber() + 1);
                case HANSOKU ->
                        participantFightStatistics.setReceivedHansokuNumber(participantFightStatistics.getReceivedHansokuNumber() + 1);
                case IPPON ->
                        participantFightStatistics.setReceivedIpponNumber(participantFightStatistics.getReceivedIpponNumber() + 1);
                default -> {
                }
            }
        }
    }
}
