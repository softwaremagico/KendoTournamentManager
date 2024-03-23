package com.softwaremagico.kt.core.tests;

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

import com.softwaremagico.kt.core.converters.ClubConverter;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.RoleConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.TournamentScoreConverter;
import com.softwaremagico.kt.core.converters.models.ClubConverterRequest;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.GroupConverterRequest;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.core.converters.models.RoleConverterRequest;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentScoreConverterRequest;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.DuelType;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentScore;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Test(groups = {"converterTests"})
public class ConverterTest extends AbstractTestNGSpringContextTests {
    private static final String CLUB_NAME = "Name";
    private static final String CLUB_COUNTRY = "Country";
    private static final String CLUB_CITY = "City";
    private static final String CLUB_ADDRESS = "Address";
    private static final String CLUB_EMAIL = "email";
    private static final String CLUB_PHONE = "phone";
    private static final String CLUB_WEB = "web";
    private static final String CLUB_REPRESENTATIVE = "representative";

    private static final String PARTICIPANT_NAME = "Name";
    private static final String PARTICIPANT_LASTNAME = "Lastname";
    private static final String PARTICIPANT_ID_CARD = "123456789Z";

    private static final ScoreType TOURNAMENT_SCORE_SCORE_TYPE = ScoreType.EUROPEAN;
    private static final Integer TOURNAMENT_SCORE_POINTS_BY_VICTORY = 34;
    private static final Integer TOURNAMENT_SCORE_POINTS_BY_DRAW = 19;

    private static final String TOURNAMENT_NAME = "tournament";
    private static final Integer TOURNAMENT_SHIAIJOS = 3;
    private static final Integer TOURNAMENT_TEAM_SIZE = 42;
    private static final TournamentType TOURNAMENT_TYPE = TournamentType.LOOP;

    private static final int DUELS_DURATION = 180;

    private static final RoleType ROLE_TYPE = RoleType.REFEREE;

    private static final String TEAM_NAME = "Name";
    private static final Integer TEAM_GROUP = 129;

    private static final List<Score> DUEL_SCORES = Arrays.asList(Score.MEN, Score.DO);
    private static final Boolean DUEL_FAULTS = true;
    private static final DuelType DUEL_TYPE = DuelType.UNDRAW;

    private static final Integer FIGHT_SHIAIJO = 43;
    private static final Integer FIGHT_LEVEL = 33;

    private static final Integer GROUP_SHIAIJO = 23;
    private static final Integer GROUP_LEVEL = 31;
    private static final int GROUP_NUMBER_OF_WINNERS = 3;

    @Autowired
    private ClubConverter clubConverter;

    @Autowired
    private ParticipantConverter participantConverter;

    @Autowired
    private TournamentScoreConverter tournamentScoreConverter;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private RoleConverter roleConverter;

    @Autowired
    private TeamConverter teamConverter;

    @Autowired
    private DuelConverter duelConverter;

    @Autowired
    private FightConverter fightConverter;

    @Autowired
    private GroupConverter groupConverter;

    private Club createClub() {
        Club club = new Club();
        club.setCity(CLUB_CITY);
        club.setCountry(CLUB_COUNTRY);
        club.setPhone(CLUB_PHONE);
        club.setAddress(CLUB_ADDRESS);
        club.setEmail(CLUB_EMAIL);
        club.setName(CLUB_NAME);
        club.setWeb(CLUB_WEB);
        club.setRepresentative(CLUB_REPRESENTATIVE);

        return club;
    }

    private void checkClub(Club club) {
        Assert.assertEquals(club.getName(), CLUB_NAME);
        Assert.assertEquals(club.getCountry(), CLUB_COUNTRY);
        Assert.assertEquals(club.getCity(), CLUB_CITY);
        Assert.assertEquals(club.getAddress(), CLUB_ADDRESS);
        Assert.assertEquals(club.getEmail(), CLUB_EMAIL);
        Assert.assertEquals(club.getPhone(), CLUB_PHONE);
        Assert.assertEquals(club.getWeb(), CLUB_WEB);
        Assert.assertEquals(club.getRepresentativeId(), CLUB_REPRESENTATIVE);
    }

    private Participant createParticipant() {
        Participant participant = new Participant();
        participant.setName(PARTICIPANT_NAME);
        participant.setLastname(PARTICIPANT_LASTNAME);
        participant.setIdCard(PARTICIPANT_ID_CARD);
        participant.setClub(createClub());
        return participant;
    }

    private void checkParticipant(Participant participant) {
        Assert.assertEquals(participant.getName(), PARTICIPANT_NAME);
        Assert.assertEquals(participant.getLastname(), PARTICIPANT_LASTNAME);
        //Assert.assertEquals(participant.getIdCard(), PARTICIPANT_ID_CARD);
        //checkClub(participant.getClub());
    }

    private TournamentScore createTournamentScore() {
        TournamentScore tournamentScore = new TournamentScore();
        tournamentScore.setScoreType(TOURNAMENT_SCORE_SCORE_TYPE);
        tournamentScore.setPointsByDraw(TOURNAMENT_SCORE_POINTS_BY_DRAW);
        tournamentScore.setPointsByVictory(TOURNAMENT_SCORE_POINTS_BY_VICTORY);
        return tournamentScore;
    }

    private void checkTournamentScore(TournamentScore tournamentScore) {
        Assert.assertEquals(tournamentScore.getScoreType(), TOURNAMENT_SCORE_SCORE_TYPE);
        Assert.assertEquals(tournamentScore.getPointsByDraw(), TOURNAMENT_SCORE_POINTS_BY_DRAW);
        Assert.assertEquals(tournamentScore.getPointsByVictory(), TOURNAMENT_SCORE_POINTS_BY_VICTORY);
    }

    private Tournament createTournament() {
        Tournament tournament = new Tournament();
        tournament.setName(TOURNAMENT_NAME);
        tournament.setShiaijos(TOURNAMENT_SHIAIJOS);
        tournament.setTeamSize(TOURNAMENT_TEAM_SIZE);
        tournament.setType(TOURNAMENT_TYPE);
        tournament.setTournamentScore(createTournamentScore());
        tournament.setDuelsDuration(DUELS_DURATION);
        return tournament;
    }

    private void checkTournament(Tournament tournament) {
        Assert.assertEquals(tournament.getName(), TOURNAMENT_NAME);
        Assert.assertEquals(tournament.getShiaijos(), TOURNAMENT_SHIAIJOS);
        Assert.assertEquals(tournament.getTeamSize(), TOURNAMENT_TEAM_SIZE);
        Assert.assertEquals(tournament.getType(), TOURNAMENT_TYPE);
        checkTournamentScore(tournament.getTournamentScore());
    }

    private Role createRole() {
        Role role = new Role();
        role.setParticipant(createParticipant());
        role.setTournament(createTournament());
        role.setRoleType(ROLE_TYPE);
        return role;
    }

    private void checkRole(Role role) {
        Assert.assertEquals(role.getRoleType(), ROLE_TYPE);
        checkTournament(role.getTournament());
        checkParticipant(role.getParticipant());
    }

    private Team createTeam() {
        Team team = new Team();
        team.setName(TEAM_NAME);
        team.setTournament(createTournament());
        team.addMember(createParticipant());
        return team;
    }

    private void checkTeam(Team team) {
        Assert.assertEquals(team.getName(), TEAM_NAME);
        checkTournament(team.getTournament());
        checkParticipant(team.getMembers().get(0));
    }

    private Duel createDuel() {
        Duel duel = new Duel();
        duel.setCompetitor1(createParticipant());
        duel.setCompetitor2(createParticipant());
        duel.setCompetitor1Fault(DUEL_FAULTS);
        duel.setCompetitor2Fault(DUEL_FAULTS);
        duel.setCompetitor1Score(DUEL_SCORES);
        duel.setCompetitor2Score(DUEL_SCORES);
        duel.setType(DUEL_TYPE);
        return duel;
    }

    private void checkDuel(Duel duel) {
        Assert.assertEquals(duel.getCompetitor1Fault(), DUEL_FAULTS);
        Assert.assertEquals(duel.getCompetitor2Fault(), DUEL_FAULTS);
        Assert.assertEquals(duel.getCompetitor1Score(), DUEL_SCORES);
        Assert.assertEquals(duel.getCompetitor2Score(), DUEL_SCORES);
        Assert.assertEquals(duel.getType(), DUEL_TYPE);
        checkParticipant(duel.getCompetitor1());
        checkParticipant(duel.getCompetitor2());
    }

    private Fight createFight() {
        Fight fight = new Fight();
        fight.setTournament(createTournament());
        fight.setTeam1(createTeam());
        fight.setTeam2(createTeam());
        fight.setLevel(FIGHT_LEVEL);
        fight.setShiaijo(FIGHT_SHIAIJO);
        fight.setDuels(Arrays.asList(createDuel(), createDuel()));
        return fight;
    }

    private void checkFight(Fight fight) {
        Assert.assertEquals(fight.getLevel(), FIGHT_LEVEL);
        Assert.assertEquals(fight.getShiaijo(), FIGHT_SHIAIJO);
        checkTeam(fight.getTeam1());
        checkTeam(fight.getTeam2());
        checkTournament(fight.getTournament());
        checkDuel(fight.getDuels().get(0));
        checkDuel(fight.getDuels().get(1));
    }

    private Group createGroup() {
        Group group = new Group();
        group.setTournament(createTournament());
        group.setTeams(Arrays.asList(createTeam(), createTeam(), createTeam()));
        group.setLevel(GROUP_LEVEL);
        group.setIndex(0);
        group.setShiaijo(GROUP_SHIAIJO);
        group.setNumberOfWinners(GROUP_NUMBER_OF_WINNERS);
        group.setFights(Arrays.asList(createFight(), createFight(), createFight()));
        group.setUnties(Arrays.asList(createDuel(), createDuel()));
        return group;
    }

    private void checkGroup(Group group) {
        Assert.assertEquals(group.getLevel(), GROUP_LEVEL);
        Assert.assertEquals(group.getShiaijo(), GROUP_SHIAIJO);
        Assert.assertEquals(group.getNumberOfWinners(), GROUP_NUMBER_OF_WINNERS);
        Assert.assertEquals((int) group.getIndex(), 0);
        checkTournament(group.getTournament());
        checkTeam(group.getTeams().get(0));
        checkTeam(group.getTeams().get(1));
        checkTeam(group.getTeams().get(2));
        checkFight(group.getFights().get(0));
        checkFight(group.getFights().get(1));
        checkFight(group.getFights().get(2));
        checkDuel(group.getUnties().get(0));
        checkDuel(group.getUnties().get(1));
    }

    @Test
    public void checkClubConverter() {
        checkClub(clubConverter.reverse(clubConverter.convert(new ClubConverterRequest(createClub()))));
    }

    @Test
    public void checkParticipantConverter() {
        checkParticipant(participantConverter.reverse(participantConverter.convert(new ParticipantConverterRequest(createParticipant()))));
    }

    @Test
    public void checkTournamentScoreConverter() {
        checkTournamentScore(tournamentScoreConverter.reverse(tournamentScoreConverter.convert(
                new TournamentScoreConverterRequest(createTournamentScore()))));
    }

    @Test
    public void checkTournamentConverter() {
        checkTournament(tournamentConverter.reverse(tournamentConverter.convert(new TournamentConverterRequest(createTournament()))));
    }

    @Test
    public void checkRoleConverter() {
        checkRole(roleConverter.reverse(roleConverter.convert(new RoleConverterRequest(createRole()))));
    }

    @Test
    public void checkTeamConverter() {
        checkTeam(teamConverter.reverse(teamConverter.convert(new TeamConverterRequest(createTeam()))));
    }

    @Test
    public void checkDuelConverter() {
        checkDuel(duelConverter.reverse(duelConverter.convert(new DuelConverterRequest(createDuel()))));
    }

    @Test
    public void checkFightConverter() {
        checkFight(fightConverter.reverse(fightConverter.convert(new FightConverterRequest(createFight()))));
    }

    @Test
    public void checkGroupConverter() {
        checkGroup(groupConverter.reverse(groupConverter.convert(new GroupConverterRequest(createGroup()))));
    }
}


