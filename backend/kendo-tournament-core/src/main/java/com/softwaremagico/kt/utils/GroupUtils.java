package com.softwaremagico.kt.utils;

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

import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.persistence.entities.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GroupUtils {

    private GroupUtils() {

    }


    public static Map<Integer, List<Group>> orderByLevel(List<Group> groups) {
        final Map<Integer, List<Group>> sortedGroups = new HashMap<>();
        groups.forEach(group -> {
            sortedGroups.computeIfAbsent(group.getLevel(), k -> new ArrayList<>());
            sortedGroups.get(group.getLevel()).add(group);
        });
        //Complete empty levels if any.
        final int maxLevel = groups.stream().mapToInt(Group::getLevel).max().orElse(-1);
        for (int level = 0; level < maxLevel; level++) {
            sortedGroups.computeIfAbsent(level, k -> new ArrayList<>());
        }
        return sortedGroups;
    }


    public static Map<Integer, List<GroupDTO>> orderDTOByLevel(List<GroupDTO> groups) {
        final Map<Integer, List<GroupDTO>> sortedGroups = new HashMap<>();
        groups.forEach(group -> {
            sortedGroups.computeIfAbsent(group.getLevel(), k -> new ArrayList<>());
            sortedGroups.get(group.getLevel()).add(group);
        });
        return sortedGroups;
    }


    public static int getNextPowerOfTwo(int x) {
        while (!isPowerOfTwo(x)) {
            x++;
        }
        return x;
    }


    public static boolean isPowerOfTwo(int x) {
        return (x != 0) && (x & (x - 1)) == 0;
    }
}
