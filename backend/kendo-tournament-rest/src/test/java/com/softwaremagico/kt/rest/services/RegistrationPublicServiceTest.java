package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.controller.RegistrationController;
import com.softwaremagico.kt.core.controller.models.RegistrationConfigurationDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationRequestDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationResponseDTO;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.security.RegistrationInvitation;
import com.softwaremagico.kt.core.security.RegistrationTicketProvider;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"registrationPublic"})
public class RegistrationPublicServiceTest {

    @Mock
    private RegistrationTicketProvider registrationTicketProvider;
    @Mock
    private TournamentProvider tournamentProvider;
    @Mock
    private RegistrationController registrationController;

    private RegistrationPublicService service;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RegistrationPublicService(registrationTicketProvider, tournamentProvider, registrationController);
    }

    @Test
    public void validInvitationUsesItsTenantAndClearsContext() {
        final Tournament tournament = tournament(7, 3);
        when(registrationTicketProvider.validateRegistrationTicket("ticket"))
                .thenReturn(new RegistrationInvitation(7, 3, 3));
        when(tournamentProvider.get(7)).thenReturn(Optional.of(tournament));
        when(registrationController.getConfiguration(7)).thenAnswer(invocation -> {
            Assert.assertEquals(TenantContext.getTenantId(), 3, "The invitation tenant must be set before repository access.");
            return new RegistrationConfigurationDTO();
        });
        TenantContext.setTenantId(99);

        service.getConfiguration("ticket");

        Assert.assertNull(TenantContext.getTenantId(), "Public requests must not leak a tenant context to the next request.");
    }

    @Test
    public void revokedInvitationIsRejectedAndClearsContext() {
        final Tournament tournament = tournament(7, 4);
        when(registrationTicketProvider.validateRegistrationTicket("old-ticket"))
                .thenReturn(new RegistrationInvitation(7, 3, 3));
        when(tournamentProvider.get(7)).thenReturn(Optional.of(tournament));
        TenantContext.setTenantId(99);

        Assert.expectThrows(BadRequestException.class, () -> service.getConfiguration("old-ticket"));
        Assert.assertNull(TenantContext.getTenantId());
    }

    @Test
    public void invalidInvitationIsRejectedWithoutTouchingRepositories() {
        when(registrationTicketProvider.validateRegistrationTicket("bad")).thenReturn(null);

        Assert.expectThrows(BadRequestException.class, () -> service.getConfiguration("bad"));
        verify(tournamentProvider, org.mockito.Mockito.never()).get(org.mockito.ArgumentMatchers.anyInt());
        Assert.assertNull(TenantContext.getTenantId());
    }

    @Test
    public void registerUsesTheTrustedTournamentFromTheInvitation() {
        final Tournament tournament = tournament(7, 1);
        final RegistrationRequestDTO request = new RegistrationRequestDTO();
        final RegistrationResponseDTO response = new RegistrationResponseDTO();
        when(registrationTicketProvider.validateRegistrationTicket("ticket"))
                .thenReturn(new RegistrationInvitation(7, 3, 1));
        when(tournamentProvider.get(7)).thenReturn(Optional.of(tournament));
        when(registrationController.register(7, request, "public-registration")).thenReturn(response);

        Assert.assertSame(service.register("ticket", request), response);
        verify(registrationController).register(7, request, "public-registration");
        Assert.assertNull(TenantContext.getTenantId());
    }

    private Tournament tournament(int id, int version) {
        final Tournament tournament = new Tournament("Spring Cup", 1, 1, TournamentType.LEAGUE, "organizer");
        tournament.setId(id);
        tournament.setRegistrationVersion(version);
        return tournament;
    }
}
