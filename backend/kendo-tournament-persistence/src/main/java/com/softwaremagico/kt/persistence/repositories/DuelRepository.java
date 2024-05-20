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

import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.Score;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
@Transactional
public interface DuelRepository extends JpaRepository<Duel, Integer> {

    long deleteByTournament(Tournament tournament);

    long countByTournament(Tournament tournament);

    List<Duel> findByTournament(Tournament tournament);

    @Query("SELECT d FROM Duel d WHERE (d.competitor1 IN :participants OR d.competitor2 IN :participants) AND d.type='UNDRAW'")
    List<Duel> findUntiesByParticipantIn(@Param("participants") Collection<Participant> participants);

    @Query("SELECT d FROM Duel d WHERE d.type='UNDRAW'")
    List<Duel> findAllUnties();

    @Query("SELECT AVG(d.duration) FROM Duel d WHERE d.duration > " + Duel.DEFAULT_DURATION)
    Long getDurationAverage();

    @Query("SELECT AVG(d.duration) FROM Duel d WHERE (d.competitor1=:participant OR d.competitor2=:participant) AND  d.duration > " + Duel.DEFAULT_DURATION)
    Long getDurationAverage(@Param("participant") Participant participant);

    @Query("""
            SELECT d FROM Duel d WHERE d.tournament=:tournament AND (
            ((size(d.competitor1Score)=2 AND NOT EXISTS (SELECT s1 FROM d.competitor1Score s1 WHERE s1 IN :forbiddenScores)) OR
            (size(d.competitor2Score)=2 AND NOT EXISTS (SELECT s2 FROM d.competitor2Score s2 WHERE s2 IN :forbiddenScores)))
            )
            """)
    Set<Duel> findByOnlyScore(@Param("tournament") Tournament tournament, @Param("forbiddenScores") Collection<Score> forbiddenScores);

    @Query("""
            SELECT d FROM Duel d LEFT JOIN d.competitor1ScoreTime t1 LEFT JOIN d.competitor2ScoreTime t2 WHERE d.tournament=:tournament
            AND (t1<=:maxSeconds OR t2<=:maxSeconds)
            """)
    Set<Duel> findByScoreOnTimeLess(@Param("tournament") Tournament tournament, @Param("maxSeconds") int maxSeconds);

    @Query("SELECT AVG(d.duration) FROM Duel d WHERE d.duration > " + Duel.DEFAULT_DURATION + " AND d.tournament=:tournament")
    Long getDurationAverage(@Param("tournament") Tournament tournament);

    Duel findFirstByTournamentOrderByStartedAtAsc(Tournament tournament);

    Duel findFirstByTournamentOrderByFinishedAtDesc(Tournament tournament);

    @Query("""
            SELECT COUNT(*) FROM Duel d LEFT JOIN d.competitor1Score s1 LEFT JOIN d.competitor2Score s2 WHERE d.tournament=:tournament AND
            (s1 IN (:scores) OR  s2 IN (:scores))
            """)
    Long countScore(@Param("tournament") Tournament tournament, @Param("scores") Collection<Score> scores);

    @Query("""
            SELECT SUM(CASE WHEN d.competitor1=:competitor THEN 1 ELSE 0 END) FROM Duel d INNER JOIN d.competitor1Score s1
            WHERE d.tournament IN (:tournaments)
            """)
    Long countLeftScoreFromCompetitor(@Param("competitor") Participant competitor, @Param("tournaments") Collection<Tournament> tournaments);

    @Query("""
            SELECT SUM(CASE WHEN d.competitor2=:competitor THEN 1 ELSE 0 END) FROM Duel d INNER JOIN d.competitor2Score s1
                        WHERE d.tournament IN (:tournaments)
            """)
    Long countRightScoreFromCompetitor(@Param("competitor") Participant competitor, @Param("tournaments") Collection<Tournament> tournaments);

    @Query("""
            SELECT SUM(CASE WHEN d.competitor1=:competitor THEN 1 ELSE 0 END) FROM Duel d INNER JOIN d.competitor2Score s1
                        WHERE d.tournament IN (:tournaments)
            """)
    Long countLeftScoreAgainstCompetitor(@Param("competitor") Participant competitor, @Param("tournaments") Collection<Tournament> tournaments);

    @Query("""
            SELECT SUM(CASE WHEN d.competitor2=:competitor THEN 1 ELSE 0 END) FROM Duel d INNER JOIN d.competitor1Score s1
                        WHERE d.tournament IN (:tournaments)
            """)
    Long countRightScoreAgainstCompetitor(@Param("competitor") Participant competitor, @Param("tournaments") Collection<Tournament> tournaments);

    //    @Query("SELECT COUNT(*) FROM Duel d WHERE d.competitor1Fault=true OR d.competitor2Fault= true AND d.tournament=:tournament")
    @Query("""
            SELECT SUM(CASE WHEN d.competitor1Fault=:status THEN 1 ELSE 0 END) + SUM(CASE WHEN d.competitor2Fault=:status THEN 1 ELSE 0 END)
            FROM Duel d WHERE d.tournament=:tournament
            """)
    Long countFaultsByTournament(@Param("tournament") Tournament tournament, @Param("status") Boolean status);

    List<Duel> findByTournamentAndCompetitor1ScoreTimeLessThanEqualOrCompetitor2ScoreTimeLessThanEqual(Tournament tournament,
                                                                                                       int score1MaxDuration, int score2MaxDuration);

    @Query("SELECT d FROM Duel d WHERE d.competitor1=:participant OR d.competitor2=:participant")
    List<Duel> findByParticipant(@Param("participant") Participant participant);

    @Query("SELECT d FROM Duel d WHERE (d.competitor1=:participant1 AND d.competitor2=:participant2) "
            + "OR (d.competitor2=:participant1 AND d.competitor1=:participant2)")
    List<Duel> findByParticipants(@Param("participant1") Participant participant1, @Param("participant2") Participant participant2);
}
