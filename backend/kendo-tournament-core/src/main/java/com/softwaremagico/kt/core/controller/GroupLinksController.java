package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.GroupLinkDTO;
import com.softwaremagico.kt.core.converters.GroupLinkConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.AchievementConverterRequest;
import com.softwaremagico.kt.core.converters.models.GroupLinkConverterRequest;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Achievement;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
public class LinksController {

    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;
    private final GroupProvider groupProvider;
    private final GroupLinkConverter groupLinkConverter;

    public LinksController(TournamentConverter tournamentConverter, TournamentProvider tournamentProvider,
                           TournamentExtraPropertyProvider tournamentExtraPropertyProvider,
                           GroupProvider groupProvider, GroupLinkConverter groupLinkConverter) {
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
        this.groupProvider = groupProvider;
        this.groupLinkConverter = groupLinkConverter;
    }

    protected GroupLinkConverterRequest createConverterRequest(GroupLinkConverterRequest groupLink) {
        return new GroupLinkConverterRequest(groupLink);
    }

    public List<GroupLinkDTO> getLinks(Tournament tournament) {
        final TournamentExtraProperty numberOfWinners = tournamentExtraPropertyProvider.getByTournamentAndProperty(
                tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
        int tournamentWinners;
        try {
            tournamentWinners = Integer.parseInt(numberOfWinners.getPropertyValue());
        } catch (Exception e) {
            tournamentWinners = 1;
        }
        final List<Group> groups = groupProvider.getGroups(tournament);
        return groupLinkConverter.convertAll(getLinks(groups, tournamentWinners, groups.stream().max(Comparator.comparing(Group::getLevel)).orElse(new Group()).getLevel()))
                .stream().map(this::createConverterRequest).toList();
    }

    private List<GroupLink> getLinks(List<Group> groups, int tournamentWinners, int tournamentLevels) {
        final List<GroupLink> groupLinks = new ArrayList<>();
        groups.forEach(group -> {
            if (group.getLevel() < tournamentLevels) {
                for (int i = 0; i < getNumberOfTotalTeamsPassNextRound(group, tournamentWinners); i++) {
                    final GroupLink groupLink = new GroupLink();
                    groupLink.setSource(group);
                    final Group destination = getDestination(group, i, groups);
                    if (destination != null) {
                        groupLink.setDestination(destination);
                        groupLinks.add(groupLink);
                    }
                }
            }
        });
        return groupLinks;
    }

    private int getNumberOfTotalTeamsPassNextRound(Group group, int tournamentWinners) {
        if (group.getLevel() == 0) {
            return tournamentWinners;
        }
        return 1;
    }

    public Group getDestination(Group sourceGroup, int winnerOrder, List<Group> groups) {
        final List<Group> currentLevelGroups = groups.stream().filter(group -> Objects.equals(group.getLevel(), sourceGroup.getLevel())).toList();
        final List<Group> nextLevelGroups = groups.stream().filter(group -> Objects.equals(group.getLevel(), sourceGroup.getLevel() + 1)).toList();
        try {
            return nextLevelGroups.get(obtainPositionOfWinner(sourceGroup.getIndex(), currentLevelGroups.size(), winnerOrder));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private int obtainPositionOfWinner(Integer sourceGroupLevelIndex, Integer sourceGroupLevelSize, int winnerOrder) {
        if (winnerOrder == 0) {
            if (sourceGroupLevelIndex % 2 == 0) {
                return sourceGroupLevelIndex / 2;
            } else {
                return (sourceGroupLevelSize - sourceGroupLevelIndex) / 2;
            }
        } else if (winnerOrder == 1) {
            if (sourceGroupLevelIndex % 2 == 0) {
                return (sourceGroupLevelSize - sourceGroupLevelIndex) / 2;
            } else {
                return sourceGroupLevelIndex / 2;
            }
        } else {
            return -1;
        }
    }
}
