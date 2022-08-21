package com.softwaremagico.kt.blog;

import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.utils.NameUtils;
import com.softwaremagico.kt.utils.ShiaijoName;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;


public class BlogExporter {
    private final static String NEW_LINE = "&nbsp;\n";

    private final MessageSource messageSource;
    private final Locale locale;

    private final TournamentDTO tournament;

    private final List<RoleDTO> roles;

    private final List<GroupDTO> groups;

    private final List<FightDTO> fights;

    private final List<ParticipantDTO> competitors;

    private final List<ScoreOfTeam> scoreOfTeams;

    private final List<ScoreOfCompetitor> scoreOfCompetitors;

    public BlogExporter(MessageSource messageSource, Locale locale, TournamentDTO tournament, List<RoleDTO> roles,
                        List<GroupDTO> groups, List<ParticipantDTO> competitors, List<ScoreOfTeam> scoreOfTeams,
                        List<ScoreOfCompetitor> scoreOfCompetitors) {
        this.messageSource = messageSource;
        this.locale = locale;
        this.tournament = tournament;
        this.roles = roles;
        this.groups = groups;
        this.fights = groups.stream().flatMap(groupDTO -> groupDTO.getFights().stream()).collect(Collectors.toList());
        this.competitors = competitors;
        this.scoreOfTeams = scoreOfTeams;
        this.scoreOfCompetitors = scoreOfCompetitors;
    }

    public String getWordpressFormat() {
        StringBuilder stringBuilder = new StringBuilder();
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
     * Header of the document
     *
     */
    private static void addTitle(StringBuilder stringBuilder, TournamentDTO tournament) {
        stringBuilder.append("<h2>").append(tournament.getName()).append("</h2>\n");
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
        List<List<String>> rows = new ArrayList<>();
        for (ParticipantDTO competitor : competitors) {
            List<String> columns = new ArrayList<>();

            RoleDTO competitorsRole = roles.stream().filter(roleDTO -> Objects.equals(roleDTO.getParticipant(), competitor)).findFirst().orElse(null);
            if (competitorsRole != null) {
                columns.add(NameUtils.getLastnameName(competitor));
                columns.add(messageSource.getMessage("role.type." + competitorsRole.getRoleType().getCode(), null, locale));
                rows.add(columns);
            }
        }
        int[] widths = {30, 15};
        createTable(stringBuilder, rows, widths);
    }

    /**
     * Create the tables with the results of the fights.
     */
    private void addScoreTables(StringBuilder stringBuilder, TournamentDTO tournament) {
        stringBuilder.append(NEW_LINE + "<h4>").append(messageSource.getMessage("fight.list", null, locale)).append("</h4>");

        int[] widths = {25, 5, 5, 5, 5, 5, 5, 5, 25};
        // Separate by groups
        for (int i = 0; i < groups.size(); i++) {
            if (groups.size() > 1) {
                stringBuilder.append("<h4>").append(messageSource.getMessage("tournament.group", null, locale))
                        .append(" ").append(i + 1).append(" (").append(messageSource.getMessage("tournament.shiaijo", null, locale))
                        .append(" ")
                        .append(ShiaijoName.getShiaijoName(groups.get(i).getShiaijo())).append(")").append("</h4>\n");
            }
            // For each fight
            for (FightDTO fight : fights) {
                List<List<String>> rows = new ArrayList<>();
                if (groups.get(i).getFights().contains(fight)) {
                    if (tournament.getTeamSize() > 1) {
                        stringBuilder.append(NEW_LINE + "<h5>").append(fight.getTeam1().getName()).append(" - ")
                                .append(fight.getTeam2().getName()).append("</h5>\n");
                    }
                    // Create for each competitor

                    for (int teamMember = 0; teamMember < tournament.getTeamSize(); teamMember++) {
                        List<String> columns = new ArrayList<>();
                        // Team 1
                        ParticipantDTO competitor = fight.getTeam1().getMembers().get(teamMember);
                        String name;
                        name = NameUtils.getLastnameName(competitor);
                        columns.add(name);
                        columns.add(getFaults(fight, teamMember, true));
                        columns.add(getScore(fight, teamMember, 1, true));
                        columns.add(getScore(fight, teamMember, 0, true));
                        columns.add(getDrawFight(fight, teamMember));
                        columns.add(getScore(fight, teamMember, 0, false));
                        columns.add(getScore(fight, teamMember, 1, false));
                        columns.add(getFaults(fight, teamMember, false));

                        // Team 2
                        competitor = fight.getTeam2().getMembers().get(teamMember);
                        if (competitor != null) {
                            name = NameUtils.getLastnameName(competitor);
                        } else {
                            name = "";
                        }
                        columns.add(name);
                        rows.add(columns);
                    }

                    createTable(stringBuilder, rows, widths);
                }
            }
        }
    }

    private void addTeamClassificationTable(StringBuilder stringBuilder) {
        stringBuilder.append(NEW_LINE + "<h4>").append(messageSource.getMessage("classification.score", null, locale)).append("</h4>\n");
        List<List<String>> rows = new ArrayList<>();
        // Header
        List<String> columns = new ArrayList<>();
        columns.add("<b>" + messageSource.getMessage("classification.teams.name", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.teams.fights.won", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.teams.duels.won", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.teams.hits", null, locale) + "</b>");
        rows.add(columns);

        for (ScoreOfTeam scoreOfTeam : scoreOfTeams) {
            columns = new ArrayList<>();
            columns.add(NameUtils.getShortName(scoreOfTeam.getTeam()));
            columns.add(scoreOfTeam.getWonFights() + "/" + scoreOfTeam.getDrawFights());
            columns.add(scoreOfTeam.getWonDuels() + "/" + scoreOfTeam.getDrawDuels());
            columns.add("" + scoreOfTeam.getHits());
            rows.add(columns);
        }
        int[] widths = {20, 10, 10, 10};
        createTable(stringBuilder, rows, widths);
    }

    private void addCompetitorClassificationTable(StringBuilder stringBuilder) {
        stringBuilder.append(NEW_LINE + "<h4>").append(messageSource.getMessage("classification.competitors.title", null, locale))
                .append("</h4>\n");
        List<List<String>> rows = new ArrayList<>();
        // Header
        List<String> columns = new ArrayList<>();
        columns.add("<b>" + messageSource.getMessage("classification.competitors.competitor.name", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.teams.duels.won", null, locale) + "</b>");
        columns.add("<b>" + messageSource.getMessage("classification.competitors.duels.won", null, locale) + "</b>");
        rows.add(columns);

        for (ScoreOfCompetitor scoreOfCompetitor : scoreOfCompetitors) {
            columns = new ArrayList<>();
            columns.add(NameUtils.getLastnameName(scoreOfCompetitor.getCompetitor()));
            columns.add(scoreOfCompetitor.getWonDuels() + "/" + scoreOfCompetitor.getDrawDuels());
            columns.add("" + scoreOfCompetitor.getHits());
            rows.add(columns);
        }
        int[] widths = {30, 15, 15};
        createTable(stringBuilder, rows, widths);
    }

    private void createTable(StringBuilder stringBuilder, List<List<String>> rows, int[] widths) {
        stringBuilder.append("<table>\n");
        stringBuilder.append("<tbody>\n");
        for (List<String> row : rows) {
            stringBuilder.append("<tr>\n");
            int columnNumber = 0;
            for (String column : row) {
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

    private String getDrawFight(FightDTO fightDTO, int duel) {
        // Draw Fights
        String draw;
        if (fightDTO.getDuels().get(duel).getWinner() == 0 && fightDTO.isOver()) {
            draw = "" + Score.DRAW.getAbbreviation();
        } else {
            draw = "" + Score.EMPTY.getAbbreviation();
        }
        return draw;
    }

    private String getFaults(FightDTO fightDTO, int duel, boolean leftTeam) {
        String faultSymbol;
        boolean faults;
        if (leftTeam) {
            faults = fightDTO.getDuels().get(duel).getCompetitor1Fault();
        } else {
            faults = fightDTO.getDuels().get(duel).getCompetitor2Fault();
        }
        if (faults) {
            faultSymbol = "" + Score.FAULT.getAbbreviation();
        } else {
            faultSymbol = "" + Score.EMPTY.getAbbreviation();
        }
        return faultSymbol;
    }

    private String getScore(FightDTO fightDTO, int duel, int score, boolean leftTeam) {
        if (leftTeam) {
            return fightDTO.getDuels().get(duel).getCompetitor1Score().get(score).getAbbreviation() + "";
        } else {
            return fightDTO.getDuels().get(duel).getCompetitor2Score().get(score).getAbbreviation() + "";
        }
    }
}
