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
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.exceptions.GroupNotFoundException;
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
import static org.testng.Assert.assertThrows;

@Test(groups = {"listsUnitTests"})
public class FightsListTest {

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

    private GroupDTO group(TournamentDTO tournament, int level, int index, List<FightDTO> fights) {
        final GroupDTO groupDTO = new GroupDTO();
        groupDTO.setTournament(tournament);
        groupDTO.setLevel(level);
        groupDTO.setIndex(index);
        groupDTO.setShiaijo(0);
        groupDTO.setTeams(List.of());
        groupDTO.setFights(fights);
        return groupDTO;
    }

    @Test
    public void championshipTournament_expectAllPhaseHeadersAndGroupFights() {
        final TournamentDTO tournament = new TournamentDTO("Championship", 1, 1, TournamentType.CHAMPIONSHIP);
        final TeamDTO teamWithNullMember = team(tournament, "TeamNull", null);
        final ParticipantDTO validMember = new ParticipantDTO("1", "Name", "Lastname", null);
        final TeamDTO teamWithValidMember = team(tournament, "TeamValid", validMember);

        // Level 0: two groups -> tests round header and multiple-groups branch.
        final FightDTO fight0a = new FightDTO(tournament, teamWithNullMember, teamWithValidMember, 0, 0);
        final GroupDTO level0GroupA = group(tournament, 0, 0, new ArrayList<>(List.of(fight0a)));
        final FightDTO fight0b = new FightDTO(tournament, teamWithValidMember, teamWithNullMember, 0, 0);
        final GroupDTO level0GroupB = group(tournament, 0, 1, new ArrayList<>(List.of(fight0b)));

        // Level 1: single group -> tests semifinal header and single-group branch.
        final FightDTO fight1 = new FightDTO(tournament, teamWithNullMember, teamWithValidMember, 0, 1);
        final GroupDTO level1Group = group(tournament, 1, 0, new ArrayList<>(List.of(fight1)));

        // Level 2: single group -> tests final header (championship type).
        final FightDTO fight2 = new FightDTO(tournament, teamWithNullMember, teamWithValidMember, 0, 2);
        final GroupDTO level2Group = group(tournament, 2, 0, new ArrayList<>(List.of(fight2)));

        // Level 3 (max level): group with no fights -> tests the "no fights in level" skip branch.
        final GroupDTO level3EmptyGroup = group(tournament, 3, 0, new ArrayList<>());

        final List<GroupDTO> groups = List.of(level0GroupA, level0GroupB, level1Group, level2Group, level3EmptyGroup);

        final FightsList fightsList = new FightsList(mockMessageSource(), Locale.getDefault(), tournament, groups);
        final PdfPTable table = new PdfPTable(fightsList.getTableWidths());
        fightsList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void nonChampionshipNonLeagueTournament_expectFinalHeaderNotAdded() {
        final TournamentDTO tournament = new TournamentDTO("Tree", 1, 1, TournamentType.TREE);
        final ParticipantDTO validMember = new ParticipantDTO("1", "Name", "Lastname", null);
        final TeamDTO team1 = team(tournament, "Team1", validMember);
        final TeamDTO team2 = team(tournament, "Team2", validMember);

        final FightDTO fight = new FightDTO(tournament, team1, team2, 0, 0);
        final GroupDTO groupDTO = group(tournament, 0, 0, new ArrayList<>(List.of(fight)));

        final FightsList fightsList = new FightsList(mockMessageSource(), Locale.getDefault(), tournament, List.of(groupDTO));
        final PdfPTable table = new PdfPTable(fightsList.getTableWidths());
        fightsList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void noGroups_expectGroupNotFoundException() {
        final TournamentDTO tournament = new TournamentDTO("Championship", 1, 1, TournamentType.CHAMPIONSHIP);
        final FightsList fightsList = new FightsList(mockMessageSource(), Locale.getDefault(), tournament, List.of());
        final PdfPTable table = new PdfPTable(fightsList.getTableWidths());

        assertThrows(GroupNotFoundException.class, () -> fightsList.createBodyRows(null, table, 0, 0, null, null, 0));
    }

    @Test
    public void leagueTournament_expectSimpleTable() {
        final TournamentDTO tournament = new TournamentDTO("League", 2, 1, TournamentType.LEAGUE);
        final ParticipantDTO validMember = new ParticipantDTO("1", "Name", "Lastname", null);
        final TeamDTO team1 = team(tournament, "Team1", validMember);
        final TeamDTO team2 = team(tournament, "Team2", validMember);

        final FightDTO fightShiaijo0 = new FightDTO(tournament, team1, team2, 0, 0);
        final FightDTO fightShiaijo1 = new FightDTO(tournament, team2, team1, 1, 0);
        final GroupDTO groupDTO = group(tournament, 0, 0, new ArrayList<>(List.of(fightShiaijo0, fightShiaijo1)));

        final FightsList fightsList = new FightsList(mockMessageSource(), Locale.getDefault(), tournament, List.of(groupDTO));
        final PdfPTable table = new PdfPTable(fightsList.getTableWidths());
        fightsList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void headerAndFooterAndProperties_expectNoException() {
        final TournamentDTO tournament = new TournamentDTO("MyTournament", 1, 1, TournamentType.LEAGUE);
        final FightsList fightsList = new FightsList(mockMessageSource(), Locale.getDefault(), tournament, List.of());

        final PdfPTable table = new PdfPTable(fightsList.getTableWidths());
        fightsList.setTableProperties(table);
        fightsList.createHeaderRow(null, table, 0, 0, null, com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
        fightsList.createFooterRow(null, table, 0, 0, null, null, 0);
    }
}

