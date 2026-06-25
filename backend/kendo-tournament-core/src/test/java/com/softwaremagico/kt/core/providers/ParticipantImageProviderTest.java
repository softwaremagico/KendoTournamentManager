package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.core.exceptions.DataInputException;
import com.softwaremagico.kt.core.images.ImageUtils;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.ParticipantImage;
import com.softwaremagico.kt.persistence.repositories.ParticipantImageRepository;
import com.softwaremagico.kt.persistence.repositories.ParticipantRepository;
import com.softwaremagico.kt.persistence.values.ImageFormat;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"participantImageProviderTests"})
public class ParticipantImageProviderTest {

    @Mock
    private ParticipantImageRepository participantImageRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MultipartFile file;

    private ParticipantImageProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new ParticipantImageProvider(participantImageRepository, participantRepository);
    }

    @Test
    public void testGetByParticipantDelegatesToRepository() {
        final Participant participant = participant("P1");
        final ParticipantImage image = participantImage(participant, new byte[]{1});
        when(participantImageRepository.findByParticipant(participant)).thenReturn(Optional.of(image));

        final Optional<ParticipantImage> result = provider.get(participant);

        assertThat(result).contains(image);
    }

    @Test
    public void testGetByParticipantsDelegatesToRepository() {
        final Participant participant = participant("P1");
        final ParticipantImage image = participantImage(participant, new byte[]{1});
        when(participantImageRepository.findByParticipantIn(List.of(participant))).thenReturn(List.of(image));

        final List<ParticipantImage> result = provider.getBy(List.of(participant));

        assertThat(result).containsExactly(image);
    }

    @Test
    public void testDeleteByParticipantClearsAvatarAndDeletesImage() {
        final Participant participant = participant("P1");
        participant.setHasAvatar(true);
        when(participantImageRepository.deleteByParticipant(participant)).thenReturn(1);

        final int deleted = provider.delete(participant);

        assertThat(deleted).isEqualTo(1);
        assertThat(participant.getHasAvatar()).isFalse();
        verify(participantRepository).save(participant);
        verify(participantImageRepository).deleteByParticipant(participant);
    }

    @Test
    public void testAddMultipartFileStoresProcessedImageAndSetsAvatar() throws Exception {
        final Participant participant = participant("P1");
        final byte[] uploaded = new byte[]{1, 2, 3};
        final byte[] processed = new byte[]{9, 8, 7};
        final BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);

        when(file.getBytes()).thenReturn(uploaded);
        when(participantImageRepository.save(any(ParticipantImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<ImageUtils> imageUtils = mockStatic(ImageUtils.class)) {
            imageUtils.when(() -> ImageUtils.getImage(uploaded)).thenReturn(image);
            imageUtils.when(() -> ImageUtils.resizeImage(image)).thenReturn(image);
            imageUtils.when(() -> ImageUtils.cropImage(image)).thenReturn(image);
            imageUtils.when(() -> ImageUtils.getBytes(image)).thenReturn(processed);

            final ParticipantImage result = provider.add(file, participant, "user");

            assertThat(result.getParticipant()).isEqualTo(participant);
            assertThat(result.getData()).containsExactly(processed);
            assertThat(result.getImageFormat()).isEqualTo(ImageFormat.BASE64);
            assertThat(result.getCreatedBy()).isEqualTo("user");
            assertThat(participant.getHasAvatar()).isTrue();
        }
    }

    @Test
    public void testAddMultipartFileThrowsDataInputExceptionOnIOException() throws Exception {
        final Participant participant = participant("P1");
        when(file.getBytes()).thenThrow(new IOException("invalid"));

        assertThatThrownBy(() -> provider.add(file, participant, "user"))
                .isInstanceOf(DataInputException.class)
                .hasMessageContaining("File creation failed");
    }

    @Test
    public void testAddParticipantImageWithDataSetsAvatarTrue() throws Exception {
        final Participant participant = participant("P1");
        final ParticipantImage participantImage = participantImage(participant, new byte[]{3, 2, 1});
        final byte[] processed = new byte[]{5, 5, 5};
        final BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);

        when(participantImageRepository.save(any(ParticipantImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<ImageUtils> imageUtils = mockStatic(ImageUtils.class)) {
            imageUtils.when(() -> ImageUtils.getImage(any(byte[].class))).thenReturn(image);
            imageUtils.when(() -> ImageUtils.resizeImage(image)).thenReturn(image);
            imageUtils.when(() -> ImageUtils.cropImage(image)).thenReturn(image);
            imageUtils.when(() -> ImageUtils.getBytes(image)).thenReturn(processed);

            final ParticipantImage result = provider.add(participantImage, "user");

            assertThat(result.getData()).containsExactly(processed);
            assertThat(result.getCreatedBy()).isEqualTo("user");
            assertThat(participant.getHasAvatar()).isTrue();
        }
    }

    @Test
    public void testAddParticipantImageWhenProcessingFailsKeepsNullDataAndAvatarFalse() {
        final Participant participant = participant("P1");
        final ParticipantImage participantImage = participantImage(participant, null);

        when(participantImageRepository.save(any(ParticipantImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<ImageUtils> imageUtils = mockStatic(ImageUtils.class)) {
            imageUtils.when(() -> ImageUtils.getImage((byte[]) null)).thenThrow(new IOException("bad image"));

            final ParticipantImage result = provider.add(participantImage, "user");

            assertThat(result.getData()).isNull();
            assertThat(participant.getHasAvatar()).isFalse();
            assertThat(result.getCreatedBy()).isEqualTo("user");
        }
    }

    private Participant participant(String name) {
        final Participant participant = new Participant();
        participant.setId(Math.floorMod(name.hashCode(), Integer.MAX_VALUE));
        participant.setName(name);
        participant.setLastname("Lastname");
        participant.setCreatedBy("user");
        return participant;
    }

    private ParticipantImage participantImage(Participant participant, byte[] data) {
        final ParticipantImage participantImage = new ParticipantImage();
        participantImage.setParticipant(participant);
        participantImage.setData(data);
        participantImage.setImageFormat(ImageFormat.RAW);
        participantImage.setCreatedBy("creator");
        return participantImage;
    }
}


