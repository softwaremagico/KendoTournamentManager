package com.softwaremagico.kt.utils;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "idCard")
public class IdCardTests {

    @Test
    public void testNifFromDniWithNull() {
        Assert.assertNull(IdCard.nifFromDni(null));
    }

    @Test
    public void testNifFromDniWithZero() {
        Assert.assertEquals(IdCard.nifFromDni(0), "0T");
    }

    @Test
    public void testNifFromDniWithOne() {
        Assert.assertEquals(IdCard.nifFromDni(1), "1R");
    }

    @Test
    public void testNifFromDniWithSmallNumber() {
        Assert.assertEquals(IdCard.nifFromDni(12345678), "12345678Z");
    }

    @Test
    public void testNifFromDniWithDifferentNumbers() {
        String nif1 = IdCard.nifFromDni(11111111);
        String nif2 = IdCard.nifFromDni(22222222);
        Assert.assertNotEquals(nif1, nif2);
    }

    @Test
    public void testNifFromDniReturnsString() {
        String nif = IdCard.nifFromDni(123);
        Assert.assertNotNull(nif);
        Assert.assertTrue(nif.matches("\\d+[A-Z]"));
    }

    @Test
    public void testNifFromDniContainsLetter() {
        String nif = IdCard.nifFromDni(999);
        Assert.assertTrue(nif.length() > 0);
        Assert.assertTrue(Character.isLetter(nif.charAt(nif.length() - 1)));
    }

    @Test
    public void testNifFromDniValidCharacters() {
        String nifChars = "TRWAGMYFPDXBNJZSQVHLCKE";
        for (int i = 0; i < 23; i++) {
            String nif = IdCard.nifFromDni(i);
            Assert.assertTrue(nif.contains(String.valueOf(nifChars.charAt(i))),
                    "NIF for " + i + " should contain letter " + nifChars.charAt(i));
        }
    }

    @Test
    public void testNifFromDniFormat() {
        String nif = IdCard.nifFromDni(456789);
        Assert.assertTrue(nif.startsWith("456789"));
    }

    @Test
    public void testNifFromDniBigNumber() {
        String nif = IdCard.nifFromDni(Integer.MAX_VALUE);
        Assert.assertNotNull(nif);
        Assert.assertTrue(nif.matches("\\d+[A-Z]"));
    }
}

