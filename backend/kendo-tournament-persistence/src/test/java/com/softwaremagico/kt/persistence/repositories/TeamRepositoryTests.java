package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.persistence.factories.ClubFactory;
import com.softwaremagico.kt.persistence.factories.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest
@Test(groups = {"teamRepository"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TeamRepositoryTests extends AbstractTestNGSpringContextTests {
    private static final Integer NUMBER_OF_MEMBERS = 3;
    private static final String TOURNAMENT_NAME = "XXX";

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private ClubFactory clubFactory;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    private Tournament tournament;
    private Club club;
    private Team team;

    @BeforeClass
    public void addUser() {
        club = clubRepository.save(clubFactory.createDefaultClub());
        Assert.assertEquals(participantRepository.count(), 0);

        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, NUMBER_OF_MEMBERS, TournamentType.LEAGUE, null);
        tournament = tournamentRepository.save(newTournament);
    }

    @Test
    public void createTeam() {
        Assert.assertEquals(teamRepository.count(), 0);
        team = teamRepository.save(new Team("Team" + String.format("%02d", 1), tournament));
        Assert.assertEquals(teamRepository.count(), 1);
    }

    @Test(dependsOnMethods = "createTeam")
    public void addMembersToTeam() {
        for (int i = 0; i < NUMBER_OF_MEMBERS; i++) {
            Participant participant = new Participant(String.format("0000%s", i), String.format("name%s", i), String.format("lastname%s", i), club);
            Assert.assertEquals(participantRepository.count(), i);
            participant = participantRepository.save(participant);
            Assert.assertEquals(participantRepository.count(), i + 1);

            team.addMember(participant);
            team = teamRepository.save(team);
        }
    }
}
