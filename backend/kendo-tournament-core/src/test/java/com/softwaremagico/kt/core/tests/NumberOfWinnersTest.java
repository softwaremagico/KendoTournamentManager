package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.providers.GroupLinkProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.List;

@SpringBootTest
@Test(groups = {"numberOfWinners"})
public class NumberOfWinnersTest extends AbstractTestNGSpringContextTests {

    private static final String TOURNAMENT_TWO_WINNERS_NAME = "TournamentTest2";
    private static final int MEMBERS = 3;

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    @Autowired
    private TreeTournamentHandler treeTournamentHandler;

    @Autowired
    private GroupLinkProvider groupLinkProvider;

    @Autowired
    private GroupProvider groupProvider;

    @Autowired
    private TournamentController tournamentController;


    private Group generateGroup(int index, Tournament tournament) {
        Group group = new Group();
        group.setIndex(index);
        group.setLevel(0);
        group.setTournament(tournament);
        return group;
    }

    private void checkLink(GroupLink groupLink, int source, int destination, int winner) {
        Assert.assertEquals(groupLink.getSource().getLevel() + 1, groupLink.getDestination().getLevel());
        Assert.assertEquals(groupLink.getSource().getIndex(), source);
        Assert.assertEquals(groupLink.getDestination().getIndex(), destination);
        Assert.assertEquals(groupLink.getWinner(), winner);
    }

    @AfterMethod
    public void removeGroups() {
        groupProvider.deleteAll();
        tournamentExtraPropertyProvider.deleteAll();
        tournamentProvider.deleteAll();
    }

    @Test
    public void swapBetweenOneAndTwoWinners() {
        Tournament tournamentChangingWinners = new Tournament(TOURNAMENT_TWO_WINNERS_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournamentChangingWinners = tournamentProvider.save(tournamentChangingWinners);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentChangingWinners, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "1"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentChangingWinners, TournamentExtraPropertyKey.ODD_TEAMS_RESOLVED_ASAP, "false"));


        treeTournamentHandler.addGroup(tournamentChangingWinners, generateGroup(0, tournamentChangingWinners));
        treeTournamentHandler.addGroup(tournamentChangingWinners, generateGroup(1, tournamentChangingWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentChangingWinners).size(), 3);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentChangingWinners);
        Assert.assertEquals(groupLinks.size(), 2);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 0, 0);

        //Change to two winners
        tournamentController.setNumberOfWinners(tournamentChangingWinners.getId(), 2, null);

        Assert.assertEquals(groupProvider.getGroups(tournamentChangingWinners).size(), 5);

        groupLinks = groupLinkProvider.generateLinks(tournamentChangingWinners);
        Assert.assertEquals(groupLinks.size(), 6);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 0, 1, 1);
        checkLink(groupLinks.get(3), 1, 0, 1);

        checkLink(groupLinks.get(4), 0, 0, 0);
        checkLink(groupLinks.get(5), 1, 0, 0);

        //Change back to one winner
        tournamentController.setNumberOfWinners(tournamentChangingWinners.getId(), 1, null);

        //Groups already there
        Assert.assertEquals(groupProvider.getGroups(tournamentChangingWinners).size(), 3);

        groupLinks = groupLinkProvider.generateLinks(tournamentChangingWinners);
        Assert.assertEquals(groupLinks.size(), 2);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 0, 0);

        //Change back to two winners again!
        tournamentController.setNumberOfWinners(tournamentChangingWinners.getId(), 2, null);

        Assert.assertEquals(groupProvider.getGroups(tournamentChangingWinners).size(), 5);

        groupLinks = groupLinkProvider.generateLinks(tournamentChangingWinners);
        Assert.assertEquals(groupLinks.size(), 6);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 0, 1, 1);
        checkLink(groupLinks.get(3), 1, 0, 1);

        checkLink(groupLinks.get(4), 0, 0, 0);
        checkLink(groupLinks.get(5), 1, 0, 0);

        //Change back to one winner
        tournamentController.setNumberOfWinners(tournamentChangingWinners.getId(), 1, null);

        //Groups already there
        Assert.assertEquals(groupProvider.getGroups(tournamentChangingWinners).size(), 3);

        groupLinks = groupLinkProvider.generateLinks(tournamentChangingWinners);
        Assert.assertEquals(groupLinks.size(), 2);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 0, 0);
    }

    @Test
    public void addingAndRemovingGroupsWithTwoWinners() {
        Tournament tournamentChangingWinners = new Tournament(TOURNAMENT_TWO_WINNERS_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournamentChangingWinners = tournamentProvider.save(tournamentChangingWinners);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentChangingWinners, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "1"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentChangingWinners, TournamentExtraPropertyKey.ODD_TEAMS_RESOLVED_ASAP, "false"));


        treeTournamentHandler.addGroup(tournamentChangingWinners, generateGroup(0, tournamentChangingWinners));
        treeTournamentHandler.addGroup(tournamentChangingWinners, generateGroup(1, tournamentChangingWinners));
        treeTournamentHandler.addGroup(tournamentChangingWinners, generateGroup(2, tournamentChangingWinners));
        treeTournamentHandler.addGroup(tournamentChangingWinners, generateGroup(3, tournamentChangingWinners));

        tournamentController.setNumberOfWinners(tournamentChangingWinners.getId(), 2, null);

        Assert.assertEquals(groupProvider.getGroups(tournamentChangingWinners).size(), 11);
    }


    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        groupProvider.deleteAll();
        tournamentExtraPropertyProvider.deleteAll();
        tournamentProvider.deleteAll();
    }
}
