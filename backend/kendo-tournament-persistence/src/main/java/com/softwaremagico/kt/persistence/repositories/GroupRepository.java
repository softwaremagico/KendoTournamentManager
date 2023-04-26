package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface GroupRepository extends JpaRepository<Group, Integer> {

    List<Group> findByTournamentOrderByLevelAscIndexAsc(Tournament tournament);

    List<Group> findByTournamentAndLevelOrderByLevelAscIndexAsc(Tournament tournament, Integer level);

    Group findByTournamentAndLevelAndIndex(Tournament tournament, Integer level, Integer index);

    int deleteByTournamentAndLevelAndIndex(Tournament tournament, Integer level, Integer index);

    List<Group> findByTournamentAndShiaijoOrderByLevelAscIndexAsc(Tournament tournament, Integer shiaijo);

    Optional<Group> findByFightsId(Integer fightId);

    List<Group> findDistinctByFightsIdIn(Collection<Integer> fightId);

    long deleteByTournament(Tournament tournament);

    long deleteByTournamentAndLevel(Tournament tournament, Integer level);

    long countByTournament(Tournament tournament);
}
