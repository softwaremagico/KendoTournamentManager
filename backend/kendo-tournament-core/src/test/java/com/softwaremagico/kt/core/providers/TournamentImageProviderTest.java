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
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import com.softwaremagico.kt.persistence.repositories.TournamentImageRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = {"tournamentExtraProperties"})
public class TournamentImageProviderTest {

    private static final byte[] MULTIPART_PAYLOAD = new byte[]{1, 2, 3, 4};
    private static final byte[] DIRECT_IMAGE_PAYLOAD = new byte[]{9, 8, 7};

    @Mock
    private TournamentImageRepository repository;
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private MultipartFile multipartFile;

    private TournamentImageProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new TournamentImageProvider(repository, tournamentRepository);
        when(repository.save(any(TournamentImage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        resetDefaultImages(provider);
    }

    @Test
    public void shouldReturnDefaultImageForAllSupportedTypes() {
        final Tournament tournament = tournament();

        for (TournamentImageType type : TournamentImageType.values()) {
            final TournamentImage image = provider.getDefaultImage(tournament, type);
            assertEquals(image.getTournament(), tournament);
            assertEquals(image.getImageType(), type);
            assertEquals(image.getImageCompression(), ImageCompression.PNG);
            assertNotNull(image.getData());
            assertTrue(image.getData().length > 0);
        }
    }

    @Test
    public void shouldReuseCachedDefaultImagesWhenAlreadyLoaded() {
        final Tournament tournament = tournament();
        final byte[] accreditation = new byte[]{1};
        final byte[] banner = new byte[]{2};
        final byte[] diploma = new byte[]{3};
        final byte[] photo = new byte[]{4};
        setField(provider, "defaultAccreditation", accreditation);
        setField(provider, "defaultBanner", banner);
        setField(provider, "defaultDiploma", diploma);
        setField(provider, "defaultPhoto", photo);

        assertTrue(Arrays.equals(provider.getDefaultImage(tournament, TournamentImageType.ACCREDITATION).getData(), accreditation));
        assertTrue(Arrays.equals(provider.getDefaultImage(tournament, TournamentImageType.BANNER).getData(), banner));
        assertTrue(Arrays.equals(provider.getDefaultImage(tournament, TournamentImageType.DIPLOMA).getData(), diploma));
        assertTrue(Arrays.equals(provider.getDefaultImage(tournament, TournamentImageType.PHOTO).getData(), photo));
    }

    @Test
    public void shouldReturnNullDataWhenDefaultResourceStreamIsMissing() {
        final TournamentImageProvider missingResourceProvider = new TournamentImageProvider(repository, tournamentRepository,
                resourcePath -> null);
        final Tournament tournament = tournament();
        resetDefaultImages(missingResourceProvider);

        for (TournamentImageType type : TournamentImageType.values()) {
            final TournamentImage image = missingResourceProvider.getDefaultImage(tournament, type);
            assertEquals(image.getImageType(), type);
            assertNull(image.getData());
        }
    }

    @Test
    public void shouldReturnNullDataWhenDefaultResourceLoadingThrowsException() {
        final TournamentImageProvider failingProvider = new TournamentImageProvider(repository, tournamentRepository,
                _ -> {
                    throw new NullPointerException("forced");
                });
        final Tournament tournament = tournament();
        resetDefaultImages(failingProvider);

        for (TournamentImageType type : TournamentImageType.values()) {
            final TournamentImage image = failingProvider.getDefaultImage(tournament, type);
            assertEquals(image.getImageType(), type);
            assertNull(image.getData());
        }
    }

    @Test
    public void shouldThrowExceptionWhenTypeIsNull() {
        org.testng.Assert.expectThrows(NullPointerException.class,
                () -> provider.getDefaultImage(tournament(), null));
    }

    @Test
    public void shouldDelegateGetDeleteAndGetAllToRepository() {
        final Tournament tournament = tournament();
        final TournamentImage image = new TournamentImage();
        image.setTournament(tournament);
        image.setImageType(TournamentImageType.BANNER);

        when(repository.findByTournamentAndImageType(tournament, TournamentImageType.BANNER)).thenReturn(Optional.of(image));
        when(repository.findByTournament(tournament)).thenReturn(List.of(image));
        when(repository.deleteByTournamentAndImageType(tournament, TournamentImageType.BANNER)).thenReturn(1);

        assertTrue(provider.get(tournament, TournamentImageType.BANNER).isPresent());
        assertEquals(provider.getAll(tournament).size(), 1);
        assertEquals(provider.delete(tournament, TournamentImageType.BANNER), 1);
    }

    @Test
    public void shouldAddMultipartImageAndPersistTournament() throws Exception {
        final Tournament tournament = tournament();
        when(multipartFile.getBytes()).thenReturn(MULTIPART_PAYLOAD);

        final TournamentImage image = provider.add(multipartFile, tournament, TournamentImageType.BANNER,
                ImageCompression.JPG, "uploader");

        assertEquals(image.getTournament(), tournament);
        assertEquals(image.getImageType(), TournamentImageType.BANNER);
        assertEquals(image.getImageCompression(), ImageCompression.JPG);
        assertTrue(Arrays.equals(image.getData(), MULTIPART_PAYLOAD));
        assertEquals(image.getCreatedBy(), "uploader");
        verify(repository).deleteByTournamentAndImageType(tournament, TournamentImageType.BANNER);
        verify(tournamentRepository).save(tournament);
        verify(repository).save(any(TournamentImage.class));
    }

    @Test
    public void shouldThrowDataInputExceptionWhenMultipartReadFails() throws Exception {
        final Tournament tournament = tournament();
        when(multipartFile.getBytes()).thenThrow(new IOException("disk error"));

        org.testng.Assert.expectThrows(DataInputException.class, () ->
                provider.add(multipartFile, tournament, TournamentImageType.PHOTO, ImageCompression.PNG, "uploader"));
    }

    @Test
    public void shouldReplaceExistingImageWhenAddingEntityDirectly() throws Exception {
        final Tournament tournament = tournament();
        final TournamentImage image = new TournamentImage();
        image.setTournament(tournament);
        image.setImageType(TournamentImageType.DIPLOMA);
        image.setImageCompression(ImageCompression.PNG);
        image.setData(DIRECT_IMAGE_PAYLOAD);

        final TournamentImage result = provider.add(image, "editor");

        assertEquals(result.getCreatedBy(), "editor");
        verify(repository).deleteByTournamentAndImageType(tournament, TournamentImageType.DIPLOMA);
        verify(tournamentRepository).save(tournament);
        verify(repository).save(image);
    }

    private Tournament tournament() {
        final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "tester");
        tournament.setId(202);
        return tournament;
    }

    private void resetDefaultImages(TournamentImageProvider imageProvider) {
        setField(imageProvider, "defaultAccreditation", null);
        setField(imageProvider, "defaultBanner", null);
        setField(imageProvider, "defaultDiploma", null);
        setField(imageProvider, "defaultPhoto", null);
    }

    private void setField(TournamentImageProvider imageProvider, String fieldName, byte[] value) {
        try {
            final Field field = TournamentImageProvider.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(imageProvider, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot update image field '" + fieldName + "'", e);
        }
    }
}


