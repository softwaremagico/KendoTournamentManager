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
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicDataTest extends AbstractTestNGSpringContextTests {
    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final Integer MEMBERS = 1;
    private static final Integer TEAMS = 3;
    private static final String TOURNAMENT_NAME = "basicTournamentTest";

    private static final Integer SHIAIJO = 0;

    private static final Integer LEVEL = 0;
    protected Club club;
    protected Tournament tournament;
    protected List<Participant> members;
    protected List<Role> roles;
    protected List<Team> teams;
    protected Group group;
    protected List<Fight> fights;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FightRepository fightRepository;

    protected Club createClub() {
        return clubRepository.save(new Club(CLUB_NAME, CLUB_COUNTRY, CLUB_CITY));
    }

    protected List<Participant> createParticipants(Club club) {
        List<Participant> members = new ArrayList<>();
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            members.add(
                    participantRepository.save(new Participant(String.format("0000%s", i), String.format("name%s", i), String.format("lastname%s", i), club)));
        }
        return members;
    }

    protected Tournament createTournament(String tournamentName) {
        return tournamentRepository.save(new Tournament(tournamentName, 1, MEMBERS, TournamentType.LEAGUE, null));
    }

    protected List<Role> createRoles(List<Participant> members, Tournament tournament) {
        List<Role> roles = new ArrayList<>();
        for (Participant competitor : members) {
            roles.add(roleRepository.save(new Role(tournament, competitor, RoleType.COMPETITOR)));
        }
        return roles;
    }

    protected List<Team> createTeams(List<Participant> members, Tournament tournament) {
        List<Team> teams = new ArrayList<>();
        int teamIndex = 0;
        Team team = null;
        int teamMember = 0;
        for (Participant competitor : members) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new Team("Team" + String.format("%02d", teamIndex), tournament);
                teamMember = 0;
                teams.add(team);
            }

            // Add member.
            team.addMember(competitor);
            team = teamRepository.save(team);
            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }
        return teams;
    }

    protected Group createGroup(Tournament tournament, List<Team> teams) {
        final Group group = new Group();
        group.setTournament(tournament);
        group.setLevel(0);
        group.setIndex(0);
        group.setTeams(teams);
        return groupRepository.save(group);
    }

    protected List<Fight> createFights(Tournament tournament, List<Team> teams, Group group) {
        List<Fight> fights = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            fights.add(fightRepository.save(new Fight(tournament, teams.get((i) % teams.size()), teams.get((i + 1) % teams.size()), SHIAIJO, LEVEL, null)));
        }
        group.setFights(fights);
        groupRepository.save(group);
        return fights;
    }

    protected void populateData() {
        populateData(TOURNAMENT_NAME);
    }

    protected void populateData(String tournamentName) {
        club = createClub();
        members = createParticipants(club);
        tournament = createTournament(tournamentName);
        roles = createRoles(members, tournament);
        teams = createTeams(members, tournament);
        group = createGroup(tournament, teams);
        fights = createFights(tournament, teams, group);
    }
}
