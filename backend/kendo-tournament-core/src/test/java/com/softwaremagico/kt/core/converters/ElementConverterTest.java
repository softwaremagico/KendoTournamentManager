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

import com.softwaremagico.kt.core.controller.models.ElementDTO;
import com.softwaremagico.kt.core.converters.models.ConverterRequest;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "elementConverter")
public class ElementConverterTest {

    private static final class FakeRequest extends ConverterRequest<String> {
        FakeRequest(String entity) {
            super(entity);
        }
    }

    private static final class FakeConverter extends ElementConverter<String, ElementDTO, FakeRequest> {
        @Override
        protected ElementDTO convertElement(FakeRequest from) {
            final ElementDTO dto = new ElementDTO();
            dto.setCreatedAt(LocalDateTime.now());
            return dto;
        }

        @Override
        public String reverse(ElementDTO to) {
            return to == null ? null : "reversed";
        }
    }

    private final FakeConverter converter = new FakeConverter();

    @Test
    public void convertAll_withNullCollection_expectEmptyList() {
        assertTrue(converter.convertAll(null).isEmpty());
    }

    @Test
    public void convertAll_withEntries_expectSortedByCreationDate() {
        final List<ElementDTO> result = converter.convertAll(List.of(new FakeRequest("a"), new FakeRequest("b")));
        assertEquals(result.size(), 2);
    }

    @Test
    public void convertAllNotSorted_withNullCollection_expectEmptyList() {
        assertTrue(converter.convertAllNotSorted(null).isEmpty());
    }

    @Test
    public void convertAllNotSorted_withEntries_expectAllConverted() {
        final List<ElementDTO> result = converter.convertAllNotSorted(List.of(new FakeRequest("a"), new FakeRequest("b")));
        assertEquals(result.size(), 2);
    }
}

