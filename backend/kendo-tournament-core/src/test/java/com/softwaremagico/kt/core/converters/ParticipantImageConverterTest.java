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

import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantReducedDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantImageConverterRequest;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.ParticipantImage;
import com.softwaremagico.kt.persistence.values.ImageFormat;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

@Test(groups = "participantImageConverter")
public class ParticipantImageConverterTest {

    @Mock
    private ParticipantConverter mockParticipantConverter;

    @Mock
    private ParticipantReducedConverter mockParticipantReducedConverter;

    private ParticipantImageConverter converter;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new ParticipantImageConverter(mockParticipantConverter, mockParticipantReducedConverter);
    }

    @Test
    public void convert_expectImageFormatAndParticipantMapped() {
        final Participant participant = new Participant("ID1", "Name", "Lastname", null);
        final ParticipantImage entity = new ParticipantImage();
        entity.setData(new byte[]{1, 2, 3});
        entity.setImageFormat(ImageFormat.BASE64);
        entity.setParticipant(participant);

        final ParticipantReducedDTO reducedDTO = new ParticipantReducedDTO();
        when(mockParticipantReducedConverter.convertElement(any())).thenReturn(reducedDTO);

        final ParticipantImageDTO result = converter.convert(new ParticipantImageConverterRequest(entity));

        assertEquals(result.getImageFormat(), ImageFormat.BASE64);
        assertSame(result.getParticipant(), reducedDTO);
    }

    @Test
    public void reverse_withNull_expectNull() {
        assertNull(converter.reverse(null));
    }

    @Test
    public void reverse_expectImageFormatAndParticipantMapped() {
        final ParticipantImageDTO dto = new ParticipantImageDTO();
        dto.setImageFormat(ImageFormat.SVG);
        final ParticipantReducedDTO reducedDTO = new ParticipantReducedDTO();
        dto.setParticipant(reducedDTO);

        final Participant participant = new Participant("ID2", "Other", "Name", null);
        when(mockParticipantConverter.reverse(reducedDTO)).thenReturn(participant);

        final ParticipantImage result = converter.reverse(dto);

        assertEquals(result.getImageFormat(), ImageFormat.SVG);
        assertSame(result.getParticipant(), participant);
    }
}



