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

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = {"tournamentExtraProperties"})
public class TournamentImageProviderTest {

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
    public void shouldAddMultipartImageAndPersistTournament() throws Exception {
        final Tournament tournament = tournament();
        final byte[] payload = new byte[]{1, 2, 3, 4};
        when(multipartFile.getBytes()).thenReturn(payload);

        final TournamentImage image = provider.add(multipartFile, tournament, TournamentImageType.BANNER,
                ImageCompression.JPG, "uploader");

        assertEquals(image.getTournament(), tournament);
        assertEquals(image.getImageType(), TournamentImageType.BANNER);
        assertEquals(image.getImageCompression(), ImageCompression.JPG);
        assertTrue(Arrays.equals(image.getData(), payload));
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
        image.setData(new byte[]{9, 8, 7});

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
}


