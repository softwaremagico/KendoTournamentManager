package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AchievementProvider extends CrudProvider<Achievement, Integer, AchievementRepository> {

    @Autowired
    public AchievementProvider(AchievementRepository achievementRepository) {
        super(achievementRepository);
    }

    public Achievement add(Participant participant, Tournament tournament, AchievementType achievementType) {
        return getRepository().save(new Achievement(participant, tournament, achievementType));
    }

    public List<Achievement> get(Participant participant) {
        return getRepository().findByParticipant(participant);
    }

    public List<Achievement> get(Tournament tournament, Participant participant) {
        return getRepository().findByParticipantAndTournament(participant, tournament);
    }

    public List<Achievement> get(Tournament tournament, AchievementType achievementType) {
        return getRepository().findByTournamentAndAchievementType(tournament, achievementType);
    }

    public List<Achievement> get(Tournament tournament, AchievementType achievementType, AchievementGrade achievementGrade) {
        if (achievementGrade == null) {
            return get(tournament, achievementType);
        }
        return getRepository().findByTournamentAndAchievementTypeAndAchievementGradeIn(tournament, achievementType, Collections.singleton(achievementGrade));
    }

    public List<Achievement> get(Tournament tournament, AchievementType achievementType, Collection<AchievementGrade> achievementGrades) {
        if (achievementGrades == null || achievementGrades.isEmpty()) {
            return new ArrayList<>();
        }
        return getRepository().findByTournamentAndAchievementTypeAndAchievementGradeIn(tournament, achievementType, achievementGrades);
    }

    /**
     * Returns all achievements that are generated after a specific time. Any achievement with a grade equals or better that the selected will be returned
     *
     * @param tournament      Filter by tournament
     * @param achievementType The achievement to look up.
     * @param grade           the grade used for comparison,  if null, any grade will be used.
     * @param after           the date used as minimum range.
     * @return a list of achievements.
     */
    public List<Achievement> getAfter(Tournament tournament, AchievementType achievementType, AchievementGrade grade, LocalDateTime after) {
        return getRepository().findByTournamentAndAchievementTypeAndAchievementGradeInAndCreatedAtGreaterThanEqual(tournament, achievementType,
                grade != null ? grade.getGreaterEqualsThan() : Arrays.asList(AchievementGrade.values()), after);
    }

    public List<Achievement> get(AchievementType achievementType) {
        return getRepository().findByAchievementType(achievementType);
    }

    public List<Achievement> get(AchievementType achievementType, AchievementGrade achievementGrade) {
        return get(achievementType, Collections.singleton(achievementGrade));
    }

    public List<Achievement> get(AchievementType achievementType, Collection<AchievementGrade> achievementGrades) {
        return getRepository().findByAchievementTypeAndAchievementGradeIn(achievementType, achievementGrades);
    }

    public List<Achievement> get(AchievementType achievementType, Collection<AchievementGrade> achievementGrades, Collection<Participant> participants,
                                 Collection<Tournament> tournaments) {
        return getRepository().findByAchievementTypeAndAchievementGradeInAndParticipantInAndTournamentIn(
                achievementType, achievementGrades, participants, tournaments);
    }

    public List<Achievement> get(Tournament tournament) {
        return getRepository().findByTournament(tournament);
    }

    public int delete(Tournament tournament) {
        return getRepository().deleteByTournament(tournament);
    }

    public long delete(AchievementType achievementType, AchievementGrade achievementGrade, Collection<Participant> participants, Tournament tournament) {
        return getRepository().deleteByAchievementTypeAndAchievementGradeAndTournamentAndParticipantIn(
                achievementType, achievementGrade, tournament, participants);
    }

    public Map<AchievementType, Map<AchievementGrade, Integer>> getAchievementsCount() {
        final List<Achievement> achievements = getRepository().findAll()
                //Filter duplicates by user and type.
                .stream().collect(Collectors.toMap(Achievement::keyByUserAndType, Function.identity(),
                        (a, b) -> a))
                .values().stream().toList();
        final Map<AchievementType, Map<AchievementGrade, Integer>> counter = new EnumMap<>(AchievementType.class);
        for (Achievement achievement : achievements) {
            counter.putIfAbsent(achievement.getAchievementType(), new EnumMap<>(AchievementGrade.class));
            counter.get(achievement.getAchievementType()).putIfAbsent(achievement.getAchievementGrade(), 0);
            counter.get(achievement.getAchievementType()).put(achievement.getAchievementGrade(),
                    counter.get(achievement.getAchievementType()).get(achievement.getAchievementGrade()) + 1);
        }
        return counter;
    }

    public int countAchievements(AchievementType achievementType) {
        return getRepository().countAchievementsByAchievementType(achievementType);
    }
}
