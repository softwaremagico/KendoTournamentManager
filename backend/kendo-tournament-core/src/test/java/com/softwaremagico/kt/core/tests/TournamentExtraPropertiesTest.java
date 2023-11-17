package com.softwaremagico.kt.core.tests;

import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@SpringBootTest
@Test(groups = {"tournamentExtraProperties"})
public class TournamentExtraPropertiesTest extends AbstractTransactionalTestNGSpringContextTests {

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    @Autowired
    private GroupProvider groupProvider;

    @Test
    public void checkDefaultProperties() {
        Tournament tournament1 = new Tournament("Tournament1", 1, 3, TournamentType.LEAGUE, null);
        tournament1 = tournamentProvider.save(tournament1);
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament1).size(), 0);

        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament1, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "2"));
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament1).size(), 1);

        //Another tournament
        Tournament tournament2 = new Tournament("Tournament2", 1, 3, TournamentType.LEAGUE, null);
        tournament2 = tournamentProvider.save(tournament2);
        //Has a default one.
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament2).size(), 1);
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament2).get(0).getPropertyValue(), "2");

        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament2, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "1"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament2, TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "true"));
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament2).size(), 2);

        //Another tournament
        Tournament tournament3 = new Tournament("Tournament3", 1, 3, TournamentType.LEAGUE, null);
        tournament3 = tournamentProvider.save(tournament3);
        //Has a default one.
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament3).size(), 2);
    }

    @AfterClass
    public void deleteTournament() {
        groupProvider.deleteAll();
        tournamentExtraPropertyProvider.deleteAll();
        tournamentProvider.deleteAll();
    }
}
