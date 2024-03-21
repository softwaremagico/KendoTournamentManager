package com.softwaremagico.kt.core.tests;

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

    private static final Color COLOR = Color.decode("#011d4a");

    private static final String SOFTWARE_URL = "https://github.com/softwaremagico/KendoTournamentManager";

    protected static final String OUTPUT_FOLDER = System.getProperty("java.io.tmpdir") + File.separator + "QrTest";

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
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, null, null, null, null, "kote.png", null, null);
        saveImage(qrImage, "withLogo");
    }

    @Test
    public void createQrWithLogoAsSvg() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, 5000, null, null, null, "kote.svg", null, null);
        saveImage(qrImage, "withLogoAsSvg");
    }


    @Test
    public void createQrWithRoundedBorders() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, null, COLOR, "kote.png");
        saveImage(qrImage, "withRoundedBorders");
    }

    @Test
    public void createQrWithRoundedBordersAndCircles() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, null, COLOR, "kote.png", true);
        saveImage(qrImage, "withRoundedBordersAndCircles");
    }

    @Test
    public void createSizedQr() {
        BufferedImage qrImage = qrProvider.getQr(SOFTWARE_URL, 500, null, null, null, "kote.png", null, null);
        saveImage(qrImage, "withSize");
    }

    @AfterClass
    public void removeFolder() {
        Assert.assertTrue(deleteDirectory(new File(OUTPUT_FOLDER)));
    }
}
