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

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentImageDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.TournamentImageConverter;
import com.softwaremagico.kt.core.converters.models.TournamentImageConverterRequest;
import com.softwaremagico.kt.core.providers.TournamentImageProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Test(groups = "tournamentImageController")
public class TournamentImageControllerTest {

    private static final int TOURNAMENT_ID = 7;

    @Mock
    private TournamentImageProvider mockProvider;

    @Mock
    private TournamentImageConverter mockConverter;

    @Mock
    private TournamentConverter mockTournamentConverter;

    @Mock
    private TournamentProvider mockTournamentProvider;

    private TournamentImageController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TournamentImageController(mockProvider, mockConverter, mockTournamentConverter, mockTournamentProvider);
    }

    @Test
    public void deleteByTournamentId_withExistingTournament_expectDelegationToProvider() {
        final Tournament tournament = new Tournament();
        when(mockTournamentProvider.get(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(mockProvider.delete(tournament, TournamentImageType.BANNER)).thenReturn(1);

        final int result = controller.deleteByTournamentId(TOURNAMENT_ID, TournamentImageType.BANNER);

        assertEquals(result, 1);
    }

    @Test
    public void deleteByTournamentId_withMissingTournament_expectException() {
        when(mockTournamentProvider.get(TOURNAMENT_ID)).thenReturn(Optional.empty());

        expectThrows(RuntimeException.class, () -> controller.deleteByTournamentId(TOURNAMENT_ID, TournamentImageType.BANNER));
    }

    @Test
    public void get_byTournamentIdWithMissingTournament_expectException() {
        when(mockTournamentProvider.get(TOURNAMENT_ID)).thenReturn(Optional.empty());

        expectThrows(RuntimeException.class, () -> controller.get(TOURNAMENT_ID, TournamentImageType.BANNER));
    }

    @Test
    public void get_withExistingImage_expectDefaultImageFalse() {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final Tournament tournament = new Tournament();
        final TournamentImage tournamentImage = new TournamentImage();

        when(mockTournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
        when(mockProvider.get(tournament, TournamentImageType.BANNER)).thenReturn(Optional.of(tournamentImage));
        final TournamentImageDTO tournamentImageDTO = new TournamentImageDTO();
        when(mockConverter.convert(any())).thenReturn(tournamentImageDTO);

        final TournamentImageDTO result = controller.get(tournamentDTO, TournamentImageType.BANNER);

        assertFalse(result.isDefaultImage());
    }

    @Test
    public void get_withoutExistingImage_expectDefaultImageTrue() {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final Tournament tournament = new Tournament();
        final TournamentImage defaultImage = new TournamentImage();

        when(mockTournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
        when(mockProvider.get(tournament, TournamentImageType.BANNER)).thenReturn(Optional.empty());
        when(mockProvider.getDefaultImage(tournament, TournamentImageType.BANNER)).thenReturn(defaultImage);
        final TournamentImageDTO tournamentImageDTO = new TournamentImageDTO();
        when(mockConverter.convert(argThat((TournamentImageConverterRequest req) -> req != null && req.getEntityWithoutChecks() == null)))
                .thenReturn(null);
        when(mockConverter.convert(argThat((TournamentImageConverterRequest req) -> req != null && req.getEntityWithoutChecks() == defaultImage)))
                .thenReturn(tournamentImageDTO);

        final TournamentImageDTO result = controller.get(tournamentDTO, TournamentImageType.BANNER);

        assertTrue(result.isDefaultImage());
    }

    @Test
    public void add_withMultipartFileAndTournamentId_expectDelegationToProviderAndConverter() throws Exception {
        final Tournament tournament = new Tournament();
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        final TournamentImage tournamentImage = new TournamentImage();
        final TournamentImageDTO tournamentImageDTO = new TournamentImageDTO();

        when(mockTournamentProvider.get(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(mockTournamentConverter.convert(any())).thenReturn(tournamentDTO);
        when(mockTournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
        when(mockProvider.add(eq(file), eq(tournament), eq(TournamentImageType.BANNER), eq(ImageCompression.PNG), eq("user")))
                .thenReturn(tournamentImage);
        when(mockConverter.convert(any())).thenReturn(tournamentImageDTO);

        final TournamentImageDTO result = controller.add(file, TOURNAMENT_ID, TournamentImageType.BANNER, ImageCompression.PNG, "user");

        assertEquals(result, tournamentImageDTO);
        verify(mockProvider, times(1)).add(file, tournament, TournamentImageType.BANNER, ImageCompression.PNG, "user");
    }

    @Test
    public void add_withTournamentImageDto_expectDelegationToProviderAndConverter() throws Exception {
        final TournamentImageDTO tournamentImageDTO = new TournamentImageDTO();
        final TournamentImage tournamentImage = new TournamentImage();
        final TournamentImage savedImage = new TournamentImage();
        final TournamentImageDTO savedDTO = new TournamentImageDTO();

        when(mockConverter.reverse(tournamentImageDTO)).thenReturn(tournamentImage);
        when(mockProvider.add(tournamentImage, "user")).thenReturn(savedImage);
        when(mockConverter.convert(any())).thenReturn(savedDTO);

        final TournamentImageDTO result = controller.add(tournamentImageDTO, "user");

        assertEquals(result, savedDTO);
    }

    @Test
    public void delete_expectTournamentSavedAndImageDeleted() {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final Tournament tournament = new Tournament();

        when(mockTournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
        when(mockProvider.delete(tournament, TournamentImageType.BANNER)).thenReturn(1);

        final int result = controller.delete(tournamentDTO, TournamentImageType.BANNER);

        assertEquals(result, 1);
        verify(mockTournamentProvider, times(1)).save(tournament);
    }
}



