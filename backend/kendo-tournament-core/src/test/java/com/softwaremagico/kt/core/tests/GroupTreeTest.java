package com.softwaremagico.kt.core.tests;

import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
    private GroupProvider groupProvider;

    private Tournament tournament;
    private Tournament tournamentTwoWinners;

    private Group generateGroup(int index) {
        Group group = new Group();
        group.setIndex(index);
        group.setLevel(0);
        group.setTournament(tournament);
        return group;
    }

    @BeforeClass
    public void addTournaments() {
        Assert.assertEquals(tournamentProvider.count(), 0);
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournament = tournamentProvider.save(newTournament);
        Assert.assertEquals(tournamentProvider.count(), 1);

        Tournament newTournamentTwoWinners = new Tournament(TOURNAMENT_TWO_WINNERS_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournamentTwoWinners = tournamentProvider.save(newTournamentTwoWinners);
        Assert.assertEquals(tournamentProvider.count(), 2);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentTwoWinners, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "2"));
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
        treeTournamentHandler.addGroup(tournament, generateGroup(0));
        treeTournamentHandler.addGroup(tournament, generateGroup(1));
        treeTournamentHandler.addGroup(tournament, generateGroup(2));
        treeTournamentHandler.addGroup(tournament, generateGroup(3));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 7);
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
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 11);
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
        treeTournamentHandler.addGroup(tournament, generateGroup(0));
        treeTournamentHandler.addGroup(tournament, generateGroup(1));
        treeTournamentHandler.addGroup(tournament, generateGroup(2));
        treeTournamentHandler.addGroup(tournament, generateGroup(3));
        treeTournamentHandler.addGroup(tournament, generateGroup(4));
        treeTournamentHandler.addGroup(tournament, generateGroup(5));
        treeTournamentHandler.addGroup(tournament, generateGroup(6));
        treeTournamentHandler.addGroup(tournament, generateGroup(7));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 15);
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
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(4));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(5));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(6));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(7));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 23);
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
        treeTournamentHandler.addGroup(tournament, generateGroup(0));
        treeTournamentHandler.addGroup(tournament, generateGroup(1));
        treeTournamentHandler.addGroup(tournament, generateGroup(2));
        treeTournamentHandler.addGroup(tournament, generateGroup(3));
        treeTournamentHandler.addGroup(tournament, generateGroup(4));
        treeTournamentHandler.addGroup(tournament, generateGroup(5));
        treeTournamentHandler.addGroup(tournament, generateGroup(6));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 14);
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
        treeTournamentHandler.addGroup(tournament, generateGroup(0));
        treeTournamentHandler.addGroup(tournament, generateGroup(1));
        treeTournamentHandler.addGroup(tournament, generateGroup(2));
        treeTournamentHandler.addGroup(tournament, generateGroup(3));
        treeTournamentHandler.addGroup(tournament, generateGroup(4));
        treeTournamentHandler.addGroup(tournament, generateGroup(5));

        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 12);
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
    public void biggerTreeMissingTwoTwoWinners() {
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(0));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(1));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(2));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(3));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(4));
        treeTournamentHandler.addGroup(tournamentTwoWinners, generateGroup(5));

        Assert.assertEquals(groupProvider.getGroups(tournamentTwoWinners).size(), 18);
    }


}
