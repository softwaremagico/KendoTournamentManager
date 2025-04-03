package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.managers.KingOfTheMountainFightManager;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class KingOfTheMountainHandler extends LeagueHandler {

    private final KingOfTheMountainFightManager kingOfTheMountainFightManager;
    private final FightProvider fightProvider;
    private final GroupProvider groupProvider;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;
    private final TournamentRepository tournamentRepository;

    public KingOfTheMountainHandler(KingOfTheMountainFightManager kingOfTheMountainFightManager, FightProvider fightProvider,
                                    GroupProvider groupProvider, TeamProvider teamProvider,
                                    RankingProvider rankingProvider,
                                    TournamentExtraPropertyProvider tournamentExtraPropertyProvider, TournamentRepository tournamentRepository) {
        super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
        this.kingOfTheMountainFightManager = kingOfTheMountainFightManager;
        this.fightProvider = fightProvider;
        this.groupProvider = groupProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public List<Fight> createInitialFights(Tournament tournament, TeamsOrder teamsOrder, String createdBy) {
        return createFights(tournament, teamsOrder, 0, createdBy);
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        //Create the first fight.
        final List<Fight> fights = fightProvider.saveAll(kingOfTheMountainFightManager.createFights(tournament,
                getFirstGroup(tournament).getTeams(), level, createdBy));
        final Group group = getFirstGroup(tournament);
        group.setFights(fights);
        groupProvider.save(group);
        //Reset the counter.
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.KING_INDEX, "1"));
        return fights;
    }

    @Override
    public List<Fight> generateNextFights(Tournament tournament, String createdBy) {
        final Group group = groupProvider.getGroups(tournament).get(0);
        final Fight lastFight = group.getFights().get(group.getFights().size() - 1);

        final Fight newFight = new Fight();
        newFight.setTournament(tournament);
        //Previous winner with no draw
        if (lastFight.getWinner() != null) {
            newFight.setTeam1(lastFight.getWinner());
            newFight.setTeam2(getNextTeam(group.getTeams(), Collections.singletonList(lastFight.getWinner()),
                    Collections.singletonList(lastFight.getLoser()), tournament));
        } else {
            final DrawResolution drawResolution = getDrawResolution(tournament);
            switch (drawResolution) {
                case BOTH_ELIMINATED -> {
                    newFight.setTeam1(getNextTeam(group.getTeams(), new ArrayList<>(),
                            Arrays.asList(lastFight.getTeam1(), lastFight.getTeam2()), tournament));
                    newFight.setTeam2(getNextTeam(group.getTeams(), new ArrayList<>(),
                            Arrays.asList(lastFight.getTeam1(), lastFight.getTeam2(), newFight.getTeam1()), tournament));
                }
                case OLDEST_ELIMINATED -> {
                    //Oldest is Team1 always.
                    newFight.setTeam1(lastFight.getTeam2());
                    newFight.setTeam2(getNextTeam(group.getTeams(), Collections.singletonList(lastFight.getTeam2()),
                            Collections.singletonList(lastFight.getTeam1()), tournament));
                }
                case NEWEST_ELIMINATED -> {
                    //Newest is Team2 always.
                    newFight.setTeam1(lastFight.getTeam1());
                    newFight.setTeam2(getNextTeam(group.getTeams(), Collections.singletonList(lastFight.getTeam1()),
                            Collections.singletonList(lastFight.getTeam2()), tournament));
                }
                default -> {
                    // Ignore.
                }
            }
        }
        newFight.generateDuels(createdBy);
        //Fight is saved in a group by cascade.
        group.getFights().add(newFight);
        groupProvider.save(group);
        return Collections.singletonList(newFight);
    }


    private Team getNextTeam(List<Team> teams, List<Team> winners, List<Team> losers, Tournament tournament) {
        final AtomicInteger kingIndex = new AtomicInteger(0);
        final TournamentExtraProperty extraProperty = getKingIndex(tournament);
        try {
            kingIndex.addAndGet(Integer.parseInt(extraProperty.getPropertyValue()));
        } catch (NumberFormatException | NullPointerException e) {
            kingIndex.set(1);
        }
        kingIndex.getAndIncrement();
        // Avoid to repeat a winner.
        Integer forbiddenWinner = null;
        for (final Team winner : winners) {
            if (teams.indexOf(winner) == (kingIndex.get() % teams.size())) {
                forbiddenWinner = kingIndex.getAndIncrement();
            }
        }
        // Avoid repeating a loser.
        for (final Team loser : losers) {
            if (teams.indexOf(loser) == (kingIndex.get() % teams.size())) {
                kingIndex.getAndIncrement();
                //Avoid the new one is still the winner.
                if (forbiddenWinner != null && (kingIndex.get() % teams.size()) == (forbiddenWinner % teams.size())) {
                    kingIndex.getAndIncrement();
                }
            }
        }

        // Get the next team and save index.
        final Team nextTeam = teams.get(kingIndex.get() % teams.size());
        extraProperty.setPropertyValue(String.valueOf(kingIndex.get()));
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

    public TournamentExtraProperty getKingIndex(Tournament tournament) {
        final TournamentExtraProperty extraProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.KING_INDEX, "1");
        //It is lazy the tournament.
        extraProperty.setTournament(tournamentRepository.findById(extraProperty.getTournament().getId()).orElse(null));
        return extraProperty;
    }


    public DrawResolution getDrawResolution(Tournament tournament) {
        final TournamentExtraProperty extraProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.KING_DRAW_RESOLUTION, DrawResolution.BOTH_ELIMINATED.name());
        return DrawResolution.getFromTag(extraProperty.getPropertyValue());
    }
}
