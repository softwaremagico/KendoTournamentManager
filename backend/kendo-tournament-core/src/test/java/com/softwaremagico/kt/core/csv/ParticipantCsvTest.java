package com.softwaremagico.kt.core.csv;

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

import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessResourceFailureException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test(groups = "participantCsvTests")
public class ParticipantCsvTest {

    private static final String HEADER = "name;lastname;idCard;club;clubCity";

    @Mock
    private ClubProvider clubProvider;

    private ParticipantCsv participantCsv;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.participantCsv = new ParticipantCsv(this.clubProvider);
    }

    @Test
    public void shouldReadParticipantWithoutClub() {
        final String csv = HEADER + "\nJohn;Doe;ID-1;;";
        final List<Participant> participants = this.participantCsv.readCSV(csv);

        assertEquals(participants.size(), 1);
        assertEquals(participants.get(0).getName(), "John");
        assertEquals(participants.get(0).getLastname(), "Doe");
        assertEquals(participants.get(0).getIdCard(), "ID1");
        assertNull(participants.get(0).getClub());
    }

    @Test
    public void shouldResolveClubWhenNameAndCityPresent() {
        final Club club = new Club();
        club.setName("MyClub");
        when(this.clubProvider.findBy("MyClub", "MyCity")).thenReturn(Optional.of(club));

        final String csv = HEADER + "\nJohn;Doe;ID-1;MyClub;MyCity";
        final List<Participant> participants = this.participantCsv.readCSV(csv);

        assertEquals(participants.get(0).getClub(), club);
    }

    @Test
    public void shouldSetClubToNullWhenNotFound() {
        when(this.clubProvider.findBy("Unknown", "Nowhere")).thenReturn(Optional.empty());

        final String csv = HEADER + "\nJohn;Doe;ID-1;Unknown;Nowhere";
        final List<Participant> participants = this.participantCsv.readCSV(csv);

        assertNull(participants.get(0).getClub());
    }

    @Test
    public void shouldHandleDataAccessExceptionWhenResolvingClub() {
        when(this.clubProvider.findBy("MyClub", "MyCity"))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        final String csv = HEADER + "\nJohn;Doe;ID-1;MyClub;MyCity";
        final List<Participant> participants = this.participantCsv.readCSV(csv);

        assertEquals(participants.size(), 1);
        assertNull(participants.get(0).getClub());
    }

    @Test
    public void shouldReadMultipleParticipantLines() {
        final String csv = HEADER + "\nJohn;Doe;ID-1;;\nJane;Smith;ID-2;;";
        final List<Participant> participants = this.participantCsv.readCSV(csv);

        assertEquals(participants.size(), 2);
        assertEquals(participants.get(0).getName(), "John");
        assertEquals(participants.get(1).getName(), "Jane");
    }
}

