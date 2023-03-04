package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface DuelRepository extends JpaRepository<Duel, Integer> {

    long deleteByTournament(Tournament tournament);

    long countByTournament(Tournament tournament);

    List<Duel> findByTournament(Tournament tournament);

    @Query("SELECT g.unties FROM Group g LEFT JOIN g.unties u WHERE u.competitor1 IN :participants OR u.competitor2 IN :participants")
    List<Duel> findUntiesByParticipantIn(@Param("participants") Collection<Participant> participants);

    @Query("SELECT AVG(CAST(d.duration AS int)) FROM Duel d WHERE d.duration > " + Duel.DEFAULT_DURATION)
    Long getDurationAverage();

    @Query("Select d FROM Duel d WHERE d.tournament=:tournament AND (" +
            "(size(d.competitor1Score)=2 AND d.competitor1Score[0] IN :scores AND d.competitor1Score[1] IN :scores) OR " +
            "(size(d.competitor2Score)=2 AND d.competitor2Score[0] IN :scores AND d.competitor2Score[1] IN :scores)" +
            ") ")
    List<Duel> findByOnlyScore(@Param("tournament") Tournament tournament, @Param("scores") Collection<Score> scores);

    List<Duel> findByTournamentAndCompetitor1ScoreTimeLessThanEqualOrCompetitor2ScoreTimeLessThanEqual(Tournament tournament,
                                                                                                       int score1MaxDuration, int score2MaxDuration);
}