package com.softwaremagico.kt.persistence.repositories;

import com.softwaremagico.kt.persistence.entities.UserImage;
import com.softwaremagico.kt.persistence.entities.User;
import com.softwaremagico.kt.utils.UserImageFactory;
import com.softwaremagico.kt.utils.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@SpringBootTest
@Test(groups = {"imageRepository"})
public class UserImageRepositoryTest extends AbstractTestNGSpringContextTests {
    private final static String IMAGE_RESOURCE = "kendo.jpg";

    @Autowired
    private UserImageRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private UserImageFactory userImageFactory;

    private User user;

    @BeforeClass
    public void createDefaultStructure() {
        user = userFactory.createDefaultUser();
        user = userRepository.save(user);
    }

    @AfterClass
    public void clearData() {
        userRepository.delete(user);
    }

    @Test
    public void addUserImage() throws Exception {
        UserImage userImage = userImageFactory.createUserImage(IMAGE_RESOURCE, user);
        userImage = photoRepository.save(userImage);

        //Check content.
        final UserImage storedImage = photoRepository.findById(userImage.getId()).orElseThrow(() -> new Exception("Invalid image"));
        Assert.assertTrue(Arrays.equals(storedImage.getData(), Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource(IMAGE_RESOURCE).toURI()))));
        photoRepository.delete(userImage);
    }

}
