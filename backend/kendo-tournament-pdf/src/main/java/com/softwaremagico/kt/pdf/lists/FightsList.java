package com.softwaremagico.kt.pdf.lists;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.exceptions.GroupNotFoundException;
import com.softwaremagico.kt.pdf.BaseColor;
import com.softwaremagico.kt.pdf.ParentList;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.utils.NameUtils;
import com.softwaremagico.kt.utils.ShiaijoName;
import org.springframework.context.MessageSource;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Gets the list of fights to be shown on a tournament. This list can be printed before the start of the tournament.
 */
public class FightsList extends ParentList {
    private static final float[] TABLE_WITH = {0.40f, 0.10f, 0.40f};

    private final MessageSource messageSource;
    private final Locale locale;
    private final TournamentDTO tournamentDto;
    private final List<FightDTO> fights;
    private final List<GroupDTO> groups;

    public FightsList(MessageSource messageSource, Locale locale, TournamentDTO tournament, List<GroupDTO> groups) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.tournamentDto = tournament;
        this.fights = groups.stream().flatMap(groupDTO -> groupDTO.getFights().stream()).toList();
        this.groups = groups;
    }

    public PdfPTable fightTable(FightDTO f) {
        final float[] widths = getTableWidths();
        final PdfPTable fightTable = new PdfPTable(widths);
        fightTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        fightTable.addCell(getHeader3(f.getTeam1().getName() + " - " + f.getTeam2().getName(), 0));

        for (int i = 0; i < f.getTournament().getTeamSize(); i++) {
            ParticipantDTO competitor = f.getTeam1().getMembers().get(i);
            String name = "";
            if (competitor != null) {
                name = NameUtils.getLastnameNameIni(competitor);
            }
            fightTable.addCell(getCell(name, 1, Element.ALIGN_LEFT));
            fightTable.addCell(getEmptyCell(1));
            competitor = f.getTeam2().getMembers().get(i);
            name = "";
            if (competitor != null) {
                name = NameUtils.getLastnameNameIni(competitor);
            }
            fightTable.addCell(getCell(name, 1, Element.ALIGN_RIGHT));
        }

        return fightTable;
    }

    private void simpleTable(PdfPTable mainTable) {
        PdfPCell cell;
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        for (int i = 0; i < tournamentDto.getShiaijos(); i++) {
            final int shiaijo = i;
            final List<FightDTO> fightsRow = this.fights.stream().filter(fightDTO -> fightDTO.getShiaijo().equals(shiaijo)).toList();
            mainTable.addCell(getEmptyRow());
            mainTable.addCell(getEmptyRow());
            mainTable.addCell(getHeader2(
                    messageSource.getMessage("tournament.shiaijo", null, locale) + " " + ShiaijoName.getShiaijoName(i), TABLE_BORDER));

            for (final FightDTO fight : fightsRow) {
                cell = new PdfPCell(fightTable(fight));
                cell.setBorderWidth(BORDER_WIDTH);
                cell.setColspan(TABLE_WITH.length);
                cell.setBackgroundColor(BaseColor.WHITE);
                mainTable.addCell(cell);
            }
        }
    }

    private void championshipTable(PdfPTable mainTable) {
        PdfPCell cell;

        final Integer levels = groups.stream().max(Comparator.comparing(GroupDTO::getLevel)).orElseThrow(() ->
                new GroupNotFoundException(this.getClass(), "Group not found!")).getLevel();

        for (int level = 0; level <= levels; level++) {
            final Integer currentLevel = level;
            final List<GroupDTO> groupsOfLevel = groups.stream().filter(groupDTO -> Objects.equals(groupDTO.getLevel(), currentLevel))
                    .toList();
            if (groupsOfLevel.stream().anyMatch(groupDTO -> !groupDTO.getFights().isEmpty())) {
                /*
                 * Header of the phase
                 */
                mainTable.addCell(getEmptyRow());
                mainTable.addCell(getEmptyRow());

                if (level < levels - 2) {
                    mainTable.addCell(getHeader1(messageSource.getMessage("tournament.fight.round", null, locale) + " " + (level + 1), 0,
                            Element.ALIGN_LEFT));
                } else if (level == levels - 2) {
                    mainTable.addCell(getHeader1(messageSource.getMessage("tournament.fight.semifinal", null, locale), 0, Element.ALIGN_LEFT));
                } else if (tournamentDto.getType().equals(TournamentType.CHAMPIONSHIP)) {
                    mainTable.addCell(getHeader1(messageSource.getMessage("tournament.fight.final", null, locale), 0, Element.ALIGN_LEFT));
                }

                for (int i = 0; i < groupsOfLevel.size(); i++) {
                    mainTable.addCell(getEmptyRow());
                    if (groupsOfLevel.size() > 1) {
                        mainTable.addCell(getHeader2(
                                messageSource.getMessage("tournament.group", null, locale) + " " + (i + 1) + " ("
                                        + messageSource.getMessage("tournament.shiaijo", null, locale) + " " + ShiaijoName.getShiaijoName(i), TABLE_BORDER));
                    }

                    for (final FightDTO fight : fights) {
                        if (groupsOfLevel.get(i).getFights().contains(fight)) {
                            cell = new PdfPCell(fightTable(fight));
                            cell.setBorderWidth(BORDER_WIDTH);
                            cell.setColspan(TABLE_WITH.length);
                            cell.setBackgroundColor(BaseColor.WHITE);
                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            mainTable.addCell(cell);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                               BaseFont font, int fontSize) {
        if (tournamentDto.getType().equals(TournamentType.LEAGUE)) {
            simpleTable(mainTable);
        } else {
            championshipTable(mainTable);
        }
    }

    @Override
    public float[] getTableWidths() {
        return TABLE_WITH;
    }

    @Override
    public void setTableProperties(PdfPTable mainTable) {
        mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        mainTable.getDefaultCell().setBorder(TABLE_BORDER);
        mainTable.getDefaultCell().setBorderColor(BaseColor.BLACK);
        mainTable.setWidthPercentage(TOTAL_WIDTH);
    }

    @Override
    public void createHeaderRow(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                                BaseFont font, int fontSize) {
        final PdfPCell cell = new PdfPCell(new Paragraph(tournamentDto.getName(), new Font(font, fontSize)));
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
