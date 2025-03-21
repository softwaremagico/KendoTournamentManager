package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import com.softwaremagico.kt.core.exceptions.UnexpectedValueException;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.QrProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class QrController {

    private static final String TOURNAMENT_FIGHTS_URL = "/#/tournaments/fights";
    private static final String PARTICIPANT_STATISTICS_URL = "/#/participants/statistics";
    private static final String LOGO_RESOURCE = "/kote.svg";
    private static final String QR_FORMAT = "png";
    private static final Integer QR_SIZE = 500;
    private static final Color QR_COLOR_LIGHT_MODE = Color.decode("#001239");
    private static final Color QR_COLOR_NIGHT_MODE = Color.decode("#b0a3d5");
    private static final Color BACKGROUND_LIGHT_MODE = Color.decode("#ffffff");
    private static final Color BACKGROUND_NIGHT_MODE = Color.decode("#424242");

    private final QrProvider qrProvider;

    @Value("${server.domain:localhost}")
    private String machineDomain;

    @Value("${server.schema:http}")
    private String schema;

    private final TournamentProvider tournamentProvider;

    private final ParticipantProvider participantProvider;

    public QrController(QrProvider qrProvider, TournamentProvider tournamentProvider, ParticipantProvider participantProvider) {
        this.qrProvider = qrProvider;
        this.tournamentProvider = tournamentProvider;
        this.participantProvider = participantProvider;
    }

    @Cacheable(cacheNames = "qr-codes", key = "#tournamentId")
    public QrCodeDTO generateGuestQrCodeForTournamentFights(Integer tournamentId, Integer port, boolean nightMode) {
        //Check that exists.
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(this.getClass(), "No tournament found with id '" + tournamentId + "'."));
        try {
            final String link = schema + "://" + machineDomain + (port != null ? ":" + port : "") + TOURNAMENT_FIGHTS_URL
                    + "?tournamentId=" + tournament.getId() + "&user=guest";
            final BufferedImage qrCode = qrProvider.getQr(link, QR_SIZE,
                    !nightMode ? QR_COLOR_LIGHT_MODE : QR_COLOR_NIGHT_MODE,
                    LOGO_RESOURCE,
                    !nightMode ? BACKGROUND_LIGHT_MODE : BACKGROUND_NIGHT_MODE);
            final QrCodeDTO qrCodeDTO = new QrCodeDTO();
            qrCodeDTO.setData(toByteArray(qrCode, QR_FORMAT));
            qrCodeDTO.setLink(link);
            return qrCodeDTO;
        } catch (IOException e) {
            throw new UnexpectedValueException(this.getClass(), e);
        }
    }

    /**
     * '/participant/statistics?participantId=1'
     *
     * @param participantId
     * @param port          if the frontend is running on a different port.
     * @return
     */
    public QrCodeDTO generateParticipantQrCodeForStatistics(Integer participantId, Integer port, boolean nightMode) {
        final Participant participant = participantProvider.get(participantId).orElseThrow(() ->
                new ParticipantNotFoundException(this.getClass(), "No participant found with id '" + participantId + "'."));

        final TemporalToken temporalToken = participantProvider.generateTemporalToken(participant);

        try {
            final String link = schema + "://" + machineDomain + (port != null ? ":" + port : "") + PARTICIPANT_STATISTICS_URL
                    + "?participantId=" + participantId + "&temporalToken="
                    + URLEncoder.encode(temporalToken.getContent(), StandardCharsets.UTF_8);
            final BufferedImage qrCode = qrProvider.getQr(link, QR_SIZE,
                    !nightMode ? QR_COLOR_LIGHT_MODE : QR_COLOR_NIGHT_MODE,
                    LOGO_RESOURCE,
                    !nightMode ? BACKGROUND_LIGHT_MODE : BACKGROUND_NIGHT_MODE);
            final QrCodeDTO qrCodeDTO = new QrCodeDTO();
            qrCodeDTO.setData(toByteArray(qrCode, QR_FORMAT));
            qrCodeDTO.setLink(link);
            return qrCodeDTO;
        } catch (IOException e) {
            throw new UnexpectedValueException(this.getClass(), e);
        }
    }

    // convert BufferedImage to byte[]
    public static byte[] toByteArray(BufferedImage bufferedImage, String format) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, format, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
