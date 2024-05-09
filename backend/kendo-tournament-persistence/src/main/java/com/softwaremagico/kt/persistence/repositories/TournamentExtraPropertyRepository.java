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

import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface TournamentExtraPropertyRepository extends JpaRepository<TournamentExtraProperty, Integer> {

    List<TournamentExtraProperty> findByTournament(Tournament tournament);

    TournamentExtraProperty findByTournamentAndPropertyKey(Tournament tournament, TournamentExtraPropertyKey tournamentExtraPropertyKey);

    TournamentExtraProperty findFirstByPropertyKeyOrderByCreatedAtDesc(TournamentExtraPropertyKey tournamentExtraPropertyKey);

    int deleteByTournament(Tournament tournament);

    void deleteByTournamentAndPropertyKey(Tournament tournament, TournamentExtraPropertyKey tournamentExtraPropertyKey);

    @Query("""
                Select p from TournamentExtraProperty p WHERE
                p.id IN (SELECT max(p2.id) FROM TournamentExtraProperty p2 WHERE
                (:createdBy IS NULL OR p2.createdBy=:createdBy) GROUP BY p2.propertyKey)
            """)
    List<TournamentExtraProperty> findDistinctPropertyKeyByCreatedByOrderByCreatedAtDesc(@Param("createdBy") String createdBy);

}
