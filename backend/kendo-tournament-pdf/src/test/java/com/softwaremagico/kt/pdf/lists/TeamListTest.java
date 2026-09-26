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
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertThrows;

@Test(groups = {"listsUnitTests"})
public class TeamListTest {

    @Test
    public void emptyTeams_expectEmptyPdfBodyException() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final TeamList teamList = new TeamList(tournament, List.of());
        final PdfPTable table = new PdfPTable(teamList.getTableWidths());

        assertThrows(EmptyPdfBodyException.class, () -> teamList.createBodyRows(null, table, 0, 0, null, null, 0));
    }

    @Test
    public void teamWithNullAndBlankAndValidMembers_expectNoException() throws EmptyPdfBodyException {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");

        final TeamDTO teamWithNullMember = new TeamDTO("Team1", tournament);
        final List<ParticipantDTO> membersWithNull = new ArrayList<>();
        membersWithNull.add(null);
        teamWithNullMember.setMembers(membersWithNull);

        final ParticipantDTO blankLastname = new ParticipantDTO("1", "Name", "", club);
        final TeamDTO teamWithBlankLastname = new TeamDTO("Team2", tournament);
        teamWithBlankLastname.addMember(blankLastname);

        final ParticipantDTO validMember = new ParticipantDTO("2", "Name2", "Lastname2", club);
        final TeamDTO teamWithValidMember = new TeamDTO("Team3", tournament);
        teamWithValidMember.addMember(validMember);

        final TeamList teamList = new TeamList(tournament, List.of(teamWithNullMember, teamWithBlankLastname, teamWithValidMember));
        final PdfPTable table = new PdfPTable(teamList.getTableWidths());
        teamList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void headerAndFooterAndProperties_expectNoException() {
        final TournamentDTO tournament = new TournamentDTO("MyTournament", 1, 3, TournamentType.LEAGUE);
        final TeamList teamList = new TeamList(tournament, List.of());

        final PdfPTable table = new PdfPTable(teamList.getTableWidths());
        teamList.setTableProperties(table);
        teamList.createHeaderRow(null, table, 0, 0, null, com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
        teamList.createFooterRow(null, table, 0, 0, null, null, 0);
    }
}

