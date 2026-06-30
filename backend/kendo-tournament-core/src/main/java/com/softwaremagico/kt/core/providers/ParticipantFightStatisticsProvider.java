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


    public ParticipantFightStatisticsProvider(ParticipantFightStatisticsRepository participantFightStatisticsRepository,
                                              DuelProvider duelProvider) {
        super(participantFightStatisticsRepository);
        this.duelProvider = duelProvider;
    }


    public ParticipantFightStatistics get(Participant participant) {
        final ParticipantFightStatistics participantFightStatistics = new ParticipantFightStatistics();
        final List<Duel> duels = duelProvider.get(participant);
        long totalDuelsDuration = 0;
        final long participantDurationAverage = duelProvider.getDurationAverage(participant);
        long totalDuelWonsWithDuration = 0L;
        long totalDuelLostsWithDuration = 0L;
        long quickestHit = Integer.MAX_VALUE;
        long quickestReceivedHit = Integer.MAX_VALUE;
        long wonDuels = 0L;
        long wonDuelsWithDuration = 0L;
        long lostDuels = 0L;
        long lostDuelsWithDuration = 0L;
        long drawDuels = 0L;
        for (final Duel duel : duels) {
            final int winner = duel.getWinner();

            final DuelAccumulator acc;
            if (Objects.equals(duel.getCompetitor1(), participant)) {
                acc = new DuelAccumulator(
                    duel.getCompetitor1Score(), duel.getCompetitor2Score(),
                    duel.getCompetitor1Fault(), duel.getCompetitor2Fault(),
                    duel.getCompetitor1ScoreTime(), duel.getCompetitor2ScoreTime(),
                    winner < 0, winner > 0);
            } else if (Objects.equals(duel.getCompetitor2(), participant)) {
                acc = new DuelAccumulator(
                    duel.getCompetitor2Score(), duel.getCompetitor1Score(),
                    duel.getCompetitor2Fault(), duel.getCompetitor1Fault(),
                    duel.getCompetitor2ScoreTime(), duel.getCompetitor1ScoreTime(),
                    winner > 0, winner < 0);
            } else {
                acc = null;
            }

            if (acc != null) {
                populateScores(participantFightStatistics, acc.myScores());
                populateReceivedScores(participantFightStatistics, acc.opponentScores());
                participantFightStatistics.setFaults(participantFightStatistics.getFaults()
                        + (Boolean.TRUE.equals(acc.myFault()) ? 1 : 0));
                participantFightStatistics.setReceivedFaults(participantFightStatistics.getReceivedFaults()
                        + (Boolean.TRUE.equals(acc.opponentFault()) ? 1 : 0));
                quickestHit = updateQuickest(acc.myScoreTimes(), quickestHit);
                quickestReceivedHit = updateQuickest(acc.opponentScoreTimes(), quickestReceivedHit);
                if (acc.won()) {
                    wonDuels++;
                } else if (winner == 0) {
                    drawDuels++;
                } else {
                    lostDuels++;
                }
                if (duel.getDuration() != null && duel.getDuration() > Duel.DEFAULT_DURATION) {
                    totalDuelsDuration += duel.getDuration();
                }
            }
            if (Objects.equals(duel.getCompetitorWinner(), participant)) {
                if (duel.getDuration() != null && duel.getDuration() > Duel.DEFAULT_DURATION) {
                    totalDuelWonsWithDuration += duel.getDuration();
                    wonDuelsWithDuration++;
                }
            }
            if (duel.getCompetitorWinner() != null && !Objects.equals(duel.getCompetitorWinner(), participant)
                    && duel.getDuration() != null && duel.getDuration() > Duel.DEFAULT_DURATION) {
                totalDuelLostsWithDuration += duel.getDuration();
                lostDuelsWithDuration++;
            }
        }
        if (participantDurationAverage > 0) {
            participantFightStatistics.setAverageTime(participantDurationAverage);
        } else {
            participantFightStatistics.setAverageTime(0L);
        }
        if (totalDuelWonsWithDuration > 0) {
            participantFightStatistics.setAverageWinTime(totalDuelWonsWithDuration / wonDuelsWithDuration);
        } else {
            participantFightStatistics.setAverageWinTime(0L);
        }
        if (totalDuelLostsWithDuration > 0) {
            participantFightStatistics.setAverageLostTime(totalDuelLostsWithDuration / lostDuelsWithDuration);
        } else {
            participantFightStatistics.setAverageLostTime(0L);
        }
        if (quickestHit < Integer.MAX_VALUE) {
            participantFightStatistics.setQuickestHit(quickestHit);
        }
        if (quickestReceivedHit < Integer.MAX_VALUE) {
            participantFightStatistics.setQuickestReceivedHit(quickestReceivedHit);
        }
        participantFightStatistics.setTotalDuelsTime(totalDuelsDuration);
        participantFightStatistics.setDuelsNumber((long) duels.size());
        participantFightStatistics.setWonDuels(wonDuels);
        participantFightStatistics.setDrawDuels(drawDuels);
        participantFightStatistics.setLostDuels(lostDuels);
        return participantFightStatistics;
    }

    private record DuelAccumulator(
        List<Score> myScores,
        List<Score> opponentScores,
        Boolean myFault,
        Boolean opponentFault,
        List<Integer> myScoreTimes,
        List<Integer> opponentScoreTimes,
        boolean won,
        boolean lost) {
    }

    private long updateQuickest(List<Integer> scoreTimes, long current) {
        long best = current;
        for (final Integer scoreTime : scoreTimes) {
            if (scoreTime != null && scoreTime < best) {
                best = scoreTime;
            }
        }
        return best;
    }

    private void populateScores(ParticipantFightStatistics participantFightStatistics, List<Score> scores) {
        //Remove null values
        scores = scores.parallelStream().filter(Objects::nonNull).toList();
        for (final Score score : scores) {
            switch (score) {
                case MEN -> participantFightStatistics.setMenNumber(participantFightStatistics.getMenNumber() + 1);
                case KOTE -> participantFightStatistics.setKoteNumber(participantFightStatistics.getKoteNumber() + 1);
                case DO -> participantFightStatistics.setDoNumber(participantFightStatistics.getDoNumber() + 1);
                case TSUKI -> participantFightStatistics.setTsukiNumber(participantFightStatistics.getTsukiNumber() + 1);
                case HANSOKU -> participantFightStatistics.setHansokuNumber(participantFightStatistics.getHansokuNumber() + 1);
                case IPPON -> participantFightStatistics.setIpponNumber(participantFightStatistics.getIpponNumber() + 1);
                case FUSEN_GACHI -> participantFightStatistics.setFusenGachiNumber(participantFightStatistics.getFusenGachiNumber() + 1);
                default -> {
                    //Do nothing for empty score.
                }
            }
        }
    }

    private void populateReceivedScores(ParticipantFightStatistics participantFightStatistics, List<Score> scores) {
        //Remove null values
        scores = scores.parallelStream().filter(Objects::nonNull).toList();
        for (final Score score : scores) {
            switch (score) {
                case MEN -> participantFightStatistics.setReceivedMenNumber(participantFightStatistics.getReceivedMenNumber() + 1);
                case KOTE -> participantFightStatistics.setReceivedKoteNumber(participantFightStatistics.getReceivedKoteNumber() + 1);
                case DO -> participantFightStatistics.setReceivedDoNumber(participantFightStatistics.getReceivedDoNumber() + 1);
                case TSUKI -> participantFightStatistics.setReceivedTsukiNumber(participantFightStatistics.getReceivedTsukiNumber() + 1);
                case HANSOKU -> participantFightStatistics.setReceivedHansokuNumber(participantFightStatistics.getReceivedHansokuNumber() + 1);
                case IPPON -> participantFightStatistics.setReceivedIpponNumber(participantFightStatistics.getReceivedIpponNumber() + 1);
                case FUSEN_GACHI -> participantFightStatistics.setReceivedFusenGachiNumber(participantFightStatistics.getReceivedFusenGachiNumber() + 1);
                default -> {
                    //Do nothing for empty score.
                }
            }
        }
    }
}
