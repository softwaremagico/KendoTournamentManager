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

import com.softwaremagico.kt.core.controller.ParticipantImageController;
import com.softwaremagico.kt.core.controller.TournamentImageController;
import com.softwaremagico.kt.core.controller.models.ParticipantImageDTO;
import com.softwaremagico.kt.core.controller.models.TournamentImageDTO;
import com.softwaremagico.kt.persistence.values.ImageCompression;
import com.softwaremagico.kt.persistence.values.TournamentImageType;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@Test(groups = "fileServices")
public class FileServicesTest {

    @Mock
    private ParticipantImageController mockParticipantImageController;

    @Mock
    private TournamentImageController mockTournamentImageController;

    @Mock
    private Authentication mockAuthentication;

    @Mock
    private HttpServletRequest mockRequest;

    private FileServices services;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        services = new FileServices(mockParticipantImageController, mockTournamentImageController);
        when(mockAuthentication.getName()).thenReturn("tester");
    }

    @Test
    public void upload_participantMultipart_expectDelegationWithAuthenticatedUser() {
        final MultipartFile file = new MockMultipartFile("file", new byte[]{1, 2, 3});
        final ParticipantImageDTO expected = new ParticipantImageDTO();
        when(mockParticipantImageController.add(eq(file), eq(7), eq("tester"))).thenReturn(expected);

        final ParticipantImageDTO result = services.upload(file, 7, mockAuthentication, mockRequest);

        assertSame(result, expected);
    }

    @Test
    public void uploadParticipantPicture_expectDelegation() {
        final ParticipantImageDTO dto = new ParticipantImageDTO();
        final ParticipantImageDTO expected = new ParticipantImageDTO();
        when(mockParticipantImageController.add(dto, "tester")).thenReturn(expected);

        final ParticipantImageDTO result = services.uploadParticipantPicture(dto, mockAuthentication, mockRequest);

        assertSame(result, expected);
    }

    @Test
    public void getParticipantImage_expectDelegation() {
        final ParticipantImageDTO expected = new ParticipantImageDTO();
        when(mockParticipantImageController.getByParticipantId(9)).thenReturn(expected);

        final ParticipantImageDTO result = services.getParticipantImage(9, mockRequest);

        assertSame(result, expected);
    }

    @Test
    public void deleteParticipantImage_expectDelegation() {
        services.deleteParticipantImage(9, mockRequest);
        verify(mockParticipantImageController).deleteByParticipantId(9);
    }

    @Test
    public void uploadTournamentImageDto_expectDelegation() {
        final TournamentImageDTO dto = new TournamentImageDTO();
        final TournamentImageDTO expected = new TournamentImageDTO();
        when(mockTournamentImageController.add(dto, "tester")).thenReturn(expected);

        final TournamentImageDTO result = services.upload(dto, mockAuthentication, mockRequest);

        assertSame(result, expected);
    }

    @Test
    public void uploadTournamentImageMultipart_expectDelegation() {
        final MultipartFile file = new MockMultipartFile("file", new byte[]{4, 5});
        final TournamentImageDTO expected = new TournamentImageDTO();
        when(mockTournamentImageController.add(eq(file), eq(3), eq(TournamentImageType.BANNER),
                eq(ImageCompression.PNG), eq("tester"))).thenReturn(expected);

        final TournamentImageDTO result = services.uploadTournamentImage(file, 3, TournamentImageType.BANNER,
                ImageCompression.PNG, mockAuthentication, mockRequest);

        assertSame(result, expected);
    }

    @Test
    public void getTournamentImage_expectDelegation() {
        final TournamentImageDTO expected = new TournamentImageDTO();
        when(mockTournamentImageController.get(3, TournamentImageType.BANNER)).thenReturn(expected);

        final TournamentImageDTO result = services.getTournamentImage(3, TournamentImageType.BANNER, mockRequest);

        assertSame(result, expected);
    }

    @Test
    public void deleteTournamentImage_expectDelegation() {
        services.deleteTournamentImage(3, TournamentImageType.BANNER, mockRequest);
        verify(mockTournamentImageController).deleteByTournamentId(3, TournamentImageType.BANNER);
    }
}

