package com.softwaremagico.kt.core.tests.tournament;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;

import java.util.Collections;
import java.util.List;

@SpringBootTest
public abstract class TreeTournamentBasedTest extends AbstractTestNGSpringContextTests {

	private static final String CLUB_NAME = "ClubName";
	private static final String CLUB_CITY = "ClubCity";
	private static final String TOURNAMENT_NAME = "TournamentTest";

	@Autowired
	private TournamentController tournamentController;

	@Autowired
	private TournamentExtraPropertyController tournamentExtraPropertyController;

	@Autowired
	private TournamentConverter tournamentConverter;

	@Autowired
	private ParticipantController participantController;

	@Autowired
	private RoleController roleController;

	@Autowired
	private TeamController teamController;

	@Autowired
	private TreeTournamentHandler treeTournamentHandler;

	@Autowired
	private ClubController clubController;

	@Autowired
	private GroupController groupController;

	@Autowired
	private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

	@Autowired
	private FightController fightController;

	@Autowired
	private DuelController duelController;

	private ClubDTO clubDTO;

	private void addClub() {
        this.clubDTO = this.clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
	}

	private void addParticipants(int teams, int membersByTeam) {
		final int existingParticipants = this.participantController.get().size();
		for (int i = existingParticipants; i < membersByTeam * teams; i++) {
            this.participantController.create(new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i),
					String.format("lastname%s", i), this.clubDTO), null, null);
		}
	}

	private TournamentDTO addTournament(int indexOfTournament, int shiaijos, int winners, int membersByTeam) {
		Assert.assertEquals(this.tournamentController.count(), 0);
		final TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME + "_" + indexOfTournament, shiaijos,
				membersByTeam, TournamentType.TREE);
		final TournamentDTO tournamentDTO = this.tournamentController.create(newTournament, null, null);
        this.tournamentExtraPropertyProvider.save(new TournamentExtraProperty(this.tournamentConverter.reverse(tournamentDTO),
				TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "true"));
        this.tournamentExtraPropertyProvider.save(new TournamentExtraProperty(this.tournamentConverter.reverse(tournamentDTO),
				TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "false"));
		Assert.assertEquals(this.tournamentController.count(), 1);

		if (winners > 1) {
            this.tournamentExtraPropertyController.create(new TournamentExtraPropertyDTO(tournamentDTO,
					TournamentExtraPropertyKey.NUMBER_OF_WINNERS, String.valueOf(winners)), null, null);
		}
		return tournamentDTO;
	}

	private void addRoles(TournamentDTO tournamentDTO) {
		for (final ParticipantDTO competitor : this.participantController.get()) {
            this.roleController.create(new RoleDTO(tournamentDTO, competitor, RoleType.COMPETITOR), null, null);
		}
		Assert.assertEquals(this.roleController.count(tournamentDTO), this.participantController.count());
	}

	private long addGroups(TournamentDTO tournamentDTO, int groupsNumber, int winners) {
		// First group is already inserted.
        this.treeTournamentHandler.adjustGroupsSizeRemovingOddNumbers(this.tournamentConverter.reverse(tournamentDTO), winners);

		for (int i = 1; i < groupsNumber; i++) {
			final GroupDTO groupDTO = new GroupDTO();
			groupDTO.setTournament(tournamentDTO);
			groupDTO.setIndex(i);
			groupDTO.setLevel(0);
			groupDTO.setShiaijo(0);
			groupDTO.setNumberOfWinners(winners);
            this.groupController.create(groupDTO, null, null);
		}
		return this.groupController.count();
	}

	private void addTeams(TournamentDTO tournamentDTO, int groupsNumber, int teamsNumber, int membersByTeam) {
		int teamIndex = 0;
		TeamDTO team = null;
		int teamMember = 0;

		final List<Group> groups = this.groupController.getGroups(tournamentDTO, 0);

		for (final ParticipantDTO competitor : this.participantController.get()) {
			// Create a new team.
			if (team == null) {
				teamIndex++;
				team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
				teamMember = 0;
			}

			// Add member.
			team.addMember(competitor);
			team = this.teamController.create(team, null, null);

			// Add the new team to a group
			if (teamMember == 0) {
                this.groupController.addTeams(
						groups.get(((teamIndex - 1) / (teamsNumber / groupsNumber)) % groupsNumber).getId(),
						Collections.singletonList(team), null, null);
			}

			teamMember++;

			// Team filled up, create a new team.
			if (teamMember >= membersByTeam) {
				team = null;
			}
		}

		Assert.assertEquals(this.teamController.count(tournamentDTO), teamsNumber);

		final List<Group> tournamentGroups = this.groupController.getGroups(tournamentDTO, 0);
		for (final Group group : tournamentGroups) {
			Assert.assertEquals(group.getTeams().size(), teamsNumber / groupsNumber);
		}
	}

	protected TournamentDTO createTournament(int initialGroups, int membersByTeam, int winners) {
		return this.createTournament(initialGroups, initialGroups * 3, membersByTeam, winners);
	}

	protected TournamentDTO createTournament(int initialGroups, int teams, int membersByTeam, int winners) {
        this.addClub();
        this.addParticipants(teams, membersByTeam);
		final TournamentDTO tournamentDTO = this.addTournament(0, 1, winners, membersByTeam);
        this.addRoles(tournamentDTO);
        this.addGroups(tournamentDTO, initialGroups, winners);
        this.addTeams(tournamentDTO, initialGroups, teams, membersByTeam);
		return tournamentDTO;
	}

	@AfterClass(alwaysRun = true)
	public void deleteTournament() {
		final List<TournamentDTO> tournamentDTOS = this.tournamentController.get();
		for (final TournamentDTO tournamentDTO : tournamentDTOS) {
            this.groupController.delete(tournamentDTO);
            this.fightController.delete(tournamentDTO);
            this.duelController.delete(tournamentDTO);
            this.teamController.delete(tournamentDTO);
            this.roleController.delete(tournamentDTO);
            this.tournamentController.delete(tournamentDTO, null, null);
		}
        this.participantController.deleteAll();
        this.clubController.delete(this.clubDTO, null, null);
		Assert.assertEquals(this.fightController.count(), 0);
		Assert.assertEquals(this.duelController.count(), 0);
	}
}
