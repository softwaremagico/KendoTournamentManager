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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

@Test(groups = {"scoreTests"})
public class DuelControllerTest {

    @Mock
    private DuelProvider duelProvider;
    @Mock
    private DuelConverter duelConverter;
    @Mock
    private TournamentConverter tournamentConverter;
    @Mock
    private FightProvider fightProvider;
    @Mock
    private FightConverter fightConverter;
    @Mock
    private TournamentProvider tournamentProvider;
    @Mock
    private ParticipantProvider participantProvider;

    private DuelController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = org.mockito.Mockito.spy(new DuelController(duelProvider, duelConverter, tournamentConverter,
                fightProvider, fightConverter, tournamentProvider, participantProvider));
    }

    private TournamentDTO tournamentDTO() {
        final TournamentDTO dto = new TournamentDTO();
        dto.setId(1);
        dto.setType(TournamentType.LEAGUE);
        return dto;
    }

    private DuelDTO validDuelDTO() {
        final DuelDTO dto = new DuelDTO();
        dto.setTournament(tournamentDTO());
        dto.setCompetitor1Score(new ArrayList<>(List.of(Score.MEN)));
        dto.setCompetitor2Score(new ArrayList<>(List.of(Score.KOTE)));
        return dto;
    }

    private Participant participant(String idCard, String name, String lastname) {
        return new Participant(idCard, name, lastname, new Club("Club", "ES", "Madrid"));
    }

    // --- Listener registration ---

    @Test
    public void shouldRegisterShiaijoFinishedListener() {
        DuelController.ShiaijoFinishedListener listener = (tournament, shiaijo) -> { };
        controller.addShiaijoFinishedListener(listener);
        assertTrue(true); // no exception = registered
    }

    @Test
    public void shouldRegisterFightUpdatedListener() {
        DuelController.FightUpdatedListener listener = (tournament, fight, duel, actor, session) -> { };
        controller.addFightUpdatedListener(listener);
        assertTrue(true);
    }

    @Test
    public void shouldRegisterUntieUpdatedListener() {
        DuelController.UntieUpdatedListener listener = (tournament, duel, actor, session) -> { };
        controller.addUntieUpdatedListener(listener);
        assertTrue(true);
    }

    // --- validate ---

    @Test
    public void shouldPassValidationForCleanScores() {
        final DuelDTO dto = validDuelDTO();
        // No exception expected
        controller.validate(dto);
    }

    @Test
    public void shouldThrowValidationExceptionWhenCompetitor1HasEmptyScore() {
        final DuelDTO dto = validDuelDTO();
        dto.setCompetitor1Score(new ArrayList<>(List.of(Score.EMPTY)));

        assertThrows(ValidateBadRequestException.class, () -> controller.validate(dto));
    }

    @Test
    public void shouldThrowValidationExceptionWhenCompetitor1HasDrawScore() {
        final DuelDTO dto = validDuelDTO();
        dto.setCompetitor1Score(new ArrayList<>(List.of(Score.DRAW)));

        assertThrows(ValidateBadRequestException.class, () -> controller.validate(dto));
    }

    @Test
    public void shouldThrowValidationExceptionWhenCompetitor1HasFaultScore() {
        final DuelDTO dto = validDuelDTO();
        dto.setCompetitor1Score(new ArrayList<>(List.of(Score.FAULT)));

        assertThrows(ValidateBadRequestException.class, () -> controller.validate(dto));
    }

    @Test
    public void shouldThrowValidationExceptionWhenCompetitor2HasEmptyScore() {
        final DuelDTO dto = validDuelDTO();
        dto.setCompetitor2Score(new ArrayList<>(List.of(Score.EMPTY)));

        assertThrows(ValidateBadRequestException.class, () -> controller.validate(dto));
    }

    @Test
    public void shouldThrowValidationExceptionWhenCompetitor2HasDrawScore() {
        final DuelDTO dto = validDuelDTO();
        dto.setCompetitor2Score(new ArrayList<>(List.of(Score.DRAW)));

        assertThrows(ValidateBadRequestException.class, () -> controller.validate(dto));
    }

    @Test
    public void shouldThrowValidationExceptionWhenCompetitor2HasFaultScore() {
        final DuelDTO dto = validDuelDTO();
        dto.setCompetitor2Score(new ArrayList<>(List.of(Score.FAULT)));

        assertThrows(ValidateBadRequestException.class, () -> controller.validate(dto));
    }

    // --- count ---

    @Test
    public void shouldCountDuelsForTournament() {
        final TournamentDTO dto = tournamentDTO();
        final Tournament tournament = new Tournament();
        tournament.setId(1);

        when(tournamentConverter.reverse(dto)).thenReturn(tournament);
        when(duelProvider.count(tournament)).thenReturn(7L);

        final long result = controller.count(dto);
        assertEquals(result, 7L);
        verify(duelProvider).count(tournament);
    }

    // --- delete ---

    @Test
    public void shouldDeleteDuelsForTournament() {
        final TournamentDTO dto = tournamentDTO();
        final Tournament tournament = new Tournament();
        tournament.setId(1);

        when(tournamentConverter.reverse(dto)).thenReturn(tournament);

        controller.delete(dto);
        verify(duelProvider).delete(tournament);
    }

    // --- getUntiesFromGroup ---

    @Test
    public void shouldGetUntiesFromGroup() {
        when(duelProvider.getUntiesFromGroup(5)).thenReturn(List.of(new Duel()));
        doReturn(List.of(new DuelDTO())).when(controller).convertAll(any());

        final List<DuelDTO> result = controller.getUntiesFromGroup(5);
        assertNotNull(result);
        verify(duelProvider).getUntiesFromGroup(5);
    }

    // --- getUntiesFromTournament ---

    @Test
    public void shouldGetUntiesFromTournament() {
        when(duelProvider.getUntiesFromTournament(1)).thenReturn(List.of(new Duel()));
        doReturn(List.of(new DuelDTO())).when(controller).convertAll(any());

        final List<DuelDTO> result = controller.getUntiesFromTournament(1);
        assertNotNull(result);
        verify(duelProvider).getUntiesFromTournament(1);
    }

    // --- getUntiesFromParticipant ---

    @Test
    public void shouldGetUntiesFromParticipant() {
        final Participant participant = participant("ID-3", "Taro", "Yamada");
        participant.setId(3);

        when(participantProvider.get(3)).thenReturn(Optional.of(participant));
        when(duelProvider.getUntiesFromParticipant(participant)).thenReturn(List.of(new Duel()));
        doReturn(List.of(new DuelDTO())).when(controller).convertAll(any());

        final List<DuelDTO> result = controller.getUntiesFromParticipant(3);
        assertNotNull(result);
        verify(participantProvider).get(3);
        verify(duelProvider).getUntiesFromParticipant(participant);
    }

    @Test
    public void shouldThrowWhenParticipantNotFoundForUnties() {
        when(participantProvider.get(99)).thenReturn(Optional.empty());

        assertThrows(ParticipantNotFoundException.class, () -> controller.getUntiesFromParticipant(99));
    }

    // --- getBy(Integer) ---

    @Test
    public void shouldGetDuelsByParticipantId() {
        final Participant participant = participant("ID-2", "Jiro", "Suzuki");
        participant.setId(2);

        when(participantProvider.get(2)).thenReturn(Optional.of(participant));
        when(duelProvider.get(participant)).thenReturn(List.of(new Duel()));
        doReturn(List.of(new DuelDTO())).when(controller).convertAll(any());

        final List<DuelDTO> result = controller.getBy(2);
        assertNotNull(result);
        verify(participantProvider).get(2);
    }

    @Test
    public void shouldThrowWhenParticipantNotFoundForGetBy() {
        when(participantProvider.get(99)).thenReturn(Optional.empty());

        assertThrows(ParticipantNotFoundException.class, () -> controller.getBy(99));
    }

    // --- getBy(Participant) ---

    @Test
    public void shouldGetDuelsByParticipant() {
        final Participant participant = participant("ID-9", "Kenji", "Tanaka");

        when(duelProvider.get(participant)).thenReturn(List.of(new Duel()));
        doReturn(List.of(new DuelDTO())).when(controller).convertAll(any());

        final List<DuelDTO> result = controller.getBy(participant);
        assertNotNull(result);
        verify(duelProvider).get(participant);
    }
}

