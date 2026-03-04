package com.softwaremagico.kt.pdf;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.exceptions.NoContentException;
import com.softwaremagico.kt.pdf.controller.PdfController;
import com.softwaremagico.kt.utils.BasicDataTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Locale;

@SpringBootTest
@Test(groups = {"accreditationPdf"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AccreditationTests extends BasicDataTest {
    private static final String PDF_PATH_OUTPUT = System.getProperty("java.io.tmpdir") + File.separator;

    @Autowired
    private PdfController pdfController;

    @Autowired
    private RoleController roleController;

    @BeforeClass
    public void prepareData() {
        populateData();
    }

    @Test
    public void generateAccreditations() {
        List<RoleDTO> roles = roleController.get(tournament);
        roles.forEach(roleDTO -> Assert.assertFalse(roleDTO.isAccreditationPrinted()));

        Assert.assertEquals(pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, true, null, null)
                // No clue why are 3 pages and not 2.
                .createFile(PDF_PATH_OUTPUT + "Accreditations.pdf"), Math.ceil(roles.size() / 4.0) + 1);

        roles = roleController.get(tournament);
        roles.forEach(roleDTO -> Assert.assertTrue(roleDTO.isAccreditationPrinted()));
    }

    @Test(dependsOnMethods = "generateAccreditations", expectedExceptions = NoContentException.class)
    public void generateNewAccreditations() {
        pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, true, null, null);
    }

    @Test(dependsOnMethods = "generateNewAccreditations")
    public void generateAccreditationsAgain() {
        Assert.assertEquals(pdfController.generateTournamentAccreditations(Locale.getDefault(), tournament, null, null, (String) null)
                // No clue why are 3 pages and not 2.
                .createFile(PDF_PATH_OUTPUT + "Accreditations.pdf"), Math.ceil(roles.size() / 4.0) + 1);
    }

}
