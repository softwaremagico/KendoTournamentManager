package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TemporalToken;
import com.softwaremagico.kt.core.controller.models.Token;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.exceptions.TokenExpiredException;
import com.softwaremagico.kt.core.exceptions.UserNotFoundException;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@Test(groups = "scoreTests")
public class ParticipantControllerTest {

	@Mock
	private ParticipantProvider provider;
	@Mock
	private ParticipantConverter converter;

	private ParticipantController controller;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
        this.controller = new ParticipantController(this.provider, this.converter);
	}

	@Test
	public void shouldGenerateTemporalToken() {
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final Participant participant = new Participant();
		final TemporalToken temporalToken = new TemporalToken();

		when(this.converter.reverse(participantDTO)).thenReturn(participant);
		when(this.provider.generateTemporalToken(participant)).thenReturn(temporalToken);

		assertSame(this.controller.generateTemporalToken(participantDTO), temporalToken);
	}

	@Test
	public void shouldGetByUserNameWhenValid() {
		final Participant participant = new Participant();
		participant.setId(7);
		final ParticipantDTO participantDTO = new ParticipantDTO();

		when(this.provider.get(7)).thenReturn(Optional.of(participant));
		when(this.converter.convert(any())).thenReturn(participantDTO);

		assertSame(this.controller.getByUserName("7_token"), participantDTO);
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenUserNameDoesNotContainSeparator() {
        this.controller.getByUserName("invalidusername");
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenUserNameHasInvalidNumberFormat() {
        this.controller.getByUserName("abc_token");
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenParticipantNotFoundByUserName() {
		when(this.provider.get(9)).thenReturn(Optional.empty());
        this.controller.getByUserName("9_token");
	}

	@Test
	public void shouldGenerateFromTokenWhenNotExpired() {
		final Participant participant = mock(Participant.class);
		final Participant tokenizedParticipant = mock(Participant.class);
		final ParticipantDTO participantDTO = new ParticipantDTO();

		when(this.provider.findByTemporalToken("temp")).thenReturn(Optional.of(participant));
		when(participant.getTemporalTokenExpiration())
				.thenReturn(LocalDateTime.now(ZoneId.systemDefault()).plusDays(1));
		when(this.provider.generateToken(participant)).thenReturn(tokenizedParticipant);
		when(tokenizedParticipant.getToken()).thenReturn("jwt-token");
		when(this.converter.convert(any())).thenReturn(participantDTO);

		final Token token = this.controller.generateFromToken("temp");

		assertEquals(token.getContent(), "jwt-token");
		assertSame(token.getParticipant(), participantDTO);
		verify(participant, times(1)).setTemporalToken(null);
		verify(participant, times(1)).setTemporalTokenExpiration(null);
		verify(this.provider, times(1)).save(participant);
	}

	@Test(expectedExceptions = TokenExpiredException.class)
	public void shouldThrowWhenTokenIsExpired() {
		final Participant participant = mock(Participant.class);

		when(this.provider.findByTemporalToken("temp")).thenReturn(Optional.of(participant));
		when(participant.getTemporalTokenExpiration())
				.thenReturn(LocalDateTime.now(ZoneId.systemDefault()).minusDays(1));

		try {
            this.controller.generateFromToken("temp");
		} finally {
			verify(this.provider, times(1)).save(participant);
		}
	}

	@Test(expectedExceptions = UserNotFoundException.class)
	public void shouldThrowWhenTemporalTokenNotFound() {
		when(this.provider.findByTemporalToken("missing")).thenReturn(Optional.empty());
        this.controller.generateFromToken("missing");
	}

	@Test
	public void shouldReturnYourWorstNightmare() {
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final Participant participant = new Participant();
		final ParticipantDTO resultDTO = new ParticipantDTO();

		when(this.converter.reverse(participantDTO)).thenReturn(participant);
		when(this.provider.getYourWorstNightmare(participant)).thenReturn(List.of(participant));
		when(this.converter.convertAll(any())).thenReturn(List.of(resultDTO));

		assertSame(this.controller.getYourWorstNightmare(participantDTO).get(0), resultDTO);
	}

	@Test
	public void shouldReturnYouAreTheWorstNightmareOf() {
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final Participant participant = new Participant();
		final ParticipantDTO resultDTO = new ParticipantDTO();

		when(this.converter.reverse(participantDTO)).thenReturn(participant);
		when(this.provider.getYouAreTheWorstNightmareOf(participant)).thenReturn(List.of(participant));
		when(this.converter.convertAll(any())).thenReturn(List.of(resultDTO));

		assertSame(this.controller.getYouAreTheWorstNightmareOf(participantDTO).get(0), resultDTO);
	}
}
