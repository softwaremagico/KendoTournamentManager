package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

    public void deleteCorrectlyFights() {
        Group group = groupRepository.findByFightsId(fights.get(0).getId());
        Assert.assertNotNull(group);
        fightRepository.deleteAll(fights);
    }

}
