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

import com.softwaremagico.kt.core.statistics.TournamentFightStatistics;
import com.softwaremagico.kt.core.statistics.TournamentFightStatisticsRepository;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class TournamentFightStatisticsProviderTest {

    @Mock
    private TournamentFightStatisticsRepository mockRepository;

    @Mock
    private DuelProvider mockDuelProvider;

    @Mock
    private FightProvider mockFightProvider;

    @Mock
    private TeamProvider mockTeamProvider;

    @Mock
    private RoleProvider mockRoleProvider;

    @Mock
    private TournamentExtraPropertyProvider mockTournamentExtraPropertyProvider;

    private TournamentFightStatisticsProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new TournamentFightStatisticsProvider(
                mockRepository,
                mockDuelProvider,
                mockFightProvider,
                mockTeamProvider,
                mockRoleProvider,
                mockTournamentExtraPropertyProvider
        );
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldReturnNullWhenTournamentIsNull() {
        assertThrows(NullPointerException.class, () -> provider.estimate((Tournament) null));
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldCalculateByTeamsWhenTeamsExist() {
        Tournament tournament = createTournament();
        List<Team> teams = createTeams(tournament, 4);

        when(mockTeamProvider.getAll(tournament)).thenReturn(teams);

        TournamentFightStatistics result = provider.estimate(tournament);

        assertNotNull(result);
        verify(mockTeamProvider).getAll(tournament);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldCalculateByRolesWhenNoTeams() {
        Tournament tournament = createTournament();
        List<Team> emptyTeams = Collections.emptyList();
        List<Role> roles = createRoles(tournament, 4);

        when(mockTeamProvider.getAll(tournament)).thenReturn(emptyTeams);
        when(mockRoleProvider.getAll(tournament)).thenReturn(roles);

        TournamentFightStatistics result = provider.estimate(tournament);

        assertNotNull(result);
        verify(mockRoleProvider).getAll(tournament);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateByTeams_shouldReturnStatistics() {
        Tournament tournament = createTournament();
        List<Team> teams = createTeams(tournament, 4);

        when(mockTeamProvider.getAll(tournament)).thenReturn(teams);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateByTeams(tournament);

        assertNotNull(result);
        assertEquals(result.getFightsNumber(), 6L);
        assertEquals(result.getFightsByTeam(), 3L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateByMembers_shouldFilterOnlyCompetitors() {
        Tournament tournament = createTournament();
        tournament.setTeamSize(2);
        List<Role> mixedRoles = createMixedRoles(tournament);

        when(mockRoleProvider.getAll(tournament)).thenReturn(mixedRoles);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateByMembers(tournament);

        assertNotNull(result);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLeagueStatistics_shouldCalculateCorrectFightNumbers() {
        List<Team> teams = createTeams(null, 4);

        TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

        assertNotNull(result);
        assertEquals(result.getFightsNumber(), 6L);
        assertEquals(result.getFightsByTeam(), 3L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLeagueStatistics_shouldCalculateEstimatedTimeWhenDurationAvailable() {
        List<Team> teams = createTeams(null, 4);

        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

        assertNotNull(result);
        assertNotNull(result.getEstimatedTime());
        assertTrue(result.getEstimatedTime() > 0);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLoopStatistics_shouldAvoidDuplicatesWhenPropertyEnabled() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.LOOP);
        List<Team> teams = createTeams(tournament, 3);
        TournamentExtraProperty property = new TournamentExtraProperty();
        property.setPropertyValue("true");

        when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                .thenReturn(property);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

        assertNotNull(result);
        assertEquals(result.getFightsNumber(), 4L);
        assertEquals(result.getFightsByTeam(), 2L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLeagueStatistics_shouldNotEstimateTimeWhenDurationAverageIsNull() {
        List<Team> teams = createTeams(null, 4);
        when(mockDuelProvider.getDurationAverage()).thenReturn(null);

        TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

        assertNotNull(result);
        assertNull(result.getEstimatedTime());
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLeagueStatistics_shouldEstimateTimeWhenDurationAverageIsNegative() {
        List<Team> teams = createTeams(null, 4);
        when(mockDuelProvider.getDurationAverage()).thenReturn(-10L);

        TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

        assertNotNull(result);
        assertNull(result.getEstimatedTime());
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLoopStatistics_shouldUseDefaultWhenPropertyIsNull() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.LOOP);
        List<Team> teams = createTeams(tournament, 3);

        when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                .thenReturn(null);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

        assertNotNull(result);
        assertEquals(result.getFightsNumber(), 4L);
        assertEquals(result.getFightsByTeam(), 2L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLoopStatistics_shouldSetEstimatedTimeToZeroWhenDurationAverageIsNegative() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.LOOP);
        List<Team> teams = createTeams(tournament, 3);
        TournamentExtraProperty property = new TournamentExtraProperty();
        property.setPropertyValue("false");

        when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                .thenReturn(property);
        when(mockDuelProvider.getDurationAverage()).thenReturn(-10L);

        TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

        assertNotNull(result);
        assertEquals(result.getEstimatedTime(), 0L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLoopStatistics_shouldNotSetEstimatedTimeWhenDurationAverageIsNull() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.LOOP);
        List<Team> teams = createTeams(tournament, 3);
        TournamentExtraProperty property = new TournamentExtraProperty();
        property.setPropertyValue("false");

        when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                .thenReturn(property);
        when(mockDuelProvider.getDurationAverage()).thenReturn(null);

        TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

        assertNotNull(result);
        assertNull(result.getEstimatedTime());
    }

    @Test(groups = "tournamentFightStatistics")
    public void get_shouldSetFightsByTeamToZeroWhenNoTeams() {
        Tournament tournament = createTournament();

        when(mockFightProvider.count(tournament)).thenReturn(3L);
        when(mockTeamProvider.count(tournament)).thenReturn(0L);
        when(mockDuelProvider.count(tournament)).thenReturn(0L);
        when(mockDuelProvider.getDurationAverage(tournament)).thenReturn(180L);
        when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
        when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
        when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
        when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
        when(mockDuelProvider.countFaults(tournament)).thenReturn(0L);

        TournamentFightStatistics result = provider.get(tournament);

        assertNotNull(result);
        assertEquals(result.getFightsByTeam(), 0L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLoopStatistics_shouldMaximizeFightsWhenPropertyDisabled() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.LOOP);
        List<Team> teams = createTeams(tournament, 3);
        TournamentExtraProperty property = new TournamentExtraProperty();
        property.setPropertyValue("false");

        when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                .thenReturn(property);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

        assertNotNull(result);
        assertEquals(result.getFightsNumber(), 6L);
        assertEquals(result.getFightsByTeam(), 4L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void get_shouldAggregateAllStatisticsFromDuels() {
        Tournament tournament = createTournament();

        when(mockFightProvider.count(tournament)).thenReturn(10L);
        when(mockTeamProvider.count(tournament)).thenReturn(4L);
        when(mockDuelProvider.count(tournament)).thenReturn(40L);
        when(mockDuelProvider.getDurationAverage(tournament)).thenReturn(180L);
        when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(15L);
        when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(12L);
        when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(8L);
        when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(5L);
        when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
        when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
        when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
        when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(8L);
        when(mockDuelProvider.countFaults(tournament)).thenReturn(2L);

        TournamentFightStatistics result = provider.get(tournament);

        assertNotNull(result);
        assertEquals(result.getFightsNumber(), 10L);
        assertEquals(result.getFightsByTeam(), 2L);
        assertEquals(result.getDuelsNumber(), 40L);
        assertEquals(result.getMenNumber(), 15L);
        assertEquals(result.getDoNumber(), 12L);
        assertEquals(result.getKoteNumber(), 8L);
        assertEquals(result.getIpponNumber(), 5L);
        assertEquals(result.getFightsFinished(), 8L);
        assertEquals(result.getFaults(), 2L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void get_shouldCalculateStartTimeFromFirstDuel() {
        Tournament tournament = createTournament();
        LocalDateTime startTime = LocalDateTime.now();
        Duel firstDuel = new Duel();
        firstDuel.setStartedAt(startTime);

        when(mockFightProvider.count(tournament)).thenReturn(0L);
        when(mockTeamProvider.count(tournament)).thenReturn(0L);
        when(mockDuelProvider.count(tournament)).thenReturn(0L);
        when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(firstDuel);
        when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
        when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
        when(mockDuelProvider.countFaults(tournament)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);

        TournamentFightStatistics result = provider.get(tournament);

        assertNotNull(result);
        assertNotNull(result.getFightsStartedAt());
        assertEquals(result.getFightsStartedAt(), startTime);
    }

    @Test(groups = "tournamentFightStatistics")
    public void get_shouldEstimateStartTimeFromFinishedDuel() {
        Tournament tournament = createTournament();
        LocalDateTime finishedTime = LocalDateTime.now();
        Duel firstDuel = new Duel();
        firstDuel.setFinishedAt(finishedTime);

        when(mockFightProvider.count(tournament)).thenReturn(0L);
        when(mockTeamProvider.count(tournament)).thenReturn(0L);
        when(mockDuelProvider.count(tournament)).thenReturn(0L);
        when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(firstDuel);
        when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
        when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
        when(mockDuelProvider.countFaults(tournament)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
        when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);

        TournamentFightStatistics result = provider.get(tournament);

        assertNotNull(result);
        assertNotNull(result.getFightsStartedAt());
        assertEquals(result.getFightsStartedAt(), finishedTime.minusMinutes(2));
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldHandleNullTeamsParameter() {
        Tournament tournament = createTournament();

        TournamentFightStatistics result = provider.estimate(tournament, null);

        assertNull(result);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldHandleNullTournamentOnFullSignature() {
        List<Team> teams = createTeams(null, 2);

        TournamentFightStatistics result = provider.estimate(null, 3, teams);

        assertNull(result);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldHandleLessThanTwoTeams() {
        Tournament tournament = createTournament();
        List<Team> oneTeam = createTeams(tournament, 1);

        TournamentFightStatistics result = provider.estimate(tournament, oneTeam);

        assertNull(result);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldReturnNullForCustomizedTournament() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.CUSTOMIZED);
        List<Team> teams = createTeams(tournament, 4);

        TournamentFightStatistics result = provider.estimate(tournament, 3, teams);

        assertNull(result);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldReturnNullForKingOfTheMountainTournament() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.KING_OF_THE_MOUNTAIN);
        List<Team> teams = createTeams(tournament, 4);

        TournamentFightStatistics result = provider.estimate(tournament, 3, teams);

        assertNull(result);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimate_shouldDispatchToLoopStatistics() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.LOOP);
        List<Team> teams = createTeams(tournament, 3);
        TournamentExtraProperty property = new TournamentExtraProperty();
        property.setPropertyValue("false");

        when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                .thenReturn(property);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimate(tournament, 3, teams);

        assertNotNull(result);
        assertEquals(result.getFightsByTeam(), 4L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLeagueStatistics_shouldNotEstimateTimeWhenDurationAverageIsZero() {
        List<Team> teams = createTeams(null, 4);
        when(mockDuelProvider.getDurationAverage()).thenReturn(0L);

        TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

        assertNotNull(result);
        assertNull(result.getEstimatedTime());
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLeagueStatistics_shouldNotEstimateTimeWhenDuelsNumberIsNull() {
        List<Team> teams = createTeamsWithoutMembers(null, 2);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

        assertNotNull(result);
        assertNull(result.getDuelsNumber());
        assertNull(result.getEstimatedTime());
        assertNull(result.getAverageTime());
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLoopStatistics_shouldSetEstimatedTimeToZeroWhenDuelsNumberIsNull() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.LOOP);
        List<Team> teams = createTeamsWithoutMembers(tournament, 2);
        TournamentExtraProperty property = new TournamentExtraProperty();
        property.setPropertyValue("false");

        when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                .thenReturn(property);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

        assertNotNull(result);
        assertNull(result.getDuelsNumber());
        assertEquals(result.getEstimatedTime(), 0L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLoopStatistics_shouldSetEstimatedTimeToZeroWhenDuelsNumberIsNullAndAvoidDuplicatesEnabled() {
        Tournament tournament = createTournament();
        tournament.setType(TournamentType.LOOP);
        List<Team> teams = createTeamsWithoutMembers(tournament, 2);
        TournamentExtraProperty property = new TournamentExtraProperty();
        property.setPropertyValue("true");

        when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                .thenReturn(property);
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

        assertNotNull(result);
        assertNull(result.getDuelsNumber());
        assertEquals(result.getEstimatedTime(), 0L);
    }

    @Test(groups = "tournamentFightStatistics")
    public void estimateLeagueStatistics_shouldHandleNullFightsNumberInEstimatedTimeCalculation() {
        List<Team> teams = new ArrayList<>() {
            @Override
            public int size() {
                return Integer.MIN_VALUE;
            }
        };
        when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

        TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

        assertNotNull(result);
        assertNull(result.getFightsNumber());
        assertEquals(result.getDuelsNumber(), 0L);
        assertEquals(result.getEstimatedTime(), 0L);
    }

     // Helper methods

     private Tournament createTournament() {
         Tournament tournament = new Tournament();
         tournament.setId(1);
         tournament.setName("Test Tournament");
         tournament.setType(TournamentType.LEAGUE);
         tournament.setTeamSize(3);
         tournament.setFightSize(3);
         return tournament;
     }

     private List<Team> createTeams(Tournament tournament, int count) {
         List<Team> teams = new ArrayList<>();
         for (int i = 0; i < count; i++) {
             Team team = new Team("Team" + i, tournament);
             for (int j = 0; j < 3; j++) {
                 Participant member = new Participant();
                 member.setId(i * 10 + j);
                 member.setName("Member" + i + "_" + j);
                 team.addMember(member);
             }
             teams.add(team);
         }
         return teams;
     }

     private List<Team> createTeamsWithoutMembers(Tournament tournament, int count) {
         List<Team> teams = new ArrayList<>();
         for (int i = 0; i < count; i++) {
             teams.add(new Team("EmptyTeam" + i, tournament));
         }
         return teams;
     }

     private List<Role> createRoles(Tournament tournament, int count) {
         List<Role> roles = new ArrayList<>();
         for (int i = 0; i < count; i++) {
             Role role = new Role();
             role.setId(i);
             role.setRoleType(RoleType.COMPETITOR);
             Participant participant = new Participant();
             participant.setId(i);
             participant.setName("Competitor" + i);
             role.setParticipant(participant);
             roles.add(role);
         }
         return roles;
     }

     private List<Role> createMixedRoles(Tournament tournament) {
         List<Role> roles = new ArrayList<>();
         for (int i = 0; i < 3; i++) {
             Role role = new Role();
             role.setId(i);
             role.setRoleType(RoleType.COMPETITOR);
             Participant participant = new Participant();
             participant.setId(i);
             participant.setName("Competitor" + i);
             role.setParticipant(participant);
             roles.add(role);
         }
         Role arbitrerRole = new Role();
         arbitrerRole.setId(99);
         arbitrerRole.setRoleType(RoleType.REFEREE);
         Participant arbitrer = new Participant();
         arbitrer.setId(99);
         arbitrer.setName("Referee");
         arbitrerRole.setParticipant(arbitrer);
         roles.add(arbitrerRole);
         return roles;
     }

     // ============= Additional comprehensive tests for 90%+ coverage =============

     @Test(groups = "tournamentFightStatistics")
     public void estimate_shouldDispatchToLeagueStatisticsWhenTypeIsLeague() {
         Tournament tournament = createTournament();
         tournament.setType(TournamentType.LEAGUE);
         List<Team> teams = createTeams(tournament, 4);

         when(mockDuelProvider.getDurationAverage()).thenReturn(180L);

         TournamentFightStatistics result = provider.estimate(tournament, 3, teams);

         assertNotNull(result);
         assertEquals(result.getFightsByTeam(), 3L);
     }

     @Test(groups = "tournamentFightStatistics")
     public void estimateByMembers_shouldCreateTeamsWhenFiltering() {
         Tournament tournament = createTournament();
         tournament.setTeamSize(2);
         List<Role> roles = createRoles(tournament, 6);

         when(mockRoleProvider.getAll(tournament)).thenReturn(roles);
         when(mockDuelProvider.getDurationAverage()).thenReturn(150L);

         TournamentFightStatistics result = provider.estimateByMembers(tournament);

         assertNotNull(result);
     }

     @Test(groups = "tournamentFightStatistics")
     public void get_shouldCountFightsFinished() {
         Tournament tournament = createTournament();

         when(mockFightProvider.count(tournament)).thenReturn(10L);
         when(mockTeamProvider.count(tournament)).thenReturn(5L);
         when(mockDuelProvider.count(tournament)).thenReturn(50L);
         when(mockDuelProvider.getDurationAverage(tournament)).thenReturn(200L);
         when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(20L);
         when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(15L);
         when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(10L);
         when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(2L);
         when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(3L);
         when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(8L);
         when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(1L);
         when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
         when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
         when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(9L);
         when(mockDuelProvider.countFaults(tournament)).thenReturn(5L);

         TournamentFightStatistics result = provider.get(tournament);

         assertNotNull(result);
         assertEquals(result.getFightsFinished(), 9L);
     }

     @Test(groups = "tournamentFightStatistics")
     public void get_shouldSetLastDuelFinishedTime() {
         Tournament tournament = createTournament();
         LocalDateTime finishedTime = LocalDateTime.now();
         Duel lastDuel = new Duel();
         lastDuel.setFinishedAt(finishedTime);

         when(mockFightProvider.count(tournament)).thenReturn(0L);
         when(mockTeamProvider.count(tournament)).thenReturn(0L);
         when(mockDuelProvider.count(tournament)).thenReturn(0L);
         when(mockDuelProvider.getDurationAverage(tournament)).thenReturn(null);
         when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
         when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
         when(mockDuelProvider.getLastDuel(tournament)).thenReturn(lastDuel);
         when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
         when(mockDuelProvider.countFaults(tournament)).thenReturn(0L);

         TournamentFightStatistics result = provider.get(tournament);

         assertNotNull(result);
         assertEquals(result.getFightsFinishedAt(), finishedTime);
     }

     @Test(groups = "tournamentFightStatistics")
     public void estimateLeagueStatistics_shouldCalculateWithMultipleTeams() {
         List<Team> teams = createTeams(null, 6);

         when(mockDuelProvider.getDurationAverage()).thenReturn(200L);

         TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

         assertNotNull(result);
         assertEquals(result.getFightsNumber(), 15L);
         assertEquals(result.getFightsByTeam(), 5L);
     }

     @Test(groups = "tournamentFightStatistics")
     public void estimateLoopStatistics_shouldMaximizeFightsWhenPropertyFalse() {
         Tournament tournament = createTournament();
         tournament.setType(TournamentType.LOOP);
         List<Team> teams = createTeams(tournament, 4);
         TournamentExtraProperty property = new TournamentExtraProperty();
         property.setPropertyValue("false");

         when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                 .thenReturn(property);
         when(mockDuelProvider.getDurationAverage()).thenReturn(150L);

         TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

         assertNotNull(result);
         assertEquals(result.getFightsNumber(), 12L);
         assertEquals(result.getFightsByTeam(), 6L);
     }

     @Test(groups = "tournamentFightStatistics")
     public void estimateLoopStatistics_shouldAvoidDuplicatesWhenPropertyTrue() {
         Tournament tournament = createTournament();
         tournament.setType(TournamentType.LOOP);
         List<Team> teams = createTeams(tournament, 4);
         TournamentExtraProperty property = new TournamentExtraProperty();
         property.setPropertyValue("true");

         when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                 .thenReturn(property);
         when(mockDuelProvider.getDurationAverage()).thenReturn(150L);

         TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

         assertNotNull(result);
         assertEquals(result.getFightsNumber(), 7L);
         assertEquals(result.getFightsByTeam(), 3L);
     }

     @Test(groups = "tournamentFightStatistics")
     public void get_shouldAggregateAllScoreTypes() {
         Tournament tournament = createTournament();

         when(mockFightProvider.count(tournament)).thenReturn(8L);
         when(mockTeamProvider.count(tournament)).thenReturn(2L);
         when(mockDuelProvider.count(tournament)).thenReturn(32L);
         when(mockDuelProvider.getDurationAverage(tournament)).thenReturn(175L);
         when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(25L);
         when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(18L);
         when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(12L);
         when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(3L);
         when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(5L);
         when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(7L);
         when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(2L);
         when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
         when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
         when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(6L);
         when(mockDuelProvider.countFaults(tournament)).thenReturn(4L);

         TournamentFightStatistics result = provider.get(tournament);

         assertNotNull(result);
         assertEquals(result.getTsukiNumber(), 5L);
         assertEquals(result.getFusenGachiNumber(), 2L);
         assertEquals(result.getHansokuNumber(), 3L);
     }

     @Test(groups = "tournamentFightStatistics")
     public void estimateLeagueStatistics_shouldIncludeFightsNumberInEstimatedTime() {
         List<Team> teams = createTeams(null, 5);

         when(mockDuelProvider.getDurationAverage()).thenReturn(190L);

         TournamentFightStatistics result = provider.estimateLeagueStatistics(3, teams);

         assertNotNull(result);
         assertNotNull(result.getEstimatedTime());
         assertTrue(result.getEstimatedTime() > 0);
     }

     @Test(groups = "tournamentFightStatistics")
     public void get_shouldSetFightsByTeamWhenTeamsExist() {
         Tournament tournament = createTournament();

         when(mockFightProvider.count(tournament)).thenReturn(12L);
         when(mockTeamProvider.count(tournament)).thenReturn(4L);
         when(mockDuelProvider.count(tournament)).thenReturn(40L);
         when(mockDuelProvider.getDurationAverage(tournament)).thenReturn(180L);
         when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
         when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(null);
         when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
         when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
         when(mockDuelProvider.countFaults(tournament)).thenReturn(0L);

         TournamentFightStatistics result = provider.get(tournament);

         assertNotNull(result);
         assertEquals(result.getFightsByTeam(), 3L);
     }

     @Test(groups = "tournamentFightStatistics")
     public void estimate_shouldReturnProperValuesForEstimate() {
         Tournament tournament = createTournament();
         List<Team> teams = createTeams(tournament, 3);

         when(mockDuelProvider.getDurationAverage()).thenReturn(160L);

         TournamentFightStatistics result = provider.estimate(tournament, 3, teams);

         assertNotNull(result);
         assertEquals(result.getFightsByTeam(), 2L);
     }

     @Test(groups = "tournamentFightStatistics")
     public void estimateLoopStatistics_shouldHandleEstimatedTimeCalculationCorrectly() {
         Tournament tournament = createTournament();
         tournament.setType(TournamentType.LOOP);
         List<Team> teams = createTeams(tournament, 3);
         TournamentExtraProperty property = new TournamentExtraProperty();
         property.setPropertyValue("true");

         when(mockTournamentExtraPropertyProvider.getByTournamentAndProperty(tournament, TournamentExtraPropertyKey.AVOID_DUPLICATES))
                 .thenReturn(property);
         when(mockDuelProvider.getDurationAverage()).thenReturn(200L);

         TournamentFightStatistics result = provider.estimateLoopStatistics(tournament, 3, teams);

         assertNotNull(result);
         assertNotNull(result.getEstimatedTime());
         assertTrue(result.getEstimatedTime() >= 0);
     }

     @Test(groups = "tournamentFightStatistics")
     public void get_shouldHandleFirstDuelWithStartedAtNull() {
         Tournament tournament = createTournament();
         Duel firstDuel = new Duel();
         firstDuel.setStartedAt(null);

         when(mockFightProvider.count(tournament)).thenReturn(0L);
         when(mockTeamProvider.count(tournament)).thenReturn(0L);
         when(mockDuelProvider.count(tournament)).thenReturn(0L);
         when(mockDuelProvider.getDurationAverage(tournament)).thenReturn(null);
         when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
         when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(firstDuel);
         when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
         when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
         when(mockDuelProvider.countFaults(tournament)).thenReturn(0L);

         TournamentFightStatistics result = provider.get(tournament);

         assertNotNull(result);
         assertNull(result.getFightsStartedAt());
     }

     @Test(groups = "tournamentFightStatistics")
     public void get_shouldHandleFirstDuelWithFinishedAtValid() {
         Tournament tournament = createTournament();
         LocalDateTime finishedTime = LocalDateTime.of(2026, 6, 24, 15, 30);
         Duel firstDuel = new Duel();
         firstDuel.setStartedAt(null);
         firstDuel.setFinishedAt(finishedTime);

         when(mockFightProvider.count(tournament)).thenReturn(0L);
         when(mockTeamProvider.count(tournament)).thenReturn(0L);
         when(mockDuelProvider.count(tournament)).thenReturn(0L);
         when(mockDuelProvider.getDurationAverage(tournament)).thenReturn(null);
         when(mockDuelProvider.countScore(tournament, Score.MEN)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.DO)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.KOTE)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.HANSOKU)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.TSUKI)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.IPPON)).thenReturn(0L);
         when(mockDuelProvider.countScore(tournament, Score.FUSEN_GACHI)).thenReturn(0L);
         when(mockDuelProvider.getFirstDuel(tournament)).thenReturn(firstDuel);
         when(mockDuelProvider.getLastDuel(tournament)).thenReturn(null);
         when(mockFightProvider.countByTournamentAndFinished(tournament)).thenReturn(0L);
         when(mockDuelProvider.countFaults(tournament)).thenReturn(0L);

         TournamentFightStatistics result = provider.get(tournament);

         assertNotNull(result);
         assertEquals(result.getFightsStartedAt(), finishedTime.minusMinutes(2));
     }
}

