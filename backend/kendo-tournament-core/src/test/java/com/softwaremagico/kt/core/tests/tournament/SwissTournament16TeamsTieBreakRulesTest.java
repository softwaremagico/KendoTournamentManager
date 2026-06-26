package com.softwaremagico.kt.core.tests.tournament;

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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.SwissTieBreakRule;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@Test(groups = {"swissTournament16TieBreakRulesTest"})
public class SwissTournament16TeamsTieBreakRulesTest extends AbstractTestNGSpringContextTests {

    // Este test construye un Swiss determinista de 16 equipos/4 rondas para forzar empates
    // en match points y poder validar que el ranking cambia segun la regla de desempate.

    private static final String CLUB_NAME = "Swiss16TieBreakClub";
    private static final String CLUB_CITY = "Swiss16TieBreakCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 16;
    private static final int ROUNDS = 4;
    private static final int FIGHTS_PER_ROUND = 8;
    private static final String TOURNAMENT_NAME = "SwissTournament16TeamsTieBreakRulesTest";

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private ClubController clubController;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private GroupController groupController;

    @Autowired
    private FightController fightController;

    @Autowired
    private DuelController duelController;

    @Autowired
    private FightConverter fightConverter;

    @Autowired
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    private ClubDTO clubDTO;
    private TournamentDTO tournamentDTO;

    @Test
    public void shouldExposeValidSwissTieBreakRules() {
        // Contrato del enum: solo estas reglas son validas para SWISS_TIE_BREAK_RULE.
        // Si este listado cambia, deben actualizarse validacion y tests de ranking.
        Assert.assertEquals(List.of(SwissTieBreakRule.values()), List.of(
                SwissTieBreakRule.BUCHHOLZ,
                SwissTieBreakRule.MEDIAN_BUCHHOLZ,
                SwissTieBreakRule.SONNEBORN_BERGER,
                SwissTieBreakRule.DIRECT_ENCOUNTER,
                SwissTieBreakRule.POINT_DIFFERENTIAL));
        // getType es case-insensitive para facilitar configuraciones via texto.
        Assert.assertEquals(SwissTieBreakRule.getType("buchholz"), SwissTieBreakRule.BUCHHOLZ);
        Assert.assertEquals(SwissTieBreakRule.getType("sonneborn_berger"), SwissTieBreakRule.SONNEBORN_BERGER);
    }

    @Test(dependsOnMethods = "shouldExposeValidSwissTieBreakRules")
    public void addClub() {
        clubDTO = clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            participantController.create(new ParticipantDTO(String.format("S16TB-%04d", i),
                    String.format("name%s", i), String.format("lastname%s", i), clubDTO), null, null);
        }
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() {
        Assert.assertEquals(tournamentController.count(), 0);
        tournamentDTO = tournamentController.create(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.SWISS), null, null);
        Assert.assertEquals(tournamentController.count(), 1);
    }

    @Test(dependsOnMethods = "addTournament")
    public void configureSwissProperties() {
        // 4 rondas para 16 equipos (sin byes) y sin evitar repetidos para que el emparejamiento
        // sea estable y totalmente reproducible en este escenario de pruebas.
        tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(tournamentDTO,
                TournamentExtraPropertyKey.SWISS_ROUNDS, String.valueOf(ROUNDS)), null, null);
        tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(tournamentDTO,
                TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, Boolean.FALSE.toString()), null, null);
        // Inicialmente se usa Buchholz; luego se sobrescribe en el DataProvider para cada caso.
        tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(tournamentDTO,
                TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()), null, null);

        Assert.assertEquals(tournamentExtraPropertyController.getByTournamentAndProperty(tournamentDTO.getId(),
                TournamentExtraPropertyKey.SWISS_ROUNDS).getPropertyValue(), String.valueOf(ROUNDS));
        Assert.assertEquals(tournamentExtraPropertyController.getByTournamentAndProperty(tournamentDTO.getId(),
                TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS).getPropertyValue(), Boolean.FALSE.toString());
    }

    @Test(dependsOnMethods = "configureSwissProperties")
    public void addRoles() {
        for (ParticipantDTO competitor : participantController.get()) {
            roleController.create(new RoleDTO(tournamentDTO, competitor, RoleType.COMPETITOR), null, null);
        }
        Assert.assertEquals(roleController.count(tournamentDTO), participantController.count());
    }

    @Test(dependsOnMethods = "addRoles")
    public void addTeams() {
        int teamIndex = 0;
        TeamDTO team = null;
        int teamMember = 0;

        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);
        Assert.assertEquals(groups.size(), 1);

        for (ParticipantDTO competitor : participantController.get()) {
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
                teamMember = 0;
            }

            team.addMember(competitor);
            team = teamController.create(team, null, null);

            if (teamMember == 0) {
                groupController.addTeams(groups.getFirst().getId(), Collections.singletonList(team), null, null);
            }

            teamMember++;
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        Assert.assertEquals(teamController.count(tournamentDTO), TEAMS);
        Assert.assertEquals(groupController.getGroups(tournamentDTO, 0).getFirst().getTeams().size(), TEAMS);
    }

    @Test(dependsOnMethods = "addTeams")
    public void createAndAdvanceSwissRoundsWithTieScenarios() {
        // expectedPairingsByRound asegura que estamos probando SIEMPRE la misma topologia de cruces.
        // scoresByFight define resultados concretos para provocar empates en puntos Swiss.
        final Map<Integer, List<String>> expectedPairingsByRound = getExpectedPairingsByRound();
        final Map<String, int[]> scoresByFight = getScoresByFight();

        for (int level = 0; level < ROUNDS; level++) {
            final List<FightDTO> createdFights = fightController.createFights(tournamentDTO.getId(), TeamsOrder.NONE, level, null, null);
            Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

            final List<Fight> fightsInRound = groupController.getGroups(tournamentDTO, level).stream()
                    .flatMap(group -> group.getFights().stream())
                    .toList();
            Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);
            Assert.assertEquals(fightsInRound.stream().map(this::fightKey).toList(), expectedPairingsByRound.get(level));

            for (Fight fight : fightsInRound) {
                final int[] configuredScore = scoresByFight.get(level + ":" + fightKey(fight));
                Assert.assertNotNull(configuredScore, "Missing configured score for fight " + level + ":" + fightKey(fight));
                applyResult(fight, configuredScore[0], configuredScore[1]);
            }
        }
    }

    @Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithTieScenarios")
    public void checkFinalSwissScoreDistribution() {
        // Distribucion esperada tras 4 rondas (match points):
        // 4W: Team01
        // 3W: Team09, Team05, Team15, Team14 (grupo que se reordena por desempate)
        // 2W: Team02, Team03, Team07, Team10, Team11, Team12
        // 1W: Team04, Team06, Team08, Team13
        // 0W: Team16
        // Este test fija la "base" del torneo; el siguiente valida solo el orden interno del 3W.
        final List<ScoreOfTeam> ranking = rankingProvider.getTeamsScoreRanking(tournamentConverter.reverse(tournamentDTO));
        Assert.assertEquals(ranking.size(), TEAMS);
        Assert.assertEquals(ranking.getFirst().getTeam().getName(), "Team01");
        Assert.assertEquals(getTeamsWithWins(ranking, 4), List.of("Team01"));
        Assert.assertEquals(getTeamsWithWins(ranking, 3), List.of("Team09", "Team05", "Team15", "Team14"));
        Assert.assertEquals(getTeamsWithWinsSorted(ranking, 2), List.of("Team02", "Team03", "Team07", "Team10", "Team11", "Team12"));
        Assert.assertEquals(getTeamsWithWinsSorted(ranking, 1), List.of("Team04", "Team06", "Team08", "Team13"));
        Assert.assertEquals(getTeamsWithWins(ranking, 0), List.of("Team16"));
    }

    @DataProvider(name = "swissTieBreakRules")
    public Object[][] swissTieBreakRules() {
        // Explicacion del orden esperado en el grupo de 3 victorias:
        // - BUCHHOLZ / MEDIAN_BUCHHOLZ / SONNEBORN_BERGER -> Team09, Team05, Team15, Team14
        //   En este dataset Team09 y Team05 acumulan mejor fuerza de rivales; Team14 queda ultimo.
        // - DIRECT_ENCOUNTER -> Team15, Team09, Team05, Team14
        //   Entre empatados, Team15 gana el duelo directo contra Team09 (R2: Team09-Team15, 1-2)
        //   y por eso sube por delante.
        // - POINT_DIFFERENTIAL -> Team14, Team05, Team09, Team15
        //   Team14 obtiene mayor diferencial total de puntos en sus combates y pasa a liderar el 3W.
        return new Object[][]{
                {SwissTieBreakRule.BUCHHOLZ, List.of("Team09", "Team05", "Team15", "Team14")},
                {SwissTieBreakRule.MEDIAN_BUCHHOLZ, List.of("Team09", "Team05", "Team15", "Team14")},
                {SwissTieBreakRule.SONNEBORN_BERGER, List.of("Team09", "Team05", "Team15", "Team14")},
                {SwissTieBreakRule.DIRECT_ENCOUNTER, List.of("Team15", "Team09", "Team05", "Team14")},
                {SwissTieBreakRule.POINT_DIFFERENTIAL, List.of("Team14", "Team05", "Team09", "Team15")}
        };
    }

    @Test(dataProvider = "swissTieBreakRules", dependsOnMethods = "checkFinalSwissScoreDistribution")
    public void checkSwissTieBreakRuleReordersTiedTeams(SwissTieBreakRule rule, List<String> expectedThreeWinTeamsOrder) {
        // Para cada regla: se actualiza la propiedad del torneo, se recalcula ranking y se verifica
        // que SOLO cambia el orden de los empatados a 3 victorias segun el criterio seleccionado.
        tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(tournamentDTO,
                TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, rule.name()), null, null);

        final TournamentExtraPropertyDTO storedProperty = tournamentExtraPropertyController.getByTournamentAndProperty(
                tournamentDTO.getId(), TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE);
        Assert.assertEquals(storedProperty.getPropertyValue(), rule.name());

        final List<ScoreOfTeam> ranking = rankingProvider.getTeamsScoreRanking(tournamentConverter.reverse(tournamentDTO));
        Assert.assertEquals(ranking.getFirst().getTeam().getName(), "Team01");
        Assert.assertEquals(getTeamsWithWins(ranking, 3), expectedThreeWinTeamsOrder,
                "Unexpected 3-win teams order for tie-break rule " + rule);
    }

    private Map<Integer, List<String>> getExpectedPairingsByRound() {
        final Map<Integer, List<String>> pairings = new LinkedHashMap<>();
        // Emparejamientos esperados para el escenario determinista (SWISS_AVOID_REPEATED_PAIRINGS=false).
        // Cualquier cambio aqui implica que el algoritmo de pairing ha cambiado y el test debe revisarse.
        pairings.put(0, List.of(
                "Team01-Team02",
                "Team03-Team04",
                "Team05-Team06",
                "Team07-Team08",
                "Team09-Team10",
                "Team11-Team12",
                "Team13-Team14",
                "Team15-Team16"));
        pairings.put(1, List.of(
                "Team01-Team03",
                "Team05-Team07",
                "Team09-Team11",
                "Team13-Team15",
                "Team02-Team04",
                "Team06-Team08",
                "Team10-Team12",
                "Team14-Team16"));
        pairings.put(2, List.of(
                "Team01-Team05",
                "Team09-Team15",
                "Team02-Team03",
                "Team06-Team07",
                "Team10-Team11",
                "Team13-Team14",
                "Team04-Team08",
                "Team12-Team16"));
        pairings.put(3, List.of(
                "Team01-Team15",
                "Team02-Team05",
                "Team07-Team09",
                "Team10-Team14",
                "Team03-Team04",
                "Team06-Team11",
                "Team12-Team13",
                "Team08-Team16"));
        return pairings;
    }

    private Map<String, int[]> getScoresByFight() {
        final Map<String, int[]> scores = new LinkedHashMap<>();
        // Marcadores elegidos para crear un bloque de 4 equipos empatados a 3 victorias
        // con desempates no triviales entre Buchholz/DirectEncounter/PointDifferential.
        scores.put("0:Team01-Team02", new int[]{2, 1});
        scores.put("0:Team03-Team04", new int[]{2, 0});
        scores.put("0:Team05-Team06", new int[]{2, 0});
        scores.put("0:Team07-Team08", new int[]{2, 1});
        scores.put("0:Team09-Team10", new int[]{2, 1});
        scores.put("0:Team11-Team12", new int[]{2, 1});
        scores.put("0:Team13-Team14", new int[]{2, 1});
        scores.put("0:Team15-Team16", new int[]{2, 1});

        scores.put("1:Team01-Team03", new int[]{2, 0});
        scores.put("1:Team05-Team07", new int[]{2, 0});
        scores.put("1:Team09-Team11", new int[]{2, 1});
        scores.put("1:Team13-Team15", new int[]{1, 2});
        scores.put("1:Team02-Team04", new int[]{2, 0});
        scores.put("1:Team06-Team08", new int[]{2, 0});
        scores.put("1:Team10-Team12", new int[]{2, 0});
        scores.put("1:Team14-Team16", new int[]{2, 0});

        scores.put("2:Team01-Team05", new int[]{2, 1});
        scores.put("2:Team09-Team15", new int[]{1, 2});
        scores.put("2:Team02-Team03", new int[]{2, 0});
        scores.put("2:Team06-Team07", new int[]{1, 2});
        scores.put("2:Team10-Team11", new int[]{2, 1});
        scores.put("2:Team13-Team14", new int[]{0, 2});
        scores.put("2:Team04-Team08", new int[]{2, 1});
        scores.put("2:Team12-Team16", new int[]{2, 0});

        scores.put("3:Team01-Team15", new int[]{2, 1});
        scores.put("3:Team02-Team05", new int[]{1, 2});
        scores.put("3:Team07-Team09", new int[]{1, 2});
        scores.put("3:Team10-Team14", new int[]{0, 2});
        scores.put("3:Team03-Team04", new int[]{2, 1});
        scores.put("3:Team06-Team11", new int[]{0, 2});
        scores.put("3:Team12-Team13", new int[]{2, 1});
        scores.put("3:Team08-Team16", new int[]{2, 0});
        return scores;
    }

    private String fightKey(Fight fight) {
        return fight.getTeam1().getName() + "-" + fight.getTeam2().getName();
    }

    private void applyResult(Fight fight, int team1Score, int team2Score) {
        for (int i = 0; i < team1Score; i++) {
            fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
        }
        for (int i = 0; i < team2Score; i++) {
            fight.getDuels().getFirst().addCompetitor2Score(Score.MEN);
        }
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null, null);
    }

    private List<String> getTeamsWithWins(List<ScoreOfTeam> ranking, int wins) {
        return ranking.stream()
                .filter(score -> score.getWonFights() == wins)
                .map(score -> score.getTeam().getName())
                .toList();
    }

    private List<String> getTeamsWithWinsSorted(List<ScoreOfTeam> ranking, int wins) {
        return getTeamsWithWins(ranking, wins).stream().sorted().toList();
    }

    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        if (tournamentDTO != null) {
            groupController.delete(tournamentDTO);
            fightController.delete(tournamentDTO);
            duelController.delete(tournamentDTO);
            teamController.delete(tournamentDTO);
            roleController.delete(tournamentDTO);
            tournamentController.delete(tournamentDTO, null, null);
        }
        participantController.deleteAll();
        if (clubDTO != null) {
            clubController.delete(clubDTO, null, null);
        }
        Assert.assertEquals(fightController.count(), 0);
        Assert.assertEquals(duelController.count(), 0);
    }
}


