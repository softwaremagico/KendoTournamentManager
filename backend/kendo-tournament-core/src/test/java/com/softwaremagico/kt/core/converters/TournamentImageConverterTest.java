package com.softwaremagico.kt.core.converters;

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
import com.softwaremagico.kt.core.converters.models.TournamentImageConverterRequest;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

@Test(groups = "tournamentImageConverter")
public class TournamentImageConverterTest {

    @Mock
    private TournamentConverter mockTournamentConverter;

    private TournamentImageConverter converter;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new TournamentImageConverter(mockTournamentConverter);
    }

    @Test
    public void convert_expectImageTypeAndTournamentConverted() {
        final Tournament tournament = new Tournament();
        final TournamentImage tournamentImage = new TournamentImage();
        tournamentImage.setTournament(tournament);
        tournamentImage.setImageType(TournamentImageType.BANNER);

        final TournamentDTO tournamentDTO = new TournamentDTO();
        when(mockTournamentConverter.convertElement(any())).thenReturn(tournamentDTO);

        final TournamentImageDTO result = converter.convert(new TournamentImageConverterRequest(tournamentImage));

        assertEquals(result.getImageType(), TournamentImageType.BANNER);
        assertSame(result.getTournament(), tournamentDTO);
    }

    @Test
    public void reverse_withNull_expectNull() {
        assertNull(converter.reverse(null));
    }

    @Test
    public void reverse_expectImageTypeAndTournamentReversed() {
        final TournamentImageDTO dto = new TournamentImageDTO();
        final TournamentDTO tournamentDTO = new TournamentDTO();
        dto.setTournament(tournamentDTO);
        dto.setImageType(TournamentImageType.DIPLOMA);

        final Tournament tournament = new Tournament();
        when(mockTournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);

        final TournamentImage result = converter.reverse(dto);

        assertEquals(result.getImageType(), TournamentImageType.DIPLOMA);
        assertSame(result.getTournament(), tournament);
    }
}


