package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.Validates;
import com.softwaremagico.kt.core.providers.CrudProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public abstract class StandardController<ENTITY, DTO, REPOSITORY extends JpaRepository<ENTITY, Integer>,
        PROVIDER extends CrudProvider<ENTITY, Integer, REPOSITORY>> implements Validates<DTO> {
    protected final PROVIDER provider;

    protected StandardController(PROVIDER provider) {
        this.provider = provider;
    }

    public abstract Collection<DTO> get();

    public abstract DTO get(Integer id);

    public void deleteById(Integer id) {
        provider.deleteById(id);
    }

    public long count() {
        return provider.count();
    }
}
