package com.softwaremagico.kt.pdf.controller;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
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

import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantImageController;
import com.softwaremagico.kt.core.controller.QrController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.TournamentImageController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.controller.models.QrCodeDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.controller.models.TournamentImageDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.exceptions.NoContentException;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.ArgumentMatchers;
import org.springframework.context.MessageSource;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

@Test(groups = {"listsUnitTests"})
public class PdfControllerTest {

    private RoleController roleController;
    private TeamController teamController;
    private GroupController groupController;
    private TournamentImageController tournamentImageController;
    private ParticipantImageController participantImageController;
    private TournamentExtraPropertyController tournamentExtraPropertyController;
    private ParticipantProvider participantProvider;
    private ParticipantConverter participantConverter;
    private QrController qrController;
    private MessageSource messageSource;
    private PdfController pdfController;

    private void setUp() {
        messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        roleController = mock(RoleController.class);
        teamController = mock(TeamController.class);
        groupController = mock(GroupController.class);
        tournamentImageController = mock(TournamentImageController.class);
        participantImageController = mock(ParticipantImageController.class);
        tournamentExtraPropertyController = mock(TournamentExtraPropertyController.class);
        participantProvider = mock(ParticipantProvider.class);
        participantConverter = mock(ParticipantConverter.class);
        qrController = mock(QrController.class);
        pdfController = new PdfController(messageSource, roleController, teamController, groupController, tournamentImageController,
                participantImageController, tournamentExtraPropertyController, participantProvider, participantConverter, qrController);
    }

    private ParticipantDTO participant(Integer id) {
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participantDTO = new ParticipantDTO("card", "Name", "Lastname", club);
        participantDTO.setId(id);
        return participantDTO;
    }

    @Test
    public void generateTournamentAccreditations_withNoMatchingRoles_expectNoContentException() {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        when(roleController.getForAccreditations(eq(tournament), any(), any())).thenReturn(List.of());

        assertThrows(NoContentException.class,
                () -> pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, true, "user", "session"));
    }

    @Test
    public void generateTournamentAccreditations_withMatchingRolesAndAllImages_expectSuccessAndUpdate() throws NoContentException {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(roleController.getForAccreditations(eq(tournament), any(), any())).thenReturn(List.of(role));
        when(participantImageController.get(ArgumentMatchers.<ParticipantDTO>anyList())).thenReturn(List.of());

        final TournamentImageDTO banner = new TournamentImageDTO();
        banner.setData(new byte[]{1});
        final TournamentImageDTO accreditation = new TournamentImageDTO();
        accreditation.setData(new byte[]{2});
        final TournamentImageDTO photo = new TournamentImageDTO();
        photo.setData(new byte[]{3});
        when(tournamentImageController.get(tournament, TournamentImageType.ACCREDITATION)).thenReturn(accreditation);
        when(tournamentImageController.get(tournament, TournamentImageType.BANNER)).thenReturn(banner);
        when(tournamentImageController.get(tournament, TournamentImageType.PHOTO)).thenReturn(photo);

        assertNotNull(pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, true, "user", "session"));

        verify(roleController, times(1)).updateAll(List.of(role), "user", "session");
    }

    @Test
    public void generateTournamentAccreditations_withoutImages_expectNullImageAssets() throws NoContentException {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(roleController.getForAccreditations(eq(tournament), any(), any())).thenReturn(List.of(role));
        when(participantImageController.get(ArgumentMatchers.<ParticipantDTO>anyList())).thenReturn(List.of());
        when(tournamentImageController.get(any(TournamentDTO.class), any())).thenReturn(null);

        assertNotNull(pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, false, "user", "session", RoleType.COMPETITOR));
    }

    @Test
    public void generateTournamentAccreditations_singleParticipantWithNullType_expectDefaultCompetitorType() {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = participant(null);
        when(tournamentImageController.get(any(TournamentDTO.class), any())).thenReturn(null);

        assertNotNull(pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, participant, (RoleType) null, "user", "session"));
    }

    @Test
    public void generateTournamentAccreditations_singleParticipantWithExistingRoleId_expectRoleUpdated() {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = participant(5);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.REFEREE);
        role.setId(10);
        when(tournamentImageController.get(any(TournamentDTO.class), any())).thenReturn(null);
        when(participantImageController.get(ArgumentMatchers.<ParticipantDTO>anyList())).thenReturn(List.of());

        assertNotNull(pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, participant, role, "user", "session"));

        verify(roleController, times(1)).update(role, "user", "session");
    }

    @Test
    public void generateTournamentAccreditations_singleParticipantWithoutId_expectNoImageLookupAndNoRoleUpdate() {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = participant(null);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);
        when(tournamentImageController.get(any(TournamentDTO.class), any())).thenReturn(null);

        assertNotNull(pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, participant, role, "user", "session"));

        verify(roleController, never()).update(any(), any(), any());
        verify(participantImageController, never()).get(ArgumentMatchers.<ParticipantDTO>anyList());
    }

    @Test
    public void generateTournamentDiplomas_withNoMatchingRoles_expectNoContentException() {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        when(roleController.getForDiplomas(eq(tournament), any(), any())).thenReturn(List.of());

        assertThrows(NoContentException.class, () -> pdfController.generateTournamentDiplomas(tournament, true, "user", "session"));
    }

    @Test
    public void generateTournamentDiplomas_withDiplomaImageAndValidNameHeight_expectParsedPosition() throws NoContentException {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        tournament.setId(7);
        final ParticipantDTO participant = participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(roleController.getForDiplomas(eq(tournament), any(), any())).thenReturn(List.of(role));
        final TournamentImageDTO diploma = new TournamentImageDTO();
        diploma.setData(new byte[]{1});
        when(tournamentImageController.get(tournament, TournamentImageType.DIPLOMA)).thenReturn(diploma);

        final TournamentExtraPropertyDTO nameHeight = new TournamentExtraPropertyDTO(tournament, TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT, "0.3");
        when(tournamentExtraPropertyController.getByTournamentAndProperty(7, TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT)).thenReturn(nameHeight);

        assertNotNull(pdfController.generateTournamentDiplomas(tournament, true, "user", "session"));
        verify(roleController, times(1)).updateAll(List.of(role), "user", "session");
    }

    @Test
    public void generateTournamentDiplomas_withInvalidNameHeightValue_expectDefaultPosition() throws NoContentException {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        tournament.setId(8);
        final ParticipantDTO participant = participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(roleController.getForDiplomas(eq(tournament), any(), any())).thenReturn(List.of(role));
        when(tournamentImageController.get(tournament, TournamentImageType.DIPLOMA)).thenReturn(null);

        final TournamentExtraPropertyDTO nameHeight = new TournamentExtraPropertyDTO(tournament, TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT,
                "not-a-number");
        when(tournamentExtraPropertyController.getByTournamentAndProperty(8, TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT)).thenReturn(nameHeight);

        assertNotNull(pdfController.generateTournamentDiplomas(tournament, true, "user", "session"));
    }

    @Test
    public void generateTournamentDiplomas_withoutExtraProperty_expectDefaultPosition() throws NoContentException {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        tournament.setId(9);
        final ParticipantDTO participant = participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(roleController.getForDiplomas(eq(tournament), any(), any())).thenReturn(List.of(role));
        when(tournamentImageController.get(tournament, TournamentImageType.DIPLOMA)).thenReturn(null);
        when(tournamentExtraPropertyController.getByTournamentAndProperty(9, TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT)).thenReturn(null);

        assertNotNull(pdfController.generateTournamentDiplomas(tournament, true, "user", "session"));
    }

    @Test
    public void generateTournamentDiploma_withNullTournament_expectDefaultPosition() {
        setUp();
        final ParticipantDTO participant = participant(1);

        assertNotNull(pdfController.generateTournamentDiploma(null, participant));
    }

    @Test
    public void generateTournamentQr_expectDelegationToQrController() {
        setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        tournament.setId(3);
        final QrCodeDTO qrCodeDTO = new QrCodeDTO();
        qrCodeDTO.setData(new byte[]{1});
        when(qrController.generateGuestQrCodeForTournamentFights(3, null, false)).thenReturn(qrCodeDTO);

        assertNotNull(pdfController.generateTournamentQr(Locale.getDefault(), tournament, null));
    }
}




