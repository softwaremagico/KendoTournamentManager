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

import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.GroupLinkController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.GroupLinkDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;

@SpringBootTest
@Test(groups = {"shiaijosTest"})
public class TournamentShiaijosTest extends AbstractTestNGSpringContextTests {

	private static final int MEMBERS = 1;
	private static final int GROUPS = 6;
	private static final String TOURNAMENT_NAME = "TournamentTest";
	private static TournamentDTO tournamentDTO = null;

	@Autowired
	private TournamentController tournamentController;

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
	private GroupController groupController;

	@Autowired
	private FightController fightController;

	@Autowired
	private DuelController duelController;

	@Autowired
	private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

	@Autowired
	private GroupLinkController groupLinkController;

	@Test
	public void addTournament() {
		Assert.assertEquals(this.tournamentController.count(), 0);
		final TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME, 2, MEMBERS, TournamentType.TREE);
		tournamentDTO = this.tournamentController.create(newTournament, null, null);
        this.tournamentExtraPropertyProvider.save(new TournamentExtraProperty(this.tournamentConverter.reverse(tournamentDTO),
				TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "true"));
        this.tournamentExtraPropertyProvider.save(new TournamentExtraProperty(this.tournamentConverter.reverse(tournamentDTO),
				TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "false"));
		Assert.assertEquals(this.tournamentController.count(), 1);
	}

	@Test(dependsOnMethods = "addTournament")
	public void add6Groups() {
		// The First group is already inserted.
        this.treeTournamentHandler.adjustGroupsSizeRemovingOddNumbers(this.tournamentConverter.reverse(tournamentDTO), 1);

		for (int i = 1; i < GROUPS; i++) {
			final GroupDTO groupDTO = new GroupDTO();
			groupDTO.setTournament(tournamentDTO);
			groupDTO.setIndex(i);
			groupDTO.setLevel(0);
			groupDTO.setShiaijo((GROUPS / (GROUPS / 2)));
			groupDTO.setNumberOfWinners(1);
            this.groupController.create(groupDTO, null, null);
		}
		Assert.assertEquals(this.groupController.count(), 13);
	}

	@Test(dependsOnMethods = {"add6Groups"})
	public void checkShiaijos() {
		final List<GroupDTO> level0Groups = this.groupController.get(tournamentDTO).stream().filter(g -> g.getLevel() == 0)
				.sorted(Comparator.comparing(GroupDTO::getIndex)).toList();
		Assert.assertEquals(level0Groups.get(0).getShiaijo(), 0);
		Assert.assertEquals(level0Groups.get(1).getShiaijo(), 0);
		Assert.assertEquals(level0Groups.get(2).getShiaijo(), 0);
		Assert.assertEquals(level0Groups.get(3).getShiaijo(), 1);
		Assert.assertEquals(level0Groups.get(4).getShiaijo(), 1);
		Assert.assertEquals(level0Groups.get(5).getShiaijo(), 1);

		final List<GroupLinkDTO> links = this.groupLinkController.getLinks(this.tournamentConverter.reverse(tournamentDTO));
		Assert.assertEquals(links.get(0).getDestination().getShiaijo(), 0);
		Assert.assertEquals(links.get(1).getDestination().getShiaijo(), 0);
		Assert.assertEquals(links.get(2).getDestination().getShiaijo(), 0);
		Assert.assertEquals(links.get(3).getDestination().getShiaijo(), 1);
		Assert.assertEquals(links.get(4).getDestination().getShiaijo(), 1);
		Assert.assertEquals(links.get(5).getDestination().getShiaijo(), 1);
	}

	@AfterClass(alwaysRun = true)
	public void deleteTournament() {
        this.groupController.delete(tournamentDTO);
        this.fightController.delete(tournamentDTO);
        this.duelController.delete(tournamentDTO);
        this.teamController.delete(tournamentDTO);
        this.roleController.delete(tournamentDTO);
        this.tournamentController.delete(tournamentDTO, null, null);
        this.participantController.deleteAll();
		Assert.assertEquals(this.fightController.count(), 0);
		Assert.assertEquals(this.duelController.count(), 0);
	}
}
