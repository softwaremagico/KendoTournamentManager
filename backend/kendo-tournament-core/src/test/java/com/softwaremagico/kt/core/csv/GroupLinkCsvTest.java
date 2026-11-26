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
import org.testng.annotations.DataProvider;
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
		this.groupLinkCsv = new GroupLinkCsv(this.mockGroupProvider);
		this.sourceGroups = List.of(new Group(), new Group());
		this.destinationGroups = List.of(new Group());
		when(this.mockGroupProvider.getGroups(this.mockTournament, 0)).thenReturn(this.sourceGroups);
		when(this.mockGroupProvider.getGroups(this.mockTournament, 1)).thenReturn(this.destinationGroups);
	}

	@Test
	public void readCSV_withoutTournament_expectEmptyList() {
		assertEquals(this.groupLinkCsv.readCSV("anything"), List.of());
	}

	@Test
	public void readCSV_withValidContent_expectGroupLinksParsed() {
		final String csv = HEADER + "\n0;0;0\n1;1;0";

		final List<GroupLink> result = this.groupLinkCsv.readCSV(this.mockTournament, csv);

		assertEquals(result.size(), 2);
		assertEquals(result.get(0).getSource(), this.sourceGroups.get(0));
		assertEquals(result.get(0).getWinner().intValue(), 0);
		assertEquals(result.get(0).getDestination(), this.destinationGroups.get(0));
		assertEquals(result.get(1).getSource(), this.sourceGroups.get(1));
		assertEquals(result.get(1).getWinner().intValue(), 1);
	}

	@Test
	public void readCSV_withInvalidContent_expectException() {
		final String csv = HEADER + "\nnotANumber;0;0";

		expectThrows(InvalidCsvFieldException.class, () -> this.groupLinkCsv.readCSV(this.mockTournament, csv));
	}

	@DataProvider(name = "invalidRows")
	public Object[][] invalidRows() {
		return new Object[][]{{"notANumber;0;0"}, {"99;0;0"}, {"0;notANumber;0"}, {"0;2;0"}, {"0;0;notANumber"},
				{"0;0;99"},};
	}

	@Test(dataProvider = "invalidRows")
	public void readCSV_withInvalidRow_expectException(final String row) {
		final String csv = HEADER + "\n" + row;

		expectThrows(InvalidCsvFieldException.class, () -> this.groupLinkCsv.readCSV(this.mockTournament, csv));
	}

	@Test
	public void getSourceGroupSize_withValidContent_expectMaxIndexPlusOne() {
		final String csv = HEADER + "\n0;0;0\n2;1;0";

		assertEquals(this.groupLinkCsv.getSourceGroupSize(csv), 3);
	}

	@Test
	public void getSourceGroupSize_withInvalidContent_expectException() {
		final String csv = HEADER + "\nnotANumber;0;0";

		expectThrows(InvalidCsvFieldException.class, () -> this.groupLinkCsv.getSourceGroupSize(csv));
	}

	@Test
	public void getDestinationGroupSize_withValidContent_expectMaxIndexPlusOne() {
		final String csv = HEADER + "\n0;0;0\n0;1;3";

		assertEquals(this.groupLinkCsv.getDestinationGroupSize(csv), 4);
	}

	@Test
	public void getDestinationGroupSize_withInvalidContent_expectException() {
		final String csv = HEADER + "\n0;0;notANumber";

		expectThrows(InvalidCsvFieldException.class, () -> this.groupLinkCsv.getDestinationGroupSize(csv));
	}
}
