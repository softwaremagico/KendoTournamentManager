package com.softwaremagico.kt.utils;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class NameUtilsTests {

    class NameTest implements IName {
        private String name;

        public NameTest(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void checkTestCopy() {
        Assert.assertEquals(NameUtils.getNameCopy(new NameTest("Test")), "Test - Copy");
    }

    @Test
    public void checkSecondCopy() {
        Assert.assertEquals(NameUtils.getNameCopy(new NameTest("Test - Copy")), "Test - Copy #2");
    }

    @Test
    public void checkThirdCopy() {
        Assert.assertEquals(NameUtils.getNameCopy(new NameTest("Test - Copy #2")), "Test - Copy #3");
    }

    @Test
    public void checkForthCopy() {
        Assert.assertEquals(NameUtils.getNameCopy(new NameTest("Test - Copy #3")), "Test - Copy #4");
    }
}
