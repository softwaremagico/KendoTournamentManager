package com.softwaremagico.kt.persistence.repositories;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.ParticipantImage;
import com.softwaremagico.kt.persistence.factories.ClubFactory;
import com.softwaremagico.kt.persistence.factories.UserFactory;
import com.softwaremagico.kt.persistence.factories.UserImageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ParticipantImageRepositoryTests extends AbstractTestNGSpringContextTests {
    private static final String IMAGE_RESOURCE = "kendo.jpg";

    @Autowired
    private ParticipantImageRepository photoRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private ClubFactory clubFactory;

    @Autowired
    private UserImageFactory userImageFactory;

    private Participant participant;

    @BeforeClass
    public void createDefaultStructure() {
        Club club = clubFactory.createDefaultClub();
        club = clubRepository.save(club);
        participant = userFactory.createDefaultUser(club);
        participant = participantRepository.save(participant);
    }

    @Test
    public void addUserImage() throws Exception {
        ParticipantImage participantImage = userImageFactory.createUserImage(IMAGE_RESOURCE, participant);
        participantImage = photoRepository.save(participantImage);

        //Check content.
        final ParticipantImage storedImage = photoRepository.findById(participantImage.getId()).orElseThrow(() -> new Exception("Invalid image"));
        Assert.assertTrue(Arrays.equals(storedImage.getData(), Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource(IMAGE_RESOURCE).toURI()))));
        photoRepository.delete(participantImage);
    }

    @AfterClass(alwaysRun = true)
    public void clearData() {
        participantRepository.delete(participant);
    }

}
