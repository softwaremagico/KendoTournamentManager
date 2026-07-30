package com.softwaremagico.kt.core.providers;

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

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class CrudProvider<E, K, R extends JpaRepository<E, K>> {

    private final R repository;

    protected CrudProvider(R repository) {
        this.repository = repository;
    }

    public R getRepository() {
        return repository;
    }

    public E save(E entity) {
        return repository.save(entity);
    }

    public List<E> save(Collection<E> entities) {
        return repository.saveAll(entities);
    }

    public List<E> saveAll(Collection<E> entity) {
        return repository.saveAll(entity);
    }

    public Optional<E> get(K id) {
        return repository.findById(id);
    }

    public List<E> get(Collection<K> ids) {
        return repository.findAllById(ids);
    }

    public List<E> getAll() {
        return repository.findAll();
    }

    public E update(E entity) {
        return repository.save(entity);
    }

    public void delete(E entity) {
        repository.delete(entity);
    }

    public void delete(Collection<E> entities) {
        repository.deleteAll(entities);
    }

    public void deleteById(K id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void deleteAll(Collection<E> entities) {
        repository.deleteAll(entities);
    }

    public long count() {
        return repository.count();
    }

    public List<E> findByIdIn(Collection<K> ids) {
        return getRepository().findAllById(ids);
    }
}
