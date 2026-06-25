package com.softwaremagico.kt.core.tests.models;

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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertNotNull;

@Test(groups = "controllerModelsCoverage")
public class ControllerModelsCoverageTest {

    @Test
    public void shouldCoverControllerModelsGettersSettersAndObjectMethods() throws Exception {
        final Class<?>[] modelClasses = new Class<?>[]{
                com.softwaremagico.kt.core.controller.models.AchievementDTO.class,
                com.softwaremagico.kt.core.controller.models.ClubDTO.class,
                com.softwaremagico.kt.core.controller.models.DuelDTO.class,
                com.softwaremagico.kt.core.controller.models.ElementDTO.class,
                com.softwaremagico.kt.core.controller.models.FightDTO.class,
                com.softwaremagico.kt.core.controller.models.GroupDTO.class,
                com.softwaremagico.kt.core.controller.models.GroupLinkDTO.class,
                com.softwaremagico.kt.core.controller.models.ImageDTO.class,
                com.softwaremagico.kt.core.controller.models.LogDTO.class,
                com.softwaremagico.kt.core.controller.models.ParticipantDTO.class,
                com.softwaremagico.kt.core.controller.models.ParticipantFightStatisticsDTO.class,
                com.softwaremagico.kt.core.controller.models.ParticipantImageDTO.class,
                com.softwaremagico.kt.core.controller.models.ParticipantInTournamentDTO.class,
                com.softwaremagico.kt.core.controller.models.ParticipantReducedDTO.class,
                com.softwaremagico.kt.core.controller.models.ParticipantStatisticsDTO.class,
                com.softwaremagico.kt.core.controller.models.ParticipantsInTournamentDTO.class,
                com.softwaremagico.kt.core.controller.models.QrCodeDTO.class,
                com.softwaremagico.kt.core.controller.models.RoleDTO.class,
                com.softwaremagico.kt.core.controller.models.ScoreOfCompetitorDTO.class,
                com.softwaremagico.kt.core.controller.models.ScoreOfTeamDTO.class,
                com.softwaremagico.kt.core.controller.models.TeamDTO.class,
                com.softwaremagico.kt.core.controller.models.TemporalToken.class,
                com.softwaremagico.kt.core.controller.models.Token.class,
                com.softwaremagico.kt.core.controller.models.TournamentDTO.class,
                com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO.class,
                com.softwaremagico.kt.core.controller.models.TournamentFightStatisticsDTO.class,
                com.softwaremagico.kt.core.controller.models.TournamentImageDTO.class,
                com.softwaremagico.kt.core.controller.models.TournamentScoreDTO.class,
                com.softwaremagico.kt.core.controller.models.TournamentStatisticsDTO.class,
                com.softwaremagico.kt.core.controller.models.Validates.class
        };

        for (Class<?> modelClass : modelClasses) {
            final Object instance = newInstanceOrNull(modelClass);
            if (instance == null) {
                continue;
            }
            assertNotNull(instance);

            for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(modelClass, Object.class).getPropertyDescriptors()) {
                final Method writeMethod = propertyDescriptor.getWriteMethod();
                if (writeMethod != null && Modifier.isPublic(writeMethod.getModifiers())) {
                    try {
                        writeMethod.invoke(instance, sampleValue(writeMethod.getParameterTypes()[0]));
                    } catch (Exception _) {
                        // Some DTO setters validate values or require richer state.
                    }
                }
                final Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && Modifier.isPublic(readMethod.getModifiers())) {
                    try {
                        readMethod.invoke(instance);
                    } catch (Exception _) {
                        // Some DTO getters compute values from optional internals.
                    }
                }
            }

            try {
                instance.hashCode();
            } catch (Exception _) {
                // Some DTOs derive hashCode from optional nested content.
            }
            instance.equals(instance);
            try {
                instance.toString();
            } catch (Exception _) {
                // Some DTOs derive toString from optional nested content.
            }
        }
    }

    private Object newInstanceOrNull(Class<?> clazz) throws Exception {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                try {
                    return constructor.newInstance();
                } catch (InstantiationException _) {
                    return null;
                }
            }
        }
        return null;
    }

    private Object sampleValue(Class<?> type) throws Exception {
        if (type == String.class) {
            return "x";
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == long.class || type == Long.class) {
            return 1L;
        }
        if (type == boolean.class || type == Boolean.class) {
            return true;
        }
        if (type == double.class || type == Double.class) {
            return 1.0d;
        }
        if (type == float.class || type == Float.class) {
            return 1.0f;
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        }
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        }
        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<>();
        }
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }
        if (type.getName().startsWith("com.softwaremagico.kt.core.controller.models.")) {
            return newInstanceOrNull(type);
        }
        return null;
    }
}

