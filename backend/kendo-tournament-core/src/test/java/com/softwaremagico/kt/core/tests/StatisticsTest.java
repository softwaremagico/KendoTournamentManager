package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.*;
import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@Test(groups = {"statisticsTests"})
public class StatisticsTest extends AbstractTransactionalTestNGSpringContextTests {

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

    private static final int DUEL_DURATION = 83;

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

    @Autowired
    private TournamentStatisticsController tournamentStatisticsController;

    private List<ParticipantDTO> participantsDTOs;

    private TournamentDTO tournament1DTO;
    private TournamentDTO tournament2DTO;

    private int totalFights;

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
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + REFEREES + i), RoleType.COMPETITOR), null);
        }

        //Add Volunteer Roles
        for (int i = 0; i < VOLUNTEER; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + REFEREES + ORGANIZER + i), RoleType.COMPETITOR), null);
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

    @BeforeClass
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

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament() {
        //Create Tournament
        tournament1DTO = tournamentController.create(new TournamentDTO(TOURNAMENT1_NAME, 1, MEMBERS, TournamentType.LEAGUE), null);
        tournamentController.update(tournament1DTO, null);
        generateRoles(tournament1DTO);
        addTeams(tournament1DTO);
        List<FightDTO> fightDTOs = fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null);


        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).setCompetitor1Fault(true);
        fightDTOs.get(0).getDuels().get(0).setDuration(DUEL_DURATION);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));


        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).addCompetitor2Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(1).setDuration(DUEL_DURATION);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));


        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).setDuration(DUEL_DURATION);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));


        fightDTOs.get(1).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(1).getDuels().get(0).setDuration(DUEL_DURATION);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null));

        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.HANSOKU);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.HANSOKU);
        fightDTOs.get(1).getDuels().get(1).setDuration(DUEL_DURATION);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null));

        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.TSUKI);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.TSUKI);
        fightDTOs.get(1).getDuels().get(2).setDuration(DUEL_DURATION);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null));

        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.IPPON);
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.IPPON);
        fightDTOs.get(2).getDuels().get(0).setDuration(DUEL_DURATION);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null));

        fightDTOs.get(3).getDuels().get(0).addCompetitor1Score(Score.MEN);
        fightDTOs.get(3).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(3).getDuels().get(0).setDuration(DUEL_DURATION);
        fightDTOs.get(3).getDuels().get(1).setCompetitor1Fault(true);
        fightDTOs.get(3).getDuels().get(1).setCompetitor2Fault(true);
        fightDTOs.get(3).getDuels().get(0).setFinished(true);
        fightDTOs.set(3, fightController.update(fightDTOs.get(3), null));

        totalFights = fightDTOs.size();

    }

    @Test
    public void basicTournamentStatistics() {
        final TournamentStatisticsDTO tournamentStatisticsDTO = tournamentStatisticsController.get(tournament1DTO);
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getMenNumber(), 5);
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getKoteNumber(), 3);
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getDoNumber(), 3);
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getHansokuNumber(), 2);
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getTsukiNumber(), 2);
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getIpponNumber(), 2);

        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getFightsNumber(), totalFights);
        Assert.assertNull(tournamentStatisticsDTO.getFightStatistics().getFightsByTeam());
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getDuelsNumber(), totalFights * 3L);
        Assert.assertNotNull(tournamentStatisticsDTO.getFightStatistics().getAverageTime());
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getAverageTime(), DUEL_DURATION);
        Assert.assertEquals(tournamentStatisticsDTO.getFightStatistics().getFaults(), 3 + 4);
        Assert.assertEquals((long) tournamentStatisticsDTO.getFightStatistics().getFightsFinished(), 2);
    }

    @AfterClass
    public void deleteTournament() {
        deleteFromTables("competitor_1_score", "competitor_2_score", "competitor_1_score_time", "competitor_2_score_time",
                "achievements", "duels_by_fight");
        deleteFromTables("duels", "fights_by_group");
        deleteFromTables("fights", "members_of_team", "teams_by_group");
        deleteFromTables("teams");
        deleteFromTables("tournament_groups", "roles");
        deleteFromTables("tournaments");
        deleteFromTables("participant_image");
        deleteFromTables("participants");
        deleteFromTables("clubs");
    }

}
