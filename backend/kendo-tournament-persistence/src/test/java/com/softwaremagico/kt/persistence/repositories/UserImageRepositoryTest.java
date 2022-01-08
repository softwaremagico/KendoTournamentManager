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
