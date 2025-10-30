package com.softwaremagico.kt.core.tests.tournament;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 SoftwareMagico
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
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
@Test(groups = {"tournamentLinkTest"})
public class TournamentLinkTests extends TreeTournamentBasedTests {

    private static final int MEMBERS = 3;

    @Autowired
    private GroupController groupController;

    @Autowired
    private GroupLinkController groupLinkController;

    private List<GroupLinkDTO> getIncomingArrows(int groupIndex, int groupLevel, List<GroupLinkDTO> groupLinkDTOS) {
        return groupLinkDTOS.stream().filter(groupLinkDTO ->
                groupLinkDTO.getDestination().getIndex() == groupIndex && groupLinkDTO.getDestination().getLevel() == groupLevel
        ).toList();
    }

    private List<GroupLinkDTO> getOutcomingArrows(int groupIndex, int groupLevel, List<GroupLinkDTO> groupLinkDTOS) {
        return groupLinkDTOS.stream().filter(groupLinkDTO ->
                groupLinkDTO.getSource().getIndex() == groupIndex && groupLinkDTO.getSource().getLevel() == groupLevel
        ).toList();
    }

    private int getMaxLevel(List<GroupDTO> groups) {
        return groups.stream().map(GroupDTO::getLevel).max(Integer::compareTo).orElse(0);
    }


    private void assertEachPoolIncomingArrowsNumber(TournamentDTO tournament) {
        final List<GroupLinkDTO> groupLinkDTOs = groupLinkController.getLinks(tournament);
        final List<GroupDTO> groups = groupController.get(tournament);
        if (groups.size() > 1) {
            for (GroupDTO groupDTO : groups) {
                if (groupDTO.getLevel() == 0) {
                    Assert.assertEquals(getIncomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), 0);
                } else if (groupDTO.getLevel() > 1) {
                    //Each pool (except the first and second column) must always have exactly two incoming arrows.
                    Assert.assertEquals(getIncomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), 2);
                } else {
                    //Level 1 may have one or two incoming arrows.
                    final int incomingArrows = getIncomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size();
                    Assert.assertTrue(incomingArrows >= 1 && incomingArrows <= 2);
                }
            }
        }
    }

    private void assertEachPoolOutcomingArrowsNumber(TournamentDTO tournament, int winners) {
        final List<GroupLinkDTO> groupLinkDTOs = groupLinkController.getLinks(tournament);
        final List<GroupDTO> groups = groupController.get(tournament);
        if (groups.size() > 1) {
            for (GroupDTO groupDTO : groups) {
                if (groupDTO.getLevel() == 0) {
                    Assert.assertEquals(getOutcomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), winners);
                    //Last level
                } else if (groupDTO.getLevel() == getMaxLevel(groups)) {
                    Assert.assertEquals(getOutcomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), 0);
                } else {
                    Assert.assertEquals(getOutcomingArrows(groupDTO.getIndex(), groupDTO.getLevel(), groupLinkDTOs).size(), 1);
                }
            }
        }
    }

    private void checkNotTwoFirstWinnersAsIncomingArrow(TournamentDTO tournament, int winners) {
        //If I have two first winners together, means that 2nd winners together are full.
        if (winners == 1) {
            return;
        }
        final List<GroupLinkDTO> secondLevelGroups = getGroupLinksToLevel(tournament, 1);
        //Check if two winners are together. Two first winners are together if the destination groups number for first winners are less than the original one.
        Assert.assertEquals(getGroupFromLevel(tournament, 0).size(),
                secondLevelGroups.stream().filter(groupLinkDTO -> groupLinkDTO.getWinner() == 0).toList().size());
    }

    private List<GroupLinkDTO> getGroupLinksToLevel(TournamentDTO tournament, int level) {
        return groupLinkController.getLinks(tournament).stream()
                .filter(groupLinkDTO -> groupLinkDTO.getDestination().getLevel() == level).toList();
    }

    private List<GroupDTO> getGroupFromLevel(TournamentDTO tournament, int level) {
        return groupController.get(tournament).stream().filter(groupDTO -> groupDTO.getLevel() == level).toList();
    }


    private void noSecondPlaceWinnerOnByeUnlessNecessary(TournamentDTO tournament) {
        final List<GroupLinkDTO> groupLinkDTOs = getGroupLinksToLevel(tournament, 1);
        final Set<GroupDTO> byes = getByes(tournament, 1);
        for (GroupLinkDTO groupLinkDTO : groupLinkDTOs) {
            //If a second winner goes to a bye
            if (groupLinkDTO.getWinner() == 1 && byes.contains(groupLinkDTO.getDestination())) {
                //All first winners are on a bye.
                Assert.assertTrue(byes.containsAll(groupLinkDTOs.stream().filter(g -> g.getWinner() == 0)
                        .map(GroupLinkDTO::getDestination).toList()));
            }
        }
    }


    private Set<GroupDTO> getByes(TournamentDTO tournament, int level) {
        final List<GroupLinkDTO> groupLinkDTOs = getGroupLinksToLevel(tournament, level);
        Set<GroupDTO> groupsFromLevel = new HashSet<>(getGroupFromLevel(tournament, level));
        Set<GroupDTO> seenGroups = new HashSet<>();
        for (GroupLinkDTO g : groupLinkDTOs) {
            if (!seenGroups.add(g.getDestination())) {
                groupsFromLevel.remove(g.getDestination());
            }
        }
        return groupsFromLevel;
    }

    private void checkGroupRules(TournamentDTO tournamentDTO, int winners) {
        assertEachPoolIncomingArrowsNumber(tournamentDTO);
        assertEachPoolOutcomingArrowsNumber(tournamentDTO, winners);
        checkNotTwoFirstWinnersAsIncomingArrow(tournamentDTO, winners);
        noSecondPlaceWinnerOnByeUnlessNecessary(tournamentDTO);
    }


    @Test
    public void oneGroupOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(1, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 1);
    }

    @Test
    public void oneGroupTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(1, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 2);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void twoGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(2, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 3);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void twoGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(2, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 5);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void threeGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(3, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 6);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void threeGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(3, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 10);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void fourGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(4, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 7);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void fourGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(4, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 11);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void fiveGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(5, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 12);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void fiveGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(5, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 20);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void sixGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(6, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 13);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void sixGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(6, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 21);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void sevenGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(7, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 14);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void sevenGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(7, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 22);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void eightGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(8, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 15);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void eightGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(8, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 23);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void nineGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(9, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 24);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void nineGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(9, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 40);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void tenGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(10, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 25);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void tenGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(10, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 41);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void elevenGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(11, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 26);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void elevenGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(11, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 42);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void twelveGroupsOneWinner() {
        final int winners = 1;
        TournamentDTO tournamentDTO = createTournament(12, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 27);
        checkGroupRules(tournamentDTO, winners);
    }

    @Test
    public void twelveGroupsTwoWinners() {
        final int winners = 2;
        TournamentDTO tournamentDTO = createTournament(12, MEMBERS, winners);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 43);
        checkGroupRules(tournamentDTO, winners);
    }

    @AfterMethod
    public void deleteTournament() {
        super.deleteTournament();
    }
}


