package com.softwaremagico.kt.pdf;

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.utils.BasicDataTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SpringBootTest
@Test(groups = {"competitorListPdf"})
public class CompetitorsListTest extends BasicDataTest {
    private static final String PDF_PATH_OUTPUT = System.getProperty("java.io.tmpdir") + File.separator;

    @Autowired
    private PdfController pdfController;

    @BeforeClass
    public void prepareData() {
        populateData();
    }

    @Test
    public void generateCompetitorsListPdf() {
        List<ScoreOfCompetitor> competitorTopTen = new ArrayList<>();
        for (ParticipantDTO member : members) {
            competitorTopTen.add(new ScoreOfCompetitor(member, fights));
        }
        Assert.assertEquals(pdfController.generateCompetitorsScoreList(Locale.getDefault(), tournament, competitorTopTen)
                .createFile(PDF_PATH_OUTPUT + "CompetitorsList.pdf"), 1);
    }
}
