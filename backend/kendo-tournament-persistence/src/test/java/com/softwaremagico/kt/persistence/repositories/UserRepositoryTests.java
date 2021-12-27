package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.User;
import com.softwaremagico.kt.utils.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest
@Test(groups = {"userRepository"})
public class UserRepositoryTests extends AbstractTestNGSpringContextTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFactory userFactory;

    @BeforeClass
    public void createDefaultStructure() {

    }

    @AfterClass
    public void clearData() {

    }

    @Test
    public void addUser() throws Exception {
        User user = userFactory.createDefaultUser();
        Assert.assertEquals(userRepository.count(), 0);
        user = userRepository.save(user);
        Assert.assertEquals(userRepository.count(), 1);
        userFactory.checkDefaultUser(userRepository.findById(user.getId()).orElseThrow(() -> new Exception("Invalid user")));
    }
}
