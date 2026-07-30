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
import java.util.Objects;

@SpringBootTest
@Test(groups = "csvReader")
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

    private TournamentDTO tournament;

    private String readCsvFile(String fileName) throws URISyntaxException, IOException {
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource(fileName)).toURI())));
    }

    @BeforeClass
    public void prepareTournament1() {
        //Create Tournament
        this.tournament = this.tournamentController.create(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LEAGUE), null, null);
    }

    @Test
    public void addOneClub() throws URISyntaxException, IOException {
        Assert.assertEquals(this.clubProvider.count(), 0);
        this.csvController.addClubs(this.readCsvFile(ONE_CLUBS_CSV_FILE_PATH), null);
        Assert.assertEquals(this.clubProvider.count(), 1);
    }

    @Test(dependsOnMethods = "addOneClub")
    public void addMultiplesClubs() throws URISyntaxException, IOException {
        Assert.assertEquals(this.clubProvider.count(), 1);
        this.csvController.addClubs(this.readCsvFile(CLUBS_CSV_FILE_PATH), null);
        Assert.assertEquals(this.clubProvider.count(), 8);
    }

    @Test(dependsOnMethods = "addMultiplesClubs")
    public void addOneParticipant() throws URISyntaxException, IOException {
        Assert.assertEquals(this.participantProvider.count(), 0);
        this.csvController.addParticipants(this.readCsvFile(ONE_PARTICIPANT_CSV_FILE_PATH), null);
        Assert.assertEquals(this.participantProvider.count(), 3);
    }

    @Test(dependsOnMethods = "addOneParticipant")
    public void addMultipleParticipant() throws URISyntaxException, IOException {
        Assert.assertEquals(this.participantProvider.count(), 3);
        this.csvController.addParticipants(this.readCsvFile(PARTICIPANTS_CSV_FILE_PATH), null);
        Assert.assertEquals(this.participantProvider.count(), 18);
    }

    @Test(dependsOnMethods = "addOneParticipant")
    public void addInvalidParticipant() throws URISyntaxException, IOException {
        final List<ParticipantDTO> invalidParticipants = this.csvController.addParticipants(this.readCsvFile(INVALID_PARTICIPANTS_CSV_FILE_PATH), null);
        Assert.assertEquals(invalidParticipants.size(), 3);
    }

    @Test(dependsOnMethods = "addMultipleParticipant")
    public void addOneTeam() throws URISyntaxException, IOException {
        Assert.assertEquals(this.teamProvider.count(), 0);
        this.csvController.addTeams(this.readCsvFile(ONE_TEAM_CSV_FILE_PATH), this.tournament.getId(), null);
        Assert.assertEquals(this.teamProvider.count(), 1);
        Assert.assertEquals(this.teamProvider.getAll().getFirst().getMembers().get(0).getIdCard(), "00000003");
        Assert.assertEquals(this.teamProvider.getAll().getFirst().getMembers().get(1).getIdCard(), "00000001");
        Assert.assertEquals(this.teamProvider.getAll().getFirst().getMembers().get(2).getIdCard(), "00000002");
    }

    @Test(dependsOnMethods = "addOneTeam")
    public void addMultipleTeams() throws URISyntaxException, IOException {
        Assert.assertEquals(this.teamProvider.count(), 1);
        this.csvController.addTeams(this.readCsvFile(TEAMS_CSV_FILE_PATH), this.tournament.getId(), null);
        Assert.assertEquals(this.teamProvider.count(), 6);
        //Members order is corrected.
        Assert.assertEquals(this.teamProvider.getAll().getFirst().getMembers().get(0).getIdCard(), "00000001");
        Assert.assertEquals(this.teamProvider.getAll().getFirst().getMembers().get(1).getIdCard(), "00000002");
        Assert.assertEquals(this.teamProvider.getAll().getFirst().getMembers().get(2).getIdCard(), "00000003");
    }

    @Test(expectedExceptions = InvalidCsvFieldException.class)
    public void checkInvalidTeamCSV() throws URISyntaxException, IOException {
        this.csvController.addTeams(this.readCsvFile(CLUBS_CSV_FILE_PATH), this.tournament.getId(), null);
    }

    @Test(expectedExceptions = InvalidCsvFieldException.class)
    public void checkInvalidClubCSV() throws URISyntaxException, IOException {
        this.csvController.addClubs(this.readCsvFile(TEAMS_CSV_FILE_PATH), null);
    }

    @Test(expectedExceptions = InvalidCsvFieldException.class)
    public void checkInvalidParticipantCSV() throws URISyntaxException, IOException {
        this.csvController.addParticipants(this.readCsvFile(CLUBS_CSV_FILE_PATH), null);
    }
}
