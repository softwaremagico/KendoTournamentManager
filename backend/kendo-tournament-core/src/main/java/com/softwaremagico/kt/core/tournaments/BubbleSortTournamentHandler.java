package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.core.exceptions.TournamentFinishedException;
import com.softwaremagico.kt.core.managers.BubbleSortTournamentManager;
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
public class BubbleSortTournamentHandler extends LeagueHandler {
    private final BubbleSortTournamentManager bubbleSortTournamentManager;
    private final GroupProvider groupProvider;
    private final FightProvider fightProvider;
    private final TeamProvider teamProvider;
    private final RankingProvider rankingProvider;
    private final TournamentExtraPropertyProvider tournamentExtraPropertyProvider;
    private final TournamentRepository tournamentRepository;

    public BubbleSortTournamentHandler(BubbleSortTournamentManager bubbleSortTournamentManager, GroupProvider groupProvider,
                                       FightProvider fightProvider, TeamProvider teamProvider, RankingProvider rankingProvider,
                                       TournamentExtraPropertyProvider tournamentExtraPropertyProvider, TournamentRepository tournamentRepository) {
        super(groupProvider, teamProvider, rankingProvider, tournamentExtraPropertyProvider);
        this.bubbleSortTournamentManager = bubbleSortTournamentManager;
        this.groupProvider = groupProvider;
        this.fightProvider = fightProvider;
        this.teamProvider = teamProvider;
        this.rankingProvider = rankingProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, Integer level, String createdBy) {
        //Create the first fight only.
        final List<Fight> fights = fightProvider.saveAll(bubbleSortTournamentManager.createFights(tournament,
                getFirstGroup(tournament).getTeams(), level, createdBy));
        final Group group = getFirstGroup(tournament);
        group.setFights(fights);
        groupProvider.save(group);
        //Reset the counter.
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.KING_INDEX, "1"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.BUBBLE_SORT_ITERATION, "0"));
        return fights;
    }

    @Override
    public List<Fight> generateNextFights(Tournament tournament, String createdBy) {
        List<Group> groups = groupProvider.getGroups(tournament);
        Group group = groups.get(groups.size() - 1);

        //It is finished if there are as many levels as teams - 1;
        if (groups.size() == teamProvider.getAll(tournament).size() - 1) {
            return new ArrayList<>();
        }

        //Check if the group is over. The Number of fights must be the number of teams -1 and one less by level.
        if (group.getFights().size() >= group.getTeams().size() - 1 - group.getLevel()) {
            //Create a new level. And add a new group to this level.
            createNextLevel(tournament);
            groups = groupProvider.getGroups(tournament);
            group = groups.get(groups.size() - 1);

            //Create the first fight from a group,
            final List<Fight> fights = fightProvider.saveAll(bubbleSortTournamentManager.createFights(tournament,
                    group.getTeams(), group.getLevel(), createdBy));
            group.setFights(fights);
            groupProvider.save(group);
            return fights;
        }


        final Fight lastFight = group.getFights().get(group.getFights().size() - 1);

        final Fight newFight = new Fight();
        newFight.setTournament(tournament);
        //Previous winner with no draw
        if (lastFight.getWinner() != null) {
            newFight.setTeam1(lastFight.getWinner());
            newFight.setTeam2(getNextTeam(group.getTeams(), Collections.singletonList(lastFight.getWinner()),
                    Collections.singletonList(lastFight.getLoser()), tournament, null));
        } else {
            final DrawResolution drawResolution = getDrawResolution(tournament);
            switch (drawResolution) {
                case BOTH_ELIMINATED -> {
                    newFight.setTeam1(getNextTeam(group.getTeams(), new ArrayList<>(),
                            Arrays.asList(lastFight.getTeam1(), lastFight.getTeam2()), tournament, drawResolution));
                    newFight.setTeam2(getNextTeam(group.getTeams(), new ArrayList<>(),
                            Arrays.asList(lastFight.getTeam1(), lastFight.getTeam2(), newFight.getTeam1()), tournament, drawResolution));
                }
                case OLDEST_ELIMINATED -> {
                    //Oldest is Team1 always.
                    newFight.setTeam1(lastFight.getTeam2());
                    newFight.setTeam2(getNextTeam(group.getTeams(), Collections.singletonList(lastFight.getTeam2()),
                            Collections.singletonList(lastFight.getTeam1()), tournament, drawResolution));
                }
                case NEWEST_ELIMINATED -> {
                    //Newest is Team2 always.
                    newFight.setTeam1(lastFight.getTeam1());
                    newFight.setTeam2(getNextTeam(group.getTeams(), Collections.singletonList(lastFight.getTeam1()),
                            Collections.singletonList(lastFight.getTeam2()), tournament, drawResolution));
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

    private Team getNextTeam(List<Team> teams, List<Team> winners, List<Team> losers, Tournament tournament, DrawResolution drawResolution) {
        final AtomicInteger kingIndex = new AtomicInteger(0);
        final TournamentExtraProperty extraProperty = getKingIndex(tournament);
        try {
            kingIndex.addAndGet(Integer.parseInt(extraProperty.getPropertyValue()));
        } catch (NumberFormatException | NullPointerException e) {
            kingIndex.set(1);
        }
        kingIndex.getAndIncrement();
        // Avoid repeating a winner.
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
    public List<Fight> createInitialFights(Tournament tournament, TeamsOrder teamsOrder, String createdBy) {
        return createFights(tournament, teamsOrder, getNextLevel(tournament), createdBy);
    }

    private int getNextLevel(Tournament tournament) {
        //Each group on a different level.
        return (int) groupProvider.count(tournament);
    }

    @Override
    public List<Group> getGroups(Tournament tournament) {
        return groupProvider.getGroups(tournament);
    }

    @Override
    public Group addGroup(Tournament tournament, Group group) {
        return groupProvider.addGroup(tournament, group);
    }

    @Override
    public void createNextLevel(Tournament tournament) throws TournamentFinishedException {
        final Group group = new Group();
        group.setTournament(tournament);
        group.setLevel(getNextLevel(tournament));
        group.setIndex(0);
        final DrawResolution drawResolution = getDrawResolution(tournament);
        final List<Group> existingGroups = groupProvider.getGroups(tournament);
        group.setTeams(getTeamsOrderedByRanks(tournament,
                !existingGroups.isEmpty() ? existingGroups.get(existingGroups.size() - 1) : null,
                drawResolution));
        addGroup(tournament, group);

        //Reset counter
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.KING_INDEX, "1"));
    }

    public List<Team> getTeamsOrderedByRanks(Tournament tournament) {
        final List<Group> groups = groupProvider.getGroups(tournament);
        if (groups.isEmpty()) {
            return new ArrayList<>();
        }
        return getTeamsOrderedByRanks(tournament, groups.get(groups.size() - 1), getDrawResolution(tournament));
    }


    public List<Team> getTeamsOrderedByRanks(Tournament tournament, Group group, DrawResolution drawResolution) {
        final List<Team> teams = new ArrayList<>();
        if (group != null) {
            //Add last teams that have no fights!
            for (int i = group.getLevel(); i > 0; i--) {
                teams.add(0, group.getTeams().get(group.getTeams().size() - (group.getLevel() - i) - 1));
            }
            if (group.getFights() != null && !group.getFights().isEmpty()) {
                //From the last fight we get both teams
                if (group.getFights().get(group.getFights().size() - 1).getWinner() != null) {
                    //Winner only if is not added before.
                    teams.add(0, group.getFights().get(group.getFights().size() - 1).getWinner());
                    teams.add(0, group.getFights().get(group.getFights().size() - 1).getLoser());
                } else {
                    switch (drawResolution) {
                        case NEWEST_ELIMINATED:
                            //Newest is Team2 always.
                            teams.add(0, group.getFights().get(group.getFights().size() - 1).getTeam1());
                            teams.add(0, group.getFights().get(group.getFights().size() - 1).getTeam2());
                            break;
                        case OLDEST_ELIMINATED:
                        case BOTH_ELIMINATED:
                            //Both cannot be on bubble sort!
                        default:
                            //Oldest is Team1 always.
                            teams.add(0, group.getFights().get(group.getFights().size() - 1).getTeam2());
                            teams.add(0, group.getFights().get(group.getFights().size() - 1).getTeam1());
                            break;
                    }

                }
                //For any other fight, we get the disqualified one.
                for (int i = group.getFights().size() - 2; i >= 0; i--) {
                    if (group.getFights().get(i).getWinner() != null) {
                        //Add the disqualified, as the winner has been already added on other fights.
                        teams.add(0, group.getFights().get(i).getLoser());
                    } else {
                        switch (drawResolution) {
                            case NEWEST_ELIMINATED:
                                //Newest is Team2 always.
                                teams.add(0, group.getFights().get(i).getTeam2());
                                break;
                            case OLDEST_ELIMINATED:
                            case BOTH_ELIMINATED:
                                //Both cannot be on bubble sort!
                            default:
                                //Oldest is Team1 always.
                                teams.add(0, group.getFights().get(i).getTeam1());
                                break;
                        }
                    }
                }
                return teams;
            }
        }
        if (group == null) {
            return teamProvider.getAll(tournament);
        }
        return group.getTeams();
    }

    protected Group addGroup(Tournament tournament, List<Team> teams, Integer level, Integer index) {
        final Group group = new Group();
        group.setTournament(tournament);
        group.setLevel(level);
        group.setIndex(index);
        group.setTeams(teams);
        return addGroup(tournament, group);
    }

    public TournamentExtraProperty getKingIndex(Tournament tournament) {
        TournamentExtraProperty extraProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.KING_INDEX);
        if (extraProperty == null) {
            extraProperty = tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                    TournamentExtraPropertyKey.KING_INDEX, "1"));
        } else {
            //It is 'lazy' the tournament.
            extraProperty.setTournament(tournamentRepository.findById(extraProperty.getTournament().getId()).orElse(null));
        }
        return extraProperty;
    }

    public DrawResolution getDrawResolution(Tournament tournament) {
        TournamentExtraProperty extraProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.KING_DRAW_RESOLUTION);
        if (extraProperty == null) {
            extraProperty = tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                    TournamentExtraPropertyKey.KING_DRAW_RESOLUTION, DrawResolution.OLDEST_ELIMINATED.name()));
        }

        return DrawResolution.getFromTag(extraProperty.getPropertyValue());
    }
}
