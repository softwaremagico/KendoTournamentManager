package com.softwaremagico.kt.core.tests.exceptions;

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

import com.softwaremagico.kt.core.exceptions.*;
import com.softwaremagico.kt.logger.ExceptionType;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "coreExceptions")
public class CoreExceptionsTests {

	@Test
	public void testNotValidInputExceptionWithBasicConstructor() {
		final String message = "Invalid input provided";
		final NotValidInputException exception = new NotValidInputException(this.getClass(), message);

		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getMessage().contains(message));
		Assert.assertNotEquals(exception.getMessage(), "");
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testNotValidInputExceptionWithExceptionType() {
		final String message = "Invalid input";
		final NotValidInputException exception = new NotValidInputException(this.getClass(), message, ExceptionType.WARNING);

		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getMessage().contains(message));
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testNotValidInputExceptionIsThrowable() {
		final NotValidInputException exception = new NotValidInputException(this.getClass(), "Test");

		Assert.assertNotNull(exception);
		Assert.assertTrue(exception instanceof Exception);
		Assert.assertTrue(exception instanceof RuntimeException);
	}

	@Test
	public void testTournamentInvalidExceptionWithBasicConstructor() {
		final String message = "Tournament is invalid";
		final TournamentInvalidException exception = new TournamentInvalidException(this.getClass(), message);

		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getMessage().contains(message));
		Assert.assertNotEquals(exception.getMessage(), "");
	}

	@Test
	public void testTournamentInvalidExceptionWithExceptionType() {
		final String message = "Invalid tournament";
		final TournamentInvalidException exception = new TournamentInvalidException(this.getClass(), message, ExceptionType.SEVERE);

		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getMessage().contains(message));
	}

	@Test
	public void testNameAlreadyInUseException() {
		final String name = "TestName";
		final NameAlreadyInUseException exception = new NameAlreadyInUseException(this.getClass(), name);

		Assert.assertNotNull(exception);
		Assert.assertTrue(exception.getMessage().contains(name) || exception.getMessage().contains("already"));
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testClubNotFoundException() {
		final ClubNotFoundException exception = new ClubNotFoundException(this.getClass(), "Club not found");

		Assert.assertNotNull(exception);
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testTeamNotFoundException() {
		final TeamNotFoundException exception = new TeamNotFoundException(this.getClass(), "Team not found");

		Assert.assertNotNull(exception);
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testFightNotFoundException() {
		final FightNotFoundException exception = new FightNotFoundException(this.getClass(), "Fight not found");

		Assert.assertNotNull(exception);
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testLevelNotFinishedException() {
		final LevelNotFinishedException exception = new LevelNotFinishedException(this.getClass(), "Level not finished");

		Assert.assertNotNull(exception);
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testUnexpectedValueException() {
		final UnexpectedValueException exception = new UnexpectedValueException(this.getClass(), "Unexpected value");

		Assert.assertNotNull(exception);
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testSenbatsuTournamentFightsException() {
		final SenbatsuTournamentFightsException exception = new SenbatsuTournamentFightsException(this.getClass(), "Senbatsu error");

		Assert.assertNotNull(exception);
		Assert.assertFalse(exception.getMessage().isEmpty());
	}

	@Test
	public void testNotValidInputExceptionWithDifferentMessages() {
		final String message1 = "Error message 1";
		final String message2 = "Error message 2";

		final NotValidInputException exception1 = new NotValidInputException(this.getClass(), message1);
		final NotValidInputException exception2 = new NotValidInputException(this.getClass(), message2);

		Assert.assertNotNull(exception1);
		Assert.assertNotNull(exception2);
		Assert.assertNotEquals(exception1.getMessage(), exception2.getMessage());
		Assert.assertTrue(exception1.getMessage().contains(message1));
		Assert.assertTrue(exception2.getMessage().contains(message2));
	}

	@Test
	public void testExceptionsCanBeCaught() {
		try {
			throw new NotValidInputException(this.getClass(), "Test exception");
		} catch (final NotValidInputException e) {
			Assert.assertNotNull(e);
			Assert.assertTrue(e.getMessage().contains("Test exception"));
		}
	}

	@Test
	public void testExceptionsCanBeCaughtAsException() {
		try {
			throw new TournamentInvalidException(this.getClass(), "Tournament error");
		} catch (final Exception e) {
			Assert.assertNotNull(e);
			Assert.assertFalse(e.getMessage().isEmpty());
		}
	}

	@Test
	public void testExceptionPreservesMessage() {
		final String originalMessage = "Original test message";
		final NotValidInputException exception = new NotValidInputException(this.getClass(), originalMessage);

		Assert.assertTrue(exception.getMessage().contains(originalMessage));
		Assert.assertFalse(exception.getMessage().isEmpty());
		Assert.assertNotEquals(exception.getMessage(), "");
	}

	@Test
	public void testExceptionWithDifferentExceptionTypes() {
		final String message = "Test message";

		final NotValidInputException warningException = new NotValidInputException(this.getClass(), message, ExceptionType.WARNING);
		final NotValidInputException infoException = new NotValidInputException(this.getClass(), message, ExceptionType.INFO);

		Assert.assertNotNull(warningException);
		Assert.assertNotNull(infoException);
		Assert.assertTrue(warningException.getMessage().contains(message));
		Assert.assertTrue(infoException.getMessage().contains(message));
	}

	@Test
	public void testCsvExceptionsExposeSpecificFields() {
		final InvalidCsvFieldException fieldException = new InvalidCsvFieldException(this.getClass(), "bad header", "name");
		final InvalidCsvRowException rowException = new InvalidCsvRowException(this.getClass(), "bad row", 7);

		Assert.assertEquals(fieldException.getHeader(), "name");
		Assert.assertEquals(rowException.getNumberOfFailedRows(), 7);
		Assert.assertTrue(fieldException.getMessage().contains("bad header"));
		Assert.assertTrue(rowException.getMessage().contains("bad row"));
	}

	@Test
	public void testNoContentAndCustomFightExceptions() {
		final NoContentException noContentException = new NoContentException(this.getClass(), "empty");
		final CustomTournamentFightsException customTournamentFightsException =
				new CustomTournamentFightsException(this.getClass(), "custom");
		final DatabaseException databaseException = new DatabaseException(this.getClass(), "db");
		final DataInputException dataInputException = new DataInputException(this.getClass(), "input");

		Assert.assertEquals(noContentException.getMessage(), "empty");
		Assert.assertTrue(customTournamentFightsException.getMessage().contains("custom"));
		Assert.assertTrue(databaseException.getMessage().contains("db"));
		Assert.assertTrue(dataInputException.getMessage().contains("input"));
	}

	@Test
	public void testDefaultConstructorsFromNotFoundHierarchy() {
		Assert.assertFalse(new DuelNotFoundException(this.getClass()).getMessage().isEmpty());
		Assert.assertFalse(new GroupNotFoundException(this.getClass()).getMessage().isEmpty());
		Assert.assertFalse(new ParticipantNotFoundException(this.getClass()).getMessage().isEmpty());
		Assert.assertFalse(new RoleNotFoundException(this.getClass()).getMessage().isEmpty());
		Assert.assertFalse(new TokenExpiredException(this.getClass()).getMessage().isEmpty());
		Assert.assertFalse(new TournamentNotFoundException(this.getClass()).getMessage().isEmpty());
		Assert.assertFalse(new UserNotFoundException(this.getClass()).getMessage().isEmpty());
	}

	@Test
	public void testThrowableConstructorsAndSpecializedExceptions() {
		final Throwable cause = new IllegalStateException("boom");

		Assert.assertNotNull(new InvalidGroupException(this.getClass(), cause));
		Assert.assertNotNull(new InvalidFightException(this.getClass(), cause));
		Assert.assertNotNull(new InvalidExtraPropertyException(this.getClass(), cause));
		Assert.assertNotNull(new ValidateBadRequestException(this.getClass(), cause));
		Assert.assertNotNull(new ClubNotFoundException(this.getClass(), cause));
		Assert.assertNotNull(new TeamNotFoundException(this.getClass(), cause));

		final TournamentFinishedException tournamentFinishedException =
				new TournamentFinishedException(this.getClass(), "finished");
		final DuplicatedUserException duplicatedUserException = new DuplicatedUserException(this.getClass(), "dup");
		final InvalidChallengeDistanceException invalidChallengeDistanceException =
				new InvalidChallengeDistanceException(this.getClass(), "distance");

		Assert.assertTrue(tournamentFinishedException.getMessage().contains("finished"));
		Assert.assertTrue(duplicatedUserException.getMessage().contains("dup"));
		Assert.assertTrue(invalidChallengeDistanceException.getMessage().contains("distance"));
	}
}


