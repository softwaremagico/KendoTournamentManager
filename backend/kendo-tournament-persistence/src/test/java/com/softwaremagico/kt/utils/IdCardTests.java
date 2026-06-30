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
        final String result = IdCard.nifFromDni(null);
        Assert.assertNull(result);
    }

    @Test
    public void testNifFromDniWithZero() {
        final String nif = IdCard.nifFromDni(0);
        Assert.assertNotNull(nif);
        Assert.assertEquals(nif, "0T");
        Assert.assertTrue(nif.startsWith("0"));
        Assert.assertTrue(nif.endsWith("T"));
    }

    @Test
    public void testNifFromDniWithOne() {
        final String nif = IdCard.nifFromDni(1);
        Assert.assertNotNull(nif);
        Assert.assertEquals(nif, "1R");
        Assert.assertNotEquals(nif, "1T");
    }

    @Test
    public void testNifFromDniWithSmallNumber() {
        final String nif = IdCard.nifFromDni(12345678);
        Assert.assertNotNull(nif);
        Assert.assertEquals(nif, "12345678Z");
        Assert.assertTrue(nif.startsWith("12345678"));
        Assert.assertTrue(Character.isLetter(nif.charAt(nif.length() - 1)));
    }

    @Test
    public void testNifFromDniWithDifferentNumbers() {
        final String nif1 = IdCard.nifFromDni(11111111);
        final String nif2 = IdCard.nifFromDni(22222222);
        Assert.assertNotNull(nif1);
        Assert.assertNotNull(nif2);
        Assert.assertNotEquals(nif1, nif2);
        Assert.assertNotEquals(nif1.charAt(nif1.length() - 1), nif2.charAt(nif2.length() - 1));
    }

    @Test
    public void testNifFromDniReturnsString() {
        final String nif = IdCard.nifFromDni(123);
        Assert.assertNotNull(nif);
        Assert.assertTrue(nif.matches("\\d+[A-Z]"));
        Assert.assertTrue(nif.length() > 1);
    }

    @Test
    public void testNifFromDniContainsLetter() {
        final String nif = IdCard.nifFromDni(999);
        Assert.assertNotNull(nif);
        Assert.assertFalse(nif.isEmpty());
        Assert.assertTrue(Character.isLetter(nif.charAt(nif.length() - 1)));
        Assert.assertFalse(Character.isDigit(nif.charAt(nif.length() - 1)));
    }

    @Test
    public void testNifFromDniValidCharacters() {
        final String nifChars = "TRWAGMYFPDXBNJZSQVHLCKE";
        for (int i = 0; i < 23; i++) {
            final String nif = IdCard.nifFromDni(i);
            Assert.assertNotNull(nif);
            Assert.assertTrue(nif.contains(String.valueOf(nifChars.charAt(i))),
                    "NIF for " + i + " should contain letter " + nifChars.charAt(i));
            // Verify the letter is indeed at the end
            Assert.assertEquals(nif.charAt(nif.length() - 1), nifChars.charAt(i));
        }
    }

    @Test
    public void testNifFromDniFormat() {
        final String nif = IdCard.nifFromDni(456789);
        Assert.assertNotNull(nif);
        Assert.assertTrue(nif.startsWith("456789"));
        Assert.assertTrue(nif.length() == "456789".length() + 1);
    }

    @Test
    public void testNifFromDniBigNumber() {
        final String nif = IdCard.nifFromDni(Integer.MAX_VALUE);
        Assert.assertNotNull(nif);
        Assert.assertTrue(nif.matches("\\d+[A-Z]"));
        Assert.assertTrue(nif.length() > 1);
        Assert.assertTrue(nif.startsWith(String.valueOf(Integer.MAX_VALUE)));
    }
}

