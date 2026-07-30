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

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.GroupLinkController;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.GroupLinkDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
@Test(groups = {"tournamentLinkTest"})
public class TournamentLinkTest extends TreeTournamentBasedTest {

	private static final int MEMBERS = 3;

	@Autowired
	private GroupController groupController;

	@Autowired
	private GroupLinkController groupLinkController;

	private List<GroupLinkDTO> getIncomingArrows(int groupIndex, int groupLevel, List<GroupLinkDTO> groupLinkDTOS) {
		return groupLinkDTOS.stream().filter(groupLinkDTO -> groupLinkDTO.getDestination().getIndex() == groupIndex
				&& groupLinkDTO.getDestination().getLevel() == groupLevel).toList();
	}

	private List<GroupLinkDTO> getOutcomingArrows(int groupIndex, int groupLevel, List<GroupLinkDTO> groupLinkDTOS) {
		return groupLinkDTOS.stream().filter(groupLinkDTO -> groupLinkDTO.getSource().getIndex() == groupIndex
				&& groupLinkDTO.getSource().getLevel() == groupLevel).toList();
	}

	private int getMaxLevel(List<GroupDTO> groups) {
		return groups.stream().map(GroupDTO::getLevel).max(Integer::compareTo).orElse(0);
	}

	private void assertEachPoolIncomingArrowsNumber(TournamentDTO tournament) {
		final List<GroupLinkDTO> groupLinkDTOs = this.groupLinkController.getLinks(tournament);
		final List<GroupDTO> groups = this.groupController.get(tournament);
		if (groups.size() > 1) {
			for (final GroupDTO groupDTO : groups) {
				if (groupDTO.getLevel() == 0) {
					Assert.assertEquals(
                        this.getIncomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), 0);
				} else if (groupDTO.getLevel() > 1) {
					// Each pool (except the first and second column) must always have exactly two
					// incoming arrows.
					Assert.assertEquals(
                        this.getIncomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), 2);
				} else {
					// Level 1 may have one or two incoming arrows.
					final int incomingArrows = this.getIncomingArrows(groupDTO.getIndex(), groupDTO.getLevel(),
							groupLinkDTOs).size();
					Assert.assertTrue(incomingArrows >= 1 && incomingArrows <= 2);
				}
			}
		}
	}

	private void assertEachPoolOutcomingArrowsNumber(TournamentDTO tournament, int winners) {
		final List<GroupLinkDTO> groupLinkDTOs = this.groupLinkController.getLinks(tournament);
		final List<GroupDTO> groups = this.groupController.get(tournament);
		if (groups.size() > 1) {
			for (final GroupDTO groupDTO : groups) {
				if (groupDTO.getLevel() == 0) {
					Assert.assertEquals(
                        this.getOutcomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(),
							winners);
					// Last level
				} else if (groupDTO.getLevel() == this.getMaxLevel(groups)) {
					Assert.assertEquals(
                        this.getOutcomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), 0);
				} else {
					Assert.assertEquals(
                        this.getOutcomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), 1);
				}
			}
		}
	}

	private void checkNotTwoFirstWinnersAsIncomingArrow(TournamentDTO tournament, int winners) {
		// If I have two first winners together, means that 2nd winners together are
		// full.
		if (winners == 1) {
			return;
		}
		final List<GroupLinkDTO> secondLevelGroups = this.getGroupLinksToLevel(tournament, 1);
		// Check if two winners are together. Two first winners are together if the
		// destination groups number for first winners are less than the original one.
		Assert.assertEquals(this.getGroupFromLevel(tournament, 0).size(),
				secondLevelGroups.stream().filter(groupLinkDTO -> groupLinkDTO.getWinner() == 0).toList().size());
	}

	private List<GroupLinkDTO> getGroupLinksToLevel(TournamentDTO tournament, int level) {
		return this.groupLinkController.getLinks(tournament).stream()
				.filter(groupLinkDTO -> groupLinkDTO.getDestination().getLevel() == level).toList();
	}

	private List<GroupDTO> getGroupFromLevel(TournamentDTO tournament, int level) {
		return this.groupController.get(tournament).stream().filter(groupDTO -> groupDTO.getLevel() == level).toList();
	}

	private void noSecondPlaceWinnerOnByeUnlessNecessary(TournamentDTO tournament) {
		final List<GroupLinkDTO> groupLinkDTOs = this.getGroupLinksToLevel(tournament, 1);
		final Set<GroupDTO> byes = this.getByes(tournament, 1);
		for (final GroupLinkDTO groupLinkDTO : groupLinkDTOs) {
			// If a second winner goes to a bye
			if (groupLinkDTO.getWinner() == 1 && byes.contains(groupLinkDTO.getDestination())) {
				// All first winners are on a bye.
				Assert.assertTrue(byes.containsAll(groupLinkDTOs.stream().filter(g -> g.getWinner() == 0)
						.map(GroupLinkDTO::getDestination).toList()));
			}
		}
	}

	private Set<GroupDTO> getByes(TournamentDTO tournament, int level) {
		final List<GroupLinkDTO> groupLinkDTOs = this.getGroupLinksToLevel(tournament, level);
		final Set<GroupDTO> groupsFromLevel = new HashSet<>(this.getGroupFromLevel(tournament, level));
		final Set<GroupDTO> seenGroups = new HashSet<>();
		for (final GroupLinkDTO g : groupLinkDTOs) {
			if (!seenGroups.add(g.getDestination())) {
				groupsFromLevel.remove(g.getDestination());
			}
		}
		return groupsFromLevel;
	}

	private void checkGroupRules(TournamentDTO tournamentDTO, int winners) {
        this.assertEachPoolIncomingArrowsNumber(tournamentDTO);
        this.assertEachPoolOutcomingArrowsNumber(tournamentDTO, winners);
        this.checkNotTwoFirstWinnersAsIncomingArrow(tournamentDTO, winners);
        this.noSecondPlaceWinnerOnByeUnlessNecessary(tournamentDTO);
	}

	@Test
	public void oneGroupOneWinner() {
		final int winners = 1;
		final TournamentDTO tournamentDTO = this.createTournament(1, MEMBERS, winners);
		Assert.assertEquals(this.groupController.get(tournamentDTO).size(), 1);
	}

	@Test
	public void oneGroupTwoWinners() {
		final int winners = 2;
		final TournamentDTO tournamentDTO = this.createTournament(1, MEMBERS, winners);
		Assert.assertEquals(this.groupController.get(tournamentDTO).size(), 2);
        this.checkGroupRules(tournamentDTO, winners);
	}

	@DataProvider(name = "groupsAndWinnersScenarios")
	public Object[][] groupsAndWinnersScenarios() {
		return new Object[][]{
				// groups, winners, expectedGroupCount
				{2, 1, 3}, {2, 2, 5}, {3, 1, 6}, {3, 2, 10}, {4, 1, 7}, {4, 2, 11}, {5, 1, 12}, {5, 2, 20}, {6, 1, 13},
				{6, 2, 21}, {7, 1, 14}, {7, 2, 22}, {8, 1, 15}, {8, 2, 23}, {9, 1, 24}, {9, 2, 40}, {10, 1, 25},
				{10, 2, 41}, {11, 1, 26}, {11, 2, 42}, {12, 1, 27}, {12, 2, 43}, {13, 1, 28}, {13, 2, 44}, {14, 1, 29},
				{14, 2, 45}, {15, 1, 30}, {15, 2, 46}, {16, 1, 31}, {16, 2, 47},};
	}

	@Test(dataProvider = "groupsAndWinnersScenarios")
	public void shouldCreateExpectedGroupsAndLinks(int groups, int winners, int expectedGroupCount) {
		final TournamentDTO tournamentDTO = this.createTournament(groups, MEMBERS, winners);
		Assert.assertEquals(this.groupController.get(tournamentDTO).size(), expectedGroupCount);
        this.checkGroupRules(tournamentDTO, winners);
	}

	@AfterMethod
	@Override
	public void deleteTournament() {
		super.deleteTournament();
	}
}
