package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public interface RoleRepository extends JpaRepository<Role, Integer> {

    List<Role> findByTournament(Tournament tournament);

    List<Role> findByTournamentAndRoleType(Tournament tournament, RoleType roleType);

    List<Role> findByRoleType(RoleType roleType);

    List<Role> findByTournamentAndRoleTypeIn(Tournament tournament, Collection<RoleType> roleTypes);

    List<Role> findByTournamentAndDiplomaPrintedAndRoleTypeIn(Tournament tournament, boolean diplomaPrinted, Collection<RoleType> roleTypes);

    List<Role> findByTournamentAndDiplomaPrinted(Tournament tournament, boolean diplomaPrinted);

    List<Role> findByTournamentAndAccreditationPrintedAndRoleTypeIn(Tournament tournament, boolean accreditationPrinted, Collection<RoleType> roleTypes);

    List<Role> findByTournamentAndAccreditationPrinted(Tournament tournament, boolean accreditationPrinted);

    List<Role> findByTournamentAndParticipantIn(Tournament tournament, Collection<Participant> participants);

    List<Role> findByParticipantIn(Collection<Participant> participants);

    Role findByTournamentAndParticipant(Tournament tournament, Participant participant);

    long countByTournament(Tournament tournament);

    long countByTournamentAndRoleType(Tournament tournament, RoleType roleType);

    long countByParticipantAndRoleType(Participant participant, RoleType roleType);

    void deleteByParticipantAndTournament(Participant participant, Tournament tournament);

    long deleteByTournament(Tournament tournament);
}
