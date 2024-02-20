package com.softwaremagico.kt.core;

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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TournamentTestUtils extends AbstractTransactionalTestNGSpringContextTests {
    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";

    @Autowired
    private ClubController clubController;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private FightController fightController;

    protected List<ParticipantDTO> participantsDTOs = new ArrayList<>();

    protected List<ParticipantDTO> addParticipants(int members, int teams, int referees, int organizers, int volunteer, int press, int extra) {
        //Add club
        final ClubDTO clubDTO = clubController.create(CLUB_NAME, CLUB_COUNTRY, CLUB_CITY, null);

        //Add participants
        participantsDTOs = new ArrayList<>();
        for (int i = 0; i < members * teams + referees + organizers + volunteer + press + extra; i++) {
            participantsDTOs.add(participantController.create(new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i),
                    String.format("lastname%s", i), clubDTO), null));
        }
        if (extra > 0) {
            return participantsDTOs.subList(participantsDTOs.size() - extra - 1, participantsDTOs.size());
        }
        return new ArrayList<>();
    }

    protected void generateRoles(TournamentDTO tournamentDTO, int members, int teams, int referees, int organizers, int volunteers, int press) {
        //Add Competitors Roles
        for (int i = 0; i < members * teams; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(i), RoleType.COMPETITOR), null);
        }

        //Add Referee Roles
        for (int i = 0; i < referees; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(members * teams + i), RoleType.REFEREE), null);
        }

        //Add organizer Roles
        for (int i = 0; i < organizers; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(members * teams + referees + i), RoleType.ORGANIZER), null);
        }

        //Add volunteer Roles
        for (int i = 0; i < volunteers; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(members * teams + referees + organizers + i), RoleType.VOLUNTEER), null);
        }

        //Add Press Roles
        for (int i = 0; i < press; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(members * teams + referees + organizers + volunteers + i), RoleType.PRESS), null);
        }
    }

    protected List<ParticipantDTO> getReferees(int members, int teams, int referees) {
        return participantsDTOs.subList(members * teams, members * teams + referees);
    }

    protected List<ParticipantDTO> getOrganizers(int members, int teams, int referees, int organizers) {
        return participantsDTOs.subList(members * teams + referees, members * teams + referees + organizers);
    }

    protected List<ParticipantDTO> getVolunteers(int members, int teams, int referees, int organizers, int volunteer) {
        return participantsDTOs.subList(members * teams + referees + organizers, members * teams + referees + organizers + volunteer);
    }

    protected List<ParticipantDTO> getPress(int members, int teams, int referees, int organizers, int volunteer, int press) {
        return participantsDTOs.subList(members * teams + referees + organizers + volunteer, members * teams + referees + organizers + volunteer + press);
    }

    protected void addTeams(TournamentDTO tournamentDTO, int members) {
        List<RoleDTO> competitorsRolesDTO = roleController.get(tournamentDTO, RoleType.COMPETITOR);

        int teamIndex = 0;
        TeamDTO teamDTO = null;
        int teamMember = 0;

        final GroupDTO groupDTO = groupController.get(tournamentDTO).get(0);

        for (RoleDTO competitorRoleDTO : competitorsRolesDTO) {
            // Create a new team.
            if (teamDTO == null) {
                teamIndex++;
                teamDTO = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
                teamMember = 0;
            }

            // Add member.
            teamDTO.addMember(competitorRoleDTO.getParticipant());
            teamDTO = teamController.update(teamDTO, null);

            if (teamMember == 0) {
                groupController.addTeams(groupDTO.getId(), Collections.singletonList(teamDTO), null);
            }

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= members) {
                teamDTO = null;
            }
        }
    }

    protected TournamentDTO addTournament(String tournamentName, int members, int teams, int referees, int organizers, int volunteers, int press, int minutesPast) {
        return addTournament(tournamentName, members, teams, referees, organizers, volunteers, press, TournamentType.LEAGUE, minutesPast);
    }

    protected TournamentDTO addTournament(String tournamentName, int members, int teams, int referees, int organizers, int volunteers, int press, TournamentType type, int minutesPast) {
        //Create Tournament
        TournamentDTO tournamentDTO = tournamentController.create(new TournamentDTO(tournamentName, 1, members, type), null);
        tournamentDTO.setCreatedAt(LocalDateTime.now().minusMinutes(minutesPast));
        tournamentController.update(tournamentDTO, null);
        generateRoles(tournamentDTO, members, teams, referees, organizers, volunteers, press);
        addTeams(tournamentDTO, members);
        return tournamentDTO;
    }

    public void wipeOut() {
        deleteFromTables("competitor_1_score", "competitor_2_score", "competitor_1_score_time", "competitor_2_score_time",
                "achievements", "duels_by_fight");
        deleteFromTables("duels", "fights_by_group");
        deleteFromTables("fights", "members_of_team", "teams_by_group");
        deleteFromTables("teams");
        deleteFromTables("tournament_groups", "roles");
        deleteFromTables("achievements", "tournament_extra_properties");
        deleteFromTables("tournaments");
        deleteFromTables("participant_image");
        deleteFromTables("participants");
        deleteFromTables("clubs");
    }

}
