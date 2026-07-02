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
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class ParticipantFightStatisticsProvider extends CrudProvider<ParticipantFightStatistics, Integer, ParticipantFightStatisticsRepository> {

    private static final Map<Score, ScoreCounterAccessor> SCORE_COUNTERS = Map.of(
            Score.MEN, new ScoreCounterAccessor(
                    ParticipantFightStatistics::getMenNumber, ParticipantFightStatistics::setMenNumber,
                    ParticipantFightStatistics::getReceivedMenNumber, ParticipantFightStatistics::setReceivedMenNumber),
            Score.KOTE, new ScoreCounterAccessor(
                    ParticipantFightStatistics::getKoteNumber, ParticipantFightStatistics::setKoteNumber,
                    ParticipantFightStatistics::getReceivedKoteNumber, ParticipantFightStatistics::setReceivedKoteNumber),
            Score.DO, new ScoreCounterAccessor(
                    ParticipantFightStatistics::getDoNumber, ParticipantFightStatistics::setDoNumber,
                    ParticipantFightStatistics::getReceivedDoNumber, ParticipantFightStatistics::setReceivedDoNumber),
            Score.TSUKI, new ScoreCounterAccessor(
                    ParticipantFightStatistics::getTsukiNumber, ParticipantFightStatistics::setTsukiNumber,
                    ParticipantFightStatistics::getReceivedTsukiNumber, ParticipantFightStatistics::setReceivedTsukiNumber),
            Score.HANSOKU, new ScoreCounterAccessor(
                    ParticipantFightStatistics::getHansokuNumber, ParticipantFightStatistics::setHansokuNumber,
                    ParticipantFightStatistics::getReceivedHansokuNumber, ParticipantFightStatistics::setReceivedHansokuNumber),
            Score.IPPON, new ScoreCounterAccessor(
                    ParticipantFightStatistics::getIpponNumber, ParticipantFightStatistics::setIpponNumber,
                    ParticipantFightStatistics::getReceivedIpponNumber, ParticipantFightStatistics::setReceivedIpponNumber),
            Score.FUSEN_GACHI, new ScoreCounterAccessor(
                    ParticipantFightStatistics::getFusenGachiNumber, ParticipantFightStatistics::setFusenGachiNumber,
                    ParticipantFightStatistics::getReceivedFusenGachiNumber, ParticipantFightStatistics::setReceivedFusenGachiNumber)
    );

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
        final FightComputationState state = new FightComputationState();
        for (final Duel duel : duels) {
            accumulateDuelStatistics(participantFightStatistics, participant, duel, state);
        }
        participantFightStatistics.setAverageTime(Math.max(participantDurationAverage, 0L));
        participantFightStatistics.setAverageWinTime(calculateAverageDuration(state.totalDuelWonsWithDuration, state.wonDuelsWithDuration));
        participantFightStatistics.setAverageLostTime(calculateAverageDuration(state.totalDuelLostsWithDuration, state.lostDuelsWithDuration));
        if (state.quickestHit < Integer.MAX_VALUE) {
            participantFightStatistics.setQuickestHit(state.quickestHit);
        }
        if (state.quickestReceivedHit < Integer.MAX_VALUE) {
            participantFightStatistics.setQuickestReceivedHit(state.quickestReceivedHit);
        }
        participantFightStatistics.setTotalDuelsTime(state.totalDuelsDuration);
        participantFightStatistics.setDuelsNumber((long) duels.size());
        participantFightStatistics.setWonDuels(state.wonDuels);
        participantFightStatistics.setDrawDuels(state.drawDuels);
        participantFightStatistics.setLostDuels(state.lostDuels);
        return participantFightStatistics;
    }

    private void accumulateDuelStatistics(ParticipantFightStatistics participantFightStatistics, Participant participant,
                                          Duel duel, FightComputationState state) {
        final DuelAccumulator accumulator = createDuelAccumulator(duel, participant);
        if (accumulator != null) {
            accumulateParticipantDuel(participantFightStatistics, duel, accumulator, state);
        }
        accumulateWinLossDurations(participant, duel, state);
    }

    private DuelAccumulator createDuelAccumulator(Duel duel, Participant participant) {
        final int winner = duel.getWinner();
        if (Objects.equals(duel.getCompetitor1(), participant)) {
            return new DuelAccumulator(
                    duel.getCompetitor1Score(), duel.getCompetitor2Score(),
                    duel.getCompetitor1Fault(), duel.getCompetitor2Fault(),
                    duel.getCompetitor1ScoreTime(), duel.getCompetitor2ScoreTime(),
                    winner < 0);
        }
        if (Objects.equals(duel.getCompetitor2(), participant)) {
            return new DuelAccumulator(
                    duel.getCompetitor2Score(), duel.getCompetitor1Score(),
                    duel.getCompetitor2Fault(), duel.getCompetitor1Fault(),
                    duel.getCompetitor2ScoreTime(), duel.getCompetitor1ScoreTime(),
                    winner > 0);
        }
        return null;
    }

    private void accumulateParticipantDuel(ParticipantFightStatistics participantFightStatistics, Duel duel,
                                           DuelAccumulator accumulator, FightComputationState state) {
        populateScores(participantFightStatistics, accumulator.myScores(), false);
        populateScores(participantFightStatistics, accumulator.opponentScores(), true);
        participantFightStatistics.setFaults(participantFightStatistics.getFaults()
                + (Boolean.TRUE.equals(accumulator.myFault()) ? 1 : 0));
        participantFightStatistics.setReceivedFaults(participantFightStatistics.getReceivedFaults()
                + (Boolean.TRUE.equals(accumulator.opponentFault()) ? 1 : 0));

        state.quickestHit = updateQuickest(accumulator.myScoreTimes(), state.quickestHit);
        state.quickestReceivedHit = updateQuickest(accumulator.opponentScoreTimes(), state.quickestReceivedHit);

        if (accumulator.won()) {
            state.wonDuels++;
        } else if (duel.getWinner() == 0) {
            state.drawDuels++;
        } else {
            state.lostDuels++;
        }

        if (isValidDuration(duel.getDuration())) {
            state.totalDuelsDuration += duel.getDuration();
        }
    }

    private void accumulateWinLossDurations(Participant participant, Duel duel, FightComputationState state) {
        if (!isValidDuration(duel.getDuration())) {
            return;
        }
        if (Objects.equals(duel.getCompetitorWinner(), participant)) {
            state.totalDuelWonsWithDuration += duel.getDuration();
            state.wonDuelsWithDuration++;
        } else if (duel.getCompetitorWinner() != null) {
            state.totalDuelLostsWithDuration += duel.getDuration();
            state.lostDuelsWithDuration++;
        }
    }

    private record DuelAccumulator(
        List<Score> myScores,
        List<Score> opponentScores,
        Boolean myFault,
        Boolean opponentFault,
        List<Integer> myScoreTimes,
        List<Integer> opponentScoreTimes,
        boolean won) {
    }

    private static final class FightComputationState {
        private long totalDuelsDuration;
        private long totalDuelWonsWithDuration;
        private long totalDuelLostsWithDuration;
        private long quickestHit = Integer.MAX_VALUE;
        private long quickestReceivedHit = Integer.MAX_VALUE;
        private long wonDuels;
        private long wonDuelsWithDuration;
        private long lostDuels;
        private long lostDuelsWithDuration;
        private long drawDuels;
    }

    private record ScoreCounterAccessor(
            Function<ParticipantFightStatistics, Long> ownGetter,
            BiConsumer<ParticipantFightStatistics, Long> ownSetter,
            Function<ParticipantFightStatistics, Long> receivedGetter,
            BiConsumer<ParticipantFightStatistics, Long> receivedSetter) {
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

    private void populateScores(ParticipantFightStatistics participantFightStatistics, List<Score> scores, boolean received) {
        // Remove null values before counting score types.
        scores = scores.stream().filter(Objects::nonNull).toList();
        for (final Score score : scores) {
            incrementScore(participantFightStatistics, score, received);
        }
    }

    private boolean isValidDuration(Integer duration) {
        return duration != null && duration > Duel.DEFAULT_DURATION;
    }

    private long calculateAverageDuration(long totalDuration, long duelsWithDuration) {
        if (totalDuration <= 0 || duelsWithDuration <= 0) {
            return 0L;
        }
        return totalDuration / duelsWithDuration;
    }

    private void incrementScore(ParticipantFightStatistics participantFightStatistics, Score score, boolean received) {
        final ScoreCounterAccessor counterAccessor = SCORE_COUNTERS.get(score);
        if (counterAccessor == null) {
            return;
        }
        final Function<ParticipantFightStatistics, Long> getter = received
                ? counterAccessor.receivedGetter()
                : counterAccessor.ownGetter();
        final BiConsumer<ParticipantFightStatistics, Long> setter = received
                ? counterAccessor.receivedSetter()
                : counterAccessor.ownSetter();
        setter.accept(participantFightStatistics, getter.apply(participantFightStatistics) + 1);
    }
}
