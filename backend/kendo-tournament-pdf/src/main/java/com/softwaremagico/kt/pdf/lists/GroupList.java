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
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.BaseColor;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.ParentList;
import com.softwaremagico.kt.utils.GroupUtils;
import com.softwaremagico.kt.utils.ShiaijoName;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GroupList extends ParentList {

    private static final float[] TABLE_WIDTH = {0.46f, 0.08f, 0.46f};
    private static final int BORDER = 0;

    private final MessageSource messageSource;
    private final Locale locale;

    private final TournamentDTO tournament;

    private final List<GroupDTO> groups;

    public GroupList(MessageSource messageSource, Locale locale, TournamentDTO tournament, List<GroupDTO> groups) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.tournament = tournament;
        this.groups = groups;
    }

    @Override
    public void setTableProperties(PdfPTable mainTable) {
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.getDefaultCell().setBorder(TABLE_BORDER);
        mainTable.getDefaultCell().setBorderColor(BaseColor.BLACK);
        mainTable.setWidthPercentage(TOTAL_WIDTH);
    }

    public PdfPTable groupTable(GroupDTO groupDTO) {
        final PdfPTable teamTable = new PdfPTable(1);

        teamTable.addCell(getHeader4(messageSource.getMessage("tournament.group", null, locale) + " " + (groupDTO.getIndex() + 1)
                + (tournament.getShiaijos() > 1 ? " (" + messageSource.getMessage("tournament.shiaijo", null, locale) + ": "
                + ShiaijoName.getShiaijoName(groupDTO.getShiaijo()) + ")" : ""), 0));

        for (final TeamDTO teamDTO : groupDTO.getTeams()) {
            teamTable.addCell(getCell(teamDTO.getName()));
        }

        return teamTable;

    }

    @Override
    public void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                               BaseFont font, int fontSize) throws EmptyPdfBodyException {
        PdfPCell cell;
        Paragraph p;

        mainTable.addCell(getEmptyRow());

        if (groups.isEmpty()) {
            throw new EmptyPdfBodyException("No existing groups");
        }

        final Map<Integer, List<GroupDTO>> groupsByLevel = GroupUtils.orderDTOByLevel(groups);

        for (int level = 0; level < groupsByLevel.size(); level++) {
            //Check if level is empty!
            boolean empty = true;
            for (GroupDTO groupDTO : groupsByLevel.get(level)) {
                if (!groupDTO.getTeams().isEmpty()) {
                    empty = false;
                    break;
                }
            }

            if (empty) {
                continue;
            }

            //Show level header.
            if (groupsByLevel.size() > 1) {
                mainTable.addCell(getHeader3(messageSource.getMessage("tournament.phase", null, locale) + " " + (level + 1), 0));
            }
            for (int i = 0; i < groupsByLevel.get(level).size(); i++) {
                cell = new PdfPCell(groupTable(groupsByLevel.get(level).get(i)));
                cell.setColspan(1);
                mainTable.addCell(cell);

                if (i % 2 == 0) {
                    p = new Paragraph(" ");
                    cell = new PdfPCell(p);
                    cell.setBorderWidth(BORDER);
                    cell.setColspan(1);
                    mainTable.addCell(cell);
                } else {
                    mainTable.addCell(getEmptyRow());
                }
            }
            mainTable.completeRow();

            //Spaces between levels.
            mainTable.addCell(getEmptyRow());
            mainTable.addCell(getEmptyRow());
        }
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
