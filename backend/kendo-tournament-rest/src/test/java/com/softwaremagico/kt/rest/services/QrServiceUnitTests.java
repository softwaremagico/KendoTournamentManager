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

import com.softwaremagico.kt.core.controller.QrController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.QrCodeDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.pdf.EmptyPdfBodyException;
import com.softwaremagico.kt.pdf.InvalidXmlElementException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.pdf.qr.TournamentQr;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test(groups = "restServicesUnit")
public class QrServiceUnitTests {

    private QrController qrController;
    private PdfController pdfController;
    private TournamentController tournamentController;
    private QrService qrService;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        qrController = mock(QrController.class);
        pdfController = mock(PdfController.class);
        tournamentController = mock(TournamentController.class);
        qrService = new QrService(qrController, pdfController, tournamentController);
    }

    @Test
    public void shouldGenerateGuestQrJsonAndImage() {
        final QrCodeDTO qrCodeDTO = new QrCodeDTO();
        qrCodeDTO.setData(new byte[]{1, 2, 3});
        when(qrController.generateGuestQrCodeForTournamentFights(10, null, true)).thenReturn(qrCodeDTO);

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpServletRequest request = mock(HttpServletRequest.class);

        final QrCodeDTO dto = qrService.generateGuestQrCodeForTournamentFights(10, Optional.of(true), response, request);
        final byte[] image = qrService.generateGuestQrCodeForTournamentFightsImage(10, Optional.of(true), response, request);

        assertNotNull(dto);
        assertEquals(image, new byte[]{1, 2, 3});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldGenerateQrAttendanceImageAndSetHeaders() {
        final QrCodeDTO qrCodeDTO = new QrCodeDTO();
        qrCodeDTO.setData(new byte[]{9, 8});
        when(qrController.generateQrCode("abc", false)).thenReturn(qrCodeDTO);

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] bytes = qrService.generateQrForAttendanceImage("abc", Optional.empty(), response, mock(HttpServletRequest.class));

        assertEquals(bytes, new byte[]{9, 8});
        verify(response).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test
    public void shouldGenerateTournamentQrPdf() throws Exception {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setId(1);
        tournamentDTO.setName("TournamentName");
        when(tournamentController.get(1)).thenReturn(tournamentDTO);

        final TournamentQr tournamentQr = mock(TournamentQr.class);
        when(pdfController.generateTournamentQr(any(Locale.class), eq(tournamentDTO), eq(null))).thenReturn(tournamentQr);
        when(tournamentQr.generate()).thenReturn(new byte[]{7, 7, 7});

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] generated = qrService.getParticipantDiplomaFromTournamentAsPdf(1, Locale.ENGLISH, response, mock(HttpServletRequest.class));

        assertEquals(generated, new byte[]{7, 7, 7});
        verify(response).setHeader(eq(HttpHeaders.CONTENT_DISPOSITION), any());
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldWrapPdfGenerationErrorsAsBadRequestForInvalidXml() throws Exception {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setId(1);
        when(tournamentController.get(1)).thenReturn(tournamentDTO);

        final TournamentQr tournamentQr = mock(TournamentQr.class);
        when(pdfController.generateTournamentQr(any(Locale.class), eq(tournamentDTO), eq(null))).thenReturn(tournamentQr);
        when(tournamentQr.generate()).thenThrow(new InvalidXmlElementException("invalid"));

        qrService.getParticipantDiplomaFromTournamentAsPdf(1, Locale.ENGLISH, mock(HttpServletResponse.class), mock(HttpServletRequest.class));
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void shouldWrapPdfGenerationErrorsAsBadRequestForEmptyPdf() throws Exception {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setId(1);
        when(tournamentController.get(1)).thenReturn(tournamentDTO);

        final TournamentQr tournamentQr = mock(TournamentQr.class);
        when(pdfController.generateTournamentQr(any(Locale.class), eq(tournamentDTO), eq(null))).thenReturn(tournamentQr);
        when(tournamentQr.generate()).thenThrow(new EmptyPdfBodyException("empty"));

        qrService.getParticipantDiplomaFromTournamentAsPdf(1, Locale.ENGLISH, mock(HttpServletResponse.class), mock(HttpServletRequest.class));
    }
}

