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
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.context.MessageSource;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;

@Test(groups = {"listsUnitTests"})
public class RoleListTest {

    private MessageSource mockMessageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    private ParticipantDTO participant(ClubDTO club, String name, String lastname) {
        return new ParticipantDTO(name, name, lastname, club);
    }

    private RoleDTO role(TournamentDTO tournament, ParticipantDTO participant) {
        return new RoleDTO(tournament, participant, RoleType.COMPETITOR);
    }

    @Test
    public void multipleClubsWithCountry_expectHeadersAndSeparators() throws EmptyPdfBodyException {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO clubA = new ClubDTO("ClubA", "CityA");
        clubA.setCountry("ES");
        final ClubDTO clubB = new ClubDTO("ClubB", "CityB");
        clubB.setCountry("");

        final Map<ClubDTO, List<RoleDTO>> rolesByClub = new LinkedHashMap<>();
        rolesByClub.put(clubA, new ArrayList<>(List.of(role(tournament, participant(clubA, "name1", "last1")))));
        rolesByClub.put(clubB, new ArrayList<>(List.of(role(tournament, participant(clubB, "name2", "last2")))));

        final RoleList roleList = new RoleList(mockMessageSource(), Locale.getDefault(), tournament, rolesByClub);
        final PdfPTable table = new PdfPTable(roleList.getTableWidths());
        roleList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void singleClub_expectNoClubHeader() throws EmptyPdfBodyException {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO clubA = new ClubDTO("ClubA", "CityA");

        final Map<ClubDTO, List<RoleDTO>> rolesByClub = new LinkedHashMap<>();
        rolesByClub.put(clubA, new ArrayList<>(List.of(role(tournament, participant(clubA, "name1", "last1")))));

        final RoleList roleList = new RoleList(mockMessageSource(), Locale.getDefault(), tournament, rolesByClub);
        final PdfPTable table = new PdfPTable(roleList.getTableWidths());
        roleList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void noRoles_expectEmptyPdfBodyException() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO clubA = new ClubDTO("ClubA", "CityA");

        final Map<ClubDTO, List<RoleDTO>> rolesByClub = new LinkedHashMap<>();
        rolesByClub.put(clubA, new ArrayList<>());

        final RoleList roleList = new RoleList(mockMessageSource(), Locale.getDefault(), tournament, rolesByClub);
        final PdfPTable table = new PdfPTable(roleList.getTableWidths());
        assertThrows(EmptyPdfBodyException.class, () -> roleList.createBodyRows(null, table, 0, 0, null, null, 0));
    }

    @Test
    public void headerAndFooterAndProperties_expectNoException() {
        final TournamentDTO tournament = new TournamentDTO("MyTournament", 1, 3, TournamentType.LEAGUE);
        final RoleList roleList = new RoleList(mockMessageSource(), Locale.getDefault(), tournament, new LinkedHashMap<>());

        final PdfPTable table = new PdfPTable(roleList.getTableWidths());
        roleList.setTableProperties(table);
        roleList.createHeaderRow(null, table, 0, 0, null, com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
        roleList.createFooterRow(null, table, 0, 0, null, null, 0);
    }
}



