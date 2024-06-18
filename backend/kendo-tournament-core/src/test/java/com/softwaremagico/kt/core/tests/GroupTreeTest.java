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
public class GroupTreeTest extends AbstractTestNGSpringContextTests {

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

    private void checkLink(GroupLink groupLink, int source, int destination) {
        Assert.assertEquals(groupLink.getSource().getLevel() + 1, groupLink.getDestination().getLevel());
        Assert.assertEquals(groupLink.getSource().getIndex(), source);
        Assert.assertEquals(groupLink.getDestination().getIndex(), destination);
    }

    @BeforeClass
    public void addTournaments() {
        Assert.assertEquals(tournamentProvider.count(), 0);
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournament = tournamentProvider.save(newTournament);
        Assert.assertEquals(tournamentProvider.count(), 1);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.ODD_TEAMS_RESOLVED_ASAP, "false"));

        Tournament newTournamentTwoWinners = new Tournament(TOURNAMENT_TWO_WINNERS_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournamentTwoWinners = tournamentProvider.save(newTournamentTwoWinners);
        Assert.assertEquals(tournamentProvider.count(), 2);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentTwoWinners, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "2"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentTwoWinners, TournamentExtraPropertyKey.ODD_TEAMS_RESOLVED_ASAP, "false"));
    }

    @AfterMethod
    public void removeGroups() {
        groupProvider.delete(tournament);
        groupProvider.delete(tournamentTwoWinners);
    }


    /*          ┌──────┐┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3││Group4│
                └┬─────┘└┬─────┘└┬─────┘└┬─────┘
                ┌▽───────▽┐┌─────▽───────▽┐
                │Group5   ││Group6        │
                └┬────────┘└┬─────────────┘
                ┌▽──────────▽┐
                │Group7      │
                └────────────┘
     */
    @Test
    public void basicTree() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(3, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 7);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 6);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);

        checkLink(groupLinks.get(4), 0, 0);
        checkLink(groupLinks.get(5), 1, 0);

        //Removing first group
        Group lastGroup = groupLinks.get(3).getSource();
        treeTournamentHandler.removeGroup(tournament, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 6);

        groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 5);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);

        checkLink(groupLinks.get(3), 0, 0);
        checkLink(groupLinks.get(4), 1, 0);

        //Removing second group
        lastGroup = groupLinks.get(2).getSource();
        treeTournamentHandler.removeGroup(tournament, lastGroup.getLevel(), lastGroup.getIndex());
        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 3);

        groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 2);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);

        //Removing third group
        lastGroup = groupLinks.get(1).getSource();
        treeTournamentHandler.removeGroup(tournament, lastGroup.getLevel(), lastGroup.getIndex());
        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 1);

        groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 0);
    }


    /*
                ┌──────┐┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3││Group4│
                └────┬┬┘└─┬───┬┘└─┬───┬┘└─┬┬───┘
                    ┌││───│──┐└───│───│──┐││
                    │└│───│──│───┐│  ┌│──│┘│
                    │ └──┐│  └───│┘  │└──│┐│
                    │ ┌──│┘   ┌──│───│───││┘
                ┌───▽─▽┐┌▽────▽┐┌▽───▽─┐┌▽▽────┐
                │Group6││Group5││Group8││Group7│
                └┬─────┘└┬─────┘└┬─────┘└┬─────┘
                ┌▽───────▽┐┌─────▽───────▽┐
                │Group9   ││Group10       │
                └┬────────┘└┬─────────────┘
                ┌▽──────────▽┐
                │Group11     │
                └────────────┘
    */
    @Test
    public void basicTreeTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 11);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 14);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 3, 3);
        checkLink(groupLinks.get(4), 0, 3);
        checkLink(groupLinks.get(5), 1, 2);
        checkLink(groupLinks.get(6), 2, 1);
        checkLink(groupLinks.get(7), 3, 0);

        checkLink(groupLinks.get(8), 0, 0);
        checkLink(groupLinks.get(9), 1, 0);
        checkLink(groupLinks.get(10), 2, 1);
        checkLink(groupLinks.get(11), 3, 1);

        checkLink(groupLinks.get(12), 0, 0);
        checkLink(groupLinks.get(13), 1, 0);

        //Removing first group
        Group lastGroup = groupLinks.get(7).getSource();
        treeTournamentHandler.removeGroup(tournamentTwoWinners, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 9);

        groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 11);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 0, 1);
        checkLink(groupLinks.get(4), 1, 2);
        checkLink(groupLinks.get(5), 2, 0);

        checkLink(groupLinks.get(6), 0, 0);
        checkLink(groupLinks.get(7), 1, 0);
        checkLink(groupLinks.get(8), 2, 1);

        checkLink(groupLinks.get(9), 0, 0);
        checkLink(groupLinks.get(10), 1, 0);
    }


    /*          ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3││Group4││Group5││Group6││Group7││Group8│
                └┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘
                ┌▽───────▽┐┌─────▽───────▽┐┌─────▽───────▽┐┌─────▽───────▽┐
                │Group9   ││Group10       ││Group11       ││Group12       │
                └┬────────┘└┬─────────────┘└┬─────────────┘└┬─────────────┘
                ┌▽──────────▽┐┌─────────────▽───────────────▽┐
                │Group13     ││Group14                       │
                └┬───────────┘└┬─────────────────────────────┘
                ┌▽─────────────▽┐
                │Group15        │
                └───────────────┘
      */
    @Test
    public void biggerTree() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(3, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(4, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(5, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(6, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(7, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 15);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 14);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);
        checkLink(groupLinks.get(5), 5, 2);
        checkLink(groupLinks.get(6), 6, 3);
        checkLink(groupLinks.get(7), 7, 3);

        checkLink(groupLinks.get(8), 0, 0);
        checkLink(groupLinks.get(9), 1, 0);
        checkLink(groupLinks.get(10), 2, 1);
        checkLink(groupLinks.get(11), 3, 1);

        checkLink(groupLinks.get(12), 0, 0);
        checkLink(groupLinks.get(13), 1, 0);

        //Removing first group
        Group lastGroup = groupLinks.get(7).getSource();
        treeTournamentHandler.removeGroup(tournament, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 14);

        groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 13);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);
        checkLink(groupLinks.get(5), 5, 2);
        checkLink(groupLinks.get(6), 6, 3);

        checkLink(groupLinks.get(7), 0, 0);
        checkLink(groupLinks.get(8), 1, 0);
        checkLink(groupLinks.get(9), 2, 1);
        checkLink(groupLinks.get(10), 3, 1);

        checkLink(groupLinks.get(11), 0, 0);
        checkLink(groupLinks.get(12), 1, 0);

        //Removing second group
        lastGroup = groupLinks.get(6).getSource();
        treeTournamentHandler.removeGroup(tournament, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 12);

        groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 11);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);
        checkLink(groupLinks.get(5), 5, 2);

        checkLink(groupLinks.get(6), 0, 0);
        checkLink(groupLinks.get(7), 1, 0);
        checkLink(groupLinks.get(8), 2, 1);

        checkLink(groupLinks.get(9), 0, 0);
        checkLink(groupLinks.get(10), 1, 0);
    }


    /*
                ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3││Group4││Group5││Group6││Group7││Group8│
                └────┬┬┘└────┬┬┘└┬────┬┘└┬────┬┘└┬────┬┘└┬────┬┘└┬─┬───┘└┬┬────┘
                    ┌││──────││──│────│──│───┐└──│────│──│────│──│─│─────││────┐
                    │││      ││┌─│───┐└──│───│───│────│──│────│──│─│──┐  ││    │
                    │││      └││─│───│───│───│───│────│──│───┐│  │ │  │  ││    │
                    │└│───────││─│───│───│───│───│──┐ │  │   │└──│─│──│┐ ││    │
                    │ └───────││─│───│───│──┐│  ┌│──│┐└──│───│───│─│──││─││────│┐
                    │         └│─│─┐ └───│──││──│┘  │└───│───│───│─│──││─┘│    ││
                    │  ┌───────│─┘ │    ┌│─┐│└──│───│────┘┌──│───│─│──││──┘    ││
                    │  │       │┌──│────│┘ └│───│───│─────│──│───┘ │  ││       ││
                ┌───▽──▽┐┌─────▽▽┐┌▽────▽─┐┌▽───▽─┐┌▽─────▽┐┌▽─────▽┐┌▽▽─────┐┌▽▽─────┐
                │Group11││Group12││Group10││Group9││Group16││Group15││Group14││Group13│
                └┬──────┘└┬──────┘└┬──────┘└┬─────┘└┬──────┘└┬──────┘└┬──────┘└┬──────┘
                ┌▽────────▽┐┌──────▽────────▽┐┌─────▽────────▽┐┌──────▽────────▽┐
                │Group18   ││Group17         ││Group20        ││Group19         │
                └┬─────────┘└┬───────────────┘└┬──────────────┘└┬───────────────┘
                ┌▽───────────▽┐┌───────────────▽────────────────▽┐
                │Group21      ││Group22                          │
                └┬────────────┘└┬────────────────────────────────┘
                ┌▽──────────────▽┐
                │Group23         │
                └────────────────┘
    */
    @Test
    public void biggerTreeTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(4, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(5, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(6, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(7, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 23);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 30);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 3, 3);
        checkLink(groupLinks.get(4), 4, 4);
        checkLink(groupLinks.get(5), 5, 5);
        checkLink(groupLinks.get(6), 6, 6);
        checkLink(groupLinks.get(7), 7, 7);
        checkLink(groupLinks.get(8), 0, 7);
        checkLink(groupLinks.get(9), 1, 6);
        checkLink(groupLinks.get(10), 2, 5);
        checkLink(groupLinks.get(11), 3, 4);
        checkLink(groupLinks.get(12), 4, 3);
        checkLink(groupLinks.get(13), 5, 2);
        checkLink(groupLinks.get(14), 6, 1);
        checkLink(groupLinks.get(15), 7, 0);

        checkLink(groupLinks.get(16), 0, 0);
        checkLink(groupLinks.get(17), 1, 0);
        checkLink(groupLinks.get(18), 2, 1);
        checkLink(groupLinks.get(19), 3, 1);
        checkLink(groupLinks.get(20), 4, 2);
        checkLink(groupLinks.get(21), 5, 2);
        checkLink(groupLinks.get(22), 6, 3);
        checkLink(groupLinks.get(23), 7, 3);

        checkLink(groupLinks.get(24), 0, 0);
        checkLink(groupLinks.get(25), 1, 0);
        checkLink(groupLinks.get(26), 2, 1);
        checkLink(groupLinks.get(27), 3, 1);

        checkLink(groupLinks.get(28), 0, 0);
        checkLink(groupLinks.get(29), 1, 0);

        //Removing first group
        Group lastGroup = groupLinks.get(15).getSource();
        treeTournamentHandler.removeGroup(tournamentTwoWinners, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 21);

        groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 27);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 3, 3);
        checkLink(groupLinks.get(4), 4, 4);
        checkLink(groupLinks.get(5), 5, 5);
        checkLink(groupLinks.get(6), 6, 6);
        checkLink(groupLinks.get(7), 0, 1);
        checkLink(groupLinks.get(8), 1, 2);
        checkLink(groupLinks.get(9), 2, 3);
        checkLink(groupLinks.get(10), 3, 4);
        checkLink(groupLinks.get(11), 4, 5);
        checkLink(groupLinks.get(12), 5, 6);
        checkLink(groupLinks.get(13), 6, 0);

        checkLink(groupLinks.get(14), 0, 0);
        checkLink(groupLinks.get(15), 1, 0);
        checkLink(groupLinks.get(16), 2, 1);
        checkLink(groupLinks.get(17), 3, 1);
        checkLink(groupLinks.get(18), 4, 2);
        checkLink(groupLinks.get(19), 5, 2);
        checkLink(groupLinks.get(20), 6, 3);

        checkLink(groupLinks.get(21), 0, 0);
        checkLink(groupLinks.get(22), 1, 0);
        checkLink(groupLinks.get(23), 2, 1);
        checkLink(groupLinks.get(24), 3, 1);

        checkLink(groupLinks.get(25), 0, 0);
        checkLink(groupLinks.get(26), 1, 0);

        //Removing second group
        lastGroup = groupLinks.get(13).getSource();
        treeTournamentHandler.removeGroup(tournamentTwoWinners, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 18);

        groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 23);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 3, 3);
        checkLink(groupLinks.get(4), 4, 4);
        checkLink(groupLinks.get(5), 5, 5);
        checkLink(groupLinks.get(6), 0, 5);
        checkLink(groupLinks.get(7), 1, 4);
        checkLink(groupLinks.get(8), 2, 3);
        checkLink(groupLinks.get(9), 3, 2);
        checkLink(groupLinks.get(10), 4, 1);
        checkLink(groupLinks.get(11), 5, 0);

        checkLink(groupLinks.get(12), 0, 0);
        checkLink(groupLinks.get(13), 1, 0);
        checkLink(groupLinks.get(14), 2, 1);
        checkLink(groupLinks.get(15), 3, 1);
        checkLink(groupLinks.get(16), 4, 2);
        checkLink(groupLinks.get(17), 5, 2);


        checkLink(groupLinks.get(18), 0, 0);
        checkLink(groupLinks.get(19), 1, 0);
        checkLink(groupLinks.get(20), 2, 1);

        checkLink(groupLinks.get(21), 0, 0);
        checkLink(groupLinks.get(22), 1, 0);
    }


    /*
                ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3││Group4││Group5││Group6││Group7│
                └┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘
                ┌▽───────▽┐┌─────▽───────▽┐┌─────▽───────▽┐┌─────▽─┐
                │Group9   ││Group10       ││Group11       ││Group12│
                └┬────────┘└┬─────────────┘└┬─────────────┘└┬──────┘
                ┌▽──────────▽┐┌─────────────▽───────────────▽┐
                │Group13     ││Group14                       │
                └┬───────────┘└┬─────────────────────────────┘
                ┌▽─────────────▽┐
                │Group15        │
                └───────────────┘
    */
    @Test
    public void biggerTreeMissingOne() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(3, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(4, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(5, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(6, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 14);
        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 13);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);
        checkLink(groupLinks.get(5), 5, 2);
        checkLink(groupLinks.get(6), 6, 3);

        checkLink(groupLinks.get(7), 0, 0);
        checkLink(groupLinks.get(8), 1, 0);
        checkLink(groupLinks.get(9), 2, 1);
        checkLink(groupLinks.get(10), 3, 1);

        checkLink(groupLinks.get(11), 0, 0);
        checkLink(groupLinks.get(12), 1, 0);

        //Removing first group
        Group lastGroup = groupLinks.get(6).getSource();
        treeTournamentHandler.removeGroup(tournament, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 12);

        groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 11);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);
        checkLink(groupLinks.get(5), 5, 2);

        checkLink(groupLinks.get(6), 0, 0);
        checkLink(groupLinks.get(7), 1, 0);
        checkLink(groupLinks.get(8), 2, 1);

        checkLink(groupLinks.get(9), 0, 0);
        checkLink(groupLinks.get(10), 1, 0);

        //Removing second group
        lastGroup = groupLinks.get(5).getSource();
        treeTournamentHandler.removeGroup(tournament, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 11);

        groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 10);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);

        checkLink(groupLinks.get(5), 0, 0);
        checkLink(groupLinks.get(6), 1, 1);
        checkLink(groupLinks.get(7), 2, 1);

        checkLink(groupLinks.get(8), 0, 0);
        checkLink(groupLinks.get(9), 1, 0);
    }


    /*
                ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3││Group4││Group5││Group6│
                └┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘
                ┌▽───────▽┐┌─────▽───────▽┐┌─────▽───────▽┐
                │Group9   ││Group10       ││Group11       │
                └┬────────┘└┬─────────────┘└┬─────────────┘
                ┌▽──────────▽┐┌─────────────▽┐
                │Group13     ││Group14       │
                └┬───────────┘└┬─────────────┘
                ┌▽─────────────▽┐
                │Group15        │
                └───────────────┘
    */
    @Test
    public void biggerTreeMissingTwo() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(3, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(4, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(5, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 12);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 11);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);
        checkLink(groupLinks.get(5), 5, 2);

        checkLink(groupLinks.get(6), 0, 0);
        checkLink(groupLinks.get(7), 1, 0);
        checkLink(groupLinks.get(8), 2, 1);

        checkLink(groupLinks.get(9), 0, 0);
        checkLink(groupLinks.get(10), 1, 0);

        //Removing first group
        Group lastGroup = groupLinks.get(5).getSource();
        treeTournamentHandler.removeGroup(tournament, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 11);

        groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 10);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);

        checkLink(groupLinks.get(5), 0, 0);
        checkLink(groupLinks.get(6), 1, 1);
        checkLink(groupLinks.get(7), 2, 1);

        checkLink(groupLinks.get(8), 0, 0);
        checkLink(groupLinks.get(9), 1, 0);
    }


    /*
                ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3││Group4││Group5││Group6│
                └────┬┬┘└─┬───┬┘└────┬┬┘└────┬┬┘└┬┬────┘└┬┬────┘
                     ││   │   │      └│──────││──││──────││┐
                     ││   │   │       └──────││──││┐     │││
                     ││   │   └───────────┐ ┌││──│││─────│┘│
                     └│───│──────┐        │ │└│──│││─────│─│┐
                      └──┐│      │     ┌──│─┘ └──│││┐    │ ││
                      ┌──│┘   ┌──│─────│──│──────││││────┘ ││
                     ┌│──│────│──│─────│──│──────┘│││      ││
                     ││  │    │  │     │  │     ┌─┘││      ││
                ┌────▽▽┐┌▽────▽┐┌▽─────▽┐┌▽─────▽┐┌▽▽────┐┌▽▽─────┐
                │Group8││Group7││Group12││Group11││Group9││Group10│
                └┬─────┘└┬─────┘└┬──────┘└┬──────┘└┬─────┘└┬──────┘
                ┌▽───────▽┐┌─────▽────────▽┐┌──────▽───────▽┐
                │Group13  ││Group15        ││Group14        │
                └────────┬┘└┬──────────────┘└┬──────────────┘
                       ┌─│──┘                │
                       │ └┐     ┌────────────┘
                ┌──────▽┐┌▽─────▽┐
                │Group17││Group16│
                └┬──────┘└┬──────┘
                ┌▽────────▽┐
                │Group18   │
                └──────────┘
    */
    @Test
    public void biggerTreeMissingTwoWithTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(4, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(5, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 18);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 23);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 3, 3);
        checkLink(groupLinks.get(4), 4, 4);
        checkLink(groupLinks.get(5), 5, 5);
        checkLink(groupLinks.get(6), 0, 5);
        checkLink(groupLinks.get(7), 1, 4);
        checkLink(groupLinks.get(8), 2, 3);
        checkLink(groupLinks.get(9), 3, 2);
        checkLink(groupLinks.get(10), 4, 1);
        checkLink(groupLinks.get(11), 5, 0);

        checkLink(groupLinks.get(12), 0, 0);
        checkLink(groupLinks.get(13), 1, 0);
        checkLink(groupLinks.get(14), 2, 1);
        checkLink(groupLinks.get(15), 3, 1);
        checkLink(groupLinks.get(16), 4, 2);
        checkLink(groupLinks.get(17), 5, 2);

        checkLink(groupLinks.get(18), 0, 0);
        checkLink(groupLinks.get(19), 1, 0);
        checkLink(groupLinks.get(20), 2, 1);

        checkLink(groupLinks.get(21), 0, 0);
        checkLink(groupLinks.get(22), 1, 0);

        //Removing first group
        Group lastGroup = groupLinks.get(11).getSource();
        treeTournamentHandler.removeGroup(tournamentTwoWinners, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 16);

        groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 20);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 3, 3);
        checkLink(groupLinks.get(4), 4, 4);
        checkLink(groupLinks.get(5), 0, 1);
        checkLink(groupLinks.get(6), 1, 2);
        checkLink(groupLinks.get(7), 2, 3);
        checkLink(groupLinks.get(8), 3, 4);
        checkLink(groupLinks.get(9), 4, 0);

        checkLink(groupLinks.get(10), 0, 0);
        checkLink(groupLinks.get(11), 1, 0);
        checkLink(groupLinks.get(12), 2, 1);
        checkLink(groupLinks.get(13), 3, 1);
        checkLink(groupLinks.get(14), 4, 2);

        checkLink(groupLinks.get(15), 0, 0);
        checkLink(groupLinks.get(16), 1, 1);
        checkLink(groupLinks.get(17), 2, 1);

        checkLink(groupLinks.get(18), 0, 0);
        checkLink(groupLinks.get(19), 1, 0);
    }


    /*
                ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3││Group4││Group5│
                └┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬─────┘
                ┌▽───────▽┐┌─────▽───────▽┐┌─────▽┐
                │Group6   ││Group7        ││Group8│
                └┬────────┘└┬─────────────┘└┬─────┘
                ┌▽─────┐┌───▽───────────────▽┐
                │Group9││Group10             │
                └┬─────┘└┬───────────────────┘
                ┌▽───────▽┐
                │Group11  │
                └─────────┘
    */
    @Test
    public void fiveGroups() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(3, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(4, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 11);

        final List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 10);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);
        checkLink(groupLinks.get(3), 3, 1);
        checkLink(groupLinks.get(4), 4, 2);

        checkLink(groupLinks.get(5), 0, 0);
        checkLink(groupLinks.get(6), 1, 1);
        checkLink(groupLinks.get(7), 2, 1);

        checkLink(groupLinks.get(8), 0, 0);
        checkLink(groupLinks.get(9), 1, 0);
    }


/*
            ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐
            │Group1││Group2││Group3││Group4││Group5│
            └────┬┬┘└┬────┬┘└┬────┬┘└┬────┬┘└┬────┬┘
                ┌││──┘   ┌│──┘   ┌│──┘   ┌│──┘    │
                │└│──┐   │└──┐   │└──┐   │└──┐    │
            ┌───▽─▽┐┌▽───▽─┐┌▽───▽─┐┌▽───▽─┐┌▽────▽─┐
            │Group6││Group7││Group8││Group9││Group10│
            └┬─────┘└┬─────┘└┬─────┘└┬─────┘└┬──────┘
            ┌▽───────▽┐┌─────▽───────▽┐┌─────▽─┐
            │Group11  ││Group12       ││Group13│
            └┬────────┘└┬─────────────┘└┬──────┘
            ┌▽──────┐┌──▽───────────────▽┐
            │Group14││Group15            │
            └┬──────┘└┬──────────────────┘
            ┌▽────────▽┐
            │Group16   │
            └──────────┘
*/

    @Test
    public void fiveGroupsTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(4, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 16);

        final List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 20);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 3, 3);
        checkLink(groupLinks.get(4), 4, 4);
        checkLink(groupLinks.get(5), 0, 1);
        checkLink(groupLinks.get(6), 1, 2);
        checkLink(groupLinks.get(7), 2, 3);
        checkLink(groupLinks.get(8), 3, 4);
        checkLink(groupLinks.get(9), 4, 0);

        checkLink(groupLinks.get(10), 0, 0);
        checkLink(groupLinks.get(11), 1, 0);
        checkLink(groupLinks.get(12), 2, 1);
        checkLink(groupLinks.get(13), 3, 1);
        checkLink(groupLinks.get(14), 4, 2);

        checkLink(groupLinks.get(15), 0, 0);
        checkLink(groupLinks.get(16), 1, 1);
        checkLink(groupLinks.get(17), 2, 1);

        checkLink(groupLinks.get(18), 0, 0);
        checkLink(groupLinks.get(19), 1, 0);
    }


    /*
                ┌──────┐┌──────┐┌──────┐
                │Group1││Group2││Group3│
                └┬─────┘└┬─────┘└┬─────┘
                ┌▽───────▽┐┌─────▽┐
                │Group4   ││Group5│
                └┬────────┘└┬─────┘
                ┌▽──────────▽┐
                │Group6      │
                └────────────┘
    */
    @Test
    public void threeGroups() {
        treeTournamentHandler.addGroup(tournament, generateGroup(0, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(1, tournament));
        treeTournamentHandler.addGroup(tournament, generateGroup(2, tournament));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 6);

        final List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournament);
        Assert.assertEquals(groupLinks.size(), 5);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 0);
        checkLink(groupLinks.get(2), 2, 1);

        checkLink(groupLinks.get(3), 0, 0);
        checkLink(groupLinks.get(4), 1, 0);
    }


/*          ┌──────┐┌──────┐┌──────┐┌──────┐
            │Group1││Group2││group3││Group3│
            └────┬┬┘└┬────┬┘└─────┬┘└┬─────┘
                ┌││──┘   ┌│───────│──┘
                ││└──┐   │└──┐    │
            ┌───▽▽─┐┌▽───▽─┐┌▽────▽┐
            │Group5││Group4││Group6│
            └┬─────┘└┬─────┘└┬─────┘
            ┌▽───────▽┐┌─────▽┐
            │Group7   ││Group8│
            └┬────────┘└┬─────┘
            ┌▽──────────▽┐
            │Group9      │
            └────────────┘
*/

    @Test
    public void threeGroupsTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1, tournamentTwoWinners));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 9);

        List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 11);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 2, 2);
        checkLink(groupLinks.get(3), 0, 1);
        checkLink(groupLinks.get(4), 1, 2);
        checkLink(groupLinks.get(5), 2, 0);

        checkLink(groupLinks.get(6), 0, 0);
        checkLink(groupLinks.get(7), 1, 0);
        checkLink(groupLinks.get(8), 2, 1);

        checkLink(groupLinks.get(9), 0, 0);
        checkLink(groupLinks.get(10), 1, 0);

        //Removing first group
        Group lastGroup = groupLinks.get(5).getSource();
        treeTournamentHandler.removeGroup(tournamentTwoWinners, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 5);

        groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 6);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 1, 1);
        checkLink(groupLinks.get(2), 0, 1);
        checkLink(groupLinks.get(3), 1, 0);

        checkLink(groupLinks.get(4), 0, 0);
        checkLink(groupLinks.get(5), 1, 0);

        //Removing second group
        lastGroup = groupLinks.get(3).getSource();
        treeTournamentHandler.removeGroup(tournamentTwoWinners, lastGroup.getLevel(), lastGroup.getIndex());

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 2);

        groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 2);
    }

    /**
     * ┌──────┐
     * │Group1│
     * └┬───┬─┘
     * ┌▽───▽─┐
     * │Group2│
     * └──────┘
     */

    @Test
    public void leagueWithFinalFight() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0, tournamentTwoWinners));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 2);

        final List<GroupLink> groupLinks = groupLinkProvider.generateLinks(tournamentTwoWinners);
        Assert.assertEquals(groupLinks.size(), 2);

        checkLink(groupLinks.get(0), 0, 0);
        checkLink(groupLinks.get(1), 0, 0);
    }


    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        groupProvider.deleteAll();
        groupLinkProvider.deleteAll();
        tournamentExtraPropertyProvider.deleteAll();
        tournamentProvider.deleteAll();
    }

}
