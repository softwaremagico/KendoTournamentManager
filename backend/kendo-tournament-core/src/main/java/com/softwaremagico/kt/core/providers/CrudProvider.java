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
import com.softwaremagico.kt.persistence.entities.Element;
import com.softwaremagico.kt.persistence.entities.TenantContext;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        if (entity == null) {
            return null;
        }
        if (entity instanceof Element tenantScopedEntity && tenantScopedEntity.getTenantId() == null) {
            tenantScopedEntity.setTenantId(TenantContext.getRequiredTenantId());
        }
        return repository.save(entity);
    }

    public List<E> save(Collection<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        final List<E> validEntities = entities.stream().filter(Objects::nonNull).toList();
        if (validEntities.isEmpty()) {
            return List.of();
        }
        validEntities.forEach(entity -> {
            if (entity instanceof Element tenantScopedEntity && tenantScopedEntity.getTenantId() == null) {
                tenantScopedEntity.setTenantId(TenantContext.getRequiredTenantId());
            }
        });
        return repository.saveAll(validEntities);
    }

    public List<E> saveAll(Collection<E> entity) {
        return save(entity);
    }

    public Optional<E> get(K id) {
        if (id == null) {
            return Optional.empty();
        }
        return repository.findById(id).filter(this::belongsToCurrentTenant);
    }

    public List<E> get(Collection<K> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        final List<K> validIds = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (validIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(validIds).stream().filter(this::belongsToCurrentTenant).toList();
    }

    public List<E> getAll() {
        return repository.findAll().stream().filter(this::belongsToCurrentTenant).toList();
    }

    public E update(E entity) {
        return save(entity);
    }

    public void delete(E entity) {
        if (entity == null || !belongsToCurrentTenant(entity)) {
            return;
        }
        repository.delete(entity);
    }

    public void delete(Collection<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        final List<E> validEntities = entities.stream().filter(Objects::nonNull).filter(this::belongsToCurrentTenant).toList();
        if (validEntities.isEmpty()) {
            return;
        }
        repository.deleteAll(validEntities);
    }

    public void deleteById(K id) {
        if (id == null) {
            return;
        }
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void deleteAll(Collection<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        final List<E> validEntities = entities.stream().filter(Objects::nonNull).filter(this::belongsToCurrentTenant).toList();
        if (validEntities.isEmpty()) {
            return;
        }
        repository.deleteAll(validEntities);
    }

    public long count() {
        return repository.count();
    }

    public List<E> findByIdIn(Collection<K> ids) {
        return get(ids);
    }

    /**
     * Repository methods remain generic for the statistics subsystem. Domain
     * entities are additionally checked here so a missed repository predicate
     * cannot expose a row from another active request tenant.
     */
    private boolean belongsToCurrentTenant(E entity) {
        if (!(entity instanceof Element tenantScopedEntity) || TenantContext.getTenantId() == null) {
            return true;
        }
        return TenantContext.getTenantId().equals(tenantScopedEntity.getTenantId());
    }
}
