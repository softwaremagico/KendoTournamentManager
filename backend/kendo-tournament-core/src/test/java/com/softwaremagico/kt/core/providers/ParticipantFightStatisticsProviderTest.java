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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ParticipantFightStatisticsProviderTest {

    @Mock
    private ParticipantFightStatisticsRepository mockRepository;

    @Mock
    private DuelProvider mockDuelProvider;

    private ParticipantFightStatisticsProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new ParticipantFightStatisticsProvider(mockRepository, mockDuelProvider);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldReturnEmptyStatsForParticipantWithNoDuels() {
        Participant participant = createParticipant(1, "Fighter One");

        when(mockDuelProvider.get(participant)).thenReturn(Collections.emptyList());
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getDuelsNumber(), 0L);
        assertEquals(result.getWonDuels(), 0L);
        assertEquals(result.getLostDuels(), 0L);
        assertEquals(result.getDrawDuels(), 0L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountWinWhenCompetitor1HasMorePoints() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor1Score(List.of(Score.IPPON, Score.IPPON));
        duel.setCompetitor2Score(List.of(Score.MEN));
        duel.setDuration(120);

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(120L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getWonDuels(), 1L);
        assertEquals(result.getLostDuels(), 0L);
        assertEquals(result.getDrawDuels(), 0L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountLossWhenCompetitor1HasLessPoints() {
        Participant participant = createParticipant(1, "Fighter One");
        Participant opponent = createParticipant(2, "Fighter Two");
        Duel duel = createDuel(participant, opponent);
        duel.setCompetitor1Score(List.of(Score.MEN));
        duel.setCompetitor2Score(List.of(Score.IPPON, Score.IPPON));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getWonDuels(), 0L);
        assertEquals(result.getLostDuels(), 1L);
        assertEquals(result.getDrawDuels(), 0L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountDrawWhenBothHaveSamePoints() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor1Score(List.of(Score.MEN, Score.MEN));
        duel.setCompetitor2Score(List.of(Score.MEN, Score.MEN));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getDrawDuels(), 1L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountWinWhenCompetitor2HasMorePoints() {
        Participant participant = createParticipant(2, "Fighter Two");
        Participant opponent = createParticipant(1, "Fighter One");
        Duel duel = createDuel(opponent, participant);
        duel.setCompetitor1Score(List.of(Score.MEN));
        duel.setCompetitor2Score(List.of(Score.IPPON, Score.IPPON));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getWonDuels(), 1L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountLossWhenCompetitor2HasLessPoints() {
        Participant participant = createParticipant(2, "Fighter Two");
        Participant opponent = createParticipant(1, "Fighter One");
        Duel duel = createDuel(opponent, participant);
        duel.setCompetitor1Score(List.of(Score.IPPON, Score.IPPON));
        duel.setCompetitor2Score(List.of(Score.MEN));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getLostDuels(), 1L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountMenScore() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor1Score(List.of(Score.MEN, Score.MEN));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getMenNumber(), 2L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountAllScoreTypes() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor1Score(List.of(Score.MEN, Score.KOTE, Score.DO, Score.TSUKI, Score.IPPON, Score.HANSOKU, Score.FUSEN_GACHI));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getMenNumber(), 1L);
        assertEquals(result.getKoteNumber(), 1L);
        assertEquals(result.getDoNumber(), 1L);
        assertEquals(result.getTsukiNumber(), 1L);
        assertEquals(result.getIpponNumber(), 1L);
        assertEquals(result.getHansokuNumber(), 1L);
        assertEquals(result.getFusenGachiNumber(), 1L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountReceivedScores() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor2Score(List.of(Score.MEN, Score.KOTE, Score.IPPON));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getReceivedMenNumber(), 1L);
        assertEquals(result.getReceivedKoteNumber(), 1L);
        assertEquals(result.getReceivedIpponNumber(), 1L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountFaults() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor1Fault(true);

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getFaults(), 1L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCountReceivedFaults() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor2Fault(true);

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getReceivedFaults(), 1L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldTrackQuickestHit() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor1ScoreTime(List.of(45, 120, 200));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getQuickestHit(), 45);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldTrackQuickestReceivedHit() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor2ScoreTime(List.of(90, 150, 250));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getQuickestReceivedHit(), 90);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCalculateAverageWinTime() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel1 = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel1.setDuration(150);
        duel1.setCompetitor1Score(List.of(Score.IPPON, Score.IPPON));
        duel1.setCompetitor2Score(List.of(Score.MEN));

        Duel duel2 = createDuel(participant, createParticipant(3, "Fighter Three"));
        duel2.setDuration(180);
        duel2.setCompetitor1Score(List.of(Score.IPPON, Score.IPPON));
        duel2.setCompetitor2Score(List.of(Score.MEN));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel1, duel2));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(165L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getAverageWinTime(), 165L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCalculateAverageLostTime() {
        Participant participant = createParticipant(1, "Fighter One");
        Participant opponent = createParticipant(2, "Fighter Two");
        Duel duel1 = createDuel(participant, opponent);
        duel1.setDuration(120);
        duel1.setCompetitor1Score(List.of(Score.MEN));
        duel1.setCompetitor2Score(List.of(Score.IPPON, Score.IPPON));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel1));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getAverageLostTime(), 120L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldCalculateTotalDuelTime() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel1 = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel1.setDuration(120);

        Duel duel2 = createDuel(participant, createParticipant(3, "Fighter Three"));
        duel2.setDuration(150);

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel1, duel2));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(135L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getTotalDuelsTime(), 270L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldIgnoreDuelWhenParticipantNotInvolved() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(createParticipant(2, "Fighter Two"), createParticipant(3, "Fighter Three"));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getDuelsNumber(), 1L);
        assertEquals(result.getWonDuels(), 0L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldHandleNullDurationValues() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setDuration(null);

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getTotalDuelsTime(), 0L);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldHandleNullScoreTime() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor1ScoreTime(List.of(null, 100, null));

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getQuickestHit(), 100);
    }

    @Test(groups = "participantFightStatistics")
    public void get_shouldHandleNullFaultValues() {
        Participant participant = createParticipant(1, "Fighter One");
        Duel duel = createDuel(participant, createParticipant(2, "Fighter Two"));
        duel.setCompetitor1Fault(null);

        when(mockDuelProvider.get(participant)).thenReturn(List.of(duel));
        when(mockDuelProvider.getDurationAverage(participant)).thenReturn(0L);

        ParticipantFightStatistics result = provider.get(participant);

        assertNotNull(result);
        assertEquals(result.getFaults(), 0L);
    }

    // Helper methods

    private Participant createParticipant(int id, String name) {
        Participant participant = new Participant();
        participant.setId(id);
        participant.setName(name);
        return participant;
    }

    private Duel createDuel(Participant competitor1, Participant competitor2) {
        Duel duel = new Duel();
        duel.setCompetitor1(competitor1);
        duel.setCompetitor2(competitor2);
        duel.setCompetitor1Score(new ArrayList<>());
        duel.setCompetitor2Score(new ArrayList<>());
        duel.setCompetitor1ScoreTime(new ArrayList<>());
        duel.setCompetitor2ScoreTime(new ArrayList<>());
        duel.setCompetitor1Fault(false);
        duel.setCompetitor2Fault(false);
        return duel;
    }
}








