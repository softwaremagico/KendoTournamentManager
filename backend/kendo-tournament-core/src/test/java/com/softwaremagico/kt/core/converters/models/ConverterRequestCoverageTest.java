package com.softwaremagico.kt.core.converters.models;

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

import com.softwaremagico.kt.core.exceptions.UnexpectedValueException;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Test(groups = {"converterRequest"})
public class ConverterRequestCoverageTest {

    // ---------- constructor(T entity) ----------

    @Test
    public void when_constructedWithNonNullEntity_expect_hasEntity() {
        final ConverterRequest<String> request = new ConverterRequest<>("hello");

        assertThat(request.hasEntity()).isTrue();
        assertThat(request.getEntity()).isEqualTo("hello");
        assertThat(request.getEntityWithoutChecks()).isEqualTo("hello");
    }

    @Test
    public void when_constructedWithNullEntity_expect_noEntity() {
        final ConverterRequest<String> request = new ConverterRequest<>((String) null);

        assertThat(request.hasEntity()).isFalse();
        assertThat(request.getEntityWithoutChecks()).isNull();
    }

    // ---------- getEntity() on null entity throws UnexpectedValueException ----------

    @Test
    public void when_getEntityOnNullEntity_expect_exception() {
        final ConverterRequest<String> request = new ConverterRequest<>((String) null);

        assertThatThrownBy(request::getEntity)
                .isInstanceOf(UnexpectedValueException.class);
    }

    // ---------- constructor(Optional<T>) ----------

    @Test
    public void when_constructedWithPresentOptional_expect_hasEntity() {
        final ConverterRequest<String> request = new ConverterRequest<>(Optional.of("world"));

        assertThat(request.hasEntity()).isTrue();
        assertThat(request.getEntity()).isEqualTo("world");
    }

    @Test
    public void when_constructedWithEmptyOptional_expect_noEntity() {
        final ConverterRequest<String> request = new ConverterRequest<>(Optional.<String>empty());

        assertThat(request.hasEntity()).isFalse();
    }

    // ---------- setEntity() ----------

    @Test
    public void when_setEntity_expect_entityUpdated() {
        final ConverterRequest<String> request = new ConverterRequest<>((String) null);
        request.setEntity("updated");

        assertThat(request.hasEntity()).isTrue();
        assertThat(request.getEntity()).isEqualTo("updated");
    }

    @Test
    public void when_setEntityToNull_expect_noEntity() {
        final ConverterRequest<String> request = new ConverterRequest<>("value");
        request.setEntity(null);

        assertThat(request.hasEntity()).isFalse();
    }

    // ---------- generic type usage with Integer ----------

    @Test
    public void when_constructedWithInteger_expect_correctEntityReturned() {
        final ConverterRequest<Integer> request = new ConverterRequest<>(42);

        assertThat(request.hasEntity()).isTrue();
        assertThat(request.getEntity()).isEqualTo(42);
    }
}

