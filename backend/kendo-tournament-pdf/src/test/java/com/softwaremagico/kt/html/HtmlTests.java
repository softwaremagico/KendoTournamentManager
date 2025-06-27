package com.softwaremagico.kt.html;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
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

import com.softwaremagico.kt.html.controller.HtmlController;
import com.softwaremagico.kt.utils.BasicDataTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Locale;

@SpringBootTest
@Test(groups = {"blogTests"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HtmlTests extends BasicDataTest {

    @Autowired
    private HtmlController htmlController;

    @BeforeClass
    public void prepareData() {
        populateData();
        resolveFights();
    }

    @Test
    public void generateBlogHtml() {
        Assert.assertTrue(htmlController.generateBlogCode(Locale.getDefault(), tournament)
                .getWordpressFormat().length() > 200);
    }
}
