package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.Validates;
import com.softwaremagico.kt.core.providers.CrudProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public abstract class StandardController<E, D, R extends JpaRepository<E, Integer>,
        P extends CrudProvider<E, Integer, R>> implements Validates<D> {
    private final P provider;

    protected StandardController(P provider) {
        this.provider = provider;
    }

    public P getProvider() {
        return provider;
    }

    public abstract Collection<D> get();

    public abstract D get(Integer id);

    @SuppressWarnings("java:S1172")
    public void deleteById(Integer id, String username, String session) {
        getProvider().deleteById(id);
    }

    public long count() {
        return getProvider().count();
    }

    public abstract List<D> get(Collection<Integer> ids);
}
