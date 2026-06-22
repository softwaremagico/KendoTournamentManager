package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.models.QrCodeDTO;
import com.softwaremagico.kt.core.controller.models.TemporalToken;
import com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.QrProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.ImageFormat;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = {"qrTest"})
public class QrControllerTest {

    @Mock
    private QrProvider qrProvider;
    @Mock
    private TournamentProvider tournamentProvider;
    @Mock
    private ParticipantProvider participantProvider;

    private QrController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        controller = new QrController(qrProvider, tournamentProvider, participantProvider);
        setField(controller, "schema", "https");
        setField(controller, "machineDomain", "kt.local");
    }

    @Test
    public void shouldGenerateGuestQrCodeWithPort() {
        final Tournament tournament = new Tournament("T", 1, 1, TournamentType.LEAGUE, "tester");
        tournament.setId(7);
        when(tournamentProvider.get(7)).thenReturn(Optional.of(tournament));
        when(qrProvider.getQr(anyString(), anyInt(), any(Color.class), anyString(), any(Color.class)))
                .thenReturn(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB));

        final QrCodeDTO qrCodeDTO = controller.generateGuestQrCodeForTournamentFights(7, 8080, true);

        assertEquals(qrCodeDTO.getContent(), "https://kt.local:8080/#/tournaments/fights?tournamentId=7&user=guest");
        assertFalse(qrCodeDTO.getData().length == 0);
        verify(qrProvider).getQr(anyString(), anyInt(), any(Color.class), anyString(), any(Color.class));
    }

    @Test
    public void shouldThrowWhenTournamentDoesNotExist() {
        when(tournamentProvider.get(99)).thenReturn(Optional.empty());

        org.testng.Assert.expectThrows(TournamentNotFoundException.class,
                () -> controller.generateGuestQrCodeForTournamentFights(99, null, false));
    }

    @Test
    public void shouldGenerateParticipantQrCodeWithEncodedToken() {
        final Participant participant = new Participant();
        participant.setId(9);
        when(participantProvider.get(9)).thenReturn(Optional.of(participant));
        when(participantProvider.generateTemporalToken(participant)).thenReturn(new TemporalToken("abc+def token?"));
        when(qrProvider.getQr(anyString(), anyInt(), any(Color.class), anyString(), any(Color.class)))
                .thenReturn(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB));

        final QrCodeDTO qrCodeDTO = controller.generateParticipantQrCodeForStatistics(9, null, false);

        assertEquals(qrCodeDTO.getContent(),
                "https://kt.local/#/participants/statistics?participantId=9&temporalToken=abc%2Bdef+token%3F");
        assertFalse(qrCodeDTO.getData().length == 0);
    }

    @Test
    public void shouldThrowWhenParticipantDoesNotExist() {
        when(participantProvider.get(13)).thenReturn(Optional.empty());

        org.testng.Assert.expectThrows(ParticipantNotFoundException.class,
                () -> controller.generateParticipantQrCodeForStatistics(13, 4200, true));
    }

    @Test
    public void shouldGenerateSvgQrCode() throws Exception {
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream("<svg xmlns=\"http://www.w3.org/2000/svg\"><rect/></svg>"
                        .getBytes(StandardCharsets.UTF_8)));
        when(qrProvider.getQrAsSvg(anyString(), anyInt(), any(), any(Color.class), any(Color.class), anyString()))
                .thenReturn(document);

        final QrCodeDTO qrCodeDTO = controller.generateQrCodeAsSvg("https://host/path", false);

        assertEquals(qrCodeDTO.getImageFormat(), ImageFormat.SVG);
        assertEquals(qrCodeDTO.getContent(), "https://host/path");
        assertTrue(new String(qrCodeDTO.getData(), StandardCharsets.UTF_8).contains("svg"));
    }

    private void setField(Object target, String fieldName, String value) throws Exception {
        final Field field = QrController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

