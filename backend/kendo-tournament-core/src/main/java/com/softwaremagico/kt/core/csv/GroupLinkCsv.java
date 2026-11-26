package com.softwaremagico.kt.core.csv;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupLinkCsv extends CsvReader<GroupLink> {
    private static final String SOURCE_GROUP_INDEX = "source";
    private static final String WINNER = "winner";
    private static final String DESTINATION_GROUP_INDEX = "destination";
    private static final int MAX_ALLOWED_WINNERS = 2;
    private final GroupProvider groupProvider;

    public GroupLinkCsv(GroupProvider groupProvider) {
        this.groupProvider = groupProvider;
    }

    @Override
    public List<GroupLink> readCSV(String csvContent) {
        return List.of();
    }


    public List<GroupLink> readCSV(Tournament tournament, String csvContent) {
        final String[] headers = getHeaders(csvContent);
        checkHeaders(headers, SOURCE_GROUP_INDEX, WINNER, DESTINATION_GROUP_INDEX);
        final String[] content = getContent(csvContent);
        final List<GroupLink> groupLinks = new ArrayList<>();

        final int sourceGroupIndex = getHeaderIndex(headers, SOURCE_GROUP_INDEX);
        final int winnerIndex = getHeaderIndex(headers, WINNER);
        final int destinationGroupIndex = getHeaderIndex(headers, DESTINATION_GROUP_INDEX);

        final List<Group> sourceGroups = groupProvider.getGroups(tournament, 0);
        final List<Group> destinationGroups = groupProvider.getGroups(tournament, 1);

        for (String groupLinkLine : content) {
            final GroupLink groupLink = new GroupLink();

            try {
                groupLink.setSource(sourceGroups.get(Integer.parseInt(getField(groupLinkLine, sourceGroupIndex))));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                throw new InvalidCsvFieldException(this.getClass(), "Group source is incorrect on line '" + groupLinkLine + "'!", null);
            }

            try {
                groupLink.setWinner(Integer.valueOf(getField(groupLinkLine, winnerIndex)));
                if (groupLink.getWinner() >= MAX_ALLOWED_WINNERS) {
                    throw new InvalidCsvFieldException(this.getClass(), "Winner is incorrect on line '" + groupLinkLine
                            + "'! Only two winners are allowed", null);
                }
            } catch (NumberFormatException e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                throw new InvalidCsvFieldException(this.getClass(), "Winner is incorrect on line '" + groupLinkLine + "'!", null);
            }

            try {
                groupLink.setDestination(destinationGroups.get(Integer.parseInt(getField(groupLinkLine, destinationGroupIndex))));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                throw new InvalidCsvFieldException(this.getClass(), "Group destination is incorrect on line '" + groupLinkLine + "'!", null);
            }
            groupLinks.add(groupLink);
        }
        return groupLinks;
    }

    public int getSourceGroupSize(String csvContent) {
        final String[] content = getContent(csvContent);

        final String[] headers = getHeaders(csvContent);
        checkHeaders(headers, SOURCE_GROUP_INDEX, WINNER, DESTINATION_GROUP_INDEX);
        final int sourceGroupIndex = getHeaderIndex(headers, SOURCE_GROUP_INDEX);

        int maxGroupIndex = -1;
        for (String groupLinkLine : content) {
            try {
                final int groupIndex = Integer.parseInt(getField(groupLinkLine, sourceGroupIndex));
                if (groupIndex > maxGroupIndex) {
                    maxGroupIndex = groupIndex;
                }
            } catch (NumberFormatException e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                throw new InvalidCsvFieldException(this.getClass(), "Group source is incorrect on line '" + groupLinkLine + "'!", null);
            }
        }
        return maxGroupIndex + 1;
    }

    public int getDestinationGroupSize(String csvContent) {
        final String[] content = getContent(csvContent);

        final String[] headers = getHeaders(csvContent);
        checkHeaders(headers, SOURCE_GROUP_INDEX, WINNER, DESTINATION_GROUP_INDEX);
        final int destinationGroupIndex = getHeaderIndex(headers, DESTINATION_GROUP_INDEX);

        int maxGroupIndex = -1;
        for (String groupLinkLine : content) {
            try {
                final int groupIndex = Integer.parseInt(getField(groupLinkLine, destinationGroupIndex));
                if (groupIndex > maxGroupIndex) {
                    maxGroupIndex = groupIndex;
                }
            } catch (NumberFormatException e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
                throw new InvalidCsvFieldException(this.getClass(), "Group destination is incorrect on line '" + groupLinkLine + "'!", null);
            }
        }
        return maxGroupIndex + 1;
    }
}
