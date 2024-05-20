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

import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ParticipantRepository extends JpaRepository<Participant, Integer> {

    @Query("SELECT r.participant FROM Role r WHERE r.tournament = :tournament")
    List<Participant> findByTournament(@Param("tournament") Tournament tournament);

    @Query("SELECT r.participant FROM Role r WHERE r.tournament = :tournament and r.roleType = :roleType")
    List<Participant> findByTournamentAndRoleType(@Param("tournament") Tournament tournament, @Param("roleType") RoleType roleType);

    @Query("""
            SELECT r.participant FROM Role r WHERE r.participant IN
            (SELECT rl.participant FROM Role rl WHERE rl.tournament = :tournament)
            GROUP BY r.participant
            HAVING COUNT(DISTINCT r.roleType) >= :differentRoleTypes
            """)
    List<Participant> findParticipantsWithMoreRoleTypesThan(@Param("tournament") Tournament tournament, @Param("differentRoleTypes") long differentRoleTypes);

    @Query("SELECT a.participant FROM Achievement a WHERE a.participant IN :participants AND a.achievementType=:achievementType")
    List<Participant> findParticipantsWithAchievementFromList(@Param("achievementType") AchievementType achievementType, List<Participant> participants);

    @Query("SELECT a.participant FROM Achievement a WHERE a.achievementType=:achievementType AND"
            + " a.achievementGrade=:achievementGrade")
    List<Participant> findParticipantsWithAchievementAndGrade(
            @Param("achievementType") AchievementType achievementType, @Param("achievementGrade") AchievementGrade achievementGrade);

    @Query("SELECT a.participant FROM Achievement a WHERE a.participant IN :participants AND a.achievementType=:achievementType AND"
            + " a.achievementGrade=:achievementGrade")
    List<Participant> findParticipantsWithAchievementAndGradeFromList(
            @Param("achievementType") AchievementType achievementType, @Param("achievementGrade") AchievementGrade achievementGrade,
            @Param("participants") List<Participant> participants);

    // Created at not working, we cannot search using this field.
    @Query("""
            SELECT r.participant FROM Role r WHERE r.tournament = :tournament AND  r.roleType = :roleType
            AND NOT EXISTS
            (SELECT r2.participant FROM Role r2 WHERE r.participant = r2.participant
            AND r.roleType = r2.roleType AND r2.tournament IN :olderTournaments)
            """)
    List<Participant> findParticipantsWithRoleNotInTournaments(@Param("tournament") Tournament tournament, @Param("roleType") RoleType roleType,
                                                               @Param("olderTournaments") Collection<Tournament> olderTournaments);

    List<Participant> findByClub(Club club);

    long countByTemporalToken(String temporalToken);

    Optional<Participant> findByTemporalToken(String temporalToken);

    Optional<Participant> findByToken(String token);
}
