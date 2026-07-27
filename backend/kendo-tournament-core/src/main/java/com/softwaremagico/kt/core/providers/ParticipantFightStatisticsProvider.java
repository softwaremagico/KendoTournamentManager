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
        final long participantDurationAverage = duelProvider.getDurationAverage(participant);
        final DuelStatisticsAccumulator accumulator = new DuelStatisticsAccumulator();

        for (final Duel duel : duels) {
            processDuel(participantFightStatistics, duel, participant, accumulator);
        }

        applyAverageTimes(participantFightStatistics, participantDurationAverage, accumulator);
        applyQuickestHits(participantFightStatistics, accumulator);

        participantFightStatistics.setTotalDuelsTime(accumulator.totalDuelsDuration);
        participantFightStatistics.setDuelsNumber((long) duels.size());
        participantFightStatistics.setWonDuels(accumulator.wonDuels);
        participantFightStatistics.setDrawDuels(accumulator.drawDuels);
        participantFightStatistics.setLostDuels(accumulator.lostDuels);
        return participantFightStatistics;
    }

    private void processDuel(ParticipantFightStatistics participantFightStatistics, Duel duel, Participant participant,
                             DuelStatisticsAccumulator accumulator) {
        if (Objects.equals(duel.getCompetitor1(), participant)) {
            processAsCompetitor(participantFightStatistics, duel, accumulator, new CompetitorDuelData(
                    duel.getCompetitor1Score(), duel.getCompetitor2Score(),
                    duel.getCompetitor1Fault(), duel.getCompetitor2Fault(),
                    duel.getCompetitor1ScoreTime(), duel.getCompetitor2ScoreTime(),
                    duel.getWinner() < 0, duel.getWinner() == 0));
        } else if (Objects.equals(duel.getCompetitor2(), participant)) {
            processAsCompetitor(participantFightStatistics, duel, accumulator, new CompetitorDuelData(
                    duel.getCompetitor2Score(), duel.getCompetitor1Score(),
                    duel.getCompetitor2Fault(), duel.getCompetitor1Fault(),
                    duel.getCompetitor2ScoreTime(), duel.getCompetitor1ScoreTime(),
                    duel.getWinner() > 0, duel.getWinner() == 0));
        }
        updateDurationAgainstWinner(duel, participant, accumulator);
    }

    /**
     * Groups the per-competitor data extracted from a {@link Duel} needed to update the statistics,
     * so that {@link #processAsCompetitor} does not require an excessive number of parameters.
     */
    private record CompetitorDuelData(List<Score> ownScores, List<Score> opponentScores, Boolean ownFault, Boolean opponentFault,
                                      List<Integer> ownScoreTimes, List<Integer> opponentScoreTimes, boolean won, boolean draw) {
    }

    private void processAsCompetitor(ParticipantFightStatistics participantFightStatistics, Duel duel, DuelStatisticsAccumulator accumulator,
                                     CompetitorDuelData competitorDuelData) {
        populateScores(participantFightStatistics, competitorDuelData.ownScores());
        populateReceivedScores(participantFightStatistics, competitorDuelData.opponentScores());
        participantFightStatistics.setFaults(participantFightStatistics.getFaults()
                + (competitorDuelData.ownFault() != null && competitorDuelData.ownFault() ? 1 : 0));
        participantFightStatistics.setReceivedFaults(participantFightStatistics.getReceivedFaults()
                + (competitorDuelData.opponentFault() != null && competitorDuelData.opponentFault() ? 1 : 0));
        accumulator.quickestHit = Math.min(accumulator.quickestHit, quickestScoreTime(competitorDuelData.ownScoreTimes()));
        accumulator.quickestReceivedHit = Math.min(accumulator.quickestReceivedHit, quickestScoreTime(competitorDuelData.opponentScoreTimes()));
        if (competitorDuelData.won()) {
            accumulator.wonDuels++;
        } else if (competitorDuelData.draw()) {
            accumulator.drawDuels++;
        } else {
            accumulator.lostDuels++;
        }
        if (duel.getDuration() != null && duel.getDuration() > Duel.DEFAULT_DURATION) {
            accumulator.totalDuelsDuration += duel.getDuration();
        }
    }

    private long quickestScoreTime(List<Integer> scoreTimes) {
        long quickest = Integer.MAX_VALUE;
        for (final Integer scoreTime : scoreTimes) {
            if (scoreTime != null && scoreTime < quickest) {
                quickest = scoreTime;
            }
        }
        return quickest;
    }

    private void updateDurationAgainstWinner(Duel duel, Participant participant, DuelStatisticsAccumulator accumulator) {
        if (duel.getDuration() == null || duel.getDuration() <= Duel.DEFAULT_DURATION) {
            return;
        }
        if (Objects.equals(duel.getCompetitorWinner(), participant)) {
            accumulator.totalDuelWonsWithDuration += duel.getDuration();
            accumulator.wonDuelsWithDuration++;
        } else if (duel.getCompetitorWinner() != null) {
            accumulator.totalDuelLostsWithDuration += duel.getDuration();
            accumulator.lostDuelsWithDuration++;
        }
    }

    private void applyAverageTimes(ParticipantFightStatistics participantFightStatistics, long participantDurationAverage,
                                   DuelStatisticsAccumulator accumulator) {
        participantFightStatistics.setAverageTime(participantDurationAverage > 0 ? participantDurationAverage : 0L);
        participantFightStatistics.setAverageWinTime(accumulator.totalDuelWonsWithDuration > 0
                ? accumulator.totalDuelWonsWithDuration / accumulator.wonDuelsWithDuration : 0L);
        participantFightStatistics.setAverageLostTime(accumulator.totalDuelLostsWithDuration > 0
                ? accumulator.totalDuelLostsWithDuration / accumulator.lostDuelsWithDuration : 0L);
    }

    private void applyQuickestHits(ParticipantFightStatistics participantFightStatistics, DuelStatisticsAccumulator accumulator) {
        if (accumulator.quickestHit < Integer.MAX_VALUE) {
            participantFightStatistics.setQuickestHit(accumulator.quickestHit);
        }
        if (accumulator.quickestReceivedHit < Integer.MAX_VALUE) {
            participantFightStatistics.setQuickestReceivedHit(accumulator.quickestReceivedHit);
        }
    }

    /**
     * Mutable holder for the statistics accumulated while iterating the duels of a participant.
     */
    private static final class DuelStatisticsAccumulator {
        private long totalDuelsDuration = 0L;
        private long totalDuelWonsWithDuration = 0L;
        private long totalDuelLostsWithDuration = 0L;
        private long quickestHit = Integer.MAX_VALUE;
        private long quickestReceivedHit = Integer.MAX_VALUE;
        private long wonDuels = 0L;
        private long wonDuelsWithDuration = 0L;
        private long lostDuels = 0L;
        private long lostDuelsWithDuration = 0L;
        private long drawDuels = 0L;
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
