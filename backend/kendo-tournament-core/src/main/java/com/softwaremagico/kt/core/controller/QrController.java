package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.exceptions.UnexpectedValueException;
import com.softwaremagico.kt.core.providers.QrProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
public class QrController {

    private static final String TOURNAMENT_FIGHTS_URL = "/tournaments/fights";
    private static final String LOGO_RESOURCE = "kote.svg";
    private static final String QR_FORMAT = "png";
    private static final Integer QR_SIZE = 500;
    private static final Color QR_COLOR = Color.decode("#001239");

    private final QrProvider qrProvider;

    @Value("${server.servlet.context-path:null}")
    private String contextPath;

    @Value("${server.domain:localhost}")
    private String machineDomain;

    @Value("${server.schema:http}")
    private String schema;

    private final TournamentProvider tournamentProvider;

    private final TournamentConverter tournamentConverter;

    public QrController(QrProvider qrProvider, TournamentProvider tournamentProvider, TournamentConverter tournamentConverter) {
        this.qrProvider = qrProvider;
        this.tournamentProvider = tournamentProvider;
        this.tournamentConverter = tournamentConverter;
    }

    @Cacheable(value = "qr-codes")
    public QrCodeDTO generateGuestQrCodeForTournamentFights(Integer tournamentId) {
        final Tournament tournament = tournamentProvider.get(tournamentId).orElseThrow(() ->
                new TournamentNotFoundException(this.getClass(), "No tournament found with id '" + tournamentId + "'."));
        try {
            final BufferedImage qrCode = qrProvider.getQr(schema + "://" + machineDomain + contextPath + TOURNAMENT_FIGHTS_URL
                            + "?tournamentId=" + tournament.getId() + "&user=guest",
                    QR_SIZE, QR_COLOR, LOGO_RESOURCE);

            final QrCodeDTO qrCodeDTO = new QrCodeDTO();
            qrCodeDTO.setTournament(tournamentConverter.convert(new TournamentConverterRequest(tournament)));
            qrCodeDTO.setData(toByteArray(qrCode, QR_FORMAT));
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
