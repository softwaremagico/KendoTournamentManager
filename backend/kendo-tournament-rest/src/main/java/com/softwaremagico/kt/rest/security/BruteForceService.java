package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.pool.BasePool;
import org.springframework.stereotype.Service;

@Service
public class BruteForceService extends BasePool<String, Integer> {
    private static final Long EXPIRATION_TIME = 10 * 60 * 1000L;
    private static final int MAX_ATTEMPTS = 3;

    public void loginSucceeded(String key) {
        removeElement(key);
    }

    public void loginFailed(String key) {
        Integer attempts = getElement(key);
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;

        addElement(attempts, key);
    }

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
