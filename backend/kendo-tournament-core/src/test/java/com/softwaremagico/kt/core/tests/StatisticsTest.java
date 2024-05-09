package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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
import com.softwaremagico.kt.core.controller.ParticipantStatisticsController;
import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.TournamentStatisticsController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantStatisticsDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.controller.models.TournamentStatisticsDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.score.CompetitorRanking;
import com.softwaremagico.kt.persistence.values.LeagueFightsOrder;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.utils.NameUtils;
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

    @Autowired
    private ParticipantStatisticsController participantStatisticsController;

    @Autowired
    private RankingController rankingController;

    @Autowired
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    private List<ParticipantDTO> participantsDTOs;

    private TournamentDTO tournament1DTO;

    private ParticipantDTO competitor1;

    private int totalFights;

    private void generateRoles(TournamentDTO tournamentDTO) {
        //Add Competitors Roles
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(i), RoleType.COMPETITOR), null);
        }

        //Add Referee Roles
        for (int i = 0; i < REFEREES; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + i), RoleType.REFEREE), null);
        }

        //Add Organizer Roles
        for (int i = 0; i < ORGANIZER; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + REFEREES + i), RoleType.ORGANIZER), null);
        }

        //Add Volunteer Roles
        for (int i = 0; i < VOLUNTEER; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + REFEREES + ORGANIZER + i), RoleType.VOLUNTEER), null);
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
        tournamentExtraPropertyController.create(new TournamentExtraPropertyDTO(tournament1DTO,
                TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION, LeagueFightsOrder.FIFO.name()), null);
        generateRoles(tournament1DTO);
        addTeams(tournament1DTO);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null));


        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1ScoreTime(3);
        fightDTOs.get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(0).addCompetitor2ScoreTime(6);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1ScoreTime(12);
        fightDTOs.get(0).getDuels().get(0).setCompetitor1Fault(true);
        fightDTOs.get(0).getDuels().get(0).setDuration(DUEL_DURATION);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));
        competitor1 = fightDTOs.get(0).getDuels().get(0).getCompetitor1();


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
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getMenNumber(), 5);
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getKoteNumber(), 3);
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getDoNumber(), 3);
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getHansokuNumber(), 2);
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getTsukiNumber(), 2);
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getIpponNumber(), 2);

        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getFightsNumber(), totalFights);
        Assert.assertNull(tournamentStatisticsDTO.getTournamentFightStatistics().getFightsByTeam());
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getDuelsNumber(), totalFights * 3L);
        Assert.assertNotNull(tournamentStatisticsDTO.getTournamentFightStatistics().getAverageTime());
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getAverageTime(), DUEL_DURATION);
        Assert.assertEquals(tournamentStatisticsDTO.getTournamentFightStatistics().getFaults(), 3 + 4);
        Assert.assertEquals((long) tournamentStatisticsDTO.getTournamentFightStatistics().getFightsFinished(), 2);
    }

    @Test
    public void basicParticipantStatistics() {
        final ParticipantStatisticsDTO participantStatisticsDTO = participantStatisticsController.get(competitor1);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getMenNumber(), 2);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getKoteNumber(), 1);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getDoNumber(), 0);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getHansokuNumber(), 0);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getTsukiNumber(), 0);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getIpponNumber(), 0);

        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getReceivedMenNumber(), 1);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getReceivedKoteNumber(), 0);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getReceivedDoNumber(), 0);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getReceivedHansokuNumber(), 0);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getReceivedTsukiNumber(), 0);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getReceivedIpponNumber(), 0);

        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getFaults(), 1);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getReceivedFaults(), 0);


        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getDuelsNumber(), 3);
        //Competitor1 is in two duels
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getTotalDuelsTime(), DUEL_DURATION * 2);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getAverageTime(), DUEL_DURATION);

        Assert.assertEquals(participantStatisticsDTO.getParticipantId(), competitor1.getId());
        Assert.assertEquals(participantStatisticsDTO.getParticipantName(), NameUtils.getLastnameName(competitor1));

        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getQuickestHit(), 3L);
        Assert.assertEquals((long) participantStatisticsDTO.getParticipantFightStatistics().getQuickestReceivedHit(), 6L);

        Assert.assertEquals(participantStatisticsDTO.getTotalTournaments(), 1L);

        Assert.assertEquals(participantStatisticsDTO.getTournaments(), 1);
    }

    @Test
    public void getCompetitorRankingPosition() {
        final CompetitorRanking competitorRanking = rankingController.getCompetitorRanking(competitor1);
        Assert.assertEquals(competitorRanking.getRanking(), 0);
        Assert.assertEquals(competitorRanking.getTotal(), MEMBERS * TEAMS);
    }

    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
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
