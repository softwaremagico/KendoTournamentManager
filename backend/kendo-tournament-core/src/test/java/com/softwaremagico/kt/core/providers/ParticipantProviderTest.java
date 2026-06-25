package com.softwaremagico.kt.core.providers;

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

import com.softwaremagico.kt.core.controller.models.TemporalToken;
import com.softwaremagico.kt.persistence.encryption.KeyProperty;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import com.softwaremagico.kt.persistence.values.Score;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"participantProviderTests"})
public class ParticipantProviderTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private DuelRepository duelRepository;

    private ParticipantProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new ParticipantProvider(participantRepository, duelRepository);
        // Reset static key for isolation between tests.
        new KeyProperty(null, null, null);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        new KeyProperty(null, null, null);
    }

    @Test
    public void shouldReturnEmptyWhenTemporalTokenIsNull() {
        assertThat(provider.findByTemporalToken(null)).isEmpty();
    }

    @Test
    public void shouldFindByTemporalTokenWhenProvided() {
        final Participant participant = participant(1, "A", "B");
        when(participantRepository.findByTemporalToken("tok")).thenReturn(Optional.of(participant));

        assertThat(provider.findByTemporalToken("tok")).contains(participant);
    }

    @Test
    public void shouldReturnEmptyForNullTokenUsername() {
        assertThat(provider.findByTokenUsername(null)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyForInvalidTokenUsernameWithoutSeparator() {
        assertThat(provider.findByTokenUsername("invalid")).isEmpty();
    }

    @Test
    public void shouldReturnEmptyForInvalidTokenUsernameNumberFormat() {
        assertThat(provider.findByTokenUsername("abc_name")).isEmpty();
    }

    @Test
    public void shouldFindByTokenUsernameWithValidPrefixId() {
        final Participant participant = participant(7, "Alice", "Smith");
        when(participantRepository.findById(7)).thenReturn(Optional.of(participant));

        assertThat(provider.findByTokenUsername("7_alice")).contains(participant);
    }

    @Test
    public void shouldUseRepositoryFindByIdCardWhenEncryptionKeyIsMissing() {
        final Participant participant = participant(10, "I", "Card");
        when(participantRepository.findByIdCard("ID-1")).thenReturn(Optional.of(participant));

        assertThat(provider.findByIdCard("ID-1")).contains(participant);
        verify(participantRepository).findByIdCard("ID-1");
    }

    @Test
    public void shouldUseRepositoryFindByIdCardWhenEncryptionKeyIsBlank() {
        new KeyProperty("   ", null, null);
        final Participant participant = participant(11, "Blank", "Key");
        when(participantRepository.findByIdCard("ID-2")).thenReturn(Optional.of(participant));

        assertThat(provider.findByIdCard("ID-2")).contains(participant);
        verify(participantRepository).findByIdCard("ID-2");
    }

    @Test
    public void shouldSearchInMemoryByIdCardWhenEncryptionKeyIsConfiguredAndFindMatch() {
        new KeyProperty("encryption-key", null, null);
        final Participant p1 = mock(Participant.class);
        final Participant p2 = mock(Participant.class);
        when(p1.getIdCard()).thenReturn("X-100");
        when(p2.getIdCard()).thenReturn("X-200");
        when(participantRepository.findAll()).thenReturn(List.of(p1, p2));

        assertThat(provider.findByIdCard("x-200")).contains(p2);
    }

    @Test
    public void shouldSearchInMemoryByIdCardWhenEncryptionKeyIsConfiguredAndReturnEmpty() {
        new KeyProperty("encryption-key", null, null);
        final Participant p1 = participant(1, "John", "A");
        p1.setIdCard("X-100");
        when(participantRepository.findAll()).thenReturn(List.of(p1));

        assertThat(provider.findByIdCard("X-999")).isEmpty();
    }

    @Test
    public void shouldIgnoreParticipantsWithNullIdCardWhenEncryptionKeyIsConfigured() {
        new KeyProperty("encryption-key", null, null);
        final Participant p1 = mock(Participant.class);
        when(p1.getIdCard()).thenReturn(null);
        when(participantRepository.findAll()).thenReturn(List.of(p1));

        assertThat(provider.findByIdCard("ANY")).isEmpty();
    }

    @Test
    public void shouldReturnEmptyWorstNightmareWhenSourceParticipantIsNull() {
        assertThat(provider.getYourWorstNightmare(null)).isEmpty();
    }

    @Test
    public void shouldReturnWorstNightmaresSortedWhenThereIsTie() {
        final Participant source = participant(1, "Src", "Fighter");
        final Participant pA = participant(2, "Anna", "Zulu");
        final Participant pB = participant(3, "Bruno", "Alpha");
        final Participant pC = participant(4, "Carl", "Bravo");

        final Duel d1 = duel(source, pA, 0, 2); // winner pA
        final Duel d2 = duel(source, pB, 0, 2); // winner pB
        final Duel d3 = duel(source, pA, 0, 2); // winner pA
        final Duel d4 = duel(source, pB, 0, 2); // winner pB
        final Duel d5 = duel(source, pC, 0, 2); // winner pC
        when(duelRepository.findByParticipant(source)).thenReturn(List.of(d1, d2, d3, d4, d5));

        final List<Participant> result = provider.getYourWorstNightmare(source);

        // pA and pB tie with 2 wins; sorted by lastname then name => Alpha(Bruno), Zulu(Anna)
        assertThat(result).containsExactly(pB, pA);
    }

    @Test
    public void shouldReturnEmptyWorstNightmareWhenNoOneBeatsSource() {
        final Participant source = participant(1, "Src", "Fighter");
        final Duel winBySource = duel(source, participant(2, "A", "B"), 2, 0);
        final Duel draw = duel(source, participant(3, "C", "D"), 1, 1);

        when(duelRepository.findByParticipant(source)).thenReturn(List.of(winBySource, draw));

        assertThat(provider.getYourWorstNightmare(source)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyYouAreWorstNightmareOfWhenSourceParticipantIsNull() {
        assertThat(provider.getYouAreTheWorstNightmareOf(null)).isEmpty();
    }

    @Test
    public void shouldReturnParticipantsYouBeatMostIncludingNullFilteringAndSorting() {
        final Participant source = participant(1, "Src", "Fighter");
        final Participant pA = participant(2, "Anna", "Zulu");
        final Participant pB = participant(3, "Bruno", "Alpha");

        final Duel d1 = duel(source, pA, 2, 0); // looser pA
        final Duel d2 = duel(source, pB, 2, 0); // looser pB
        final Duel d3 = duel(source, pA, 2, 0); // looser pA
        final Duel draw = duel(source, pB, 1, 1); // ignored
        when(duelRepository.findByParticipant(source)).thenReturn(List.of(d1, d2, d3, draw));

        final List<Participant> result = provider.getYouAreTheWorstNightmareOf(source);

        // pA has max wins against and is the only one selected.
        assertThat(result).containsExactly(pA);
    }

    @Test
    public void shouldReturnEmptyYouAreWorstNightmareOfWhenSourceNeverWins() {
        final Participant source = participant(1, "Src", "Fighter");
        final Duel loss = duel(source, participant(2, "A", "B"), 0, 2);
        final Duel draw = duel(source, participant(3, "C", "D"), 1, 1);

        when(duelRepository.findByParticipant(source)).thenReturn(List.of(loss, draw));

        assertThat(provider.getYouAreTheWorstNightmareOf(source)).isEmpty();
    }

    @Test
    public void shouldFilterOutNullLoserWhenSourceIsWinner() {
        final Participant source = participant(1, "Src", "Fighter");
        final Participant pA = participant(2, "Anna", "Zulu");

        final Duel winWithLoser = duel(source, pA, 2, 0);
        final Duel winWithNullLoser = duel(source, null, 2, 0);
        when(duelRepository.findByParticipant(source)).thenReturn(List.of(winWithLoser, winWithNullLoser));

        final List<Participant> result = provider.getYouAreTheWorstNightmareOf(source);

        assertThat(result).containsExactly(pA);
    }

    @Test
    public void shouldReturnParticipantsInOriginalOrderByIds() {
        final Participant p1 = participant(1, "A", "Z");
        final Participant p2 = participant(2, "B", "Y");
        final Participant p3 = participant(3, "C", "X");
        when(participantRepository.findAllById(List.of(3, 1, 2))).thenReturn(List.of(p1, p2, p3));

        final List<Participant> result = provider.getOriginalOrder(List.of(3, 1, 2));

        assertThat(result).containsExactly(p3, p1, p2);
    }

    @Test
    public void shouldGenerateTemporalTokenWithRetryOnCollision() {
        final Participant participant = participant(1, "Tok", "User");
        when(participantRepository.countByTemporalToken(anyString())).thenReturn(1L, 0L);
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final TemporalToken temporalToken = provider.generateTemporalToken(participant);

        assertThat(temporalToken).isNotNull();
        verify(participantRepository, times(2)).countByTemporalToken(anyString());
        verify(participantRepository).save(participant);
    }

    private Participant participant(int id, String name, String lastName) {
        final Participant participant = new Participant();
        participant.setId(id);
        participant.setName(name);
        participant.setLastname(lastName);
        participant.setCreatedBy("tester");
        return participant;
    }

    private Duel duel(Participant competitor1, Participant competitor2, int competitor1Points, int competitor2Points) {
        final Duel duel = new Duel();
        duel.setCompetitor1(competitor1);
        duel.setCompetitor2(competitor2);
        duel.setCompetitor1Fault(false);
        duel.setCompetitor2Fault(false);
        final List<Score> c1Scores = new ArrayList<>();
        final List<Score> c2Scores = new ArrayList<>();
        for (int i = 0; i < competitor1Points; i++) {
            c1Scores.add(Score.IPPON);
        }
        for (int i = 0; i < competitor2Points; i++) {
            c2Scores.add(Score.IPPON);
        }
        duel.setCompetitor1Score(c1Scores);
        duel.setCompetitor2Score(c2Scores);
        duel.setCompetitor1ScoreTime(new ArrayList<>());
        duel.setCompetitor2ScoreTime(new ArrayList<>());
        return duel;
    }
}





