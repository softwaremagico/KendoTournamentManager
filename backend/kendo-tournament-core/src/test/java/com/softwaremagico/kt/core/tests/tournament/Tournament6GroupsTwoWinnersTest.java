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

import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.entities.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;

@SpringBootTest
@Test(groups = {"tournament6GroupsTwoWinnersTest"})
public class Tournament6GroupsTwoWinnersTest extends TreeTournamentBasedTests {

    private static final int MEMBERS = 1;
    private static final int TEAMS = 12;
    private static final int GROUPS = 6;
    private static final int WINNERS = 2;

    @Autowired
    private GroupController groupController;

    @Autowired
    private FightController fightController;

    private TournamentDTO tournamentDTO;

    @Test
    public void checkTournamentWith6Groups() {
        tournamentDTO = createTournament(GROUPS, TEAMS, MEMBERS, WINNERS);
        Assert.assertEquals(groupController.get(tournamentDTO).size(), 21);
    }

    @Test(dependsOnMethods = {"checkTournamentWith6Groups"})
    public void pressWizardButton() {
        Assert.assertEquals(groupController.count(), 21);
        List<FightDTO> tournamentFights = fightController.createFights(tournamentDTO.getId(), TeamsOrder.NONE, 0, null, null);
        Assert.assertEquals(groupController.count(), 21);
        Assert.assertEquals(tournamentFights.size(), TEAMS / 2);
        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);
        for (final Group group : groups) {
            Assert.assertEquals(group.getFights().size(), 1);
        }
    }

    @Test(dependsOnMethods = {"pressWizardButton"})
    public void getGroupsAgain() {
        final List<GroupDTO> groupDTOS = groupController.get(tournamentDTO);
        groupDTOS.sort(Comparator.comparing(GroupDTO::getLevel).thenComparing(GroupDTO::getIndex));
        Assert.assertEquals(groupDTOS.size(), 21);
    }
}
