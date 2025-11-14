package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class CrudProvider<ENTITY, KEY, REPOSITORY extends JpaRepository<ENTITY, KEY>> {

    private final REPOSITORY repository;

    protected CrudProvider(REPOSITORY repository) {
        this.repository = repository;
    }

    public REPOSITORY getRepository() {
        return repository;
    }

    public ENTITY save(ENTITY entity) {
        return repository.save(entity);
    }

    public List<ENTITY> save(Collection<ENTITY> entities) {
        return repository.saveAll(entities);
    }

    public List<ENTITY> saveAll(Collection<ENTITY> entity) {
        return repository.saveAll(entity);
    }

    public Optional<ENTITY> get(KEY id) {
        return repository.findById(id);
    }

    public List<ENTITY> get(Collection<KEY> ids) {
        return repository.findAllById(ids);
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

    public void delete(Collection<ENTITY> entities) {
        repository.deleteAll(entities);
    }

    public void deleteById(KEY id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void deleteAll(Collection<ENTITY> entities) {
        repository.deleteAll(entities);
    }

    public long count() {
        return repository.count();
    }

    public List<ENTITY> findByIdIn(Collection<KEY> ids) {
        return getRepository().findAllById(ids);
    }
}
