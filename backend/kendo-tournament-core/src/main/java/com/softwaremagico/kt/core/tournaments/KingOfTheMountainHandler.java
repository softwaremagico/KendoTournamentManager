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
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.persistence.entities.*;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class KingOfTheMountainHandler extends LeagueHandler {

    private final KingOfTheMountainFightManager kingOfTheMountainFightManager;
    private final FightProvider fightProvider;
    private final GroupProvider groupProvider;
    private final TeamProvider teamProvider;
    private final RankingController rankingController;
    private final GroupConverter groupConverter;
    private final TeamConverter teamConverter;

    private final TournamentRepository tournamentRepository;

    public KingOfTheMountainHandler(KingOfTheMountainFightManager kingOfTheMountainFightManager, FightProvider fightProvider,
                                    GroupProvider groupProvider, TeamProvider teamProvider, GroupConverter groupConverter,
                                    RankingController rankingController, TeamConverter teamConverter,
                                    TournamentRepository tournamentRepository) {
        super(groupProvider, teamProvider, groupConverter, rankingController);
        this.kingOfTheMountainFightManager = kingOfTheMountainFightManager;
        this.fightProvider = fightProvider;
        this.groupProvider = groupProvider;
        this.teamProvider = teamProvider;
        this.rankingController = rankingController;
        this.groupConverter = groupConverter;
        this.teamConverter = teamConverter;
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, boolean maximizeFights, Integer level, String createdBy) {
        if (level != 0) {
            return null;
        }
        //Create fights from first group.
        final List<Fight> fights = fightProvider.saveAll(kingOfTheMountainFightManager.createFights(tournament,
                getGroup(tournament).getTeams().subList(0, 2), level, createdBy));
        final Group group = getGroup(tournament);
        group.setFights(fights);
        groupProvider.save(group);
        return fights;
    }

    public List<Fight> createNextFights(Tournament tournament, String createdBy) {
        final Integer level = 0;
        //Gets the two teams of the group.
        final List<Team> teams = new ArrayList<>();
        // Winner team must maintain the color.
        //Generates next group.
        final Group group = addGroup(tournament, getGroupTeams(tournament), level);
        final List<Fight> fights = fightProvider.saveAll(kingOfTheMountainFightManager.createFights(tournament, teams,
                level, createdBy));
        group.setFights(fights);
        groupProvider.save(group);
        return fights;
    }

    private List<Team> getGroupTeams(Tournament tournament) {
        final List<Team> existingTeams = teamProvider.getAll(tournament);
        final List<Team> teams = new ArrayList<>();
        List<Group> groups = groupProvider.getGroupsByLevel(tournament, 0);
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
            //A draw!
            teams.add(getNextTeam(existingTeams, teamConverter.reverseAll(ranking.get(0)), tournament));
            teams.add(getNextTeam(existingTeams, teamConverter.reverseAll(ranking.get(0)), tournament));
        }
        return teams;
    }

    private Team getNextTeam(List<Team> teams, List<Team> winners, Tournament tournament) {
        int kingIndex;
        try {
            kingIndex = Integer.parseInt(tournament.getExtraProperties().get(TournamentProperty.KING_INDEX));
        } catch (NumberFormatException e) {
            kingIndex = 2;
        }
        kingIndex++;
        // Avoid to repeat a winner.
        for (Team winner : winners) {
            if (teams.indexOf(winner) == kingIndex % teams.size()) {
                kingIndex++;
            }
        }

        // Get next team and save index.
        final Team nextTeam = teams.get(kingIndex % teams.size());
        tournament.addExtraProperties(TournamentProperty.KING_INDEX, "" + ++kingIndex);
        tournamentRepository.save(tournament);
        return nextTeam;
    }
}
