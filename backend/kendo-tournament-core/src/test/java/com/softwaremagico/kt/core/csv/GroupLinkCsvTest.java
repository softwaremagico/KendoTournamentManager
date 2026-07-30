package com.softwaremagico.kt.core.csv;

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

import com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

@Test(groups = "groupLinkCsv")
public class GroupLinkCsvTest {

    private static final String HEADER = "source;winner;destination";

    @Mock
    private GroupProvider mockGroupProvider;

    @Mock
    private Tournament mockTournament;

    private GroupLinkCsv groupLinkCsv;

    private List<Group> sourceGroups;
    private List<Group> destinationGroups;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        groupLinkCsv = new GroupLinkCsv(mockGroupProvider);
        sourceGroups = List.of(new Group(), new Group());
        destinationGroups = List.of(new Group());
        when(mockGroupProvider.getGroups(mockTournament, 0)).thenReturn(sourceGroups);
        when(mockGroupProvider.getGroups(mockTournament, 1)).thenReturn(destinationGroups);
    }

    @Test
    public void readCSV_withoutTournament_expectEmptyList() {
        assertEquals(groupLinkCsv.readCSV("anything"), List.of());
    }

    @Test
    public void readCSV_withValidContent_expectGroupLinksParsed() {
        final String csv = HEADER + "\n0;0;0\n1;1;0";

        final List<GroupLink> result = groupLinkCsv.readCSV(mockTournament, csv);

        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getSource(), sourceGroups.get(0));
        assertEquals(result.get(0).getWinner().intValue(), 0);
        assertEquals(result.get(0).getDestination(), destinationGroups.get(0));
        assertEquals(result.get(1).getSource(), sourceGroups.get(1));
        assertEquals(result.get(1).getWinner().intValue(), 1);
    }

    @Test
    public void readCSV_withInvalidSource_expectException() {
        final String csv = HEADER + "\nnotANumber;0;0";

        expectThrows(InvalidCsvFieldException.class, () -> groupLinkCsv.readCSV(mockTournament, csv));
    }

    @Test
    public void readCSV_withOutOfBoundsSource_expectException() {
        final String csv = HEADER + "\n99;0;0";

        expectThrows(InvalidCsvFieldException.class, () -> groupLinkCsv.readCSV(mockTournament, csv));
    }

    @Test
    public void readCSV_withInvalidWinner_expectException() {
        final String csv = HEADER + "\n0;notANumber;0";

        expectThrows(InvalidCsvFieldException.class, () -> groupLinkCsv.readCSV(mockTournament, csv));
    }

    @Test
    public void readCSV_withTooManyWinners_expectException() {
        final String csv = HEADER + "\n0;2;0";

        expectThrows(InvalidCsvFieldException.class, () -> groupLinkCsv.readCSV(mockTournament, csv));
    }

    @Test
    public void readCSV_withInvalidDestination_expectException() {
        final String csv = HEADER + "\n0;0;notANumber";

        expectThrows(InvalidCsvFieldException.class, () -> groupLinkCsv.readCSV(mockTournament, csv));
    }

    @Test
    public void readCSV_withOutOfBoundsDestination_expectException() {
        final String csv = HEADER + "\n0;0;99";

        expectThrows(InvalidCsvFieldException.class, () -> groupLinkCsv.readCSV(mockTournament, csv));
    }

    @Test
    public void getSourceGroupSize_withValidContent_expectMaxIndexPlusOne() {
        final String csv = HEADER + "\n0;0;0\n2;1;0";

        assertEquals(groupLinkCsv.getSourceGroupSize(csv), 3);
    }

    @Test
    public void getSourceGroupSize_withInvalidContent_expectException() {
        final String csv = HEADER + "\nnotANumber;0;0";

        expectThrows(InvalidCsvFieldException.class, () -> groupLinkCsv.getSourceGroupSize(csv));
    }

    @Test
    public void getDestinationGroupSize_withValidContent_expectMaxIndexPlusOne() {
        final String csv = HEADER + "\n0;0;0\n0;1;3";

        assertEquals(groupLinkCsv.getDestinationGroupSize(csv), 4);
    }

    @Test
    public void getDestinationGroupSize_withInvalidContent_expectException() {
        final String csv = HEADER + "\n0;0;notANumber";

        expectThrows(InvalidCsvFieldException.class, () -> groupLinkCsv.getDestinationGroupSize(csv));
    }
}

