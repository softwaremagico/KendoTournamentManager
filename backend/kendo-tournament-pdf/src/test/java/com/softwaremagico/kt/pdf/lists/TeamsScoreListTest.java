package com.softwaremagico.kt.pdf.lists;

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

import com.lowagie.text.pdf.PdfPTable;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.SwissTieBreakRule;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.context.MessageSource;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = {"listsUnitTests"})
public class TeamsScoreListTest {

    private MessageSource mockMessageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    private TeamDTO team(TournamentDTO tournament, String name) {
        return new TeamDTO(name, tournament);
    }

    private ScoreOfTeamDTO score(TeamDTO team, int wonFights, int drawFights, int wonDuels, int drawDuels, int hits, int untieDuels, int fightsDone) {
        final ScoreOfTeamDTO scoreOfTeamDTO = new ScoreOfTeamDTO();
        scoreOfTeamDTO.setTeam(team);
        scoreOfTeamDTO.setWonFights(wonFights);
        scoreOfTeamDTO.setDrawFights(drawFights);
        scoreOfTeamDTO.setWonDuels(wonDuels);
        scoreOfTeamDTO.setDrawDuels(drawDuels);
        scoreOfTeamDTO.setHits(hits);
        scoreOfTeamDTO.setUntieDuels(untieDuels);
        scoreOfTeamDTO.setFightsDone(fightsDone);
        return scoreOfTeamDTO;
    }

    @Test
    public void nonSwissTournament_expectDefaultWidthsAndNoTieBreakColumn() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final TeamDTO teamDTO = team(tournament, "TeamA");
        final ScoreOfTeamDTO scoreOfTeamDTO = score(teamDTO, 1, 0, 2, 0, 5, 0, 1);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), tournament, List.of(scoreOfTeamDTO));

        assertEquals(teamsScoreList.getTableWidths().length, 5);

        final PdfPTable table = new PdfPTable(teamsScoreList.getTableWidths());
        teamsScoreList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void nullTournament_expectNoTieBreakColumn() {
        final TeamDTO teamDTO = new TeamDTO("TeamA", null);
        final ScoreOfTeamDTO scoreOfTeamDTO = score(teamDTO, 1, 0, 2, 0, 5, 1, 1);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), null, List.of(scoreOfTeamDTO));

        assertFalse(teamsScoreList.getTableWidths().length == 6);
        final PdfPTable table = new PdfPTable(teamsScoreList.getTableWidths());
        teamsScoreList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void swissTournamentWithoutTieBreakRule_expectDefaultWidths() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.SWISS);
        final TeamDTO teamDTO = team(tournament, "TeamA");
        final ScoreOfTeamDTO scoreOfTeamDTO = score(teamDTO, 1, 0, 2, 0, 5, 0, 1);
        scoreOfTeamDTO.setSwissTieBreakRuleUsed(null);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), tournament, List.of(scoreOfTeamDTO));

        assertEquals(teamsScoreList.getTableWidths().length, 5);
    }

    @Test
    public void swissTournamentWithBuchholz_expectTieBreakColumnAndDefaultLabel() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.SWISS);
        final TeamDTO teamDTO = team(tournament, "TeamA");
        final ScoreOfTeamDTO scoreOfTeamDTO = score(teamDTO, 1, 0, 2, 0, 5, 0, 1);
        scoreOfTeamDTO.setSwissTieBreakRuleUsed(SwissTieBreakRule.BUCHHOLZ);
        scoreOfTeamDTO.setSwissTieBreakValue(3.0);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), tournament, List.of(scoreOfTeamDTO));

        assertEquals(teamsScoreList.getTableWidths().length, 6);
        final PdfPTable table = new PdfPTable(teamsScoreList.getTableWidths());
        teamsScoreList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void swissTournamentWithMedianBuchholz_expectMedianLabel() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.SWISS);
        final TeamDTO teamDTO = team(tournament, "TeamA");
        final ScoreOfTeamDTO scoreOfTeamDTO = score(teamDTO, 1, 0, 2, 0, 5, 0, 1);
        scoreOfTeamDTO.setSwissTieBreakRuleUsed(SwissTieBreakRule.MEDIAN_BUCHHOLZ);
        scoreOfTeamDTO.setSwissTieBreakValue(4.0);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), tournament, List.of(scoreOfTeamDTO));

        assertTrue(teamsScoreList.getTableWidths().length == 6);
        final PdfPTable table = new PdfPTable(teamsScoreList.getTableWidths());
        teamsScoreList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void swissTournamentWithSonnebornBerger_expectDecimalFormatAndLabel() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.SWISS);
        final TeamDTO teamDTO = team(tournament, "TeamA");
        final ScoreOfTeamDTO scoreOfTeamDTO = score(teamDTO, 1, 0, 2, 0, 5, 2, 1);
        scoreOfTeamDTO.setSwissTieBreakRuleUsed(SwissTieBreakRule.SONNEBORN_BERGER);
        scoreOfTeamDTO.setSwissTieBreakValue(2.5);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), tournament, List.of(scoreOfTeamDTO));

        final PdfPTable table = new PdfPTable(teamsScoreList.getTableWidths());
        teamsScoreList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void swissTournamentWithNonDisplayableRule_expectDefaultWidths() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.SWISS);
        final TeamDTO teamDTO = team(tournament, "TeamA");
        final ScoreOfTeamDTO scoreOfTeamDTO = score(teamDTO, 1, 0, 2, 0, 5, 0, 1);
        scoreOfTeamDTO.setSwissTieBreakRuleUsed(SwissTieBreakRule.DIRECT_ENCOUNTER);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), tournament, List.of(scoreOfTeamDTO));

        assertEquals(teamsScoreList.getTableWidths().length, 5);
    }

    @Test
    public void formatSwissTieBreakValue_withNullValue_expectDash() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.SWISS);
        final TeamDTO teamDTO = team(tournament, "TeamA");
        final ScoreOfTeamDTO scoreOfTeamDTO = score(teamDTO, 1, 0, 2, 0, 5, 0, 1);
        scoreOfTeamDTO.setSwissTieBreakRuleUsed(SwissTieBreakRule.BUCHHOLZ);
        scoreOfTeamDTO.setSwissTieBreakValue(null);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), tournament, List.of(scoreOfTeamDTO));

        final PdfPTable table = new PdfPTable(teamsScoreList.getTableWidths());
        teamsScoreList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void headerAndFooterAndProperties_expectNoException() {
        final TournamentDTO tournament = new TournamentDTO("MyTournament", 1, 3, TournamentType.LEAGUE);
        final TeamsScoreList teamsScoreList = new TeamsScoreList(mockMessageSource(), Locale.getDefault(), tournament, List.of());

        final PdfPTable table = new PdfPTable(teamsScoreList.getTableWidths());
        teamsScoreList.setTableProperties(table);
        teamsScoreList.createHeaderRow(null, table, 0, 0, null,
                com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
        teamsScoreList.createFooterRow(null, table, 0, 0, null, null, 0);
        assertEquals(teamsScoreList.getPageSize(), com.lowagie.text.PageSize.A4);
    }
}

