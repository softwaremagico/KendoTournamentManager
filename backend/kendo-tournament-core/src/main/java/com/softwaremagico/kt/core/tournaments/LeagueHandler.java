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

import com.softwaremagico.kt.core.controller.RankingController;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.models.GroupConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentFinishedException;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public abstract class LeagueHandler implements ITournamentManager {

    private final GroupProvider groupProvider;
    private final TeamProvider teamProvider;
    private final GroupConverter groupConverter;
    private final RankingController rankingController;


    @Autowired
    public LeagueHandler(GroupProvider groupProvider, TeamProvider teamProvider, GroupConverter groupConverter, RankingController rankingController) {
        this.groupProvider = groupProvider;
        this.teamProvider = teamProvider;
        this.groupConverter = groupConverter;
        this.rankingController = rankingController;
    }

    protected Group getGroup(Tournament tournament) {
        final List<Group> groups = groupProvider.getGroups(tournament);
        if (groups.isEmpty()) {
            final Group group = new Group();
            group.setTournament(tournament);
            group.setLevel(0);
            group.setIndex(0);
            group.setTeams(teamProvider.getAll(tournament));
            return addGroup(tournament, group);
        }
        return groups.get(0);
    }

    protected Group addGroup(Tournament tournament, Integer level) {
        return addGroup(tournament, teamProvider.getAll(tournament), level);
    }

    protected Group addGroup(Tournament tournament, List<Team> teams, Integer level) {
        return addGroup(tournament, teams, level, 0);
    }

    protected Group addGroup(Tournament tournament, List<Team> teams, Integer level, Integer index) {
        final Group group = new Group();
        group.setTournament(tournament);
        group.setLevel(level);
        group.setIndex(index);
        group.setTeams(teams);
        return addGroup(tournament, group);
    }

    @Override
    public List<Group> getGroups(Tournament tournament, Integer level) {
        if (level == 0) {
            return getGroups(tournament);
        }
        return null;
    }


    @Override
    public List<Group> getGroups(Tournament tournament) {
        final List<Group> groups = new ArrayList<>();
        groups.add(getGroup(tournament));
        return groups;
    }

    @Override
    public Group addGroup(Tournament tournament, Group group) {
        if (!groupProvider.getGroups(tournament).isEmpty()) {
            groupProvider.delete(tournament);
        }
        return groupProvider.addGroup(tournament, group);
    }

    @Override
    public void removeGroup(Tournament tournament, Integer level, Integer groupIndex) {
        if (level == 0 && groupIndex == 0) {
            groupProvider.delete(tournament);
        }
    }

    @Override
    public boolean exist(Tournament tournament, Team team) {
        final List<Group> groups = groupProvider.getGroups(tournament);
        if (!groups.isEmpty()) {
            return groups.get(0).getTeams().contains(team);
        }
        return false;
    }

    @Override
    public void removeTeams(Tournament tournament, Integer level) {
        final List<Group> groups = groupProvider.getGroups(tournament);
        if (!groups.isEmpty()) {
            groups.get(0).getTeams().clear();
            groupProvider.save(groups.get(0));
        }
    }

    @Override
    public void setDefaultFightAreas(Tournament tournament) {
        final Group group = getGroup(tournament);
        group.setShiaijo(0);
        groupProvider.save(group);
    }

    @Override
    public Group getGroup(Tournament tournament, Fight fight) {
        if (getGroup(tournament).isFightOfGroup(fight)) {
            return getGroup(tournament);
        }
        return null;
    }

    @Override
    public void setHowManyTeamsOfGroupPassToTheTree(Integer winners) {
        // Do nothing.
    }

    @Override
    public int getIndex(Integer level, Group group) {
        return 0;
    }

    @Override
    public boolean isTheLastFight(Tournament tournament) {
        final List<Fight> fights = getGroup(tournament).getFights();
        return (fights.size() > 0) && (fights.size() == 1 || fights.get(fights.size() - 2).isOver());
    }

    @Override
    public void removeFights(Tournament tournament) {
        final Group group = getGroup(tournament);
        group.removeFights();
        groupProvider.save(group);
    }

    @Override
    public void removeTeams(Tournament tournament) {
        final Group group = getGroup(tournament);
        group.removeTeams();
        groupProvider.save(group);
    }

    @Override
    public int getIndexOfGroup(Group group) {
        return getIndex(0, group);
    }

    @Override
    public List<Group> getGroupsByShiaijo(Tournament tournament, Integer shiaijo) {
        return groupProvider.getGroupsByShiaijo(tournament, shiaijo);
    }

    @Override
    public void createNextLevel() throws TournamentFinishedException {
        // Only one level is needed.
    }

    @Override
    public boolean hasDrawScore(Group group) {
        final List<TeamDTO> teamsInDraw = rankingController.getFirstTeamsWithDrawScore(
                groupConverter.convert(new GroupConverterRequest(group)), group.getNumberOfWinners());
        return (teamsInDraw != null);
    }

    @Override
    public List<Fight> createFights(Tournament tournament, TeamsOrder teamsOrder, String createdBy) {
        return createFights(tournament, teamsOrder, 0, createdBy);
    }

    @Override
    public List<Fight> generateNextFights(Tournament tournament, String createdBy) {
        return new ArrayList<>();
    }
}
