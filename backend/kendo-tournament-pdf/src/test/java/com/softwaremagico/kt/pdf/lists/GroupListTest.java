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
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
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
public class GroupListTest {

    private MessageSource mockMessageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    private GroupDTO group(TournamentDTO tournament, int level, int index, int shiaijo, List<TeamDTO> teams) {
        final GroupDTO groupDTO = new GroupDTO();
        groupDTO.setTournament(tournament);
        groupDTO.setLevel(level);
        groupDTO.setIndex(index);
        groupDTO.setShiaijo(shiaijo);
        groupDTO.setTeams(teams);
        groupDTO.setFights(new ArrayList<>());
        return groupDTO;
    }

    @Test
    public void emptyGroups_expectEmptyPdfBodyException() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final GroupList groupList = new GroupList(mockMessageSource(), Locale.getDefault(), tournament, List.of());
        final PdfPTable table = new PdfPTable(groupList.getTableWidths());

        assertThrows(EmptyPdfBodyException.class, () -> groupList.createBodyRows(null, table, 0, 0, null, null, 0));
    }

    @Test
    public void multipleLevelsWithEmptyLevelSkipped_expectNoException() throws EmptyPdfBodyException {
        final TournamentDTO tournament = new TournamentDTO("T", 2, 3, TournamentType.LEAGUE);
        final TeamDTO team1 = new TeamDTO("Team1", tournament);
        final TeamDTO team2 = new TeamDTO("Team2", tournament);
        final TeamDTO team3 = new TeamDTO("Team3", tournament);

        final GroupDTO level0GroupA = group(tournament, 0, 0, 0, List.of(team1));
        final GroupDTO level0GroupB = group(tournament, 0, 1, 1, List.of(team2));
        // Level 1 has no teams in any group -> must be skipped.
        final GroupDTO level1EmptyGroup = group(tournament, 1, 0, 0, List.of());
        final GroupDTO level2Group = group(tournament, 2, 0, 0, List.of(team3));

        final GroupList groupList = new GroupList(mockMessageSource(), Locale.getDefault(), tournament,
                List.of(level0GroupA, level0GroupB, level1EmptyGroup, level2Group));
        final PdfPTable table = new PdfPTable(groupList.getTableWidths());

        groupList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void singleShiaijoTournament_expectNoShiaijoSuffix() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final TeamDTO team1 = new TeamDTO("Team1", tournament);
        final GroupDTO groupDTO = group(tournament, 0, 0, 0, List.of(team1));

        final GroupList groupList = new GroupList(mockMessageSource(), Locale.getDefault(), tournament, List.of(groupDTO));
        groupList.groupTable(groupDTO);
    }

    @Test
    public void headerAndFooterAndProperties_expectNoException() {
        final TournamentDTO tournament = new TournamentDTO("MyTournament", 1, 3, TournamentType.LEAGUE);
        final GroupList groupList = new GroupList(mockMessageSource(), Locale.getDefault(), tournament, List.of());

        final PdfPTable table = new PdfPTable(groupList.getTableWidths());
        groupList.setTableProperties(table);
        groupList.createHeaderRow(null, table, 0, 0, null, com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
        groupList.createFooterRow(null, table, 0, 0, null, null, 0);
    }
}

