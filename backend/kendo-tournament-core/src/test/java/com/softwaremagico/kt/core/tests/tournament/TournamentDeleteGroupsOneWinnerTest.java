package com.softwaremagico.kt.core.tests.tournament;

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

import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Test(groups = {"deleteGroupsOneWinnerTest"})
public class TournamentDeleteGroupsOneWinnerTest extends AbstractTestNGSpringContextTests {

    private static final int MEMBERS = 1;
    private static final int GROUPS = 8;
    private static final String TOURNAMENT_NAME = "TournamentTest";
    private TournamentDTO tournamentDTO = null;

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private TreeTournamentHandler treeTournamentHandler;

    @Autowired
    private GroupController groupController;

    @Autowired
    private FightController fightController;

    @Autowired
    private DuelController duelController;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;


    @Test
    public void addTournament() {
        Assert.assertEquals(tournamentController.count(), 0);
        TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.TREE);
        tournamentDTO = tournamentController.create(newTournament, null, null);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentConverter.reverse(tournamentDTO), TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "true"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentConverter.reverse(tournamentDTO), TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "false"));
        Assert.assertEquals(tournamentController.count(), 1);
    }

    @Test(dependsOnMethods = "addTournament")
    public void add8Groups() {
        //The First group is already inserted.
        treeTournamentHandler.adjustGroupsSizeRemovingOddNumbers(tournamentConverter.reverse(tournamentDTO), 1);

        for (int i = 1; i < GROUPS; i++) {
            final GroupDTO groupDTO = new GroupDTO();
            groupDTO.setTournament(tournamentDTO);
            groupDTO.setIndex(i);
            groupDTO.setLevel(0);
            groupDTO.setShiaijo(0);
            groupDTO.setNumberOfWinners(2);
            groupController.create(groupDTO, null, null);
        }
        Assert.assertEquals(groupController.count(), 15);
    }

    @Test(dependsOnMethods = {"add8Groups"})
    public void deleteGroupsOneByOne() {
        final List<GroupDTO> level0Groups = groupController.get(tournamentDTO).stream().filter(g -> g.getLevel() == 0)
                .sorted(Comparator.comparing(GroupDTO::getIndex)).collect(Collectors.toList());
        groupController.delete(level0Groups.get(level0Groups.size() - 1), null, null);
        level0Groups.remove(level0Groups.size() - 1);
        Assert.assertEquals(groupController.count(), 14);

        groupController.delete(level0Groups.get(level0Groups.size() - 1), null, null);
        level0Groups.remove(level0Groups.size() - 1);
        Assert.assertEquals(groupController.count(), 13);

        groupController.delete(level0Groups.get(level0Groups.size() - 1), null, null);
        level0Groups.remove(level0Groups.size() - 1);
        Assert.assertEquals(groupController.count(), 12);

        //4 groups left
        groupController.delete(level0Groups.get(level0Groups.size() - 1), null, null);
        level0Groups.remove(level0Groups.size() - 1);
        Assert.assertEquals(groupController.count(), 7);

        groupController.delete(level0Groups.get(level0Groups.size() - 1), null, null);
        level0Groups.remove(level0Groups.size() - 1);
        Assert.assertEquals(groupController.count(), 6);

        //2 groups left
        groupController.delete(level0Groups.get(level0Groups.size() - 1), null, null);
        level0Groups.remove(level0Groups.size() - 1);
        Assert.assertEquals(groupController.count(), 3);

        groupController.delete(level0Groups.get(level0Groups.size() - 1), null, null);
        level0Groups.remove(level0Groups.size() - 1);
        Assert.assertEquals(groupController.count(), 1);

        groupController.delete(level0Groups.get(level0Groups.size() - 1), null, null);
        level0Groups.remove(level0Groups.size() - 1);
        Assert.assertEquals(groupController.count(), 0);
    }

    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        groupController.delete(tournamentDTO);
        fightController.delete(tournamentDTO);
        duelController.delete(tournamentDTO);
        teamController.delete(tournamentDTO);
        roleController.delete(tournamentDTO);
        tournamentController.delete(tournamentDTO, null, null);
        participantController.deleteAll();
        Assert.assertEquals(fightController.count(), 0);
        Assert.assertEquals(duelController.count(), 0);
    }
}
