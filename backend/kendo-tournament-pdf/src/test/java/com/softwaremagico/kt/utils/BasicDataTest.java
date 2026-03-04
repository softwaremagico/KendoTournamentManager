package com.softwaremagico.kt.utils;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BasicDataTest extends AbstractTestNGSpringContextTests {
    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_CITY = "ClubCity";
    private static final Integer MEMBERS = 3;
    private static final Integer TEAMS = 2;
    private static final String TOURNAMENT_NAME = "basicTournamentTest";

    private static final Integer SHIAIJO = 0;

    private static final Integer LEVEL = 0;
    protected ClubDTO club;
    protected TournamentDTO tournament;
    protected List<ParticipantDTO> members;
    protected List<RoleDTO> roles;
    protected List<TeamDTO> teams;
    protected GroupDTO group;
    protected List<FightDTO> fights;
    @Autowired
    private ClubController clubController;
    @Autowired
    private ParticipantController participantController;
    @Autowired
    private TournamentController tournamentController;
    @Autowired
    private RoleController roleController;
    @Autowired
    private TeamController teamController;
    @Autowired
    private GroupController groupController;
    @Autowired
    private FightController fightController;

    protected ClubDTO createClub() {
        return clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
    }

    protected List<ParticipantDTO> createParticipants(ClubDTO club) {
        List<ParticipantDTO> participants = new ArrayList<>();
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            participants.add(participantController.create(
                    new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i), String.format("lastname%s", i), club), null, null));
        }
        return participants;
    }

    protected TournamentDTO createTournament() {
        return tournamentController.create(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LEAGUE), null, null);
    }

    protected List<RoleDTO> createRoles(List<ParticipantDTO> members, TournamentDTO tournament) {
        List<RoleDTO> rolesCreated = new ArrayList<>();
        for (ParticipantDTO competitor : members) {
            rolesCreated.add(roleController.create(new RoleDTO(tournament, competitor, RoleType.COMPETITOR), null, null));
        }
        return rolesCreated;
    }

    protected RoleDTO createReferee() {
        ParticipantDTO referee = participantController.create(new ParticipantDTO("Ref001", "Referee", "Referee", club), null, null);
        return roleController.create(new RoleDTO(tournament, referee, RoleType.REFEREE), null, null);
    }

    protected List<TeamDTO> createTeams(List<ParticipantDTO> members, TournamentDTO tournament) {
        List<TeamDTO> teamsCreated = new ArrayList<>();
        int teamIndex = 0;
        TeamDTO team = null;
        int teamMember = 0;
        for (ParticipantDTO competitor : members) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournament);
                teamMember = 0;
                team = teamController.create(team, null, null);
                teamsCreated.add(team);
            }

            // Add member.
            team.addMember(competitor);
            team = teamController.update(team, null, null);
            teamsCreated.set(teamsCreated.size() - 1, team);
            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }
        return teamsCreated;
    }

    protected GroupDTO createGroup(TournamentDTO tournament, List<TeamDTO> teams) {
        final GroupDTO groupsCreated = new GroupDTO();
        groupsCreated.setTournament(tournament);
        groupsCreated.setLevel(0);
        groupsCreated.setTeams(teams);
        return groupController.create(groupsCreated, null, null);
    }

    protected List<FightDTO> createFights(TournamentDTO tournament, List<TeamDTO> teams, GroupDTO group) {
        List<FightDTO> fightsCreated = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            FightDTO fightDTO = new FightDTO(tournament, teams.get((i) % teams.size()), teams.get((i + 1) % teams.size()), SHIAIJO, LEVEL);
            List<DuelDTO> duels = new ArrayList<>();
            for (int j = 0; j < tournament.getTeamSize(); j++) {
                duels.add(new DuelDTO(teams.get((i) % teams.size()).getMembers().get(j), teams.get((i + 1) % teams.size()).getMembers().get(j),
                        tournament, null));
            }
            fightDTO.setDuels(duels);
            fightsCreated.add(fightController.create(fightDTO, null, null));
        }
        group.setFights(fightsCreated);
        groupController.create(group, null, null);
        return fightsCreated;
    }

    protected void resolveFights() {
        int counter = 0;
        for (final FightDTO fight : fights) {
            if(counter %4 == 0){
                fight.getDuels().get(1).setCompetitor1Fault(true);
            }
            for (final DuelDTO duel : fight.getDuels()) {
                List<Score> scores = new ArrayList<>();
                for (int i = 0; i < (counter % 3); i++) {
                    scores.add(Score.MEN);
                }
                for (int i = 0; i < (counter % 2); i++) {
                    scores.add(Score.FUSEN_GACHI);
                }
                duel.setCompetitor1Score(scores);
                counter++;
            }
            fightController.update(fight, null, null);
        }
    }

    protected void populateData() {
        club = createClub();
        members = createParticipants(club);
        tournament = createTournament();
        roles = createRoles(members, tournament);
        createReferee();
        teams = createTeams(members, tournament);
        group = createGroup(tournament, teams);
        fights = createFights(tournament, teams, group);
    }

    protected boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
