package com.softwaremagico.kt.rest.services;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.controller.AchievementController;
import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.rest.exceptions.InvalidRequestException;
import com.softwaremagico.kt.rest.security.KendoSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

@Test(groups = {"restServicesUnit"})
public class AchievementServicesTest {

    @Mock
    private AchievementController achievementController;

    @Mock
    private KendoSecurityService kendoSecurityService;

    @Mock
    private ParticipantProvider participantProvider;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    private AchievementServices achievementServices;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        achievementServices = new AchievementServices(achievementController, kendoSecurityService, participantProvider);
    }

    @Test
    public void shouldGetParticipantAchievementsWhenAuthenticationIsNull() {
        final AchievementDTO dto = Mockito.mock(AchievementDTO.class);
        when(achievementController.getParticipantAchievements(12)).thenReturn(List.of(dto));

        final List<AchievementDTO> result = achievementServices.getParticipantAchievements(12, null, request);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), dto);
    }

    @Test
    public void shouldGetParticipantAchievementsWhenAuthenticatedUserIsNotParticipantToken() {
        final AchievementDTO dto = Mockito.mock(AchievementDTO.class);
        when(authentication.getName()).thenReturn("viewerUser");
        when(participantProvider.findByTokenUsername("viewerUser")).thenReturn(Optional.empty());
        when(achievementController.getParticipantAchievements(34)).thenReturn(List.of(dto));

        final List<AchievementDTO> result = achievementServices.getParticipantAchievements(34, authentication, request);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), dto);
    }

    @Test
    public void shouldGetParticipantAchievementsWhenParticipantMatchesAuthentication() {
        final AchievementDTO dto = Mockito.mock(AchievementDTO.class);
        final Participant participant = Mockito.mock(Participant.class);

        when(authentication.getName()).thenReturn("participantUser");
        when(participantProvider.findByTokenUsername("participantUser")).thenReturn(Optional.of(participant));
        when(participant.getId()).thenReturn(55);
        when(achievementController.getParticipantAchievements(55)).thenReturn(List.of(dto));

        final List<AchievementDTO> result = achievementServices.getParticipantAchievements(55, authentication, request);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), dto);
    }

    @Test
    public void shouldThrowInvalidRequestWhenParticipantTokenTriesToAccessOtherParticipant() {
        final Participant participant = Mockito.mock(Participant.class);
        when(authentication.getName()).thenReturn("participantUser");
        when(participantProvider.findByTokenUsername("participantUser")).thenReturn(Optional.of(participant));
        when(participant.getId()).thenReturn(77);

        final InvalidRequestException exception = expectThrows(InvalidRequestException.class,
                () -> achievementServices.getParticipantAchievements(88, authentication, request));

        assertEquals(exception.getMessage(), "User 'participantUser' is trying to access to statistics from user '88'.");
    }

    @Test
    public void shouldDelegateGetTournamentAchievements() {
        final AchievementDTO dto = Mockito.mock(AchievementDTO.class);
        when(achievementController.getTournamentAchievements(9)).thenReturn(List.of(dto));

        final List<AchievementDTO> result = achievementServices.getTournamentAchievements(9, request);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), dto);
    }

    @Test
    public void shouldDelegateRegenerateTournamentAchievements() {
        final AchievementDTO dto = Mockito.mock(AchievementDTO.class);
        when(achievementController.regenerateAchievements(10)).thenReturn(List.of(dto));

        final List<AchievementDTO> result = achievementServices.regenerateTournamentAchievements(10, request);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), dto);
    }

    @Test
    public void shouldDelegateRegenerateAllTournamentAchievements() {
        final AchievementDTO dto = Mockito.mock(AchievementDTO.class);
        when(achievementController.regenerateAllAchievements()).thenReturn(List.of(dto));

        final List<AchievementDTO> result = achievementServices.regenerateAllTournamentAchievements(request);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), dto);
    }

    @Test
    public void shouldDelegateCountByTypeMap() {
        final Map<AchievementType, Map<AchievementGrade, Integer>> expected = Map.of(
                AchievementType.THE_WINNER,
                Map.of(AchievementGrade.BRONZE, 3, AchievementGrade.SILVER, 2)
        );
        when(achievementController.getAchievementsCount()).thenReturn(expected);

        final Map<AchievementType, Map<AchievementGrade, Integer>> result = achievementServices.countByType(request);

        assertSame(result, expected);
        assertEquals(result.get(AchievementType.THE_WINNER).get(AchievementGrade.BRONZE).intValue(), 3);
    }

    @Test
    public void shouldDelegateCountByAchievementType() {
        when(achievementController.countAchievements(AchievementType.THE_WINNER)).thenReturn(42);

        final long count = achievementServices.count(AchievementType.THE_WINNER, request);

        assertEquals(count, 42L);
    }
}

