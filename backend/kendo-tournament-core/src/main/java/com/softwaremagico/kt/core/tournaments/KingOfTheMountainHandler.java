package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class KingOfTheMountainHandler extends LeagueHandler {

    private final KingOfTheMountainFightManager kingOfTheMountainFightManager;
    private final FightProvider fightProvider;
    private final GroupProvider groupProvider;
    private final TeamProvider teamProvider;
    private final RankingProvider rankingProvider;
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
        this.teamProvider = teamProvider;
        this.rankingProvider = rankingProvider;
        this.tournamentExtraPropertyProvider = tournamentExtraPropertyProvider;
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, String createdBy) {
        return createFights(tournament, teamsOrder, getNextLevel(tournament), createdBy);
    }

    private int getNextLevel(Tournament tournament) {
        //Each group on a different level, to ensure that the last group winner is the king of the mountain and the winner of the league.
        return (int) groupProvider.count(tournament);
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
    public List<Fight> generateNextFights(Tournament tournament, String createdBy) {
        //Generates next group.
        final int level = getNextLevel(tournament);
        final Group group = addGroup(tournament, getGroupTeams(tournament, level), level, 0);
        final List<Fight> fights = fightProvider.saveAll(kingOfTheMountainFightManager.createFights(tournament, group.getTeams(),
                level, createdBy));
        group.setFights(fights);
        groupProvider.save(group);
        return fights;
    }

    private List<Team> getGroupTeams(Tournament tournament, int level) {
        final List<Team> existingTeams = teamProvider.getAll(tournament);
        final List<Team> teams = new ArrayList<>();
        final List<Group> groups = groupProvider.getGroups(tournament, level - 1);
        //Repository OrderByIndex not working well...
        groups.sort(Comparator.comparing(Group::getLevel).thenComparing(Group::getIndex));
        final Group lastGroup = !groups.isEmpty() ? groups.get(groups.size() - 1) : null;
        final Map<Integer, List<Team>> ranking = rankingProvider.getTeamsByPosition(lastGroup);
        //Previous winner with no draw
        if (lastGroup != null && ranking.get(0) != null && ranking.get(0).size() == 1) {
            final Team previousWinner = ranking.get(0).get(0);
            final Team previousLooser = ranking.get(1).get(0);
            //Next team on the list. Looser is the other team on the previous group.
            teams.add(getNextTeam(existingTeams, Collections.singletonList(previousWinner), Collections.singletonList(previousLooser), tournament));
            //Add winner on the same color
            teams.add(lastGroup.getTeams().indexOf(previousWinner), previousWinner);
        } else {
            //Depending on the configuration.
            TournamentExtraProperty extraProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                    TournamentExtraPropertyKey.KING_DRAW_RESOLUTION);
            if (extraProperty == null) {
                extraProperty = tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                        TournamentExtraPropertyKey.KING_DRAW_RESOLUTION, DrawResolution.BOTH_ELIMINATED.name()));
            }

            final DrawResolution drawResolution = DrawResolution.getFromTag(extraProperty.getPropertyValue());
            final Group previousLastGroup = level > 1 ? groupProvider.getGroups(tournament, level - 2).get(0) : null;
            switch (drawResolution) {
                case BOTH_ELIMINATED -> bothEliminated(existingTeams, teams, ranking.get(0), tournament);
                case OLDEST_ELIMINATED -> {
                    if (previousLastGroup == null) {
                        bothEliminated(existingTeams, teams, ranking.get(0), tournament);
                    } else {
                        final List<Team> previousLastGroupTeams = previousLastGroup.getTeams();
                        if (lastGroup != null) {
                            previousLastGroupTeams.retainAll(lastGroup.getTeams());
                        }
                        oldestEliminated(existingTeams, teams, ranking.get(0), previousLastGroupTeams, tournament, lastGroup);
                    }
                }
                case NEWEST_ELIMINATED -> {
                    if (previousLastGroup == null) {
                        bothEliminated(existingTeams, teams, ranking.get(0), tournament);
                    } else {
                        final List<Team> previousLastGroupTeams = previousLastGroup.getTeams();
                        if (lastGroup != null) {
                            previousLastGroupTeams.retainAll(lastGroup.getTeams());
                        }
                        newestEliminated(existingTeams, teams, ranking.get(0), previousLastGroupTeams, tournament, lastGroup);
                    }
                }
                default -> {
                    // Ignore.
                }
            }
        }
        return teams;
    }

    private void bothEliminated(final List<Team> existingTeams, final List<Team> nextTeams, List<Team> previousWinners, Tournament tournament) {
        //A draw!
        final Team firstTeam = getNextTeam(existingTeams, previousWinners, new ArrayList<>(), tournament);
        nextTeams.add(firstTeam);
        //Avoid to select again the same team.
        previousWinners.add(firstTeam);
        nextTeams.add(getNextTeam(existingTeams, previousWinners, new ArrayList<>(), tournament));
    }

    private void oldestEliminated(final List<Team> existingTeams, final List<Team> nextTeams, List<Team> previousWinners, List<Team> previousLastGroupWinners,
                                  Tournament tournament, Group lastGroup) {
        // Add a new team to the fight.
        final Team firstTeam = getNextTeam(existingTeams, previousWinners, new ArrayList<>(), tournament);
        nextTeams.add(firstTeam);
        //Remove the winner that has been on the previous group.
        previousWinners.removeAll(previousLastGroupWinners);
        //Include the newest winner on the same position.
        if (lastGroup != null) {
            nextTeams.add(lastGroup.getTeams().indexOf(previousWinners.get(0)), previousWinners.get(0));
        }
    }

    private void newestEliminated(final List<Team> existingTeams, final List<Team> nextTeams, List<Team> previousWinners, List<Team> previousLastGroupWinners,
                                  Tournament tournament, Group lastGroup) {
        // Add a new team to the fight.
        final Team firstTeam = getNextTeam(existingTeams, previousWinners, new ArrayList<>(), tournament);
        nextTeams.add(firstTeam);
        //Remove the winner that has been on the previous group.
        previousWinners.retainAll(previousLastGroupWinners);
        //Include the newest winner on the same position.
        if (lastGroup != null) {
            nextTeams.add(lastGroup.getTeams().indexOf(previousWinners.get(0)), previousWinners.get(0));
        }
    }

    private Team getNextTeam(List<Team> teams, List<Team> winners, List<Team> loosers, Tournament tournament) {
        final AtomicInteger kingIndex = new AtomicInteger(0);
        TournamentExtraProperty extraProperty = tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
                TournamentExtraPropertyKey.KING_INDEX);
        if (extraProperty == null) {
            extraProperty = tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                    TournamentExtraPropertyKey.KING_INDEX, "1"));
        } else {
            //It is lazy the tournament.
            extraProperty.setTournament(tournamentRepository.findById(extraProperty.getTournament().getId()).orElse(null));
        }
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
        // Avoid to repeat a looser.
        for (final Team looser : loosers) {
            if (teams.indexOf(looser) == (kingIndex.get() % teams.size())) {
                kingIndex.getAndIncrement();
                //Avoid the new one is still the winner.
                if (forbiddenWinner != null && (kingIndex.get() % teams.size()) == (forbiddenWinner % teams.size())) {
                    kingIndex.getAndIncrement();
                }
            }
        }

        // Get next team and save index.
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
}
