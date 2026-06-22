package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.AchievementConverter;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.AchievementProvider;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.BubbleSortTournamentHandler;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@Test(groups = "roleAchievementTests")
public class AchievementControllerTest {

    @Mock private AchievementProvider provider;
    @Mock private AchievementConverter converter;
    @Mock private TournamentConverter tournamentConverter;
    @Mock private TournamentProvider tournamentProvider;
    @Mock private ParticipantProvider participantProvider;
    @Mock private ParticipantConverter participantConverter;
    @Mock private RoleProvider roleProvider;
    @Mock private TeamProvider teamProvider;
    @Mock private AchievementProvider achievementProvider;
    @Mock private FightProvider fightProvider;
    @Mock private DuelProvider duelProvider;
    @Mock private RankingProvider rankingProvider;
    @Mock private GroupProvider groupProvider;
    @Mock private BubbleSortTournamentHandler bubbleSortTournamentHandler;
    @Mock private SenbatsuTournamentHandler senbatsuTournamentHandler;

    private AchievementController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AchievementController(provider, converter, tournamentConverter, tournamentProvider, participantProvider,
                participantConverter, roleProvider, teamProvider, achievementProvider, fightProvider, duelProvider,
                rankingProvider, groupProvider, bubbleSortTournamentHandler, senbatsuTournamentHandler);
    }

    @Test
    public void shouldGetParticipantAchievementsById() {
        final Participant participant = new Participant();
        participant.setId(7);
        final Tournament tournament = tournament(1, LocalDateTime.now().minusDays(1));
        final Achievement achievement = new Achievement(participant, tournament, AchievementType.BILLY_THE_KID, AchievementGrade.NORMAL);
        final AchievementDTO achievementDTO = new AchievementDTO();

        when(participantProvider.get(7)).thenReturn(Optional.of(participant));
        when(provider.get(participant)).thenReturn(List.of(achievement));
        when(converter.convertAll(any())).thenReturn(List.of(achievementDTO));

        final List<AchievementDTO> result = controller.getParticipantAchievements(7);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), achievementDTO);
    }

    @Test(expectedExceptions = ParticipantNotFoundException.class)
    public void shouldThrowWhenParticipantDoesNotExist() {
        when(participantProvider.get(99)).thenReturn(Optional.empty());

        controller.getParticipantAchievements(99);
    }

    @Test
    public void shouldRegenerateAchievementsDeleteThenGenerateAndNotify() {
        final AchievementController spyController = spy(controller);
        final Tournament tournament = tournament(10, LocalDateTime.now().minusDays(5));
        final TournamentDTO tournamentDTO = tournamentDTO(10, tournament.getCreatedAt());
        final List<AchievementDTO> generated = List.of(new AchievementDTO());
        final List<AchievementDTO> notified = new ArrayList<>();
        final List<Integer> notifiedTournamentId = new ArrayList<>();

        when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
        doReturn(generated).when(spyController).generateAchievements(tournamentDTO);
        spyController.addAchievementsGeneratedListener((achievementsGenerated, t) -> {
            notified.addAll(achievementsGenerated);
            notifiedTournamentId.add(t.getId());
        });

        final List<AchievementDTO> result = spyController.regenerateAchievements(tournamentDTO);

        verify(provider).delete(tournament);
        assertEquals(result.size(), 1);
        assertEquals(notified.size(), 1);
        assertEquals(notifiedTournamentId, List.of(10));
    }

    @Test
    public void shouldRegenerateAllAchievementsSortedByCreatedAtThenIdAndNotify() {
        final AchievementController spyController = spy(controller);
        final TournamentDTO t1 = tournamentDTO(2, LocalDateTime.of(2024, 1, 1, 0, 0));
        final TournamentDTO t2 = tournamentDTO(1, LocalDateTime.of(2024, 1, 1, 0, 0));
        final TournamentDTO t3 = tournamentDTO(3, LocalDateTime.of(2025, 1, 1, 0, 0));
        final List<Integer> generationOrder = new ArrayList<>();
        final List<Integer> listenerTournamentIds = new ArrayList<>();

        when(tournamentProvider.getAll()).thenReturn(List.of(tournament(2, t1.getCreatedAt()), tournament(1, t2.getCreatedAt()), tournament(3, t3.getCreatedAt())));
        when(tournamentConverter.convertAll(any())).thenReturn(new ArrayList<>(List.of(t1, t2, t3)));

        doAnswer(invocation -> {
            TournamentDTO dto = invocation.getArgument(0);
            generationOrder.add(dto.getId());
            AchievementDTO achievementDTO = new AchievementDTO();
            achievementDTO.setId(dto.getId());
            return List.of(achievementDTO);
        }).when(spyController).generateAchievements(any(TournamentDTO.class));

        spyController.addAchievementsGeneratedAllTournamentsListener((achievementsGenerated, tournaments) ->
                tournaments.forEach(t -> listenerTournamentIds.add(t.getId())));

        final List<AchievementDTO> all = spyController.regenerateAllAchievements();

        verify(achievementProvider).deleteAll();
        assertEquals(generationOrder, List.of(1, 2, 3));
        assertEquals(listenerTournamentIds, List.of(1, 2, 3));
        assertEquals(all.size(), 3);
        assertNotNull(all.get(0));
    }

    @Test
    public void shouldRegenerateAchievementsByTournamentId() {
        final AchievementController spyController = spy(controller);
        final Tournament tournament = tournament(33, LocalDateTime.now().minusDays(10));
        final TournamentDTO tournamentDTO = tournamentDTO(33, tournament.getCreatedAt());

        when(tournamentProvider.get(33)).thenReturn(Optional.of(tournament));
        when(tournamentConverter.convert(any())).thenReturn(tournamentDTO);
        doReturn(List.of(new AchievementDTO())).when(spyController).regenerateAchievements(tournamentDTO);

        final List<AchievementDTO> result = spyController.regenerateAchievements(33);

        assertEquals(result.size(), 1);
        verify(tournamentProvider).get(33);
    }

    @Test(expectedExceptions = TournamentNotFoundException.class)
    public void shouldThrowOnRegenerateUnknownTournamentId() {
        when(tournamentProvider.get(404)).thenReturn(Optional.empty());
        controller.regenerateAchievements(404);
    }

    @Test
    public void shouldGetTournamentAchievementsAndCounters() {
        final Tournament tournament = tournament(55, LocalDateTime.now().minusDays(2));
        final AchievementDTO dto = new AchievementDTO();
        final Map<AchievementType, Map<AchievementGrade, Integer>> counters = new HashMap<>();
        counters.put(AchievementType.BILLY_THE_KID, Collections.singletonMap(AchievementGrade.NORMAL, 2));

        when(tournamentProvider.get(55)).thenReturn(Optional.of(tournament));
        when(provider.get(tournament)).thenReturn(List.of(new Achievement()));
        when(converter.convertAll(any())).thenReturn(List.of(dto));
        when(provider.getAchievementsCount()).thenReturn(counters);
        when(provider.countAchievements(AchievementType.BILLY_THE_KID)).thenReturn(2);

        final List<AchievementDTO> achievements = controller.getTournamentAchievements(55);

        assertEquals(achievements.size(), 1);
        assertSame(achievements.get(0), dto);
        assertFalse(controller.getAchievementsCount().isEmpty());
        assertEquals(controller.countAchievements(AchievementType.BILLY_THE_KID), 2);
    }

    @Test(expectedExceptions = TournamentNotFoundException.class)
    public void shouldThrowWhenTournamentAchievementsNotFound() {
        when(tournamentProvider.get(808)).thenReturn(Optional.empty());
        controller.getTournamentAchievements(808);
    }

    private Tournament tournament(int id, LocalDateTime createdAt) {
        final Tournament tournament = new Tournament("Tournament", 1, 1, TournamentType.LEAGUE, "tester");
        tournament.setId(id);
        tournament.setCreatedAt(createdAt);
        return tournament;
    }

    private TournamentDTO tournamentDTO(int id, LocalDateTime createdAt) {
        final TournamentDTO tournamentDTO = new TournamentDTO("Tournament", 1, 1, TournamentType.LEAGUE);
        tournamentDTO.setId(id);
        tournamentDTO.setCreatedAt(createdAt);
        return tournamentDTO;
    }
}



