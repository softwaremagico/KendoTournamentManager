package com.softwaremagico.kt.utils;

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

import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.GroupLink;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "groupsTest")
public class GroupLinkUtilsTest {

    @Test
    public void shouldOrderLinksBySourceLevel() {
        final GroupLink levelOneFirst = createLink(1, 0, 2, 0, 1);
        final GroupLink levelOneSecond = createLink(1, 1, 2, 1, 2);
        final GroupLink levelThree = createLink(3, 0, 4, 0, 1);

        final Map<Integer, List<GroupLink>> ordered = GroupLinkUtils.orderBySourceLevel(List.of(levelOneFirst, levelThree, levelOneSecond));

        assertEquals(ordered.size(), 2);
        assertEquals(ordered.get(1), List.of(levelOneFirst, levelOneSecond));
        assertEquals(ordered.get(3), List.of(levelThree));
    }

    @Test
    public void shouldReturnEmptyMapForEmptyInput() {
        final Map<Integer, List<GroupLink>> ordered = GroupLinkUtils.orderBySourceLevel(List.of());

        assertTrue(ordered.isEmpty());
    }

    private GroupLink createLink(int sourceLevel, int sourceIndex, int destinationLevel, int destinationIndex, int winner) {
        final GroupLink link = new GroupLink();
        link.setSource(createGroup(sourceLevel, sourceIndex));
        link.setDestination(createGroup(destinationLevel, destinationIndex));
        link.setWinner(winner);
        return link;
    }

    private Group createGroup(int level, int index) {
        final Group group = new Group();
        group.setLevel(level);
        group.setIndex(index);
        return group;
    }
}

