package com.softwaremagico.kt.core.pool;

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


import com.softwaremagico.kt.logger.PoolLogger;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BasePool<ID, E> {
    private static final String CACHE_PREFIX = "Cache: ";

    //Elements by id;
    private Map<ID, Long> elementsTime; // id -> time.
    private Map<ID, E> elementsById;

    protected BasePool() {
        reset();
    }

    public synchronized void reset() {
        PoolLogger.debug(this.getClass(), "Resetting all pool.");
        elementsTime = new ConcurrentHashMap<>();
        elementsById = new ConcurrentHashMap<>();
    }

    public synchronized void addElement(E element, ID key) {
        PoolLogger.debug(this.getClass(), "Adding element '" + element + "' with key '" + key + "'.");
        if (getExpirationTime() > 0) {
            elementsTime.put(key, System.currentTimeMillis());
            elementsById.put(key, element);
        }
    }

    /**
     * Gets all previously stored elements of a user in a site.
     *
     * @param id element key for the pool.
     * @return the element that has the selected key.
     */
    public synchronized E getElement(ID id) {
        if (isInvalidCacheRequest(id)) {
            logCacheMissById(id);
            return null;
        }

        final long now = System.currentTimeMillis();
        final Map<ID, Long> elementsByTimeChecked = getElementsTimeSnapshot();
        PoolLogger.debug(this.getClass(), "Elements on cache: " + elementsByTimeChecked.size() + ".");

        for (final Map.Entry<ID, Long> entry : elementsByTimeChecked.entrySet()) {
            final ID storedObjectId = entry.getKey();
            final E cachedElement = getValidCachedElement(storedObjectId, entry.getValue(), now);
            if (cachedElement != null && Objects.equals(storedObjectId, id)) {
                logStoreHit(cachedElement, id);
                return cachedElement;
            }
        }

        logCacheMissById(id);
        return null;
    }

    public synchronized ID getKey(E element) {
        if (isInvalidCacheRequest(element)) {
            logCacheMissByValue(element);
            return null;
        }

        final long now = System.currentTimeMillis();
        final Map<ID, Long> elementsByTimeChecked = getElementsTimeSnapshot();
        PoolLogger.debug(this.getClass(), "Elements on cache: " + elementsByTimeChecked.size() + ".");
        for (final Map.Entry<ID, Long> entry : elementsByTimeChecked.entrySet()) {
            final ID id = entry.getKey();
            final E cachedElement = getValidCachedElement(id, entry.getValue(), now);
            if (Objects.equals(cachedElement, element)) {
                logStoreHit(cachedElement, element);
                return id;
            }
        }

        logCacheMissByValue(element);
        return null;
    }

    private boolean isInvalidCacheRequest(Object value) {
        return value == null || getExpirationTime() <= 0 || elementsTime.isEmpty();
    }

    private Map<ID, Long> getElementsTimeSnapshot() {
        return new ConcurrentHashMap<>(elementsTime);
    }

    private E getValidCachedElement(ID storedObjectId, Long timestamp, long now) {
        if (removeIfExpired(storedObjectId, timestamp, now)) {
            return null;
        }
        final E cachedElement = elementsById.get(storedObjectId);
        if (removeIfDirty(storedObjectId, cachedElement)) {
            return null;
        }
        return cachedElement;
    }

    private void logCacheMissById(ID id) {
        PoolLogger.debug(this.getClass(), "Object with Id '" + id + "' - Cache Miss.");
    }

    private void logCacheMissByValue(Object element) {
        PoolLogger.debug(this.getClass(), "Object '" + element + "' - Cache Miss.");
    }

    private boolean removeIfExpired(ID storedObjectId, Long timestamp, long now) {
        if (!isExpired(timestamp, now)) {
            return false;
        }
        PoolLogger.debug(this.getClass(), "Element '" + timestamp
                + "' has expired (elapsed time: '" + (now - timestamp) + "' > '"
                + getExpirationTime() + "'.)");
        removeElement(storedObjectId);
        return true;
    }

    private boolean isExpired(Long timestamp, long now) {
        return timestamp != null && (now - timestamp) > getExpirationTime();
    }

    private boolean removeIfDirty(ID storedObjectId, E cachedElement) {
        if (cachedElement == null || !isDirty(cachedElement)) {
            return false;
        }
        PoolLogger.debug(this.getClass(), CACHE_PREFIX + cachedElement.getClass().getName() + " is dirty! ");
        removeElement(storedObjectId);
        return true;
    }

    private void logStoreHit(E cachedElement, Object searchedElement) {
        PoolLogger.info(this.getClass(), CACHE_PREFIX + cachedElement.getClass().getName() + " store hit for " + searchedElement);
    }


    public abstract long getExpirationTime();


    public Set<E> getAllPooledElements() {
        return new HashSet<>(elementsById.values());
    }


    public Set<ID> getAllPooledKeys() {
        return new HashSet<>(elementsById.keySet());
    }


    public Map<ID, E> getElementsById() {
        return elementsById;
    }


    public Long getElementsTime(ID id) {
        return elementsTime.get(id);
    }

    public Map<ID, Long> getElementsTime() {
        return elementsTime;
    }


    public synchronized E removeElement(ID id) {
        if (id != null) {
            PoolLogger.debug(this.getClass(), "Removing element '" + id + "'.");
            elementsTime.remove(id);
            return elementsById.remove(id);
        }
        return null;
    }

    /**
     * An element is dirty if cannot be used by the pool any more.
     *
     * @param element element to check
     * @return if it is dirty or not.
     */

    public abstract boolean isDirty(E element);
}
