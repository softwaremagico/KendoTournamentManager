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
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
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

@Test(groups = {"listsUnitTests"})
public class CompetitorsScoreListTest {

    private MessageSource mockMessageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    private ScoreOfCompetitorDTO score(ParticipantDTO participant, int wonDuels, int untieDuels, int hits, int untieHits, int duelsDone) {
        final ScoreOfCompetitorDTO scoreOfCompetitorDTO = new ScoreOfCompetitorDTO(participant, false);
        scoreOfCompetitorDTO.setWonDuels(wonDuels);
        scoreOfCompetitorDTO.setDrawDuels(0);
        scoreOfCompetitorDTO.setUntieDuels(untieDuels);
        scoreOfCompetitorDTO.setHits(hits);
        scoreOfCompetitorDTO.setUntieHits(untieHits);
        scoreOfCompetitorDTO.setDuelsDone(duelsDone);
        return scoreOfCompetitorDTO;
    }

    @Test
    public void withTournament_showClassification_untieValues_expectNoException() {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participant = new ParticipantDTO("1", "Name", "Lastname", club);
        final ScoreOfCompetitorDTO scoreOfCompetitorDTO = score(participant, 3, 1, 5, 1, 4);

        final CompetitorsScoreList competitorsScoreList = new CompetitorsScoreList(mockMessageSource(), Locale.getDefault(), tournament,
                List.of(scoreOfCompetitorDTO), true);

        assertEquals(competitorsScoreList.getTableWidths().length, 5);
        final PdfPTable table = new PdfPTable(competitorsScoreList.getTableWidths());
        competitorsScoreList.createHeaderRow(null, table, 0, 0, null, com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
        competitorsScoreList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void withoutTournament_withoutClassification_withoutUntieValues_expectNoException() {
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participant = new ParticipantDTO("1", "Name", "Lastname", club);
        final ScoreOfCompetitorDTO scoreOfCompetitorDTO = score(participant, 3, 0, 5, 0, 4);

        final CompetitorsScoreList competitorsScoreList = new CompetitorsScoreList(mockMessageSource(), Locale.getDefault(), null,
                List.of(scoreOfCompetitorDTO), false);

        assertEquals(competitorsScoreList.getTableWidths().length, 4);
        final PdfPTable table = new PdfPTable(competitorsScoreList.getTableWidths());
        competitorsScoreList.createHeaderRow(null, table, 0, 0, null, com.softwaremagico.kt.pdf.PdfTheme.getBasicFont(), 12);
        competitorsScoreList.createBodyRows(null, table, 0, 0, null, null, 0);
    }

    @Test
    public void footerAndProperties_expectNoException() {
        final CompetitorsScoreList competitorsScoreList = new CompetitorsScoreList(mockMessageSource(), Locale.getDefault(), null, List.of(), false);

        final PdfPTable table = new PdfPTable(competitorsScoreList.getTableWidths());
        competitorsScoreList.setTableProperties(table);
        competitorsScoreList.createFooterRow(null, table, 0, 0, null, null, 0);
        assertEquals(competitorsScoreList.getPageSize(), com.lowagie.text.PageSize.A4);
    }
}

