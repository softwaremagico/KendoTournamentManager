package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

@SpringBootTest
@Test(groups = {"fightRepository"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FightRepositoryTests extends BasicDataTest {

    @Autowired
    private FightRepository fightRepository;

    @BeforeClass
    public void prepareData() {
        populateData();
    }

    @Test
    private void getAllFightsFromParticipants() {
        Assert.assertEquals(fightRepository.findByParticipantIn(members).size(), fights.size());
        Assert.assertEquals(fightRepository.findByParticipantIn(Collections.singletonList(members.get(0))).size(), 2);
        Assert.assertEquals(fightRepository.findByParticipantIn(Collections.singletonList(members.get(1))).size(), 2);
        Assert.assertEquals(fightRepository.findByParticipantIn(Collections.singletonList(members.get(2))).size(), 2);
    }

    @Test
    private void getFightFromDuel() {
        Assert.assertEquals(fightRepository.findByDuels(fights.get(0).getDuels().get(0)).get(), fights.get(0));
        Assert.assertNotEquals(fightRepository.findByDuels(fights.get(1).getDuels().get(0)).get(), fights.get(0));
    }
}
