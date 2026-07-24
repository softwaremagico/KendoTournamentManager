package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.ParticipantImageConverter;
import com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException;
import com.softwaremagico.kt.core.providers.ParticipantImageProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.ParticipantImage;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

public class ParticipantImageControllerTest {

    @Mock
    private ParticipantImageProvider participantImageProvider;

    @Mock
    private ParticipantImageConverter participantImageConverter;

    @Mock
    private ParticipantConverter participantConverter;

    @Mock
    private ParticipantProvider participantProvider;

    @Mock
    private MultipartFile file;

    private ParticipantImageController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ParticipantImageController(participantImageProvider, participantImageConverter, participantConverter, participantProvider);
    }

    @Test(groups = "participantImageProviderTests")
    public void deleteByParticipantId_shouldSaveParticipantEvenWhenDeleteFails() {
        Participant participant = new Participant();
        participant.setId(7);
        participant.setHasAvatar(true);

        when(participantProvider.get(7)).thenReturn(Optional.of(participant));
        when(participantImageProvider.delete(participant)).thenThrow(new RuntimeException("db"));

        try {
            controller.deleteByParticipantId(7);
            fail("Expected RuntimeException");
        } catch (RuntimeException ignored) {
            verify(participantProvider).save(participant);
            assertEquals(participant.getHasAvatar(), Boolean.FALSE);
        }
    }

    @Test(groups = "participantImageProviderTests", expectedExceptions = ParticipantNotFoundException.class)
    public void deleteByParticipantId_shouldThrowWhenParticipantDoesNotExist() {
        when(participantProvider.get(9)).thenReturn(Optional.empty());

        controller.deleteByParticipantId(9);
    }

    @Test(groups = "participantImageProviderTests")
    public void getByParticipantId_shouldReturnConvertedImageWhenImageExists() {
        Participant participant = new Participant();
        ParticipantImage participantImage = new ParticipantImage();
        ParticipantImageDTO imageDTO = new ParticipantImageDTO();

        when(participantProvider.get(12)).thenReturn(Optional.of(participant));
        when(participantImageProvider.get(participant)).thenReturn(Optional.of(participantImage));
        when(participantImageConverter.convert(any())).thenReturn(imageDTO);

        ParticipantImageDTO result = controller.getByParticipantId(12);

        assertEquals(result, imageDTO);
    }

    @Test(groups = "participantImageProviderTests")
    public void getByParticipantId_shouldReturnNullWhenImageDoesNotExist() {
        Participant participant = new Participant();

        when(participantProvider.get(13)).thenReturn(Optional.of(participant));
        when(participantImageProvider.get(participant)).thenReturn(Optional.empty());
        when(participantImageConverter.convert(any())).thenReturn(null);

        ParticipantImageDTO result = controller.getByParticipantId(13);

        assertNull(result);
    }

    @Test(groups = "participantImageProviderTests")
    public void addWithParticipantId_shouldResolveParticipantAndDelegateToProvider() {
        Participant participant = new Participant();
        ParticipantDTO participantDTO = new ParticipantDTO();
        ParticipantImage participantImage = new ParticipantImage();
        ParticipantImageDTO participantImageDTO = new ParticipantImageDTO();

        when(participantProvider.get(21)).thenReturn(Optional.of(participant));
        when(participantConverter.convert(any())).thenReturn(participantDTO);
        when(participantConverter.reverse(participantDTO)).thenReturn(participant);
        when(participantImageProvider.add(file, participant, "tester")).thenReturn(participantImage);
        when(participantImageConverter.convert(any())).thenReturn(participantImageDTO);

        ParticipantImageDTO result = controller.add(file, 21, "tester");

        assertEquals(result, participantImageDTO);
        verify(participantImageProvider).add(file, participant, "tester");
    }

    @Test(groups = "participantImageProviderTests")
    public void delete_shouldDisableAvatarBeforeRemovingImage() {
        Participant participant = new Participant();
        participant.setHasAvatar(true);
        ParticipantDTO participantDTO = new ParticipantDTO();

        when(participantConverter.reverse(participantDTO)).thenReturn(participant);
        when(participantImageProvider.delete(participant)).thenReturn(1);

        int deleted = controller.delete(participantDTO);

        assertEquals(deleted, 1);
        assertEquals(participant.getHasAvatar(), Boolean.FALSE);
        verify(participantProvider).save(participant);
        verify(participantImageProvider).delete(participant);
        verify(participantProvider, never()).get(Mockito.anyInt());
    }
}

