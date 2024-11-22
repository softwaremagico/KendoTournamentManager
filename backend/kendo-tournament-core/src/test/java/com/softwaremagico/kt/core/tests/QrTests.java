package com.softwaremagico.kt.core.tests;

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

import com.softwaremagico.kt.core.providers.QrProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@Test(groups = {"qrTest"})
public class QrTests extends AbstractTestNGSpringContextTests {

    private static final String LOGO = "/kote.png";

    private static final Color COLOR = Color.decode("#011d4a");

    private static final String SOFTWARE_URL = "https://github.com/softwaremagico/KendoTournamentManager";

    protected static final String OUTPUT_FOLDER = System.getProperty("java.io.tmpdir") + File.separator + "QrTest";
    protected static final String JWT_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eaJzdWIiOiIxLGFkbWluQHRlc3QuY29tLDEyNy4wLjAuMSwiLCJpc3MiOiJjb11cc29mdHdhcmVtYWdpY28iLCJpYXQiOjE3MTEwMjUxOTMsImV4cCI6MTcxMTAyNjM5M30.b5ts7OHymYJ9TwPB81JRxrss2y31zkJfwj5vXEOHYlRCWztbdqLSLFVW9ojb88paDwUj6wOCC1juzGZXkCzwMA";

    @Autowired
    private QrProvider qrProvider;

    private void saveImage(BufferedImage qrImage, String fileName) {
        File outputfile = new File(OUTPUT_FOLDER + File.separator + fileName + ".png");
        try {
            ImageIO.write(qrImage, "png", outputfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @BeforeClass
    public void prepareFolder() throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_FOLDER));
    }


    @Test
    public void createBasicQr() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, null, null, null, null, null, null, null);
        saveImage(qrImage, "simpleImage");
    }

    @Test
    public void createQrWithLogo() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, null, null, null, null, LOGO, null, null);
        saveImage(qrImage, "withLogo");
    }

    @Test
    public void createQrWithLogoAsSvg() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, 5000, null, null, null, "/kote.svg", null, null);
        saveImage(qrImage, "withLogoAsSvg");
    }

    @Test
    public void createQrWithLogoAsSvgAndColor() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, 5000, Color.PINK, "/kote.svg", Color.white);
        saveImage(qrImage, "withLogoAsSvgAndColor");
    }


    @Test
    public void createQrWithRoundedBorders() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, null, COLOR, LOGO, Color.white);
        saveImage(qrImage, "withRoundedBorders");
    }

    @Test
    public void createQrWithRoundedBordersAndCircles() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, null, COLOR, LOGO, true, Color.white);
        saveImage(qrImage, "withRoundedBordersAndCircles");
    }

    @Test
    public void createSizedQr() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, 500, null, null, null, LOGO, null, null);
        saveImage(qrImage, "withSize");
    }

    @Test
    public void createJwtQr() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL + "?token=" + JWT_TOKEN, 500, null, null, null, LOGO, null, null);
        saveImage(qrImage, "withToken");
    }

    @AfterClass
    public void removeFolder() {
        Assert.assertTrue(deleteDirectory(new File(OUTPUT_FOLDER)));
    }
}
