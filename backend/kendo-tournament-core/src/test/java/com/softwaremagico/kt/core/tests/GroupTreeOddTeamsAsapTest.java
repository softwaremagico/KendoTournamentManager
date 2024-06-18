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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

@SpringBootTest
@Test(groups = {"groupsTest"})
public class GroupTreeOddTeamsAsapTest extends AbstractTestNGSpringContextTests {

    private static final String TOURNAMENT_NAME = "TournamentTest";
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

    private Tournament tournament;
    private Tournament tournamentTwoWinners;

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

    @BeforeClass
    public void addTournaments() {
        Assert.assertEquals(tournamentProvider.count(), 0);
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournament = tournamentProvider.save(newTournament);
        Assert.assertEquals(tournamentProvider.count(), 1);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "true"));

        Tournament newTournamentTwoWinners = new Tournament(TOURNAMENT_TWO_WINNERS_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournamentTwoWinners = tournamentProvider.save(newTournamentTwoWinners);
        Assert.assertEquals(tournamentProvider.count(), 2);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentTwoWinners, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "2"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentTwoWinners, TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "true"));
    }

    @AfterMethod
    public void removeGroups() {
        groupProvider.delete(tournament);
        groupProvider.delete(tournamentTwoWinners);
    }


    @Test
    public void threeStartingGroupsOneWinners() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 6);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 5);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 1, 0);

        checkLink(groupLinks.get(3), 0, 0, 0);
        checkLink(groupLinks.get(4), 1, 0, 0);
    }


    @Test
    public void threeStartingGroupsTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 10);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 12);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 3, 0);
        checkLink(groupLinks.get(3), 0, 2, 1);
        checkLink(groupLinks.get(4), 1, 2, 1);
        checkLink(groupLinks.get(5), 2, 1, 1);

        checkLink(groupLinks.get(6), 0, 0, 0);
        checkLink(groupLinks.get(7), 1, 0, 0);
        checkLink(groupLinks.get(8), 2, 1, 0);
        checkLink(groupLinks.get(9), 3, 1, 0);

        checkLink(groupLinks.get(10), 0, 0, 0);
        checkLink(groupLinks.get(11), 1, 0, 0);
    }


    @Test
    public void fourStartingGroupsOneWinners() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(3, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 7);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 6);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 0, 0);
        checkLink(groupLinks.get(3), 3, 1, 0);

        checkLink(groupLinks.get(4), 0, 0, 0);
        checkLink(groupLinks.get(5), 1, 0, 0);
    }


    @Test
    public void fourStartingGroupsTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 11);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 14);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 2, 0);
        checkLink(groupLinks.get(3), 3, 3, 0);
        checkLink(groupLinks.get(4), 0, 3, 1);
        checkLink(groupLinks.get(5), 1, 2, 1);
        checkLink(groupLinks.get(6), 2, 1, 1);
        checkLink(groupLinks.get(7), 3, 0, 1);

        checkLink(groupLinks.get(8), 0, 0, 0);
        checkLink(groupLinks.get(9), 1, 0, 0);
        checkLink(groupLinks.get(10), 2, 1, 0);
        checkLink(groupLinks.get(11), 3, 1, 0);

        checkLink(groupLinks.get(12), 0, 0, 0);
        checkLink(groupLinks.get(13), 1, 0, 0);

        //Removing last group
        treeTournamentHandler.removeGroup(tournamentTwoWinners, 0, 3);
        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 10);

        groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 12);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 3, 0);
        checkLink(groupLinks.get(3), 0, 2, 1);
        checkLink(groupLinks.get(4), 1, 2, 1);
        checkLink(groupLinks.get(5), 2, 1, 1);

        checkLink(groupLinks.get(6), 0, 0, 0);
        checkLink(groupLinks.get(7), 1, 0, 0);
        checkLink(groupLinks.get(8), 2, 1, 0);
        checkLink(groupLinks.get(9), 3, 1, 0);

        checkLink(groupLinks.get(10), 0, 0, 0);
        checkLink(groupLinks.get(11), 1, 0, 0);
    }


    @Test
    public void fiveStartingGroupsOneWinners() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(3, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(4, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 12);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 11);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 2, 0);
        checkLink(groupLinks.get(3), 3, 2, 0);
        checkLink(groupLinks.get(4), 4, 3, 0);

        checkLink(groupLinks.get(5), 0, 0, 0);
        checkLink(groupLinks.get(6), 1, 0, 0);
        checkLink(groupLinks.get(7), 2, 1, 0);
        checkLink(groupLinks.get(8), 3, 1, 0);

        checkLink(groupLinks.get(9), 0, 0, 0);
        checkLink(groupLinks.get(10), 1, 0, 0);
    }


    /*
                                        ┌───────────┐
                             ┌──────────►  Group0   ├────┐
                             │          └───────────┘    │  ┌──────────┐
                             │                           ├──►  Group0  ├──┐
                             │          ┌───────────┐    │  └──────────┘  │
                             │   ┌──────►  Group1   ├────┘                │
┌──────────┐                 │   │      └───────────┘                     │    ┌──────────┐
│  Group0  ├─────────────────┤   │                                        ├────►  Group0  ├────┐
└──────────┘                 │   │      ┌───────────┐                     │    └──────────┘    │
                             │   │   ┌──►  Group2   ├────┐                │                    │
┌───────────┐                │   │   │  └───────────┘    │  ┌───────────┐ │                    │
│  Group1   ├────────────────┼───┤   │                   ├──►  Group1   ├─┘                    │
└───────────┘                │   │   │  ┌───────────┐    │  └───────────┘                      │
                  ┌─────┬────┼───┼───┼──┤  Group3   ├────┘                                     │
┌───────────┐     │     │    │   │   │  └───────────┘                                          │ ┌──────────┐
│  Group2   ├─────┼─────┼────┼───┼───┤                                                         ├─►  Group0  │
└───────────┘     │     │    │   │   │  ┌───────────┐                                          │ └──────────┘
                  │     │    └───┴───┼──┤  Group4   ├────┐                                     │
┌───────────┐     │     │            │  └───────────┘    │  ┌───────────┐                      │
│  Group3   ├─────┼─────┤            │                   ├──►  Group2   ├─┐                    │
└───────────┘     │     │            │  ┌───────────┐    │  └───────────┘ │                    │
                  │     │            └──┤  Group5   ├────┘                │                    │
┌───────────┐     │     │               └───────────┘                     │     ┌──────────┐   │
│  Group4   ├─────┤     │                                                 ├─────►  Group1  ├───┘
└───────────┘     │     │               ┌───────────┐                     │     └──────────┘
                  │     └───────────────►  Group6   ├────┐                │
                  │                     └───────────┘    │  ┌───────────┐ │
                  │                                      ├──►  Group3   ├─┘
                  │                     ┌───────────┐    │  └───────────┘
                  └─────────────────────►  Group7   ├────┘
                                        └───────────┘
 */
    @Test
    public void fiveStartingGroupsTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(4, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 20);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 24);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 2, 0);
        checkLink(groupLinks.get(3), 3, 6, 0);
        checkLink(groupLinks.get(4), 4, 7, 0);
        checkLink(groupLinks.get(5), 0, 4, 1);
        checkLink(groupLinks.get(6), 1, 4, 1);
        checkLink(groupLinks.get(7), 2, 5, 1);
        checkLink(groupLinks.get(8), 3, 3, 1);
        checkLink(groupLinks.get(9), 4, 3, 1);

        checkLink(groupLinks.get(10), 0, 0, 0);
        checkLink(groupLinks.get(11), 1, 0, 0);
        checkLink(groupLinks.get(12), 2, 1, 0);
        checkLink(groupLinks.get(13), 3, 1, 0);
        checkLink(groupLinks.get(14), 4, 2, 0);
        checkLink(groupLinks.get(15), 5, 2, 0);
        checkLink(groupLinks.get(16), 6, 3, 0);
        checkLink(groupLinks.get(17), 7, 3, 0);

        checkLink(groupLinks.get(18), 0, 0, 0);
        checkLink(groupLinks.get(19), 1, 0, 0);
        checkLink(groupLinks.get(20), 2, 1, 0);
        checkLink(groupLinks.get(21), 3, 1, 0);

        checkLink(groupLinks.get(22), 0, 0, 0);
        checkLink(groupLinks.get(23), 1, 0, 0);

        //Removing last group
        treeTournamentHandler.removeGroup(tournamentTwoWinners, 0, 4);
        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 11);

        groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 14);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 2, 0);
        checkLink(groupLinks.get(3), 3, 3, 0);
        checkLink(groupLinks.get(4), 0, 3, 1);
        checkLink(groupLinks.get(5), 1, 2, 1);
        checkLink(groupLinks.get(6), 2, 1, 1);
        checkLink(groupLinks.get(7), 3, 0, 1);

        checkLink(groupLinks.get(8), 0, 0, 0);
        checkLink(groupLinks.get(9), 1, 0, 0);
        checkLink(groupLinks.get(10), 2, 1, 0);
        checkLink(groupLinks.get(11), 3, 1, 0);

        checkLink(groupLinks.get(12), 0, 0, 0);
        checkLink(groupLinks.get(13), 1, 0, 0);
    }



    @Test
    public void sixStartingGroupsOneWinners() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(3, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(4, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(5, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 13);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 12);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 2, 0);
        checkLink(groupLinks.get(3), 3, 1, 0);
        checkLink(groupLinks.get(4), 4, 2, 0);
        checkLink(groupLinks.get(5), 5, 3, 0);

        checkLink(groupLinks.get(6), 0, 0, 0);
        checkLink(groupLinks.get(7), 1, 0, 0);
        checkLink(groupLinks.get(8), 2, 1, 0);
        checkLink(groupLinks.get(9), 3, 1, 0);

        checkLink(groupLinks.get(10), 0, 0, 0);
        checkLink(groupLinks.get(11), 1, 0, 0);
    }



    /*
                                        ┌───────────┐
              ┌─────────────────────────►  Group0   ├────┐
              │                         └───────────┘    │  ┌──────────┐
              │                                          ├──►  Group0  ├──┐
              │                         ┌───────────┐    │  └──────────┘  │
              │    ┌────────────────────►  Group1   ├────┘                │
┌──────────┐  │    │                    └───────────┘                     │    ┌──────────┐
│  Group0  ├──┤    │                                                      ├────►  Group0  ├────┐
└──────────┘  │    │                    ┌───────────┐                     │    └──────────┘    │
              │    │    ┌──┬────────────►  Group2   ├────┐                │                    │
┌───────────┐ │    │    │  │            └───────────┘    │  ┌───────────┐ │                    │
│  Group1   ├─┼────┤    │  │                             ├──►  Group1   ├─┘                    │
└───────────┘ │    │    │  │            ┌───────────┐    │  └───────────┘                      │
              │    │    │  │   ┌───┬────┤  Group3   ├────┘                                     │
┌───────────┐ │    │    │  │   │   │    └───────────┘                                          │ ┌──────────┐
│  Group2   ├─┼────┼────┼──┤   │   │                                                           ├─►  Group0  │
└───────────┘ │    │    │  │   │   │    ┌───────────┐                                          │ └──────────┘
              └────┴────┼──┼───┼───┼────┤  Group4   ├────┐                                     │
┌───────────┐           │  │   │   │    └───────────┘    │  ┌───────────┐                      │
│  Group3   ├───────────┤  │   │   │                     ├──►  Group2   ├─┐                    │
└───────────┘           │  │   │   │    ┌───────────┐    │  └───────────┘ │                    │
                        └──┴───┼───┼────►  Group5   ├────┘                │                    │
┌───────────┐                  │   │    └───────────┘                     │     ┌──────────┐   │
│  Group4   ├──────────────────┤   │                                      ├─────►  Group1  ├───┘
└───────────┘                  │   │    ┌───────────┐                     │     └──────────┘
                               └───┼────►  Group6   ├────┐                │
┌───────────┐                      │    └───────────┘    │  ┌───────────┐ │
│  Group5   ├──────────────────────┤                     ├──►  Group3   ├─┘
└───────────┘                      │    ┌───────────┐    │  └───────────┘
                                   └────►  Group7   ├────┘
                                        └───────────┘
 */
    @Test
    public void sixStartingGroupsTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(4, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(5, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 21);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 26);

        checkLink(groupLinks.get(0), 0, 0, 0);
        checkLink(groupLinks.get(1), 1, 1, 0);
        checkLink(groupLinks.get(2), 2, 2, 0);
        checkLink(groupLinks.get(3), 3, 5, 0);
        checkLink(groupLinks.get(4), 4, 6, 0);
        checkLink(groupLinks.get(5), 5, 7, 0);
        checkLink(groupLinks.get(6), 0, 4, 1);
        checkLink(groupLinks.get(7), 1, 4, 1);
        checkLink(groupLinks.get(8), 2, 5, 1);
        checkLink(groupLinks.get(9), 3, 2, 1);
        checkLink(groupLinks.get(10), 4, 3, 1);
        checkLink(groupLinks.get(11), 5, 3, 1);

        checkLink(groupLinks.get(12), 0, 0, 0);
        checkLink(groupLinks.get(13), 1, 0, 0);
        checkLink(groupLinks.get(14), 2, 1, 0);
        checkLink(groupLinks.get(15), 3, 1, 0);
        checkLink(groupLinks.get(16), 4, 2, 0);
        checkLink(groupLinks.get(17), 5, 2, 0);
        checkLink(groupLinks.get(18), 6, 3, 0);
        checkLink(groupLinks.get(19), 7, 3, 0);

        checkLink(groupLinks.get(20), 0, 0, 0);
        checkLink(groupLinks.get(21), 1, 0, 0);
        checkLink(groupLinks.get(22), 2, 1, 0);
        checkLink(groupLinks.get(23), 3, 1, 0);

        checkLink(groupLinks.get(24), 0, 0, 0);
        checkLink(groupLinks.get(25), 1, 0, 0);
    }


    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        groupProvider.deleteAll();
        tournamentExtraPropertyProvider.deleteAll();
        tournamentProvider.deleteAll();
    }

}
