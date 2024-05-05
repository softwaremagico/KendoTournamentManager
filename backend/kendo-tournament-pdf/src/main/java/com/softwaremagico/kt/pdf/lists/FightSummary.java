package com.softwaremagico.kt.pdf.lists;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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
import com.lowagie.text.DocumentException;
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
import com.softwaremagico.kt.pdf.PdfTheme;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.utils.NameUtils;
import com.softwaremagico.kt.utils.ShiaijoName;
import org.springframework.context.MessageSource;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Creates a sheet with all fights and all its score. The scope is to have a report after the tournament is finished.
 */
public class FightSummary extends ParentList {
    private static final float[] TABLE_WIDTH = {0.29f, 0.03f, 0.08f, 0.08f, 0.04f, 0.08f, 0.08f, 0.03f, 0.29f};
    private static final int DEFAULT_CELL_HEIGHT = 50;
    private static final int FIGHT_BORDER = 1;
    private final MessageSource messageSource;
    private final Locale locale;
    private final TournamentDTO tournament;
    private final Integer useOnlyShiaijo;
    private final List<GroupDTO> groups;
    private final List<FightDTO> fights;

    public FightSummary(MessageSource messageSource, Locale locale, TournamentDTO tournament, List<GroupDTO> groups, Integer shiaijo) {
        this.tournament = tournament;
        this.messageSource = messageSource;
        this.locale = locale;
        this.useOnlyShiaijo = shiaijo;
        this.groups = groups;
        this.fights = groups.stream().flatMap(groupDTO -> groupDTO.getFights().stream()).toList();
    }

    protected String getDrawFight(FightDTO fightDTO, int duel) {
        // Draw Fights
        if (Objects.equals(fightDTO.getDuels().get(duel).getWinner(), 0) && fightDTO.isOver()) {
            return String.valueOf(Score.DRAW.getAbbreviation());
        } else {
            return String.valueOf(Score.EMPTY.getAbbreviation());
        }
    }

    protected String getFaults(FightDTO fightDTO, int duel, boolean leftTeam) {
        if (leftTeam) {
            return fightDTO.getDuels().get(duel).getCompetitor1Fault() ? String.valueOf(Score.FAULT.getAbbreviation())
                    : String.valueOf(Score.EMPTY.getAbbreviation());
        } else {
            return fightDTO.getDuels().get(duel).getCompetitor2Fault() ? String.valueOf(Score.FAULT.getAbbreviation())
                    : String.valueOf(Score.EMPTY.getAbbreviation());
        }
    }

    protected String getScore(FightDTO fightDTO, int duel, int score, boolean leftTeam) {
        try {
            if (leftTeam) {
                return String.valueOf(fightDTO.getDuels().get(duel).getCompetitor1Score().get(score).getAbbreviation());
            } else {
                return String.valueOf(fightDTO.getDuels().get(duel).getCompetitor2Score().get(score).getAbbreviation());
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            return "";
        }
    }

    private PdfPTable fightTable(FightDTO fightDTO, boolean first) throws DocumentException {
        final PdfPTable table = new PdfPTable(getTableWidths());

        if (!first) {
            table.addCell(getEmptyRow(DEFAULT_CELL_HEIGHT));
        }

        table.addCell(getHeader3(fightDTO.getTeam1().getName() + " - " + fightDTO.getTeam2().getName(), 0));

        for (int i = 0; i < fightDTO.getTournament().getTeamSize(); i++) {
            // Team 1
            ParticipantDTO competitor = fightDTO.getTeam1().getMembers().get(i);
            String name = "";
            if (competitor != null) {
                name = NameUtils.getLastnameNameIni(competitor);
            }
            table.addCell(getCell(name, FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_LEFT));

            // Faults
            table.addCell(getCell(getFaults(fightDTO, i, true), FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));

            // Points
            table.addCell(getCell(getScore(fightDTO, i, 1, true), FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
            table.addCell(getCell(getScore(fightDTO, i, 0, true), FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));

            table.addCell(getCell(getDrawFight(fightDTO, i), FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));

            // Points Team 2
            table.addCell(getCell(getScore(fightDTO, i, 0, false), FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));
            table.addCell(getCell(getScore(fightDTO, i, 1, false), FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));

            // Faults
            table.addCell(getCell(getFaults(fightDTO, i, false), FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_CENTER));

            // Team 2
            competitor = fightDTO.getTeam2().getMembers().get(i);
            name = "";
            if (competitor != null) {
                name = NameUtils.getLastnameNameIni(competitor);
            }
            table.addCell(getCell(name, FIGHT_BORDER, PdfTheme.getHandwrittenFont(), 1, Element.ALIGN_RIGHT));
        }
        table.addCell(getEmptyRow(DEFAULT_CELL_HEIGHT));

        return table;
    }

    @Override
    public void createBodyRows(Document document, PdfPTable mainTable, float width, float height, PdfWriter writer,
                               BaseFont font, int fontSize) {

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
                } else if (tournament.getType().equals(TournamentType.CHAMPIONSHIP)) {
                    mainTable.addCell(getHeader1(messageSource.getMessage("tournament.fight.final", null, locale), 0, Element.ALIGN_LEFT));
                }

                for (int i = 0; i < groups.size(); i++) {
                    // Only groups of shiaijo X.
                    if (useOnlyShiaijo == null || groups.get(i).getShiaijo().equals(useOnlyShiaijo)) {
                        mainTable.addCell(getEmptyRow());
                        if (groupsOfLevel.size() > 1) {
                            final StringBuilder header = new StringBuilder(messageSource.getMessage("tournament.group", null, locale) + " " + (i + 1));
                            if (useOnlyShiaijo != null) {
                                header.append(" (").append(messageSource.getMessage("tournament.shiaijo", null, locale)).append(" ")
                                        .append(ShiaijoName.getShiaijoName(groups.get(i).getShiaijo())).append(")");
                            }
                            mainTable.addCell(getHeader2(header.toString(), 0));
                        }

                        for (final FightDTO fight : fights) {
                            if (groupsOfLevel.get(i).getFights().contains(fight)) {
                                final PdfPCell cell = new PdfPCell(fightTable(fight, true));
                                cell.setBorderWidth(BORDER_WIDTH);
                                cell.setColspan(getTableWidths().length);
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                mainTable.addCell(cell);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public float[] getTableWidths() {
        return TABLE_WIDTH;
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
        String header = tournament.getName();
        if (useOnlyShiaijo != null) {
            header += " (" + messageSource.getMessage("tournament.shiaijo", null, locale) + " " + ShiaijoName.getShiaijoName(useOnlyShiaijo) + ")";
        }
        final PdfPCell cell = new PdfPCell(new Paragraph(header, new Font(font, fontSize)));
        cell.setColspan(getTableWidths().length);
        cell.setBorderWidth(HEADER_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
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
