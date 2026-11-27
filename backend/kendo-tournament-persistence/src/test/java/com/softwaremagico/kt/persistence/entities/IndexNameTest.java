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
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Test
    public void shouldDeclareTenantScopedPerformanceIndexes() throws NoSuchFieldException {
        assertIndex(Achievement.class, "ind_achievements_tenant_tournament_type_grade_created",
                "tenant_id, tournament, achievement_type, achievement_grade, created_at");
        assertIndex(Role.class, "ind_roles_tenant_tournament_type", "tenant_id, tournament, role_type");
        assertIndex(Group.class, "ind_tournament_groups_tenant_level_index",
                "tenant_id, tournament, group_level, group_index");
        assertIndex(Fight.class, "ind_fights_tenant_tournament_shiaijo", "tenant_id, tournament, shiaijo");
        assertIndex(Fight.class, "ind_fights_tenant_tournament_level", "tenant_id, tournament, fight_level");

        Field duels = Fight.class.getDeclaredField("duels");
        JoinTable joinTable = duels.getAnnotation(JoinTable.class);
        Assert.assertNotNull(joinTable, "duels should be mapped with @JoinTable");
        Assert.assertEquals(joinTable.indexes().length, 1);
        Assert.assertEquals(joinTable.indexes()[0].name(), "ind_duels_by_fight_duel");
        Assert.assertEquals(joinTable.indexes()[0].columnList(), "duel_id");
    }

    @Test
    public void shouldIncludePerformanceIndexesInMigrationScripts() throws IOException {
        List<String> statements = List.of(
                "ind_duels_by_fight_duel",
                "ind_roles_tenant_tournament_type",
                "ind_tournament_groups_tenant_level_index",
                "ind_fights_tenant_tournament_shiaijo",
                "ind_fights_tenant_tournament_level",
                "ind_achievements_tenant_tournament_type_grade_created"
        );

        for (String script : List.of("3.6.0-tenancy-mysql.sql", "3.6.0-tenancy-postgresql.sql")) {
            String content = Files.readString(resolveMigrationScript(script), StandardCharsets.UTF_8);
            for (String indexName : statements) {
                Assert.assertTrue(content.contains(indexName), script + " should create " + indexName);
            }
        }
    }

    private static void assertIndex(Class<?> entity, String name, String columnList) {
        Table table = entity.getAnnotation(Table.class);
        Assert.assertNotNull(table, entity.getSimpleName() + " should be mapped to a table");
        Assert.assertTrue(List.of(table.indexes()).stream().anyMatch(index ->
                        index.name().equals(name) && index.columnList().equals(columnList)),
                entity.getSimpleName() + " should declare " + name + " on " + columnList);
    }

    private static Path resolveMigrationScript(String fileName) throws IOException {
        Path directory = Path.of("").toAbsolutePath();
        for (int level = 0; level < 6; level++) {
            Path candidate = directory.resolve("documents").resolve("migrations").resolve(fileName);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            directory = directory.getParent();
        }
        throw new IOException("Cannot locate documents/migrations/" + fileName + " from " + Path.of("").toAbsolutePath());
    }
}
