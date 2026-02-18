package com.softwaremagico.kt.core.tests.csv;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.core.controller.CsvController;
import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.core.providers.GroupLinkProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.GroupLinkRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.utils.GroupLinkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Test(groups = {"csvReader"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CsvTournamentGroupsTest extends AbstractTestNGSpringContextTests {

    private static final String TOURNAMENT_NAME = "TournamentCsvTest";
    private static final int MEMBERS = 3;

    private static final String CSV_3_GROUPS_2_WINNERS_TO_2_GROUPS_CSV = "csv/links3groups2winnersTo2groups.csv";
    private static final String CSV_4_GROUPS_2_WINNERS_TO_4_GROUPS_CSV = "csv/links4groups2winnersTo4groups.csv";
    private static final String CSV_5_GROUPS_2_WINNERS_TO_5_GROUPS_CSV = "csv/links5groups2winnersTo5groups.csv";
    private static final String CSV_INVALID = "csv/linksInvalidNumber.csv.csv";

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    @Autowired
    private GroupLinkProvider groupLinkProvider;

    @Autowired
    private GroupLinkRepository groupLinkRepository;

    @Autowired
    private CsvController csvController;

    private Tournament tournament;

    private String readCsvFile(String fileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource(fileName).toURI())));
    }

    @BeforeClass
    public void setUp() {
        Assert.assertEquals(tournamentProvider.count(), 0);
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.TREE, null);
        tournament = tournamentProvider.save(newTournament);
        Assert.assertEquals(tournamentProvider.count(), 1);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "true"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "2"));
    }

    @Test
    public void test3Groups2WinnersTo2GroupsCsvFile() throws URISyntaxException, IOException {
        csvController.addGroupLinks(tournament.getId(), readCsvFile(CSV_3_GROUPS_2_WINNERS_TO_2_GROUPS_CSV), null);
        //6 defined links, + 2 for the inner groups.
        Assert.assertEquals(groupLinkProvider.count(), 8);
    }

    @Test
    public void test4Groups2WinnersTo4GroupsCsvFile() throws URISyntaxException, IOException {
        csvController.addGroupLinks(tournament.getId(), readCsvFile(CSV_4_GROUPS_2_WINNERS_TO_4_GROUPS_CSV), null);
        //6 defined links, + 2 for the inner groups.
        Assert.assertEquals(groupLinkProvider.count(), 14);
    }

    @Test
    public void test5Groups2WinnersTo5GroupsCsvFile() throws URISyntaxException, IOException {
        csvController.addGroupLinks(tournament.getId(), readCsvFile(CSV_5_GROUPS_2_WINNERS_TO_5_GROUPS_CSV), null);
        //6 defined links, + 2 for the inner groups.
        Assert.assertEquals(groupLinkProvider.count(), 20);
        //Check byes.
        final List<GroupLink> groupLinks = groupLinkRepository.findByTournament(tournament);
        final Map<Integer, List<GroupLink>> groupLinksByLevels = GroupLinkUtils.orderBySourceLevel(groupLinks);
        //On level 1, index 5 goes to a bye.
        Assert.assertEquals(groupLinksByLevels.get(1).get(0).getDestination().getIndex(), 0);
        Assert.assertEquals(groupLinksByLevels.get(1).get(1).getDestination().getIndex(), 0);
        Assert.assertEquals(groupLinksByLevels.get(1).get(2).getDestination().getIndex(), 1);
        Assert.assertEquals(groupLinksByLevels.get(1).get(3).getDestination().getIndex(), 1);
        Assert.assertEquals(groupLinksByLevels.get(1).get(4).getDestination().getIndex(), 2);
        //On level 2, index 3 cannot be on a bye.
        Assert.assertEquals(groupLinksByLevels.get(2).get(0).getDestination().getIndex(), 0);
        Assert.assertEquals(groupLinksByLevels.get(2).get(1).getDestination().getIndex(), 1);
        Assert.assertEquals(groupLinksByLevels.get(2).get(2).getDestination().getIndex(), 1);
    }

    @Test(expectedExceptions = InvalidCsvFieldException.class)
    public void testInvalidCsvFile() throws URISyntaxException, IOException {
        csvController.addGroupLinks(tournament.getId(), readCsvFile(CSV_INVALID), null);
    }
}
