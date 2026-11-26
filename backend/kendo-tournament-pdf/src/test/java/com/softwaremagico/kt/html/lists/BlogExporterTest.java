package com.softwaremagico.kt.html.lists;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.context.MessageSource;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

@Test(groups = {"listsUnitTests"})
public class BlogExporterTest {

    private MessageSource mockMessageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    private GroupDTO group(TournamentDTO tournament, int level, int shiaijo, List<FightDTO> fights) {
        final GroupDTO groupDTO = new GroupDTO();
        groupDTO.setTournament(tournament);
        groupDTO.setLevel(level);
        groupDTO.setIndex(0);
        groupDTO.setShiaijo(shiaijo);
        groupDTO.setTeams(List.of());
        groupDTO.setFights(fights);
        return groupDTO;
    }

    @Test
    public void teamTournamentWithMultipleGroups_expectAllBranches() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 2, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO withRole = new ParticipantDTO("1", "Alice", "Alastname", club);
        final ParticipantDTO withoutRole = new ParticipantDTO("2", "Bob", "Blastname", club);

        final List<RoleDTO> roles = new ArrayList<>();
        roles.add(new RoleDTO(tournament, withRole, RoleType.COMPETITOR));

        final TeamDTO team1 = new TeamDTO("Team1", tournament);
        final TeamDTO team2 = new TeamDTO("Team2", tournament);

        // Duel 1: normal scores, one side with valid scoreTime, other side score out of
        // range (null) and EMPTY.
        final DuelDTO duel1 = new DuelDTO(withRole, withoutRole, tournament, null);
        duel1.setCompetitor1Score(new ArrayList<>(List.of(Score.MEN)));
        duel1.setCompetitor1ScoreTime(new ArrayList<>(List.of(10)));
        duel1.setCompetitor2Score(new ArrayList<>(List.of(Score.EMPTY, Score.MEN)));
        duel1.setCompetitor2ScoreTime(null);
        duel1.setCompetitor1Fault(true);
        duel1.setCompetitor2Fault(false);
        duel1.setFinished(true);

        // Duel 2: draw fight and null competitor2 (covers ternary null branch).
        final DuelDTO duel2 = new DuelDTO(withoutRole, null, tournament, null);
        duel2.setCompetitor1Score(new ArrayList<>(List.of(Score.MEN)));
        duel2.setCompetitor2Score(new ArrayList<>(List.of(Score.MEN)));
        duel2.setFinished(true);

        final FightDTO fight1 = new FightDTO(tournament, team1, team2, 0, 0);
        fight1.setDuels(new ArrayList<>(List.of(duel1)));
        final GroupDTO group1 = this.group(tournament, 0, 0, new ArrayList<>(List.of(fight1)));

        final FightDTO fight2 = new FightDTO(tournament, team2, team1, 1, 0);
        fight2.setDuels(new ArrayList<>(List.of(duel2)));
        final GroupDTO group2 = this.group(tournament, 0, 1, new ArrayList<>(List.of(fight2)));

        final ScoreOfTeamDTO scoreOfTeam = new ScoreOfTeamDTO();
        scoreOfTeam.setTeam(team1);
        scoreOfTeam.setWonFights(1);
        scoreOfTeam.setDrawFights(0);
        scoreOfTeam.setWonDuels(1);
        scoreOfTeam.setDrawDuels(0);
        scoreOfTeam.setHits(3);

        final ScoreOfCompetitorDTO scoreOfCompetitor = new ScoreOfCompetitorDTO(withRole, false);
        scoreOfCompetitor.setWonDuels(1);
        scoreOfCompetitor.setDrawDuels(0);
        scoreOfCompetitor.setHits(3);

        final BlogExporter blogExporter = new BlogExporter(this.mockMessageSource(), Locale.getDefault(), tournament,
                roles, List.of(group1, group2), List.of(withRole, withoutRole), List.of(scoreOfTeam),
                List.of(scoreOfCompetitor));

        assertFalse(blogExporter.getWordpressFormat().isEmpty());
    }

    @Test
    public void individualTournamentSingleGroup_expectNoTeamTable() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO competitor = new ParticipantDTO("1", "Alice", "Alastname", club);

        final List<RoleDTO> roles = new ArrayList<>(List.of(new RoleDTO(tournament, competitor, RoleType.COMPETITOR)));

        final TeamDTO team1 = new TeamDTO("Team1", tournament);
        final TeamDTO team2 = new TeamDTO("Team2", tournament);

        final DuelDTO duel = new DuelDTO(competitor, competitor, tournament, null);
        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 0);
        fight.setDuels(new ArrayList<>(List.of(duel)));
        final GroupDTO group = this.group(tournament, 0, 0, new ArrayList<>(List.of(fight)));

        final ScoreOfCompetitorDTO scoreOfCompetitor = new ScoreOfCompetitorDTO(competitor, false);
        scoreOfCompetitor.setWonDuels(0);
        scoreOfCompetitor.setDrawDuels(0);
        scoreOfCompetitor.setHits(0);

        final BlogExporter blogExporter = new BlogExporter(this.mockMessageSource(), Locale.getDefault(), tournament,
                roles, List.of(group), List.of(competitor), List.of(), List.of(scoreOfCompetitor));

        assertFalse(blogExporter.getWordpressFormat().isEmpty());
    }

    @Test
    public void scoreDataRecordConstructor_expectSameResultAsExplicitLists() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO competitor = new ParticipantDTO("1", "Alice", "Alastname", club);
        final List<RoleDTO> roles = new ArrayList<>(List.of(new RoleDTO(tournament, competitor, RoleType.COMPETITOR)));

        final BlogExporter.ScoreData scoreData = new BlogExporter.ScoreData(List.of(), List.of());
        final BlogExporter blogExporter = new BlogExporter(this.mockMessageSource(), Locale.getDefault(), tournament,
                roles, List.of(), List.of(competitor), scoreData);

        assertFalse(blogExporter.getWordpressFormat().isEmpty());
    }
}
