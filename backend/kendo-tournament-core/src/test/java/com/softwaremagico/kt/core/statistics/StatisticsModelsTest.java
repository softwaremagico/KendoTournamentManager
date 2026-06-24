package com.softwaremagico.kt.core.statistics;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.persistence.values.RoleType;
import org.testng.annotations.Test;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {"statisticsTests"})
public class StatisticsModelsTest {

    @Test
    public void testParticipantStatisticsAddRolePerformedInitializesMapWhenNull() {
        final ParticipantStatistics statistics = new ParticipantStatistics();
        statistics.setRolesPerformed(null);

        statistics.addRolePerformed(RoleType.COMPETITOR, 5L);

        assertThat(statistics.getRolesPerformed()).isNotNull();
        assertThat(statistics.getRolesPerformed()).containsEntry(RoleType.COMPETITOR, 5L);
    }

    @Test
    public void testParticipantStatisticsAddRolePerformedUsesExistingMap() {
        final ParticipantStatistics statistics = new ParticipantStatistics();
        statistics.setRolesPerformed(new EnumMap<>(RoleType.class));

        statistics.addRolePerformed(RoleType.REFEREE, 2L);

        assertThat(statistics.getRolesPerformed()).containsEntry(RoleType.REFEREE, 2L);
    }

    @Test
    public void testTournamentStatisticsAddNumberOfParticipantsInitializesMapWhenNull() {
        final TournamentStatistics statistics = new TournamentStatistics();
        statistics.setNumberOfParticipants(null);

        statistics.addNumberOfParticipants(RoleType.COMPETITOR, 12L);

        assertThat(statistics.getNumberOfParticipants()).isNotNull();
        assertThat(statistics.getNumberOfParticipants()).containsEntry(RoleType.COMPETITOR, 12L);
    }

    @Test
    public void testTournamentStatisticsAddNumberOfParticipantsUsesExistingMap() {
        final TournamentStatistics statistics = new TournamentStatistics();
        statistics.setNumberOfParticipants(new EnumMap<>(RoleType.class));

        statistics.addNumberOfParticipants(RoleType.ORGANIZER, 3L);

        assertThat(statistics.getNumberOfParticipants()).containsEntry(RoleType.ORGANIZER, 3L);
    }

    @Test
    public void testParticipantFightStatisticsDuelsNumberAcceptsOnlyNonNegativeValues() {
        final ParticipantFightStatistics statistics = new ParticipantFightStatistics();

        statistics.setDuelsNumber(7L);
        assertThat(statistics.getDuelsNumber()).isEqualTo(7L);

        statistics.setDuelsNumber(-1L);
        assertThat(statistics.getDuelsNumber()).isZero();

        statistics.setDuelsNumber(null);
        assertThat(statistics.getDuelsNumber()).isZero();
    }

    @Test
    public void testTournamentFightStatisticsCountsAcceptOnlyNonNegativeValues() {
        final TournamentFightStatistics statistics = new TournamentFightStatistics();

        statistics.setFightsNumber(5L);
        statistics.setDuelsNumber(13L);
        assertThat(statistics.getFightsNumber()).isEqualTo(5L);
        assertThat(statistics.getDuelsNumber()).isEqualTo(13L);

        statistics.setFightsNumber(-5L);
        statistics.setDuelsNumber(-2L);
        assertThat(statistics.getFightsNumber()).isNull();
        assertThat(statistics.getDuelsNumber()).isNull();

        statistics.setFightsNumber(null);
        statistics.setDuelsNumber(null);
        assertThat(statistics.getFightsNumber()).isNull();
        assertThat(statistics.getDuelsNumber()).isNull();
    }

    @Test
    public void testParticipantFightStatisticsAllFields() {
        final ParticipantFightStatistics stats = new ParticipantFightStatistics();

        // Test all hit setters/getters
        stats.setMenNumber(1L);
        assertThat(stats.getMenNumber()).isEqualTo(1L);

        stats.setKoteNumber(2L);
        assertThat(stats.getKoteNumber()).isEqualTo(2L);

        stats.setDoNumber(3L);
        assertThat(stats.getDoNumber()).isEqualTo(3L);

        stats.setTsukiNumber(4L);
        assertThat(stats.getTsukiNumber()).isEqualTo(4L);

        stats.setHansokuNumber(5L);
        assertThat(stats.getHansokuNumber()).isEqualTo(5L);

        stats.setIpponNumber(6L);
        assertThat(stats.getIpponNumber()).isEqualTo(6L);

        stats.setFusenGachiNumber(7L);
        assertThat(stats.getFusenGachiNumber()).isEqualTo(7L);

        stats.setFaults(8L);
        assertThat(stats.getFaults()).isEqualTo(8L);

        // Test all received hit setters/getters
        stats.setReceivedMenNumber(10L);
        assertThat(stats.getReceivedMenNumber()).isEqualTo(10L);

        stats.setReceivedKoteNumber(11L);
        assertThat(stats.getReceivedKoteNumber()).isEqualTo(11L);

        stats.setReceivedDoNumber(12L);
        assertThat(stats.getReceivedDoNumber()).isEqualTo(12L);

        stats.setReceivedTsukiNumber(13L);
        assertThat(stats.getReceivedTsukiNumber()).isEqualTo(13L);

        stats.setReceivedHansokuNumber(14L);
        assertThat(stats.getReceivedHansokuNumber()).isEqualTo(14L);

        stats.setReceivedIpponNumber(15L);
        assertThat(stats.getReceivedIpponNumber()).isEqualTo(15L);

        stats.setReceivedFusenGachiNumber(16L);
        assertThat(stats.getReceivedFusenGachiNumber()).isEqualTo(16L);

        stats.setReceivedFaults(17L);
        assertThat(stats.getReceivedFaults()).isEqualTo(17L);
    }

    @Test
    public void testParticipantFightStatisticsNullDefaults() {
        final ParticipantFightStatistics stats = new ParticipantFightStatistics();

        // All null fields should return 0L when using getters with default fallback
        assertThat(stats.getMenNumber()).isZero();
        assertThat(stats.getKoteNumber()).isZero();
        assertThat(stats.getDoNumber()).isZero();
        assertThat(stats.getTsukiNumber()).isZero();
        assertThat(stats.getHansokuNumber()).isZero();
        assertThat(stats.getIpponNumber()).isZero();
        assertThat(stats.getFusenGachiNumber()).isZero();
        assertThat(stats.getFaults()).isZero();
        assertThat(stats.getReceivedMenNumber()).isZero();
        assertThat(stats.getReceivedKoteNumber()).isZero();
        assertThat(stats.getReceivedDoNumber()).isZero();
        assertThat(stats.getReceivedTsukiNumber()).isZero();
        assertThat(stats.getReceivedHansokuNumber()).isZero();
        assertThat(stats.getReceivedIpponNumber()).isZero();
        assertThat(stats.getReceivedFusenGachiNumber()).isZero();
        assertThat(stats.getReceivedFaults()).isZero();
    }

    @Test
    public void testParticipantFightStatisticsTimeFields() {
        final ParticipantFightStatistics stats = new ParticipantFightStatistics();

        stats.setAverageTime(100L);
        assertThat(stats.getAverageTime()).isEqualTo(100L);

        stats.setAverageWinTime(200L);
        assertThat(stats.getAverageWinTime()).isEqualTo(200L);

        stats.setAverageLostTime(300L);
        assertThat(stats.getAverageLostTime()).isEqualTo(300L);

        stats.setTotalDuelsTime(400L);
        assertThat(stats.getTotalDuelsTime()).isEqualTo(400L);

        stats.setQuickestHit(50L);
        assertThat(stats.getQuickestHit()).isEqualTo(50L);

        stats.setQuickestReceivedHit(60L);
        assertThat(stats.getQuickestReceivedHit()).isEqualTo(60L);
    }

    @Test
    public void testParticipantFightStatisticsWinLossDraw() {
        final ParticipantFightStatistics stats = new ParticipantFightStatistics();

        stats.setWonDuels(10L);
        assertThat(stats.getWonDuels()).isEqualTo(10L);

        stats.setLostDuels(5L);
        assertThat(stats.getLostDuels()).isEqualTo(5L);

        stats.setDrawDuels(2L);
        assertThat(stats.getDrawDuels()).isEqualTo(2L);
    }

    @Test
    public void testTournamentFightStatisticsAllFields() {
        final TournamentFightStatistics stats = new TournamentFightStatistics();

        // Test hit setters/getters
        stats.setMenNumber(1L);
        assertThat(stats.getMenNumber()).isEqualTo(1L);

        stats.setKoteNumber(2L);
        assertThat(stats.getKoteNumber()).isEqualTo(2L);

        stats.setDoNumber(3L);
        assertThat(stats.getDoNumber()).isEqualTo(3L);

        stats.setTsukiNumber(4L);
        assertThat(stats.getTsukiNumber()).isEqualTo(4L);

        stats.setHansokuNumber(5L);
        assertThat(stats.getHansokuNumber()).isEqualTo(5L);

        stats.setIpponNumber(6L);
        assertThat(stats.getIpponNumber()).isEqualTo(6L);

        stats.setFusenGachiNumber(7L);
        assertThat(stats.getFusenGachiNumber()).isEqualTo(7L);
    }

    @Test
    public void testTournamentFightStatisticsCounterFields() {
        final TournamentFightStatistics stats = new TournamentFightStatistics();

        // Valid values
        stats.setFightsByTeam(8L);
        assertThat(stats.getFightsByTeam()).isEqualTo(8L);

        stats.setEstimatedTime(9L);
        assertThat(stats.getEstimatedTime()).isEqualTo(9L);

        stats.setAverageTime(10L);
        assertThat(stats.getAverageTime()).isEqualTo(10L);

        stats.setFightsFinished(11L);
        assertThat(stats.getFightsFinished()).isEqualTo(11L);

        stats.setFaults(12L);
        assertThat(stats.getFaults()).isEqualTo(12L);
    }

    @Test
    public void testTournamentFightStatisticsDateFields() {
        final TournamentFightStatistics stats = new TournamentFightStatistics();
        final java.time.LocalDateTime now = java.time.LocalDateTime.now();
        final java.time.LocalDateTime later = now.plusHours(1);

        stats.setFightsStartedAt(now);
        assertThat(stats.getFightsStartedAt()).isEqualTo(now);

        stats.setFightsFinishedAt(later);
        assertThat(stats.getFightsFinishedAt()).isEqualTo(later);
    }

    @Test
    public void testTournamentFightStatisticsFightsNumberNegativeValue() {
        final TournamentFightStatistics stats = new TournamentFightStatistics();

        // Branch: fightsNumber < 0
        stats.setFightsNumber(-1L);
        assertThat(stats.getFightsNumber()).isNull();
    }

    @Test
    public void testTournamentFightStatisticsDuelsNumberNegativeValue() {
        final TournamentFightStatistics stats = new TournamentFightStatistics();

        // Branch: duelsNumber < 0
        stats.setDuelsNumber(-1L);
        assertThat(stats.getDuelsNumber()).isNull();
    }
}


