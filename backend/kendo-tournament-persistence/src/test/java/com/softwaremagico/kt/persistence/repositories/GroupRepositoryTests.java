package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@Test(groups = {"groupRepository"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GroupRepositoryTests extends BasicDataTest {
    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final Integer MEMBERS = 1;
    private static final Integer TEAMS = 3;
    private static final String TOURNAMENT_NAME = "groupTournamentTest";

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private FightRepository fightRepository;

    @BeforeClass
    public void prepareData() {
        populateData();
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public void checkFightsAreNotDeleted() {
        fightRepository.deleteAll(fights);
    }

    @Test
    public void findGroupByFights() {
        Group groupOfFight = groupRepository.findByFightsId(fights.get(0).getId());
        Assert.assertNotNull(groupOfFight);
        Assert.assertEquals(groupOfFight, group);
    }
    @Test(dependsOnMethods = "findGroupByFights")
    public void deleteCorrectlyFights() {
        Group group = groupRepository.findByFightsId(fights.get(0).getId());
        Assert.assertNotNull(group);
        List<Group> groups = groupRepository.findDistinctByFightsIdIn(fights.stream()
                .map(Fight::getId)
                .collect(Collectors.toList()));
        Assert.assertEquals(groups.size(), 1);
        group.getFights().removeAll(fights);
        groupRepository.save(group);
        fightRepository.deleteAll(fights);
    }

}
