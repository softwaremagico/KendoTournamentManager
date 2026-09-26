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

import com.softwaremagico.kt.core.converters.models.ConverterRequest;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "simpleConverter")
public class SimpleConverterTest {

    private static final class FakeRequest extends ConverterRequest<String> {
        FakeRequest(String entity) {
            super(entity);
        }
    }

    private static final class FakeConverter extends SimpleConverter<String, String, FakeRequest> {
        @Override
        protected String convertElement(FakeRequest from) {
            return from.getEntity() + "-converted";
        }

        @Override
        public String reverse(String to) {
            return to == null ? null : to + "-reversed";
        }
    }

    private final FakeConverter converter = new FakeConverter();

    @Test
    public void convert_withNullRequest_expectNull() {
        assertNull(converter.convert(null));
    }

    @Test
    public void convert_withoutEntity_expectNull() {
        assertNull(converter.convert(new FakeRequest(null)));
    }

    @Test
    public void convertAll_withNullCollection_expectEmptyList() {
        assertTrue(converter.convertAll(null).isEmpty());
    }

    @Test
    public void reverseAll_withNullCollection_expectEmptyList() {
        assertTrue(converter.reverseAll(null).isEmpty());
    }

    @Test
    public void reverseAll_withEntries_expectAllReversed() {
        final List<String> result = converter.reverseAll(List.of("a", "b"));
        assertTrue(result.contains("a-reversed"));
        assertTrue(result.contains("b-reversed"));
    }
}

