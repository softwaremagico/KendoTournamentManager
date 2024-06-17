package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface AchievementRepository extends JpaRepository<Achievement, Integer> {

    List<Achievement> findByParticipant(Participant participant);

    List<Achievement> findByParticipantAndTournament(Participant participant, Tournament tournament);

    List<Achievement> findByTournament(Tournament tournament);

    List<Achievement> findByTournamentAndAchievementType(Tournament tournament, AchievementType achievementType);

    List<Achievement> findByTournamentAndAchievementTypeAndAchievementGradeIn(Tournament tournament, AchievementType achievementType,
                                                                              Collection<AchievementGrade> achievementGrades);

    List<Achievement> findByAchievementTypeAndAchievementGradeIn(AchievementType achievementType, Collection<AchievementGrade> achievementGrades);

    List<Achievement> findByAchievementTypeAndAchievementGradeInAndParticipantInAndTournamentIn(
            AchievementType achievementType, Collection<AchievementGrade> achievementGrades, Collection<Participant> participants,
            Collection<Tournament> tournaments);

    List<Achievement> findByTournamentAndAchievementTypeAndAchievementGradeInAndCreatedAtGreaterThanEqual(
            Tournament tournament, AchievementType achievementType, Collection<AchievementGrade> grades, LocalDateTime range);

    List<Achievement> findByAchievementType(AchievementType achievementType);

    long deleteByAchievementTypeAndAchievementGradeAndTournamentAndParticipantIn(
            AchievementType achievementType, AchievementGrade achievementGrade, Tournament tournament, Collection<Participant> participants);

    int deleteByTournament(Tournament tournament);
}

