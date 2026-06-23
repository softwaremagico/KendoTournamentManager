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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
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

    private Tournament tournament(String name) {
        final Tournament tournament = new Tournament(name, 2, 3, TournamentType.LEAGUE, "user");
        tournament.setId(Math.abs(name.hashCode()));
        return tournament;
    }

    private Participant participant(String name) {
        final Participant participant = new Participant();
        participant.setId(Math.abs(name.hashCode()));
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


