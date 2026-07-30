package com.softwaremagico.kt.core.converters;

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
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantReducedDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.models.AchievementConverterRequest;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import org.hibernate.LazyInitializationException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

@Test(groups = "achievementConverter")
public class AchievementConverterTest {

    @Mock
    private TournamentConverter mockTournamentConverter;

    @Mock
    private TournamentRepository mockTournamentRepository;

    @Mock
    private ParticipantReducedConverter mockParticipantReducedConverter;

    @Mock
    private ParticipantConverter mockParticipantConverter;

    @Mock
    private ParticipantProvider mockParticipantProvider;

    private AchievementConverter converter;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new AchievementConverter(mockTournamentConverter, mockTournamentRepository,
                mockParticipantReducedConverter, mockParticipantConverter, mockParticipantProvider);
    }

    private Achievement newAchievement() {
        final Achievement achievement = new Achievement();
        final Tournament tournament = new Tournament();
        tournament.setId(5);
        final Participant participant = new Participant("ID1", "John", "Doe", null);
        participant.setId(9);
        achievement.setTournament(tournament);
        achievement.setParticipant(participant);
        return achievement;
    }

    @Test
    public void convert_withNormalAccess_expectDirectConversion() {
        final Achievement achievement = newAchievement();
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final ParticipantReducedDTO participantDTO = new ParticipantReducedDTO();

        when(mockTournamentConverter.convert(any())).thenReturn(tournamentDTO);
        when(mockParticipantReducedConverter.convert(any())).thenReturn(participantDTO);

        final AchievementDTO result = converter.convert(new AchievementConverterRequest(achievement));

        assertSame(result.getTournament(), tournamentDTO);
        assertSame(result.getParticipant(), participantDTO);
    }

    @Test
    public void convert_withLazyTournament_expectFallbackToRepository() {
        final Achievement achievement = newAchievement();
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final ParticipantReducedDTO participantDTO = new ParticipantReducedDTO();
        final Tournament reloadedTournament = new Tournament();
        reloadedTournament.setId(5);

        when(mockTournamentConverter.convert(any()))
                .thenThrow(new LazyInitializationException("lazy tournament"))
                .thenReturn(tournamentDTO);
        when(mockTournamentRepository.findById(5)).thenReturn(Optional.of(reloadedTournament));
        when(mockParticipantReducedConverter.convert(any())).thenReturn(participantDTO);

        final AchievementDTO result = converter.convert(new AchievementConverterRequest(achievement));

        assertSame(result.getTournament(), tournamentDTO);
    }

    @Test
    public void convert_withLazyParticipant_expectFallbackToProvider() {
        final Achievement achievement = newAchievement();
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final ParticipantReducedDTO participantDTO = new ParticipantReducedDTO();
        final Participant reloadedParticipant = new Participant("ID1", "John", "Doe", null);
        reloadedParticipant.setId(9);

        when(mockTournamentConverter.convert(any())).thenReturn(tournamentDTO);
        when(mockParticipantReducedConverter.convert(any()))
                .thenThrow(new LazyInitializationException("lazy participant"))
                .thenReturn(participantDTO);
        when(mockParticipantProvider.get(9)).thenReturn(Optional.of(reloadedParticipant));

        final AchievementDTO result = converter.convert(new AchievementConverterRequest(achievement));

        assertSame(result.getParticipant(), participantDTO);
    }

    @Test
    public void reverse_withNull_expectNull() {
        assertNull(converter.reverse(null));
    }

    @Test
    public void reverse_expectParticipantAndTournamentReversed() {
        final AchievementDTO dto = new AchievementDTO();
        final ParticipantDTO participantDTO = new ParticipantReducedDTO();
        final TournamentDTO tournamentDTO = new TournamentDTO();
        dto.setParticipant(participantDTO);
        dto.setTournament(tournamentDTO);

        final Participant participant = new Participant("ID1", "John", "Doe", null);
        final Tournament tournament = new Tournament();
        when(mockParticipantConverter.reverse(participantDTO)).thenReturn(participant);
        when(mockTournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);

        final Achievement result = converter.reverse(dto);

        assertSame(result.getParticipant(), participant);
        assertSame(result.getTournament(), tournament);
    }
}

