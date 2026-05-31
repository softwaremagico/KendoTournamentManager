package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.pool.BasePool;
import org.springframework.stereotype.Service;

/**
 * Service that tracks failed login attempts and blocks an IP address (or username)
 * after {@link #MAX_ATTEMPTS} consecutive failures.
 * <p>
 * The tracking data is held in an in-memory pool ({@link BasePool}) with a TTL of
 * 600000 ms (10 minutes). Entries are automatically evicted after
 * the expiration window, so a blocked key is automatically unblocked after 10 minutes
 * of inactivity.
 * </p>
 * <p>Usage pattern:</p>
 * <pre>{@code
 * // On failed authentication:
 * bruteForceService.loginFailed(clientIp);
 * // Before attempting auth:
 * if (bruteForceService.isBlocked(clientIp)) { throw new TooManyAttemptsException(); }
 * // On successful authentication:
 * bruteForceService.loginSucceeded(clientIp);
 * }</pre>
 */
@Service
public class BruteForceService extends BasePool<String, Integer> {
    private static final Long EXPIRATION_TIME = 10 * 60 * 1000L;
    /** Maximum number of consecutive failures before an IP/key is blocked. */
    public static final int MAX_ATTEMPTS = 10;

    /**
     * Resets the failure counter for the given key after a successful login.
     *
     * @param key the IP address or username that authenticated successfully
     */
    public void loginSucceeded(String key) {
        removeElement(key);
    }

    /**
     * Increments the failure counter for the given key.
     * If the key is new its counter starts at 1.
     *
     * @param key the IP address or username that failed authentication
     */
    public void loginFailed(String key) {
        Integer attempts = getElement(key);
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;

        addElement(attempts, key);
    }

    /**
     * Returns {@code true} if the failure counter for {@code key} has reached
     * or exceeded {@link #MAX_ATTEMPTS}.
     *
     * @param key the IP address or username to check
     * @return {@code true} if the key is currently blocked
     */
    public boolean isBlocked(String key) {
        final Integer attempts = getElement(key);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    @Override
    public long getExpirationTime() {
        return EXPIRATION_TIME;
    }

    @Override
    public boolean isDirty(Integer element) {
        return false;
    }

}
