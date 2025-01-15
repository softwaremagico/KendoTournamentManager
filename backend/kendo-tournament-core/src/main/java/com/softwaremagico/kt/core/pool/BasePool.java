package com.softwaremagico.kt.core.pool;

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


import com.softwaremagico.kt.logger.PoolLogger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BasePool<ElementId, Type> {
    // Elements by id;
    private Map<ElementId, Long> elementsTime; // id -> time.
    private Map<ElementId, Type> elementsById;

    protected BasePool() {
        reset();
    }

    public synchronized void reset() {
        PoolLogger.debug(this.getClass(), "Resetting all pool.");
        elementsTime = new ConcurrentHashMap<>();
        elementsById = new ConcurrentHashMap<>();
    }

    public synchronized void addElement(Type element, ElementId key) {
        PoolLogger.debug(this.getClass(), "Adding element '" + element + "' with key '" + key + "'.");
        if (getExpirationTime() > 0) {
            elementsTime.put(key, System.currentTimeMillis());
            elementsById.put(key, element);
        }
    }

    /**
     * Gets all previously stored elements of a user in a site.
     *
     * @param elementId element key for the pool.
     * @return the element that has the selected key.
     */
    public synchronized Type getElement(ElementId elementId) {
        if (elementId != null && getExpirationTime() > 0) {
            final long now = System.currentTimeMillis();
            ElementId storedObjectId;
            if (elementsTime.size() > 0) {
                PoolLogger.debug(this.getClass(), "Elements on cache: " + elementsTime.size() + ".");
                final Map<ElementId, Long> elementsByTimeChecked = new ConcurrentHashMap<>(elementsTime);
                final Map<ElementId, Type> elementsByIdChecked = new ConcurrentHashMap<>(elementsById);
                final Iterator<ElementId> elementByTime = elementsByTimeChecked.keySet().iterator();

                for (final Entry<ElementId, Long> elementsByTimeEntry : elementsByTimeChecked.entrySet()) {
                    storedObjectId = elementByTime.next();
                    if (elementsByTimeEntry.getValue() != null
                            && (now - elementsByTimeEntry.getValue()) > getExpirationTime()) {
                        PoolLogger.debug(this.getClass(), "Element '" + elementsByTimeEntry.getValue()
                                + "' has expired (elapsed time: '" + (now - elementsByTimeEntry.getValue()) + "' > '"
                                + getExpirationTime() + "'.)");
                        // object has expired
                        removeElement(storedObjectId);
                    } else {
                        if (elementsByIdChecked.get(storedObjectId) != null) {
                            // Remove not valid elements.
                            if (isDirty(elementsByIdChecked.get(storedObjectId))) {
                                PoolLogger.debug(this.getClass(),
                                        "Cache: " + elementsByIdChecked.get(storedObjectId).getClass().getName()
                                                + " is dirty! ");
                                removeElement(storedObjectId);
                            } else if (Objects.equals(storedObjectId, elementId)) {
                                PoolLogger.info(this.getClass(), "Cache: "
                                        + elementsByIdChecked.get(storedObjectId).getClass().getName()
                                        + " store hit for " + elementId);
                                return elementsByIdChecked.get(storedObjectId);
                            }
                        }
                    }
                }
            }
        }
        PoolLogger.debug(this.getClass(), "Object with Id '" + elementId + "' - Cache Miss.");
        return null;
    }

    public synchronized ElementId getKey(Type element) {
        if (element != null && getExpirationTime() > 0) {
            final long now = System.currentTimeMillis();
            ElementId storedObjectId;
            if (elementsTime.size() > 0) {
                PoolLogger.debug(this.getClass(), "Elements on cache: " + elementsTime.size() + ".");
                for (final ElementId elementId : new ConcurrentHashMap<>(elementsTime).keySet()) {
                    storedObjectId = elementId;
                    if (elementsTime.get(storedObjectId) != null
                            && (now - elementsTime.get(storedObjectId)) > getExpirationTime()) {
                        PoolLogger.debug(this.getClass(), "Element '" + elementsTime.get(storedObjectId)
                                + "' has expired (elapsed time: '" + (now - elementsTime.get(storedObjectId)) + "' > '"
                                + getExpirationTime() + "'.)");
                        // object has expired
                        removeElement(storedObjectId);
                    } else {
                        if (elementsById.get(storedObjectId) != null) {
                            // Remove not valid elements.
                            if (isDirty(elementsById.get(storedObjectId))) {
                                PoolLogger.debug(this.getClass(), "Cache: "
                                        + elementsById.get(storedObjectId).getClass().getName() + " is dirty! ");
                                removeElement(storedObjectId);
                            } else if (Objects.equals(elementsById.get(storedObjectId), element)) {
                                PoolLogger.info(this.getClass(), "Cache: "
                                        + elementsById.get(storedObjectId).getClass().getName() + " store hit for "
                                        + element);
                                return storedObjectId;
                            }
                        }
                    }
                }
            }
        }
        PoolLogger.debug(this.getClass(), "Object '" + element + "' - Cache Miss.");
        return null;
    }


    public abstract long getExpirationTime();


    public Set<Type> getAllPooledElements() {
        return new HashSet<>(elementsById.values());
    }


    public Set<ElementId> getAllPooledKeys() {
        return new HashSet<>(elementsById.keySet());
    }


    public Map<ElementId, Type> getElementsById() {
        return elementsById;
    }


    public Long getElementsTime(ElementId elementId) {
        return elementsTime.get(elementId);
    }

    public Map<ElementId, Long> getElementsTime() {
        return elementsTime;
    }


    public synchronized Type removeElement(ElementId elementId) {
        if (elementId != null) {
            PoolLogger.debug(this.getClass(), "Removing element '" + elementId + "'.");
            elementsTime.remove(elementId);
            return elementsById.remove(elementId);
        }
        return null;
    }

    /**
     * An element is dirty if cannot be used by the pool any more.
     *
     * @param element element to check
     * @return if it is dirty or not.
     */

    public abstract boolean isDirty(Type element);
}
