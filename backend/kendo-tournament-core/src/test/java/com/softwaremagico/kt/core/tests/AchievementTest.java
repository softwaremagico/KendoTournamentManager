package com.softwaremagico.kt.core.tests;

import com.softwaremagico.kt.core.controller.*;
import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@Test(groups = {"achievementTests"})
public class AchievementTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 4;

    private static final int REFEREES = 3;

    private static final int ORGANIZER = 2;

    private static final int VOLUNTEER = 2;

    private static final String TOURNAMENT1_NAME = "Tournament 1";

    private static final String TOURNAMENT2_NAME = "Tournament 2";

    private static final String TOURNAMENT3_NAME = "Tournament 3";

    @Autowired
    private ClubController clubController;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private FightController fightController;

    private List<ParticipantDTO> participantsDTOs;

    private TournamentDTO tournament1DTO;
    private TournamentDTO tournament2DTO;
    private TournamentDTO tournament3DTO;

    private void generateRoles(TournamentDTO tournamentDTO) {
        //Add Competitors Roles
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(i), RoleType.COMPETITOR), null);
        }

        //Add Referee Roles
        for (int i = 0; i < REFEREES; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + i), RoleType.COMPETITOR), null);
        }

        //Add Organizer Roles
        for (int i = 0; i < ORGANIZER; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(participantsDTOs.size() + REFEREES + i), RoleType.COMPETITOR), null);
        }

        //Add Volunteer Roles
        for (int i = 0; i < VOLUNTEER; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(participantsDTOs.size() + REFEREES + ORGANIZER + i), RoleType.COMPETITOR), null);
        }
    }

    private void addTeams(TournamentDTO tournamentDTO) {
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
            if (teamMember >= MEMBERS) {
                teamDTO = null;
            }
        }
    }

    @BeforeTest
    public void prepareData() {
        //Add club
        final ClubDTO clubDTO = clubController.create(CLUB_NAME, CLUB_COUNTRY, CLUB_CITY, null);

        //Add participants
        participantsDTOs = new ArrayList<>();
        for (int i = 0; i < MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER; i++) {
            participantsDTOs.add(participantController.create(new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i),
                    String.format("lastname%s", i), clubDTO), null));
        }
    }

    @BeforeTest(dependsOnMethods = "prepareData")
    public void prepareTournament1() {
        //Create Tournament
        tournament1DTO = tournamentController.create(new TournamentDTO(TOURNAMENT1_NAME, 1, MEMBERS, TournamentType.LEAGUE), null);
        generateRoles(tournament1DTO);
        addTeams(tournament1DTO);
        fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null);
    }

    @BeforeTest(dependsOnMethods = "prepareData")
    public void prepareTournament2() {
        //Create Tournament
        tournament2DTO = tournamentController.create(new TournamentDTO(TOURNAMENT2_NAME, 1, MEMBERS, TournamentType.LEAGUE), null);
        generateRoles(tournament2DTO);
        addTeams(tournament2DTO);
        fightController.createFights(tournament2DTO.getId(), TeamsOrder.SORTED, 0, null);
    }

    @BeforeTest(dependsOnMethods = "prepareData")
    public void prepareTournament3() {
        //Create Tournament
        tournament3DTO = tournamentController.create(new TournamentDTO(TOURNAMENT3_NAME, 1, MEMBERS, TournamentType.LEAGUE), null);
        generateRoles(tournament3DTO);
        addTeams(tournament3DTO);
        fightController.createFights(tournament3DTO.getId(), TeamsOrder.SORTED, 0, null);
    }
}
