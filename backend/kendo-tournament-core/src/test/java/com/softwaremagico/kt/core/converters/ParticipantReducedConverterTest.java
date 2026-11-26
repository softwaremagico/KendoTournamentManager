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

import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantReducedDTO;
import com.softwaremagico.kt.core.converters.models.ParticipantConverterRequest;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.repositories.ClubRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

@Test(groups = "participantReducedConverter")
public class ParticipantReducedConverterTest {

    @Mock
    private ClubConverter mockClubConverter;

    @Mock
    private ClubRepository mockClubRepository;

    private ParticipantReducedConverter converter;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new ParticipantReducedConverter(mockClubConverter, mockClubRepository);
    }

    @Test
    public void convert_withoutClubOverride_expectClubConverterInvokedWithEntityClub() {
        final Club club = new Club();
        final Participant participant = new Participant();
        participant.setName("John");
        participant.setClub(club);
        final ClubDTO clubDTO = new ClubDTO();

        when(mockClubConverter.convert(any())).thenReturn(clubDTO);

        final ParticipantReducedDTO result = converter.convert(new ParticipantConverterRequest(participant));

        // ParticipantReducedDTO always ignores the club (see ParticipantReducedDTO#setClub), so it remains null,
        // but the entity's club is still delegated to the club converter.
        assertNull(result.getClub());
        assertEquals(result.getName(), "John");
        verify(mockClubConverter, times(1)).convert(any());
    }

    @Test
    public void convert_withClubOverride_expectClubConverterNotInvoked() {
        final Club club = new Club();
        final Participant participant = new Participant();
        participant.setClub(club);
        final Club overrideClub = new Club();

        final ParticipantReducedDTO result = converter.convert(new ParticipantConverterRequest(participant, overrideClub));

        assertNull(result.getClub());
        verify(mockClubConverter, never()).convert(any());
    }

    @Test
    public void reverse_withNull_expectNull() {
        assertNull(converter.reverse(null));
    }

    @Test
    public void reverse_expectClubReversedFromNullDto() {
        final ParticipantReducedDTO dto = new ParticipantReducedDTO();
        final Club club = new Club();

        when(mockClubConverter.reverse(null)).thenReturn(club);

        final Participant result = converter.reverse(dto);

        assertSame(result.getClub(), club);
    }
}



