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
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.exceptions.GroupNotFoundException;
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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

@Test(groups = {"listsUnitTests"})
public class FightSummaryTest {

    private MessageSource mockMessageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    private TeamDTO team(TournamentDTO tournament, String name, ParticipantDTO member) {
        final TeamDTO teamDTO = new TeamDTO(name, tournament);
        final List<ParticipantDTO> members = new ArrayList<>();
        members.add(member);
        teamDTO.setMembers(members);
        return teamDTO;
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

    private FightDTO fightWithDuel(TournamentDTO tournament, TeamDTO team1, TeamDTO team2, int shiaijo, int level, DuelDTO duel) {
        final FightDTO fightDTO = new FightDTO(tournament, team1, team2, shiaijo, level);
        fightDTO.setDuels(new ArrayList<>(List.of(duel)));
        return fightDTO;
    }

    @Test
    public void getDrawFight_withDrawAndOverFight_expectDrawAbbreviation() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
        final DuelDTO duel = new DuelDTO(null, null, tournament, null);
        duel.setFinished(true);
        final TeamDTO team1 = team(tournament, "T1", null);
        final TeamDTO team2 = team(tournament, "T2", null);
        final FightDTO fight = fightWithDuel(tournament, team1, team2, 0, 0, duel);

        final FightSummary fightSummary = new FightSummary(mockMessageSource(), Locale.getDefault(), tournament, List.of(group(tournament, 0, 0,
                new ArrayList<>(List.of(fight)))), null);

        assertEquals(fightSummary.getDrawFight(fight, 0), String.valueOf(Score.DRAW.getPdfAbbreviation()));
    }

    @Test
    public void getDrawFight_withNotOverFight_expectEmptyAbbreviation() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
        final DuelDTO duel = new DuelDTO(null, null, tournament, null);
        final TeamDTO team1 = team(tournament, "T1", null);
        final TeamDTO team2 = team(tournament, "T2", null);
        final FightDTO fight = fightWithDuel(tournament, team1, team2, 0, 0, duel);

        final FightSummary fightSummary = new FightSummary(mockMessageSource(), Locale.getDefault(), tournament, List.of(group(tournament, 0, 0,
                new ArrayList<>(List.of(fight)))), null);

        assertEquals(fightSummary.getDrawFight(fight, 0), String.valueOf(Score.EMPTY.getPdfAbbreviation()));
    }

    @Test
    public void getFaults_bothSides_expectCorrectValues() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
        final DuelDTO duel = new DuelDTO(null, null, tournament, null);
        duel.setCompetitor1Fault(true);
        duel.setCompetitor2Fault(false);
        final TeamDTO team1 = team(tournament, "T1", null);
        final TeamDTO team2 = team(tournament, "T2", null);
        final FightDTO fight = fightWithDuel(tournament, team1, team2, 0, 0, duel);

        final FightSummary fightSummary = new FightSummary(mockMessageSource(), Locale.getDefault(), tournament, List.of(group(tournament, 0, 0,
                new ArrayList<>(List.of(fight)))), null);

        assertTrue(fightSummary.getFaults(fight, 0, true));
        assertFalse(fightSummary.getFaults(fight, 0, false));
    }

    @Test
    public void getScore_withExistingAndMissingValues_expectCorrectResult() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
        final DuelDTO duel = new DuelDTO(null, null, tournament, null);
        duel.addCompetitor1Score(Score.MEN);
        final TeamDTO team1 = team(tournament, "T1", null);
        final TeamDTO team2 = team(tournament, "T2", null);
        final FightDTO fight = fightWithDuel(tournament, team1, team2, 0, 0, duel);

        final FightSummary fightSummary = new FightSummary(mockMessageSource(), Locale.getDefault(), tournament, List.of(group(tournament, 0, 0,
                new ArrayList<>(List.of(fight)))), null);

        assertEquals(fightSummary.getScore(fight, 0, 0, true), Score.MEN);
        assertNull(fightSummary.getScore(fight, 0, 1, true));
        assertNull(fightSummary.getScore(fight, 0, 0, false));
    }

    @Test
    public void championshipWithMultipleLevelsAndShiaijoFilter_expectAllBranches() {
        final TournamentDTO tournament = new TournamentDTO("Championship", 2, 1, TournamentType.CHAMPIONSHIP);
        final ParticipantDTO member = new ParticipantDTO("1", "Name", "Lastname", null);
        final TeamDTO teamA = team(tournament, "TeamA", member);
        final TeamDTO teamB = team(tournament, "TeamB", member);

        final DuelDTO duel = new DuelDTO(null, null, tournament, null);

        // Level 0: two groups on different shiaijos, only shiaijo 0 selected -> tests matchesSelectedShiaijo false branch.
        final FightDTO fight0a = fightWithDuel(tournament, teamA, teamB, 0, 0, duel);
        final GroupDTO level0GroupShiaijo0 = group(tournament, 0, 0, new ArrayList<>(List.of(fight0a)));
        final FightDTO fight0b = fightWithDuel(tournament, teamB, teamA, 1, 0, duel);
        final GroupDTO level0GroupShiaijo1 = group(tournament, 0, 1, new ArrayList<>(List.of(fight0b)));

        // Level 1: single group -> tests semifinal header and groupsOfLevel.size()<=1 (no group header).
        final FightDTO fight1 = fightWithDuel(tournament, teamA, teamB, 0, 1, duel);
        final GroupDTO level1Group = group(tournament, 1, 0, new ArrayList<>(List.of(fight1)));

        // Level 2: single group -> tests final header (championship type).
        final FightDTO fight2 = fightWithDuel(tournament, teamA, teamB, 0, 2, duel);
        final GroupDTO level2Group = group(tournament, 2, 0, new ArrayList<>(List.of(fight2)));

        final List<GroupDTO> groups = List.of(level0GroupShiaijo0, level0GroupShiaijo1, level1Group, level2Group);

        final FightSummary fightSummary = new FightSummary(mockMessageSource(), Locale.getDefault(), tournament, groups, 0);
        final PdfPTable table = new PdfPTable(fightSummary.getTableWidths());
        fightSummary.createBodyRows(null, table, 0, 0, null, null, 0);

        fightSummary.createHeaderRow(null, table, 0, 0, null, com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
    }

    @Test
    public void withoutShiaijoFilter_expectHeaderWithoutSuffix() {
        final TournamentDTO tournament = new TournamentDTO("League", 1, 1, TournamentType.LEAGUE);
        final ParticipantDTO member = new ParticipantDTO("1", "Name", "Lastname", null);
        final TeamDTO teamA = team(tournament, "TeamA", member);
        final TeamDTO teamB = team(tournament, "TeamB", member);
        final DuelDTO duel = new DuelDTO(null, null, tournament, null);
        final FightDTO fight = fightWithDuel(tournament, teamA, teamB, 0, 0, duel);
        final GroupDTO groupDTO = group(tournament, 0, 0, new ArrayList<>(List.of(fight)));

        final FightSummary fightSummary = new FightSummary(mockMessageSource(), Locale.getDefault(), tournament, List.of(groupDTO), null);
        final PdfPTable table = new PdfPTable(fightSummary.getTableWidths());
        fightSummary.createBodyRows(null, table, 0, 0, null, null, 0);
        fightSummary.createHeaderRow(null, table, 0, 0, null, com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
    }

    @Test
    public void noGroups_expectGroupNotFoundException() {
        final TournamentDTO tournament = new TournamentDTO("Championship", 1, 1, TournamentType.CHAMPIONSHIP);
        final FightSummary fightSummary = new FightSummary(mockMessageSource(), Locale.getDefault(), tournament, List.of(), null);
        final PdfPTable table = new PdfPTable(fightSummary.getTableWidths());

        assertThrows(GroupNotFoundException.class, () -> fightSummary.createBodyRows(null, table, 0, 0, null, null, 0));
    }

    @Test
    public void footerAndPropertiesAndPageSize_expectNoException() {
        final TournamentDTO tournament = new TournamentDTO("MyTournament", 1, 1, TournamentType.LEAGUE);
        final FightSummary fightSummary = new FightSummary(mockMessageSource(), Locale.getDefault(), tournament, List.of(), null);

        final PdfPTable table = new PdfPTable(fightSummary.getTableWidths());
        fightSummary.setTableProperties(table);
        fightSummary.createFooterRow(null, table, 0, 0, null, null, 0);
        assertEquals(fightSummary.getPageSize(), com.lowagie.text.PageSize.A4);
    }
}

