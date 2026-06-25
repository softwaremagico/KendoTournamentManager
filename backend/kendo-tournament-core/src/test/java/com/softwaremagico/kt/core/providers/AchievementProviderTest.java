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

import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.AchievementRepository;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"achievementProviderTests"})
public class AchievementProviderTest {

    @Mock
    private AchievementRepository achievementRepository;

    private AchievementProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new AchievementProvider(achievementRepository);
    }

    @Test
    public void testGetWithNullGradeDelegatesToTournamentAndTypeQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Achievement achievement = achievement(participant("P1"), tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);
        when(achievementRepository.findByTournamentAndAchievementType(tournament, AchievementType.THE_WINNER)).thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(tournament, AchievementType.THE_WINNER, (AchievementGrade) null);

        assertThat(result).containsExactly(achievement);
    }

    @Test
    public void testGetWithSpecificGradeUsesFilteredQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Achievement achievement = achievement(participant("P1"), tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);
        when(achievementRepository.findByTournamentAndAchievementTypeAndAchievementGradeIn(
                org.mockito.ArgumentMatchers.eq(tournament), org.mockito.ArgumentMatchers.eq(AchievementType.THE_WINNER), anyCollection()))
                .thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);

        assertThat(result).containsExactly(achievement);
    }

    @Test
    public void testGetWithAchievementGradesNullOrEmptyReturnsEmptyList() {
        final Tournament tournament = tournament("Autumn Cup");

        final List<Achievement> resultWhenNull = provider.get(tournament, AchievementType.THE_WINNER, (Collection<AchievementGrade>) null);
        final List<Achievement> resultWhenEmpty = provider.get(tournament, AchievementType.THE_WINNER, List.of());

        assertThat(resultWhenNull).isEmpty();
        assertThat(resultWhenEmpty).isEmpty();
    }

    @Test
    public void testGetWithAchievementGradesNonEmptyDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup");
        final List<AchievementGrade> grades = List.of(AchievementGrade.BRONZE, AchievementGrade.GOLD);
        final Achievement achievement = achievement(participant("P1"), tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);

        when(achievementRepository.findByTournamentAndAchievementTypeAndAchievementGradeIn(
                tournament, AchievementType.THE_WINNER, grades)).thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(tournament, AchievementType.THE_WINNER, grades);

        assertThat(result).containsExactly(achievement);
    }

    @Test
    public void testGetAfterUsesSpecificGradeOrAllGradesDependingOnInput() {
        final Tournament tournament = tournament("Autumn Cup");
        final LocalDateTime after = LocalDateTime.now().minusDays(1);
        when(achievementRepository.findByTournamentAndAchievementTypeAndAchievementGradeInAndCreatedAtGreaterThanEqual(
                any(Tournament.class), any(AchievementType.class), any(Collection.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        provider.getAfter(tournament, AchievementType.THE_WINNER, AchievementGrade.SILVER, after);
        provider.getAfter(tournament, AchievementType.THE_WINNER, null, after);

        final ArgumentCaptor<Collection<AchievementGrade>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(achievementRepository, times(2)).findByTournamentAndAchievementTypeAndAchievementGradeInAndCreatedAtGreaterThanEqual(
                org.mockito.ArgumentMatchers.eq(tournament),
                org.mockito.ArgumentMatchers.eq(AchievementType.THE_WINNER),
                captor.capture(),
                org.mockito.ArgumentMatchers.eq(after));

        assertThat(captor.getAllValues().get(0)).containsExactlyElementsOf(AchievementGrade.SILVER.getGreaterEqualsThan());
        assertThat(captor.getAllValues().get(1)).containsExactly(AchievementGrade.values());
    }

    @Test
    public void testGetAchievementsCountDeduplicatesByUserTypeAndCountsByGrade() {
        final Tournament tournament = tournament("Autumn Cup");
        final Participant participant1 = participant("P1");
        final Participant participant2 = participant("P2");

        final Achievement duplicateA = achievement(participant1, tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);
        final Achievement duplicateB = achievement(participant1, tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);
        final Achievement other = achievement(participant2, tournament, AchievementType.THE_WINNER, AchievementGrade.BRONZE);

        when(achievementRepository.findAll()).thenReturn(List.of(duplicateA, duplicateB, other));

        final Map<AchievementType, Map<AchievementGrade, Integer>> counter = provider.getAchievementsCount();

        assertThat(counter).containsKey(AchievementType.THE_WINNER);
        assertThat(counter.get(AchievementType.THE_WINNER).get(AchievementGrade.GOLD)).isEqualTo(1);
        assertThat(counter.get(AchievementType.THE_WINNER).get(AchievementGrade.BRONZE)).isEqualTo(1);
    }

    // ===== add() tests =====

    @Test
    public void testAddCreatesAchievementWithParticipantTournamentAndType() {
        final Tournament tournament = tournament("Summer Cup");
        final Participant participant = participant("P3");
        final Achievement savedAchievement = achievement(participant, tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);

        when(achievementRepository.save(any(Achievement.class))).thenReturn(savedAchievement);

        final Achievement result = provider.add(participant, tournament, AchievementType.THE_WINNER);

        assertThat(result).isEqualTo(savedAchievement);
        verify(achievementRepository).save(any(Achievement.class));
    }

    // ===== get(Participant) tests =====

    @Test
    public void testGetByParticipantDelegatesToRepository() {
        final Participant participant = participant("P4");
        final Tournament tournament = tournament("Spring Cup");
        final Achievement achievement = achievement(participant, tournament, AchievementType.THE_WINNER, AchievementGrade.SILVER);

        when(achievementRepository.findByParticipant(participant)).thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(participant);

        assertThat(result).containsExactly(achievement);
        verify(achievementRepository).findByParticipant(participant);
    }

    @Test
    public void testGetByParticipantReturnsEmptyListWhenNoAchievements() {
        final Participant participant = participant("P5");

        when(achievementRepository.findByParticipant(participant)).thenReturn(List.of());

        final List<Achievement> result = provider.get(participant);

        assertThat(result).isEmpty();
    }

    // ===== get(Tournament, Participant) tests =====

    @Test
    public void testGetByTournamentAndParticipantDelegatesToRepository() {
        final Tournament tournament = tournament("Winter Cup");
        final Participant participant = participant("P6");
        final Achievement achievement = achievement(participant, tournament, AchievementType.THE_WINNER, AchievementGrade.BRONZE);

        when(achievementRepository.findByParticipantAndTournament(participant, tournament)).thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(tournament, participant);

        assertThat(result).containsExactly(achievement);
    }

    // ===== get(Tournament, AchievementType) tests =====

    @Test
    public void testGetByTournamentAndTypeDelegatesToRepository() {
        final Tournament tournament = tournament("Fall Cup");
        final Achievement achievement = achievement(participant("P7"), tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);

        when(achievementRepository.findByTournamentAndAchievementType(tournament, AchievementType.THE_WINNER))
                .thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(tournament, AchievementType.THE_WINNER);

        assertThat(result).containsExactly(achievement);
    }

    // ===== get(AchievementType) tests =====

    @Test
    public void testGetByAchievementTypeDelegatesToRepository() {
        final Achievement achievement1 = achievement(participant("P8"), tournament("Cup1"), AchievementType.BILLY_THE_KID, AchievementGrade.GOLD);
        final Achievement achievement2 = achievement(participant("P9"), tournament("Cup2"), AchievementType.BILLY_THE_KID, AchievementGrade.SILVER);

        when(achievementRepository.findByAchievementType(AchievementType.BILLY_THE_KID))
                .thenReturn(List.of(achievement1, achievement2));

        final List<Achievement> result = provider.get(AchievementType.BILLY_THE_KID);

        assertThat(result).containsExactly(achievement1, achievement2);
    }

    // ===== get(AchievementType, AchievementGrade) tests =====

    @Test
    public void testGetByTypeAndGradeDelegatesToRepository() {
        final Achievement achievement = achievement(participant("P10"), tournament("Cup3"), AchievementType.BILLY_THE_KID, AchievementGrade.GOLD);

        when(achievementRepository.findByAchievementTypeAndAchievementGradeIn(
                AchievementType.BILLY_THE_KID, Collections.singleton(AchievementGrade.GOLD)))
                .thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(AchievementType.BILLY_THE_KID, AchievementGrade.GOLD);

        assertThat(result).containsExactly(achievement);
    }

    // ===== get(AchievementType, Collection<AchievementGrade>) tests =====

    @Test
    public void testGetByTypeAndGradesDelegatesToRepository() {
        final List<AchievementGrade> grades = List.of(AchievementGrade.GOLD, AchievementGrade.SILVER);
        final Achievement achievement = achievement(participant("P11"), tournament("Cup4"), AchievementType.LETHAL_WEAPON, AchievementGrade.GOLD);

        when(achievementRepository.findByAchievementTypeAndAchievementGradeIn(AchievementType.LETHAL_WEAPON, grades))
                .thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(AchievementType.LETHAL_WEAPON, grades);

        assertThat(result).containsExactly(achievement);
    }

    // ===== get(AchievementType, Collection<AchievementGrade>, Collection<Participant>, Collection<Tournament>) tests =====

    @Test
    public void testGetByTypeGradesParticipantsAndTournamentsDelegatesToRepository() {
        final List<AchievementGrade> grades = List.of(AchievementGrade.GOLD);
        final Participant participant = participant("P12");
        final Tournament tournament = tournament("Cup5");
        final Achievement achievement = achievement(participant, tournament, AchievementType.TERMINATOR, AchievementGrade.GOLD);

        when(achievementRepository.findByAchievementTypeAndAchievementGradeInAndParticipantInAndTournamentIn(
                AchievementType.TERMINATOR, grades, Collections.singleton(participant), Collections.singleton(tournament)))
                .thenReturn(List.of(achievement));

        final List<Achievement> result = provider.get(AchievementType.TERMINATOR, grades,
                Collections.singleton(participant), Collections.singleton(tournament));

        assertThat(result).containsExactly(achievement);
    }

    // ===== get(Tournament) tests =====

    @Test
    public void testGetByTournamentDelegatesToRepository() {
        final Tournament tournament = tournament("Cup6");
        final Achievement achievement1 = achievement(participant("P13"), tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);
        final Achievement achievement2 = achievement(participant("P14"), tournament, AchievementType.JUGGERNAUT, AchievementGrade.SILVER);

        when(achievementRepository.findByTournament(tournament)).thenReturn(List.of(achievement1, achievement2));

        final List<Achievement> result = provider.get(tournament);

        assertThat(result).containsExactly(achievement1, achievement2);
    }

    // ===== delete(Tournament) tests =====

    @Test
    public void testDeleteByTournamentDelegatesToRepository() {
        final Tournament tournament = tournament("Cup7");

        when(achievementRepository.deleteByTournament(tournament)).thenReturn(5);

        final int result = provider.delete(tournament);

        assertThat(result).isEqualTo(5);
        verify(achievementRepository).deleteByTournament(tournament);
    }

    @Test
    public void testDeleteByTournamentReturnsZeroWhenNoAchievementsDeleted() {
        final Tournament tournament = tournament("Cup8");

        when(achievementRepository.deleteByTournament(tournament)).thenReturn(0);

        final int result = provider.delete(tournament);

        assertThat(result).isZero();
    }

    // ===== delete(AchievementType, AchievementGrade, Collection<Participant>, Tournament) tests =====

    @Test
    public void testDeleteByTypeGradeParticipantsAndTournamentDelegatesToRepository() {
        final Tournament tournament = tournament("Cup9");
        final Participant participant = participant("P15");
        final AchievementGrade grade = AchievementGrade.GOLD;

        when(achievementRepository.deleteByAchievementTypeAndAchievementGradeAndTournamentAndParticipantIn(
                AchievementType.THE_WINNER, grade, tournament, Collections.singleton(participant)))
                .thenReturn(3L);

        final long result = provider.delete(AchievementType.THE_WINNER, grade,
                Collections.singleton(participant), tournament);

        assertThat(result).isEqualTo(3L);
    }

    @Test
    public void testDeleteByTypeGradeParticipantsAndTournamentReturnsZeroWhenNothingDeleted() {
        final Tournament tournament = tournament("Cup10");
        final Participant participant = participant("P16");

        when(achievementRepository.deleteByAchievementTypeAndAchievementGradeAndTournamentAndParticipantIn(
                any(), any(), any(), any()))
                .thenReturn(0L);

        final long result = provider.delete(AchievementType.THE_WINNER, AchievementGrade.SILVER,
                Collections.singleton(participant), tournament);

        assertThat(result).isZero();
    }

    // ===== getAchievementsCount() tests - Branch coverage critical =====

    @Test
    public void testGetAchievementsCountWithMultipleTypesAndGrades() {
        final Tournament tournament = tournament("Cup11");
        final Participant p1 = participant("P17");
        final Participant p2 = participant("P18");

        final Achievement a1 = achievement(p1, tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);
        final Achievement a2 = achievement(p1, tournament, AchievementType.THE_WINNER, AchievementGrade.GOLD);
        final Achievement a3 = achievement(p2, tournament, AchievementType.THE_WINNER, AchievementGrade.SILVER);
        final Achievement a4 = achievement(p2, tournament, AchievementType.BILLY_THE_KID, AchievementGrade.GOLD);

        when(achievementRepository.findAll()).thenReturn(List.of(a1, a2, a3, a4));

        final Map<AchievementType, Map<AchievementGrade, Integer>> result = provider.getAchievementsCount();

        // Branch: New EnumMap creation for BILLY_THE_KID type
        assertThat(result).containsKeys(AchievementType.THE_WINNER, AchievementType.BILLY_THE_KID);
        assertThat(result.get(AchievementType.THE_WINNER).get(AchievementGrade.GOLD)).isEqualTo(1);
        assertThat(result.get(AchievementType.THE_WINNER).get(AchievementGrade.SILVER)).isEqualTo(1);
        assertThat(result.get(AchievementType.BILLY_THE_KID).get(AchievementGrade.GOLD)).isEqualTo(1);
    }

    @Test
    public void testGetAchievementsCountWithEmptyRepository() {
        when(achievementRepository.findAll()).thenReturn(List.of());

        final Map<AchievementType, Map<AchievementGrade, Integer>> result = provider.getAchievementsCount();

        assertThat(result).isEmpty();
    }

    @Test
    public void testGetAchievementsCountHandlesMultipleDuplicates() {
        final Tournament tournament = tournament("Cup12");
        final Participant participant = participant("P19");

        // Create multiple duplicates of the same achievement
        final Achievement a1 = achievement(participant, tournament, AchievementType.THE_WINNER, AchievementGrade.BRONZE);
        final Achievement a2 = achievement(participant, tournament, AchievementType.THE_WINNER, AchievementGrade.BRONZE);
        final Achievement a3 = achievement(participant, tournament, AchievementType.THE_WINNER, AchievementGrade.BRONZE);

        when(achievementRepository.findAll()).thenReturn(List.of(a1, a2, a3));

        final Map<AchievementType, Map<AchievementGrade, Integer>> result = provider.getAchievementsCount();

        // Only one entry should remain after deduplication
        assertThat(result.get(AchievementType.THE_WINNER).get(AchievementGrade.BRONZE)).isEqualTo(1);
    }

    @Test
    public void testGetAchievementsCountIncrementsBranchForNewGrade() {
        final Tournament tournament = tournament("Cup13");
        final Participant p1 = participant("P20");

        // Create achievements with different grades on the same type
        final Achievement a1 = achievement(p1, tournament, AchievementType.LETHAL_WEAPON, AchievementGrade.GOLD);
        final Achievement a2 = achievement(p1, tournament, AchievementType.LETHAL_WEAPON, AchievementGrade.SILVER);
        final Achievement a3 = achievement(p1, tournament, AchievementType.LETHAL_WEAPON, AchievementGrade.BRONZE);

        when(achievementRepository.findAll()).thenReturn(List.of(a1, a2, a3));

        final Map<AchievementType, Map<AchievementGrade, Integer>> result = provider.getAchievementsCount();

        // Branch: putIfAbsent for each new grade
        assertThat(result.get(AchievementType.LETHAL_WEAPON))
                .containsEntry(AchievementGrade.GOLD, 1)
                .containsEntry(AchievementGrade.SILVER, 1)
                .containsEntry(AchievementGrade.BRONZE, 1);
    }

    // ===== countAchievements() tests =====

    @Test
    public void testCountAchievementsByTypeDelegatesToRepository() {
        when(achievementRepository.countAchievementsByAchievementType(AchievementType.THE_WINNER))
                .thenReturn(42);

        final int result = provider.countAchievements(AchievementType.THE_WINNER);

        assertThat(result).isEqualTo(42);
        verify(achievementRepository).countAchievementsByAchievementType(AchievementType.THE_WINNER);
    }

    @Test
    public void testCountAchievementsReturnsZeroWhenNoAchievements() {
        when(achievementRepository.countAchievementsByAchievementType(AchievementType.TERMINATOR))
                .thenReturn(0);

        final int result = provider.countAchievements(AchievementType.TERMINATOR);

        assertThat(result).isZero();
    }

    private Tournament tournament(String name) {
        final Tournament tournament = new Tournament(name, 2, 3, TournamentType.LEAGUE, "user");
        tournament.setId(Math.floorMod(name.hashCode(), Integer.MAX_VALUE));
        return tournament;
    }

    private Participant participant(String name) {
        final Participant participant = new Participant();
        participant.setId(Math.floorMod(name.hashCode(), Integer.MAX_VALUE));
        participant.setName(name);
        participant.setLastname("Lastname");
        participant.setCreatedBy("user");
        return participant;
    }

    private Achievement achievement(Participant participant, Tournament tournament, AchievementType type, AchievementGrade grade) {
        final Achievement achievement = new Achievement(participant, tournament, type, grade);
        achievement.setCreatedBy("user");
        return achievement;
    }
}


