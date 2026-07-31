package com.softwaremagico.kt.pdf.qr;

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

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import org.springframework.context.MessageSource;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

@Test(groups = {"listsUnitTests"})
public class TournamentQrTest {

    private byte[] validImage() throws IOException {
        final BufferedImage bufferedImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);
        return outputStream.toByteArray();
    }

    private MessageSource messageSource() {
        final MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Text");
        return messageSource;
    }

    @Test
    public void constructor_withValidImages_expectGeneratedDocument() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, com.softwaremagico.kt.persistence.values.TournamentType.LEAGUE);
        final TournamentQr tournamentQr = new TournamentQr(messageSource(), Locale.getDefault(), tournament, validImage(), validImage());

        assertNotNull(tournamentQr.generate());
    }

    @Test
    public void constructor_withInvalidImages_expectNullImagesAndGeneratedDocument() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, com.softwaremagico.kt.persistence.values.TournamentType.LEAGUE);
        final TournamentQr tournamentQr = new TournamentQr(messageSource(), Locale.getDefault(), tournament, new byte[]{1}, new byte[]{2});

        assertNotNull(tournamentQr.generate());
    }

    @Test
    public void constructor_withNullBackgroundImage_expectNoBackground() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, com.softwaremagico.kt.persistence.values.TournamentType.LEAGUE);
        final TournamentQr tournamentQr = new TournamentQr(messageSource(), Locale.getDefault(), tournament, validImage(), null);

        assertNotNull(tournamentQr.generate());
    }

    @Test
    public void addDocumentWriterEvents_expectNoException() throws Exception {
        final TournamentDTO tournament = new TournamentDTO("T", 1, 3, com.softwaremagico.kt.persistence.values.TournamentType.LEAGUE);
        final TournamentQr tournamentQr = new TournamentQr(messageSource(), Locale.getDefault(), tournament, validImage(), null);

        tournamentQr.addDocumentWriterEvents(null);
    }
}

