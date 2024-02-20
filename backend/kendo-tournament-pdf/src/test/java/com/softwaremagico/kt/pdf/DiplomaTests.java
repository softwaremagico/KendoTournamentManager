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

@SpringBootTest
@Test(groups = {"diplomaPdf"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DiplomaTests extends BasicDataTest {
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
    public void generateDiplomas() {
        List<RoleDTO> roles = roleController.get(tournament);
        roles.forEach(roleDTO -> Assert.assertFalse(roleDTO.isDiplomaPrinted()));

        Assert.assertEquals(pdfController.generateTournamentDiplomas(tournament, true, null)
                // No clue why are 7 pages and not 6.
                .createFile(PDF_PATH_OUTPUT + "Diplomas.pdf"), roles.size() + 1);

        roles = roleController.get(tournament);
        roles.forEach(roleDTO -> Assert.assertTrue(roleDTO.isDiplomaPrinted()));
    }

    @Test(dependsOnMethods = "generateDiplomas", expectedExceptions = NoContentException.class)
    public void generateNewDiplomas() {
        pdfController.generateTournamentDiplomas(tournament, true, null);
    }

    @Test(dependsOnMethods = "generateNewDiplomas")
    public void generateDiplomasAgain() {
        Assert.assertEquals(pdfController.generateTournamentDiplomas(tournament, false, null)
                // No clue why are 7 pages and not 6. 6 Members + 1 referee
                .createFile(PDF_PATH_OUTPUT + "Diplomas.pdf"), roles.size() + 1 + 1);
    }
}
