package com.softwaremagico.kt.pdf.lists;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
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


import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.BaseColor;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.ParentList;
import com.softwaremagico.kt.pdf.PdfTheme;
import com.softwaremagico.kt.utils.NameUtils;

import java.util.List;

public class TeamList extends ParentList {

    private static final float[] TABLE_WIDTH = {0.46f, 0.08f, 0.46f};
    private static final int BORDER = 0;
    private final TournamentDTO tournament;

    private final List<TeamDTO> teams;

    public TeamList(TournamentDTO tournament, List<TeamDTO> teams) {
        this.tournament = tournament;
        this.teams = teams;
    }

    @Override
    public void setTableProperties(PdfPTable mainTable) {
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.getDefaultCell().setBorder(TABLE_BORDER);
        mainTable.getDefaultCell().setBorderColor(BaseColor.BLACK);
        mainTable.setWidthPercentage(TOTAL_WIDTH);
    }

    public PdfPTable teamTable(TeamDTO teamDTO) {
        final PdfPTable teamTable = new PdfPTable(1);

        teamTable.addCell(getHeader4(NameUtils.getShortName(teamDTO), 0));

        for (final ParticipantDTO member : teamDTO.getMembers()) {
            String memberName;
            try {
                if (member.getLastname() != null && !member.getLastname().isEmpty()) {
                    memberName = NameUtils.getLastnameName(member);
                } else {
                    memberName = " ";
                }
            } catch (NullPointerException npe) {
                memberName = " ";
            }
            teamTable.addCell(getCell(memberName, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_LEFT));
        }

        return teamTable;

    }

    @Override
    public void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                               BaseFont font, int fontSize) throws EmptyPdfBodyException {
        PdfPCell cell;
        Paragraph p;

        mainTable.addCell(getEmptyRow());

        if (teams.isEmpty()) {
            throw new EmptyPdfBodyException("No existing teams");
        }

        for (int i = 0; i < teams.size(); i++) {
            cell = new PdfPCell(teamTable(teams.get(i)));
            cell.setColspan(1);
            mainTable.addCell(cell);

            if (i % 2 == 0) {
                p = new Paragraph(" ");
                cell = new PdfPCell(p);
                cell.setBorderWidth(BORDER);
                cell.setColspan(1);
                mainTable.addCell(cell);
            }
        }
        mainTable.completeRow();
    }

    @Override
    public float[] getTableWidths() {
        return TABLE_WIDTH;
    }


    @Override
    public void createHeaderRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        final PdfPCell cell = new PdfPCell(new Paragraph(tournament.getName(), new Font(font, fontSize)));
        cell.setColspan(getTableWidths().length);
        cell.setBorderWidth(HEADER_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setMinimumHeight(MIN_HEADER_HIGH);
        mainTable.addCell(cell);
    }

    @Override
    public void createFooterRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        mainTable.addCell(getEmptyRow());
    }

    @Override
    protected Rectangle getPageSize() {
        return PageSize.A4;
    }


    @Override
    protected void addDocumentWriterEvents(PdfWriter writer) {
        // No background.
    }
}
