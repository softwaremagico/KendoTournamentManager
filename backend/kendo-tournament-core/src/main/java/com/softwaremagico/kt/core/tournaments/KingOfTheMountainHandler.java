package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.models.GroupConverterRequest;
import com.softwaremagico.kt.core.managers.KingOfTheMountainFightManager;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.*;
import com.softwaremagico.kt.persistence.entities.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class KingOfTheMountainHandler extends LeagueHandler {

    private final KingOfTheMountainFightManager kingOfTheMountainFightManager;
    private final FightProvider fightProvider;
    private final GroupProvider groupProvider;
    private final TeamProvider teamProvider;
    private final RankingController rankingController;
    private final GroupConverter groupConverter;
    private final TeamConverter teamConverter;

    private final TournamentProvider tournamentProvider;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    public KingOfTheMountainHandler(KingOfTheMountainFightManager kingOfTheMountainFightManager, FightProvider fightProvider,
                                    GroupProvider groupProvider, TeamProvider teamProvider, GroupConverter groupConverter,
                                    RankingController rankingController, TeamConverter teamConverter, TournamentProvider tournamentProvider,
                                    TournamentExtraPropertyProvider tournamentExtraPropertyProvider) {
        super(groupProvider, teamProvider, groupConverter, rankingController);
        this.kingOfTheMountainFightManager = kingOfTheMountainFightManager;
        this.fightProvider = fightProvider;
        this.groupProvider = groupProvider;
        this.teamProvider = teamProvider;
        this.rankingController = rankingController;
        this.groupConverter = groupConverter;
        this.teamConverter = teamConverter;
        this.tournamentProvider = tournamentProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, String createdBy) {
        return createFights(tournament, teamsOrder, (int) getNextLevel(tournament), createdBy);
    }

    private long getNextLevel(Tournament tournament) {
        //Each group on a different level, to ensure that the last group winner is the king of the mountain and the winner of the league.
        return groupProvider.count(tournament);
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        //Create fights from first group.
        final List<Fight> fights = fightProvider.saveAll(kingOfTheMountainFightManager.createFights(tournament,
                getGroup(tournament).getTeams().subList(0, 2), level, createdBy));
        final Group group = getGroup(tournament);
        group.setFights(fights);
        groupProvider.save(group);
        return fights;
    }

    @Override
    public List<Fight> createNextFights(Tournament tournament, String createdBy) {
        final Integer level = 0;
        //Generates next group.
        final Group group = addGroup(tournament, getGroupTeams(tournament), level, groupProvider.getGroupsByLevel(tournament, 0).size());
        final List<Fight> fights = fightProvider.saveAll(kingOfTheMountainFightManager.createFights(tournament, group.getTeams(),
                level, createdBy));
        group.setFights(fights);
        groupProvider.save(group);
        return fights;
    }

    private List<Team> getGroupTeams(Tournament tournament) {
        final List<Team> existingTeams = teamProvider.getAll(tournament);
        final List<Team> teams = new ArrayList<>();
        final List<Group> groups = groupProvider.getGroupsByLevel(tournament, 0);
        //Repository OrderByIndex not working well...
        groups.sort(Comparator.comparing(Group::getIndex));
        final Group lastGroup = groups.get(groups.size() - 1);
        final HashMap<Integer, List<TeamDTO>> ranking = rankingController.getTeamsByPosition(groupConverter.convert(new GroupConverterRequest(lastGroup)));
        //Previous winner with no draw
        if (ranking.get(0) != null && ranking.get(0).size() == 1) {
            final Team previousWinner = teamConverter.reverse(ranking.get(0).get(0));
            //Next team on the list. Looser is the other team on the previous group.
            teams.add(getNextTeam(existingTeams, Collections.singletonList(previousWinner), tournament));
            //Add winner on the same color
            teams.add(lastGroup.getTeams().indexOf(previousWinner), previousWinner);
        } else {
            final List<Team> previousWinners = teamConverter.reverseAll(ranking.get(0));
            //A draw!
            final Team firstTeam = getNextTeam(existingTeams, previousWinners, tournament);
            teams.add(firstTeam);
            //Avoid to select again the same team.
            previousWinners.add(firstTeam);
            teams.add(getNextTeam(existingTeams, previousWinners, tournament));
        }
        return teams;
    }

    private Team getNextTeam(List<Team> teams, List<Team> winners, Tournament tournament) {
        final AtomicInteger kingIndex = new AtomicInteger(0);
        TournamentExtraProperty extraProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.KING_INDEX);
        if (extraProperty == null) {
            extraProperty = tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                    TournamentExtraPropertyKey.KING_INDEX, "1"));
        }
        try {
            kingIndex.addAndGet(Integer.parseInt(extraProperty.getValue()));
        } catch (NumberFormatException | NullPointerException e) {
            kingIndex.set(1);
        }
        kingIndex.getAndIncrement();
        // Avoid to repeat a winner.
        for (final Team winner : winners) {
            if (teams.indexOf(winner) == kingIndex.get() % teams.size()) {
                kingIndex.getAndIncrement();
            }
        }

        // Get next team and save index.
        final Team nextTeam = teams.get(kingIndex.get() % teams.size());
        extraProperty.setValue(kingIndex.get() + "");
        tournamentExtraPropertyProvider.save(extraProperty);
        return nextTeam;
    }

    @Override
    public List<Group> getGroups(Tournament tournament) {
        return groupProvider.getGroups(tournament);
    }

    @Override
    public Group addGroup(Tournament tournament, Group group) {
        return groupProvider.addGroup(tournament, group);
    }
}
