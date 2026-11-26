package com.softwaremagico.kt.core.pool;

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

import org.testng.annotations.Test;

import java.time.Duration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "poolTests")
public class BasePoolTest {

    @Test
    public void shouldAddAndReadElementByKeyAndByValue() {
        final TestPool pool = new TestPool(1_000L, false);

        pool.addElement("value-1", "key-1");

        assertEquals(pool.getElement("key-1"), "value-1");
        assertEquals(pool.getKey("value-1"), "key-1");
        assertTrue(pool.getAllPooledKeys().contains("key-1"));
        assertTrue(pool.getAllPooledElements().contains("value-1"));
        assertNotNull(pool.getElementsById().get("key-1"));
        assertNotNull(pool.getElementsTime("key-1"));
    }

    @Test
    public void shouldReturnNullWhenElementExpires() {
        final TestPool pool = new TestPool(5L, false);
        pool.addElement("value-2", "key-2");

        waitUntil(() -> pool.getElement("key-2") == null, Duration.ofMillis(250));

        assertNull(pool.getElement("key-2"));
        assertFalse(pool.getAllPooledKeys().contains("key-2"));
    }

    @Test
    public void shouldDiscardDirtyElementOnRead() {
        final TestPool pool = new TestPool(1_000L, true);
        pool.addElement("dirty", "key-3");

        assertNull(pool.getElement("key-3"));
        assertNull(pool.getKey("dirty"));
    }

    @Test
    public void shouldRemoveAndResetPool() {
        final TestPool pool = new TestPool(1_000L, false);
        pool.addElement("value-4", "key-4");

        assertEquals(pool.removeElement("key-4"), "value-4");
        assertNull(pool.getElement("key-4"));

        pool.addElement("value-5", "key-5");
        pool.reset();

        assertTrue(pool.getAllPooledElements().isEmpty());
        assertTrue(pool.getAllPooledKeys().isEmpty());
        assertTrue(pool.getElementsTime().isEmpty());
    }

    @Test
    public void shouldReturnNullWhenGetKeyCalledWithExpiredElement() {
        final TestPool pool = new TestPool(5L, false);
        pool.addElement("value-exp", "key-exp");

        waitUntil(() -> pool.getKey("value-exp") == null, Duration.ofMillis(250));

        assertNull(pool.getKey("value-exp"));
    }

    @Test
    public void shouldReturnNullWhenGetKeyCalledWithDirtyElement() {
        final TestPool pool = new TestPool(1_000L, true);
        pool.addElement("dirty-val", "key-dirty");

        assertNull(pool.getKey("dirty-val"));
    }

    @Test
    public void shouldReturnNullWhenGetKeyCalledWithNonMatchingElement() {
        final TestPool pool = new TestPool(1_000L, false);
        pool.addElement("value-a", "key-a");

        assertNull(pool.getKey("value-z"));
    }

    @Test
    public void shouldReturnNullWhenRemoveCalledWithNullId() {
        final TestPool pool = new TestPool(1_000L, false);
        pool.addElement("value-x", "key-x");

        assertNull(pool.removeElement(null));
    }

    @Test
    public void shouldReturnNullWhenGetElementCalledWithNullId() {
        final TestPool pool = new TestPool(1_000L, false);

        assertNull(pool.getElement(null));
    }

    @Test
    public void shouldReturnNullWhenAddElementCalledWithZeroExpirationTime() {
        final TestPool pool = new TestPool(0L, false);
        pool.addElement("val", "key");

        assertNull(pool.getElement("key"));
        assertTrue(pool.getAllPooledElements().isEmpty());
    }

    @Test
    public void shouldReturnNullWhenGetElementCalledOnEmptyPool() {
        final TestPool pool = new TestPool(1_000L, false);

        assertNull(pool.getElement("missing-key"));
    }

    private void waitUntil(java.util.function.BooleanSupplier condition, Duration timeout) {
        final long deadline = System.nanoTime() + timeout.toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertTrue(condition.getAsBoolean());
    }

    private static final class TestPool extends BasePool<String, String> {
        private final long expirationTime;
        private final boolean dirty;

        private TestPool(long expirationTime, boolean dirty) {
            this.expirationTime = expirationTime;
            this.dirty = dirty;
        }

        @Override
        public long getExpirationTime() {
            return expirationTime;
        }

        @Override
        public boolean isDirty(String element) {
            return dirty;
        }
    }
}

