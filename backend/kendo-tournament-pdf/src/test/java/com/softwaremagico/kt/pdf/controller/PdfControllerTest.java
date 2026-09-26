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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
        this.messageSource = mock(MessageSource.class);
        when(this.messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        this.roleController = mock(RoleController.class);
        this.teamController = mock(TeamController.class);
        this.groupController = mock(GroupController.class);
        this.tournamentImageController = mock(TournamentImageController.class);
        this.participantImageController = mock(ParticipantImageController.class);
        this.tournamentExtraPropertyController = mock(TournamentExtraPropertyController.class);
        this.participantProvider = mock(ParticipantProvider.class);
        this.participantConverter = mock(ParticipantConverter.class);
        this.qrController = mock(QrController.class);
        this.pdfController = new PdfController(this.messageSource, this.roleController, this.teamController,
                this.groupController, this.tournamentImageController, this.participantImageController,
                this.tournamentExtraPropertyController, this.participantProvider, this.participantConverter,
                this.qrController);
    }

    private ParticipantDTO participant(Integer id) {
        final ClubDTO club = new ClubDTO("Club", "City");
        final ParticipantDTO participantDTO = new ParticipantDTO("card", "Name", "Lastname", club);
        participantDTO.setId(id);
        return participantDTO;
    }

    @Test
    public void generateTournamentAccreditations_withNoMatchingRoles_expectNoContentException() {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        when(this.roleController.getForAccreditations(eq(tournament), any(), any())).thenReturn(List.of());

        assertThrows(NoContentException.class, () -> this.pdfController
                .generateTournamentAccreditations(Locale.getDefault(), tournament, true, "user", "session"));
    }

    @Test
    public void generateTournamentAccreditations_withMatchingRolesAndAllImages_expectSuccessAndUpdate()
            throws NoContentException {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = this.participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(this.roleController.getForAccreditations(eq(tournament), any(), any())).thenReturn(List.of(role));
        when(this.participantImageController.get(ArgumentMatchers.<ParticipantDTO>anyList())).thenReturn(List.of());

        final TournamentImageDTO banner = new TournamentImageDTO();
        banner.setData(new byte[]{1});
        final TournamentImageDTO accreditation = new TournamentImageDTO();
        accreditation.setData(new byte[]{2});
        final TournamentImageDTO photo = new TournamentImageDTO();
        photo.setData(new byte[]{3});
        when(this.tournamentImageController.get(tournament, TournamentImageType.ACCREDITATION))
                .thenReturn(accreditation);
        when(this.tournamentImageController.get(tournament, TournamentImageType.BANNER)).thenReturn(banner);
        when(this.tournamentImageController.get(tournament, TournamentImageType.PHOTO)).thenReturn(photo);

        assertNotNull(this.pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, true, "user",
                "session"));

        verify(this.roleController, times(1)).updateAll(List.of(role), "user", "session");
    }

    @Test
    public void generateTournamentAccreditations_withoutImages_expectNullImageAssets() throws NoContentException {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = this.participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(this.roleController.getForAccreditations(eq(tournament), any(), any())).thenReturn(List.of(role));
        when(this.participantImageController.get(ArgumentMatchers.<ParticipantDTO>anyList())).thenReturn(List.of());
        when(this.tournamentImageController.get(any(TournamentDTO.class), any())).thenReturn(null);

        assertNotNull(this.pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, false,
                "user", "session", RoleType.COMPETITOR));
    }

    @Test
    public void generateTournamentAccreditations_singleParticipantWithNullType_expectDefaultCompetitorType() {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = this.participant(null);
        when(this.tournamentImageController.get(any(TournamentDTO.class), any())).thenReturn(null);

        assertNotNull(this.pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, participant,
                (RoleType) null, "user", "session"));
    }

    @Test
    public void generateTournamentAccreditations_singleParticipantWithExistingRoleId_expectRoleUpdated() {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = this.participant(5);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.REFEREE);
        role.setId(10);
        when(this.tournamentImageController.get(any(TournamentDTO.class), any())).thenReturn(null);
        when(this.participantImageController.get(ArgumentMatchers.<ParticipantDTO>anyList())).thenReturn(List.of());

        assertNotNull(this.pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, participant,
                role, "user", "session"));

        verify(this.roleController, times(1)).update(role, "user", "session");
    }

    @Test
    public void generateTournamentAccreditations_singleParticipantWithoutId_expectNoImageLookupAndNoRoleUpdate() {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        final ParticipantDTO participant = this.participant(null);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);
        when(this.tournamentImageController.get(any(TournamentDTO.class), any())).thenReturn(null);

        assertNotNull(this.pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, participant,
                role, "user", "session"));

        verify(this.roleController, never()).update(any(), any(), any());
        verify(this.participantImageController, never()).get(ArgumentMatchers.<ParticipantDTO>anyList());
    }

    @Test
    public void generateTournamentDiplomas_withNoMatchingRoles_expectNoContentException() {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        when(this.roleController.getForDiplomas(eq(tournament), any(), any())).thenReturn(List.of());

        assertThrows(NoContentException.class,
                () -> this.pdfController.generateTournamentDiplomas(tournament, true, "user", "session"));
    }

    @Test
    public void generateTournamentDiplomas_withDiplomaImageAndValidNameHeight_expectParsedPosition()
            throws NoContentException {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        tournament.setId(7);
        final ParticipantDTO participant = this.participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(this.roleController.getForDiplomas(eq(tournament), any(), any())).thenReturn(List.of(role));
        final TournamentImageDTO diploma = new TournamentImageDTO();
        diploma.setData(new byte[]{1});
        when(this.tournamentImageController.get(tournament, TournamentImageType.DIPLOMA)).thenReturn(diploma);

        final TournamentExtraPropertyDTO nameHeight = new TournamentExtraPropertyDTO(tournament,
                TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT, "0.3");
        when(this.tournamentExtraPropertyController.getByTournamentAndProperty(7,
                TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT)).thenReturn(nameHeight);

        assertNotNull(this.pdfController.generateTournamentDiplomas(tournament, true, "user", "session"));
        verify(this.roleController, times(1)).updateAll(List.of(role), "user", "session");
    }

    @Test
    public void generateTournamentDiplomas_withInvalidNameHeightValue_expectDefaultPosition()
            throws NoContentException {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        tournament.setId(8);
        final ParticipantDTO participant = this.participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(this.roleController.getForDiplomas(eq(tournament), any(), any())).thenReturn(List.of(role));
        when(this.tournamentImageController.get(tournament, TournamentImageType.DIPLOMA)).thenReturn(null);

        final TournamentExtraPropertyDTO nameHeight = new TournamentExtraPropertyDTO(tournament,
                TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT, "not-a-number");
        when(this.tournamentExtraPropertyController.getByTournamentAndProperty(8,
                TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT)).thenReturn(nameHeight);

        assertNotNull(this.pdfController.generateTournamentDiplomas(tournament, true, "user", "session"));
    }

    @Test
    public void generateTournamentDiplomas_withoutExtraProperty_expectDefaultPosition() throws NoContentException {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        tournament.setId(9);
        final ParticipantDTO participant = this.participant(1);
        final RoleDTO role = new RoleDTO(tournament, participant, RoleType.COMPETITOR);

        when(this.roleController.getForDiplomas(eq(tournament), any(), any())).thenReturn(List.of(role));
        when(this.tournamentImageController.get(tournament, TournamentImageType.DIPLOMA)).thenReturn(null);
        when(this.tournamentExtraPropertyController.getByTournamentAndProperty(9,
                TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT)).thenReturn(null);

        assertNotNull(this.pdfController.generateTournamentDiplomas(tournament, true, "user", "session"));
    }

    @Test
    public void generateTournamentDiploma_withNullTournament_expectDefaultPosition() {
        this.setUp();
        final ParticipantDTO participant = this.participant(1);

        assertNotNull(this.pdfController.generateTournamentDiploma(null, participant));
    }

    @Test
    public void generateTournamentQr_expectDelegationToQrController() {
        this.setUp();
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, TournamentType.LEAGUE);
        tournament.setId(3);
        final QrCodeDTO qrCodeDTO = new QrCodeDTO();
        qrCodeDTO.setData(new byte[]{1});
        when(this.qrController.generateGuestQrCodeForTournamentFights(3, null, false)).thenReturn(qrCodeDTO);

        assertNotNull(this.pdfController.generateTournamentQr(Locale.getDefault(), tournament, null));
    }
}
