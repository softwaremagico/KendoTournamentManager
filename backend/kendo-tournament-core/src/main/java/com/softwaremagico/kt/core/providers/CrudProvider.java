package com.softwaremagico.kt.core.providers;

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

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class CrudProvider<ENTITY, KEY, REPOSITORY extends JpaRepository<ENTITY, KEY>> {

    protected final REPOSITORY repository;

    public CrudProvider(REPOSITORY repository) {
        this.repository = repository;
    }

    public ENTITY save(ENTITY entity) {
        return repository.save(entity);
    }

    public List<ENTITY> saveAll(Collection<ENTITY> entity) {
        return repository.saveAll(entity);
    }

    public Optional<ENTITY> get(KEY id) {
        return repository.findById(id);
    }

    public List<ENTITY> getAll() {
        return repository.findAll();
    }

    public ENTITY update(ENTITY entity) {
        return repository.save(entity);
    }

    public void delete(ENTITY entity) {
        repository.delete(entity);
    }

    public void deleteById(KEY id) {
        repository.deleteById(id);
    }

    public void delete() {
        repository.deleteAll();
    }

    public long count() {
        return repository.count();
    }
}
