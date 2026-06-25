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

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

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
    public void shouldGenerateGuestQrWithPortJsonAndImage() {
        final QrCodeDTO qrCodeDTO = new QrCodeDTO();
        qrCodeDTO.setData(new byte[]{4, 5, 6});
        when(qrController.generateGuestQrCodeForTournamentFights(10, 4200, false)).thenReturn(qrCodeDTO);

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpServletRequest request = mock(HttpServletRequest.class);

        final QrCodeDTO dto = qrService.generateGuestQrCodeForTournamentFights(10, 4200, Optional.empty(), response, request);
        final byte[] image = qrService.generateGuestQrCodeForTournamentFightsImage(10, 4200, Optional.empty(), response, request);

        assertNotNull(dto);
        assertEquals(image, new byte[]{4, 5, 6});
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
    public void shouldGenerateQrAttendanceSvgAndSetHeaders() {
        final QrCodeDTO qrCodeDTO = new QrCodeDTO();
        qrCodeDTO.setData("<svg>ok</svg>".getBytes(StandardCharsets.UTF_8));
        when(qrController.generateQrCodeAsSvg("hello", false)).thenReturn(qrCodeDTO);

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final String svg = qrService.generateQrForAttendanceSvg("hello", Optional.empty(), response, mock(HttpServletRequest.class));

        assertEquals(svg, "<svg>ok</svg>");
        verify(response).setHeader(HttpHeaders.CONTENT_TYPE, "image/svg+xml");
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

    @Test
    public void shouldGenerateTournamentQrPdfWithPortEndpoint() throws Exception {
        final TournamentDTO tournamentDTO = new TournamentDTO();
        tournamentDTO.setId(2);
        tournamentDTO.setName("TournamentNamePort");
        when(tournamentController.get(2)).thenReturn(tournamentDTO);

        final TournamentQr tournamentQr = mock(TournamentQr.class);
        // Current implementation ignores received port and passes null to pdf generator.
        when(pdfController.generateTournamentQr(any(Locale.class), eq(tournamentDTO), eq(null))).thenReturn(tournamentQr);
        when(tournamentQr.generate()).thenReturn(new byte[]{8, 8, 8});

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final byte[] generated = qrService.getParticipantDiplomaFromTournamentAsPdf(2, 4200, Locale.ENGLISH, response, mock(HttpServletRequest.class));

        assertEquals(generated, new byte[]{8, 8, 8});
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

    @Test
    public void shouldGenerateParticipantQrWithoutAndWithPort() {
        final QrCodeDTO withoutPort = new QrCodeDTO();
        withoutPort.setData(new byte[]{1});
        final QrCodeDTO withPort = new QrCodeDTO();
        withPort.setData(new byte[]{2});

        when(qrController.generateParticipantQrCodeForStatistics(77, null, true)).thenReturn(withoutPort);
        when(qrController.generateParticipantQrCodeForStatistics(77, 4300, false)).thenReturn(withPort);

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final HttpServletRequest request = mock(HttpServletRequest.class);

        final QrCodeDTO dto1 = qrService.generateParticipantQrCodeForStatistics(77, Optional.of(true), response, request);
        final QrCodeDTO dto2 = qrService.generateParticipantQrCodeForStatisticsWithPort(77, 4300, Optional.empty(), response, request);

        assertSame(dto1, withoutPort);
        assertSame(dto2, withPort);
    }

    @Test
    public void shouldUseNightModeDefaultFalseOnGuestJsonWithoutPort() {
        final QrCodeDTO qrCodeDTO = new QrCodeDTO();
        qrCodeDTO.setData(new byte[]{3, 3, 3});
        when(qrController.generateGuestQrCodeForTournamentFights(11, null, false)).thenReturn(qrCodeDTO);

        final QrCodeDTO dto = qrService.generateGuestQrCodeForTournamentFights(11, Optional.empty(), mock(HttpServletResponse.class), mock(HttpServletRequest.class));

        assertSame(dto, qrCodeDTO);
        verify(qrController).generateGuestQrCodeForTournamentFights(11, null, false);
    }
}
