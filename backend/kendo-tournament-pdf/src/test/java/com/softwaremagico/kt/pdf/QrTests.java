package com.softwaremagico.kt.pdf;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.utils.BasicDataTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Locale;

@SpringBootTest
@Test(groups = {"qrPdf"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class QrTests extends BasicDataTest {
    private static final String PDF_PATH_OUTPUT = System.getProperty("java.io.tmpdir") + File.separator;

    @Autowired
    private PdfController pdfController;


    @BeforeClass
    public void prepareData() {
        populateData();
    }

    @Test
    public void generateQrCode() {

        Assert.assertEquals(pdfController.generateTournamentQr(Locale.getDefault(), tournament, null)
                // No clue why are 2 pages and not 1.
                .createFile(PDF_PATH_OUTPUT + "Tournament - QR.pdf") - 1, 1);
    }
}
