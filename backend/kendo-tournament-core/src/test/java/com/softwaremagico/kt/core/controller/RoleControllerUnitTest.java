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
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.RoleConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"roleControllerUnitTests"})
public class RoleControllerUnitTest {

    private RoleProvider roleProvider;
    private ParticipantConverter participantConverter;
    private TournamentConverter tournamentConverter;
    private RoleController roleController;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        roleProvider = mock(RoleProvider.class);
        participantConverter = mock(ParticipantConverter.class);
        tournamentConverter = mock(TournamentConverter.class);
        roleController = new RoleController(roleProvider, mock(RoleConverter.class), tournamentConverter,
                mock(TournamentProvider.class), participantConverter);
    }

    @Test
    public void deletesOnlyTheRequestedRoleType() {
        final ParticipantDTO participantDTO = new ParticipantDTO();
        final TournamentDTO tournamentDTO = new TournamentDTO();
        final Participant participant = new Participant();
        final Tournament tournament = new Tournament();
        when(participantConverter.reverse(participantDTO)).thenReturn(participant);
        when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);

        roleController.delete(participantDTO, tournamentDTO, RoleType.REFEREE);

        verify(roleProvider).delete(participant, tournament, RoleType.REFEREE);
    }
}
