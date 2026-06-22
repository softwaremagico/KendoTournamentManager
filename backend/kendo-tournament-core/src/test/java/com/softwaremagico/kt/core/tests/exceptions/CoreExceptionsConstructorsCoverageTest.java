package com.softwaremagico.kt.core.tests.exceptions;

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

import com.softwaremagico.kt.logger.ExceptionType;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;

import static org.testng.Assert.assertNotNull;

@Test(groups = "coreExceptions")
public class CoreExceptionsConstructorsCoverageTest {

    @Test
    public void shouldInstantiateAllCoreExceptionConstructors() {
        final Class<?>[] exceptionClasses = new Class<?>[]{
                com.softwaremagico.kt.core.exceptions.ClubNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.CustomTournamentFightsException.class,
                com.softwaremagico.kt.core.exceptions.DataInputException.class,
                com.softwaremagico.kt.core.exceptions.DatabaseException.class,
                com.softwaremagico.kt.core.exceptions.DuelNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.DuplicatedUserException.class,
                com.softwaremagico.kt.core.exceptions.FightNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.GroupNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.InvalidChallengeDistanceException.class,
                com.softwaremagico.kt.core.exceptions.InvalidCsvFieldException.class,
                com.softwaremagico.kt.core.exceptions.InvalidCsvRowException.class,
                com.softwaremagico.kt.core.exceptions.InvalidExtraPropertyException.class,
                com.softwaremagico.kt.core.exceptions.InvalidFightException.class,
                com.softwaremagico.kt.core.exceptions.InvalidGroupException.class,
                com.softwaremagico.kt.core.exceptions.LevelNotFinishedException.class,
                com.softwaremagico.kt.core.exceptions.NameAlreadyInUseException.class,
                com.softwaremagico.kt.core.exceptions.NoContentException.class,
                com.softwaremagico.kt.core.exceptions.NotFoundException.class,
                com.softwaremagico.kt.core.exceptions.NotValidInputException.class,
                com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.RoleNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.SenbatsuTournamentFightsException.class,
                com.softwaremagico.kt.core.exceptions.TeamNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.TokenExpiredException.class,
                com.softwaremagico.kt.core.exceptions.TournamentFinishedException.class,
                com.softwaremagico.kt.core.exceptions.TournamentInvalidException.class,
                com.softwaremagico.kt.core.exceptions.TournamentNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.UnexpectedValueException.class,
                com.softwaremagico.kt.core.exceptions.UserNotFoundException.class,
                com.softwaremagico.kt.core.exceptions.ValidateBadRequestException.class
        };

        for (Class<?> exceptionClass : exceptionClasses) {
            for (Constructor<?> constructor : exceptionClass.getDeclaredConstructors()) {
                constructor.setAccessible(true);
                final Object instance = instantiate(constructor);
                assertNotNull(instance);
            }
        }
    }

    private Object instantiate(Constructor<?> constructor) {
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            args[i] = sample(parameterTypes[i]);
        }
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new AssertionError("Cannot instantiate " + constructor, e);
        }
    }

    private Object sample(Class<?> type) {
        if (type == Class.class) {
            return getClass();
        }
        if (type == String.class) {
            return "sample";
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == Throwable.class || Throwable.class.isAssignableFrom(type)) {
            return new RuntimeException("cause");
        }
        if (type == ExceptionType.class) {
            return ExceptionType.WARNING;
        }
        return null;
    }
}

