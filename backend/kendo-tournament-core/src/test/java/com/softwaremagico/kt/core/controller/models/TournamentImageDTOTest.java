package com.softwaremagico.kt.core.controller.models;

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

import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "tournamentImageDTO")
public class TournamentImageDTOTest {

    private TournamentImageDTO newDto(byte[] data, TournamentImageType type, ImageCompression compression, boolean defaultImage) {
        final TournamentImageDTO dto = new TournamentImageDTO();
        dto.setData(data);
        dto.setImageType(type);
        dto.setImageCompression(compression);
        dto.setDefaultImage(defaultImage);
        return dto;
    }

    @Test
    public void getBase64_withPngCompression_expectPngDataUri() {
        final TournamentImageDTO dto = newDto(new byte[]{1, 2, 3}, TournamentImageType.BANNER, ImageCompression.PNG, false);
        assertTrue(dto.getBase64().startsWith("data:image/png;base64,"));
    }

    @Test
    public void getBase64_withJpgCompression_expectJpegDataUri() {
        final TournamentImageDTO dto = newDto(new byte[]{1, 2, 3}, TournamentImageType.BANNER, ImageCompression.JPG, false);
        assertTrue(dto.getBase64().startsWith("data:image/jpeg;base64,"));
    }

    @Test
    public void getBase64_withNullData_expectNull() {
        final TournamentImageDTO dto = newDto(null, TournamentImageType.BANNER, ImageCompression.PNG, false);
        assertNull(dto.getBase64());
    }

    @Test
    public void equals_withMatchingFields_expectTrue() {
        final TournamentImageDTO dto1 = newDto(new byte[]{1, 2}, TournamentImageType.PHOTO, ImageCompression.PNG, true);
        final TournamentImageDTO dto2 = newDto(new byte[]{1, 2}, TournamentImageType.PHOTO, ImageCompression.PNG, true);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    public void equals_withDifferentImageType_expectFalse() {
        final TournamentImageDTO dto1 = newDto(new byte[]{1, 2}, TournamentImageType.PHOTO, ImageCompression.PNG, false);
        final TournamentImageDTO dto2 = newDto(new byte[]{1, 2}, TournamentImageType.BANNER, ImageCompression.PNG, false);

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void equals_withDifferentData_expectFalse() {
        final TournamentImageDTO dto1 = newDto(new byte[]{1, 2}, TournamentImageType.PHOTO, ImageCompression.PNG, false);
        final TournamentImageDTO dto2 = newDto(new byte[]{9, 9}, TournamentImageType.PHOTO, ImageCompression.PNG, false);

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void equals_withDifferentDefaultImageFlag_expectFalse() {
        final TournamentImageDTO dto1 = newDto(new byte[]{1, 2}, TournamentImageType.PHOTO, ImageCompression.PNG, true);
        final TournamentImageDTO dto2 = newDto(new byte[]{1, 2}, TournamentImageType.PHOTO, ImageCompression.PNG, false);

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void equals_withNonTournamentImageDTO_expectFalse() {
        final TournamentImageDTO dto = newDto(new byte[]{1, 2}, TournamentImageType.PHOTO, ImageCompression.PNG, false);
        assertFalse(dto.equals("not-a-dto"));
    }

    @Test
    public void equals_withItself_expectTrue() {
        final TournamentImageDTO dto = newDto(new byte[]{1, 2}, TournamentImageType.PHOTO, ImageCompression.PNG, false);
        assertEquals(dto, dto);
    }

    @Test
    public void tournamentAccessors_expectRoundTrip() {
        final TournamentImageDTO dto = new TournamentImageDTO();
        final TournamentDTO tournament = new TournamentDTO();
        dto.setTournament(tournament);

        assertEquals(dto.getTournament(), tournament);
    }
}

