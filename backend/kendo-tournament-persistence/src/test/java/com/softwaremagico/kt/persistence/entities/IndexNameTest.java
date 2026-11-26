package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test(groups = "entityBeans")
public class IndexNameTest {

    @Test
    public void shouldUseUniqueIndexNamesAcrossTables() {
        List<Class<?>> entities = List.of(
                Achievement.class,
                Duel.class,
                Fight.class,
                Group.class,
                GroupLink.class,
                Role.class,
                Team.class,
                TournamentExtraProperty.class,
                TournamentImage.class
        );

        Map<String, String> owners = new HashMap<>();
        for (Class<?> entity : entities) {
            Table table = entity.getAnnotation(Table.class);
            Assert.assertNotNull(table, entity.getSimpleName() + " should be mapped to a table");
            for (Index index : table.indexes()) {
                String previousOwner = owners.putIfAbsent(index.name(), entity.getSimpleName());
                Assert.assertNull(previousOwner,
                        "Index '" + index.name() + "' is declared by " + previousOwner + " and " + entity.getSimpleName());
            }
        }
    }
}
