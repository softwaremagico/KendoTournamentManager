package com.softwaremagico.kt.core.tests.csv;

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

import com.softwaremagico.kt.core.controller.CsvController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.persistence.values.TournamentType;
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

@SpringBootTest
@Test(groups = {"csvReader"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CsvReaderTest extends AbstractTestNGSpringContextTests {

    private static final String TOURNAMENT_NAME = "CsvTournament";
    private static final int MEMBERS = 3;

    private static final String ONE_CLUBS_CSV_FILE_PATH = "csv/oneClub.csv";
    private static final String CLUBS_CSV_FILE_PATH = "csv/clubs.csv";

    private static final String ONE_PARTICIPANT_CSV_FILE_PATH = "csv/oneParticipant.csv";
    private static final String PARTICIPANTS_CSV_FILE_PATH = "csv/participants.csv";
    private static final String INVALID_PARTICIPANTS_CSV_FILE_PATH = "csv/invalidParticipants.csv";

    private static final String ONE_TEAM_CSV_FILE_PATH = "csv/oneTeam.csv";
    private static final String TEAMS_CSV_FILE_PATH = "csv/teams.csv";

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private CsvController csvController;

    @Autowired
    private ClubProvider clubProvider;

    @Autowired
    private ParticipantProvider participantProvider;

    @Autowired
    private TeamProvider teamProvider;

    private String readCsvFile(String fileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource(fileName).toURI())));
    }

    @BeforeClass
    public void prepareTournament1() {
        //Create Tournament
        tournamentController.create(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LEAGUE), null, null);
    }

    @Test
    public void addOneClub() throws URISyntaxException, IOException {
        Assert.assertEquals(clubProvider.count(), 0);
        csvController.addClubs(readCsvFile(ONE_CLUBS_CSV_FILE_PATH), null);
        Assert.assertEquals(clubProvider.count(), 1);
    }

    @Test(dependsOnMethods = "addOneClub")
    public void addMultiplesClubs() throws URISyntaxException, IOException {
        Assert.assertEquals(clubProvider.count(), 1);
        csvController.addClubs(readCsvFile(CLUBS_CSV_FILE_PATH), null);
        Assert.assertEquals(clubProvider.count(), 8);
    }

    @Test(dependsOnMethods = "addMultiplesClubs")
    public void addOneParticipant() throws URISyntaxException, IOException {
        Assert.assertEquals(participantProvider.count(), 0);
        csvController.addParticipants(readCsvFile(ONE_PARTICIPANT_CSV_FILE_PATH), null);
        Assert.assertEquals(participantProvider.count(), 3);
    }

    @Test(dependsOnMethods = "addOneParticipant")
    public void addMultipleParticipant() throws URISyntaxException, IOException {
        Assert.assertEquals(participantProvider.count(), 3);
        csvController.addParticipants(readCsvFile(PARTICIPANTS_CSV_FILE_PATH), null);
        Assert.assertEquals(participantProvider.count(), 18);
    }

    @Test(dependsOnMethods = "addOneParticipant")
    public void addInvalidParticipant() throws URISyntaxException, IOException {
        final List<ParticipantDTO> invalidParticipants = csvController.addParticipants(readCsvFile(INVALID_PARTICIPANTS_CSV_FILE_PATH), null);
        Assert.assertEquals(invalidParticipants.size(), 3);
    }

    @Test(dependsOnMethods = "addMultipleParticipant")
    public void addOneTeam() throws URISyntaxException, IOException {
        Assert.assertEquals(teamProvider.count(), 0);
        csvController.addTeams(readCsvFile(ONE_TEAM_CSV_FILE_PATH), null);
        Assert.assertEquals(teamProvider.count(), 1);
        Assert.assertEquals(teamProvider.getAll().get(0).getMembers().get(0).getIdCard(), "00000003");
        Assert.assertEquals(teamProvider.getAll().get(0).getMembers().get(1).getIdCard(), "00000001");
        Assert.assertEquals(teamProvider.getAll().get(0).getMembers().get(2).getIdCard(), "00000002");
    }

    @Test(dependsOnMethods = "addOneTeam")
    public void addMultipleTeams() throws URISyntaxException, IOException {
        Assert.assertEquals(teamProvider.count(), 1);
        csvController.addTeams(readCsvFile(TEAMS_CSV_FILE_PATH), null);
        Assert.assertEquals(teamProvider.count(), 6);
        //Members order is corrected.
        Assert.assertEquals(teamProvider.getAll().get(0).getMembers().get(0).getIdCard(), "00000001");
        Assert.assertEquals(teamProvider.getAll().get(0).getMembers().get(1).getIdCard(), "00000002");
        Assert.assertEquals(teamProvider.getAll().get(0).getMembers().get(2).getIdCard(), "00000003");
    }

    @Test(expectedExceptions = InvalidCsvFieldException.class)
    public void checkInvalidTeamCSV() throws URISyntaxException, IOException {
        csvController.addTeams(readCsvFile(CLUBS_CSV_FILE_PATH), null);
    }

    @Test(expectedExceptions = InvalidCsvFieldException.class)
    public void checkInvalidClubCSV() throws URISyntaxException, IOException {
        csvController.addClubs(readCsvFile(TEAMS_CSV_FILE_PATH), null);
    }

    @Test(expectedExceptions = InvalidCsvFieldException.class)
    public void checkInvalidParticipantCSV() throws URISyntaxException, IOException {
        csvController.addParticipants(readCsvFile(CLUBS_CSV_FILE_PATH), null);
    }
}
