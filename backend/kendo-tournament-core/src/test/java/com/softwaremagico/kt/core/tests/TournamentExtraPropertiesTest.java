package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

    private final static String USER = "Me";

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    @Autowired
    private GroupProvider groupProvider;

    @Test
    public void checkDefaultProperties() {
        Tournament tournament1 = new Tournament("Tournament1", 1, 3, TournamentType.LEAGUE, USER);
        tournament1 = tournamentProvider.save(tournament1);
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament1).size(), 0);

        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament1, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "2", USER));
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament1).size(), 1);

        //Another tournament
        Tournament tournament2 = new Tournament("Tournament2", 1, 3, TournamentType.LEAGUE, USER);
        tournament2 = tournamentProvider.save(tournament2);
        //Has a default one.
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament2).size(), 1);
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament2).get(0).getPropertyValue(), "2");

        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament2, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "1", USER));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament2, TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "true", USER));
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament2).size(), 2);

        //Another tournament
        Tournament tournament3 = new Tournament("Tournament3", 1, 3, TournamentType.LEAGUE, USER);
        tournament3 = tournamentProvider.save(tournament3);
        //Has a default one.
        Assert.assertEquals(tournamentExtraPropertyProvider.getAll(tournament3).size(), 2);
    }

    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        groupProvider.deleteAll();
        tournamentExtraPropertyProvider.deleteAll();
        tournamentProvider.deleteAll();
    }
}
