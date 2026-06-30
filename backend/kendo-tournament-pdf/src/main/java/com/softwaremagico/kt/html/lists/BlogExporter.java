package com.softwaremagico.kt.html.lists;

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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO;
import com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.utils.NameUtils;
import com.softwaremagico.kt.utils.ShiaijoName;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class BlogExporter {
    private static final String NEW_LINE = "&nbsp;\n";
    private static final String HEADER_4_CLOSE_NEW_LINE = "</h4>\n";
    private static final String DIV_CLOSE = "</div>";

    private final MessageSource messageSource;
    private final Locale locale;

    private final TournamentDTO tournament;

    private final List<RoleDTO> roles;

    private final List<GroupDTO> groups;

    private final List<FightDTO> fights;

    private final List<ParticipantDTO> competitors;

    private final List<ScoreOfTeamDTO> scoreOfTeams;

    private final List<ScoreOfCompetitorDTO> scoreOfCompetitors;

    public BlogExporter(MessageSource messageSource, Locale locale, TournamentDTO tournament, List<RoleDTO> roles,
                        List<GroupDTO> groups, List<ParticipantDTO> competitors, List<ScoreOfTeamDTO> scoreOfTeams,
                        List<ScoreOfCompetitorDTO> scoreOfCompetitors) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.tournament = tournament;
        this.roles = roles;
        this.groups = groups;
        this.fights = groups.stream().flatMap(groupDTO -> groupDTO.getFights().stream()).toList();
        this.competitors = new ArrayList<>(competitors);
        this.competitors.sort(Comparator.comparing(NameUtils::getLastnameName));
        this.scoreOfTeams = scoreOfTeams;
        this.scoreOfCompetitors = scoreOfCompetitors;
    }

    /**
     * Header of the document
     */
    private static void addTitle(StringBuilder stringBuilder, TournamentDTO tournament) {
        stringBuilder.append("<h2>").append(tournament.getName()).append("</h2>\n");
    }

    public String getWordpressFormat() {
        final StringBuilder stringBuilder = new StringBuilder();
        addTitle(stringBuilder, tournament);
        addInformation(stringBuilder, tournament);
        addCompetitors(stringBuilder);
        addScoreTables(stringBuilder, tournament);
        if (tournament.getTeamSize() > 1) {
            addTeamClassificationTable(stringBuilder);
        }
        addCompetitorClassificationTable(stringBuilder);
        return stringBuilder.toString();
    }

    /**
     * Extra information of the tournament
     */
    private void addInformation(StringBuilder stringBuilder, TournamentDTO tournament) {
        stringBuilder.append(messageSource.getMessage("tournament.type." + tournament.getType().getCode(), null, locale));
        if (tournament.getTeamSize() > 1) {
            stringBuilder.append(" (").append(messageSource.getMessage("classification.teams.name", null, locale)).append(" ")
                    .append(tournament.getTeamSize()).append(").\n");
        } else {
            stringBuilder.append(".\n");
        }
        stringBuilder.append(NEW_LINE);
    }

    /**
     * List of all people that goes to the championship.
     */
    private void addCompetitors(StringBuilder stringBuilder) {
        stringBuilder.append("<h4>").append(messageSource.getMessage("club.list", null, locale)).append("</h4>");
        final List<List<String>> rows = new ArrayList<>();
        for (final ParticipantDTO competitor : competitors) {
            final List<String> columns = new ArrayList<>();

            final RoleDTO competitorsRole = roles.stream().filter(roleDTO -> Objects.equals(roleDTO.getParticipant(), competitor)).findFirst().orElse(null);
            if (competitorsRole != null) {
                columns.add(NameUtils.getLastnameName(competitor));
                columns.add(messageSource.getMessage("role.type." + competitorsRole.getRoleType().getCode(), null, locale));
                rows.add(columns);
            }
        }
        final int[] widths = {30, 15};
        createTable(stringBuilder, rows, widths);
    }

    /**
     * Create the tables with the results of the fights.
     */
    private void addScoreTables(StringBuilder stringBuilder, TournamentDTO tournament) {
        stringBuilder.append(NEW_LINE + "<h4>").append(messageSource.getMessage("fight.list", null, locale)).append("</h4>");

        final int[] widths = {25, 5, 5, 5, 5, 5, 5, 5, 25};
        for (int i = 0; i < groups.size(); i++) {
            addGroupScoreTables(stringBuilder, tournament, groups.get(i), i, widths);
        }
    }

    private void addGroupScoreTables(StringBuilder stringBuilder, TournamentDTO tournament, GroupDTO group, int groupIndex, int[] widths) {
        addGroupHeader(stringBuilder, group, groupIndex);
        for (final FightDTO fight : getFightsOfGroup(group)) {
            addFightTitle(stringBuilder, tournament, fight);
            createTable(stringBuilder, getFightRows(fight), widths);
        }
    }

    private void addGroupHeader(StringBuilder stringBuilder, GroupDTO group, int groupIndex) {
        if (groups.size() <= 1) {
            return;
        }
        stringBuilder.append("<h4>").append(messageSource.getMessage("tournament.group", null, locale))
                .append(" ").append(groupIndex + 1).append(" (").append(messageSource.getMessage("tournament.shiaijo", null, locale))
                .append(" ").append(ShiaijoName.getShiaijoName(group.getShiaijo())).append(")").append(HEADER_4_CLOSE_NEW_LINE);
    }

    private List<FightDTO> getFightsOfGroup(GroupDTO group) {
        return fights.stream().filter(fight -> group.getFights().contains(fight)).toList();
    }

    private void addFightTitle(StringBuilder stringBuilder, TournamentDTO tournament, FightDTO fight) {
        if (tournament.getTeamSize() > 1) {
            stringBuilder.append(NEW_LINE).append("<h5>").append(fight.getTeam1().getName()).append(" - ")
                    .append(fight.getTeam2().getName()).append("</h5>\n");
        }
    }

    private List<List<String>> getFightRows(FightDTO fight) {
        final List<List<String>> rows = new ArrayList<>();
        for (DuelDTO duelDTO : fight.getDuels()) {
            final List<String> columns = new ArrayList<>();
            columns.add(NameUtils.getLastnameName(duelDTO.getCompetitor1()));
            columns.add(getFaultsDiv(duelDTO, true));
            columns.add(getScoreDiv(duelDTO, 1, true));
            columns.add(getScoreDiv(duelDTO, 0, true));
            columns.add(getDrawFight(duelDTO));
            columns.add(getScoreDiv(duelDTO, 0, false));
            columns.add(getScoreDiv(duelDTO, 1, false));
            columns.add(getFaultsDiv(duelDTO, false));
            columns.add(duelDTO.getCompetitor2() != null ? NameUtils.getLastnameName(duelDTO.getCompetitor2()) : "");
            rows.add(columns);
        }
        return rows;
    }

    private void addTeamClassificationTable(StringBuilder stringBuilder) {
        stringBuilder.append(NEW_LINE + "<h4>").append(messageSource.getMessage("classification.score", null, locale))
                .append(HEADER_4_CLOSE_NEW_LINE);
        final List<List<String>> rows = new ArrayList<>();
        // Header
        List<String> columns = new ArrayList<>();
        columns.add("<b>" + messageSource.getMessage("classification.team.name", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.teams.fights.won", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.teams.duels.won", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.teams.hits", null, locale) + "</b>");
        rows.add(columns);

        for (final ScoreOfTeamDTO scoreOfTeam : scoreOfTeams) {
            columns = new ArrayList<>();
            columns.add(NameUtils.getShortName(scoreOfTeam.getTeam()));
            columns.add(scoreOfTeam.getWonFights() + "/" + scoreOfTeam.getDrawFights());
            columns.add(scoreOfTeam.getWonDuels() + "/" + scoreOfTeam.getDrawDuels());
            columns.add(String.valueOf(scoreOfTeam.getHits()));
            rows.add(columns);
        }
        final int[] widths = {20, 10, 10, 10};
        createTable(stringBuilder, rows, widths);
    }

    private void addCompetitorClassificationTable(StringBuilder stringBuilder) {
        stringBuilder.append(NEW_LINE + "<h4>").append(messageSource.getMessage("classification.competitors.title", null, locale))
                .append(HEADER_4_CLOSE_NEW_LINE);
        final List<List<String>> rows = new ArrayList<>();
        // Header
        List<String> columns = new ArrayList<>();
        columns.add("<b>" + messageSource.getMessage("classification.competitors.competitor.name", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.competitors.duels.won", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.competitors.hits", null, locale) + "</b>");
        rows.add(columns);

        for (final ScoreOfCompetitorDTO scoreOfCompetitor : scoreOfCompetitors) {
            columns = new ArrayList<>();
            columns.add(NameUtils.getLastnameName(scoreOfCompetitor.getCompetitor()));
            columns.add(scoreOfCompetitor.getWonDuels() + "/" + scoreOfCompetitor.getDrawDuels());
            columns.add(String.valueOf(scoreOfCompetitor.getHits()));
            rows.add(columns);
        }
        final int[] widths = {30, 15, 15};
        createTable(stringBuilder, rows, widths);
    }

    private void createTable(StringBuilder stringBuilder, List<List<String>> rows, int[] widths) {
        stringBuilder.append("<table>\n");
        stringBuilder.append("<tbody>\n");
        for (final List<String> row : rows) {
            stringBuilder.append("<tr>\n");
            int columnNumber = 0;
            for (final String column : row) {
                if (widths != null && columnNumber < widths.length) {
                    stringBuilder.append("<td style=\"width:").append(widths[columnNumber]).append("%\">\n");
                } else {
                    stringBuilder.append("<td>\n");
                }
                stringBuilder.append(column);
                stringBuilder.append("</td>\n");
                columnNumber++;
            }
            stringBuilder.append("</tr>\n");
        }
        stringBuilder.append("</tbody>\n");
        stringBuilder.append("</table>\n");
    }

    private String getDrawFight(DuelDTO duelDTO) {
        if (duelDTO.getWinner() != 0 || !duelDTO.isOver()) {
            return String.valueOf(Score.EMPTY.getPdfAbbreviation());
        }
        return "<div style=\"text-align: center;\">" + Score.DRAW.getPdfAbbreviation() + DIV_CLOSE;
    }

    private String getFaultsDiv(DuelDTO duelDTO, boolean leftTeam) {
        final boolean fault = getFaults(duelDTO, leftTeam);
        if (!fault) {
            return "";
        }
        return "<div style=\"width: 0;height: 0;border-left: 5px solid transparent;border-right: 5px solid transparent;border-bottom: 10px solid black;\">"
                + DIV_CLOSE;
    }

    private boolean getFaults(DuelDTO duelDTO, boolean leftTeam) {
        return leftTeam ? duelDTO.getCompetitor1Fault() : duelDTO.getCompetitor2Fault();
    }

    private String getScoreDiv(DuelDTO duelDTO, int score, boolean leftTeam) {
        final Score scoreText = getScore(duelDTO, score, leftTeam);
        if (scoreText == null || scoreText == Score.EMPTY) {
            return "";
        }
        final int scoreTime = getScoreTime(duelDTO, score, leftTeam);
        return "<div style=\"border-radius: 50%;border: 1px solid black; text-align: center;height=100%\""
                + (scoreTime > 0 ? "  title=\"" + scoreTime + "&quot;\">" : ">")
                + String.valueOf(scoreText.getPdfAbbreviation()).replace(" ", "&nbsp;")
                + DIV_CLOSE;
    }

    private int getScoreTime(DuelDTO duelDTO, int score, boolean leftTeam) {
        final Integer scoreTime = getListValue(getScoresTimeBySide(duelDTO, leftTeam), score);
        return scoreTime != null ? scoreTime : -1;
    }

    private Score getScore(DuelDTO duelDTO, int score, boolean leftTeam) {
        return getListValue(getScoresBySide(duelDTO, leftTeam), score);
    }

    private List<Integer> getScoresTimeBySide(DuelDTO duelDTO, boolean leftTeam) {
        return leftTeam ? duelDTO.getCompetitor1ScoreTime() : duelDTO.getCompetitor2ScoreTime();
    }

    private List<Score> getScoresBySide(DuelDTO duelDTO, boolean leftTeam) {
        return leftTeam ? duelDTO.getCompetitor1Score() : duelDTO.getCompetitor2Score();
    }

    private <T> T getListValue(List<T> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }
}
