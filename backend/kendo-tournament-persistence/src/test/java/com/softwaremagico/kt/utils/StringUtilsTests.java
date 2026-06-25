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

@Test(groups = "stringUtils")
public class StringUtilsTests {

    @Test
    public void testSetCaseWithNull() {
        final String result = StringUtils.setCase(null);
        Assert.assertNull(result);
    }

    @Test
    public void testSetCaseWithSingleWord() {
        final String result = StringUtils.setCase("john");
        Assert.assertNotNull(result);
        Assert.assertEquals(result, "John");
        Assert.assertNotEquals(result, "john");
    }

    @Test
    public void testSetCaseWithMultipleWords() {
        final String result = StringUtils.setCase("john doe");
        Assert.assertNotNull(result);
        Assert.assertEquals(result, "John Doe");
        Assert.assertTrue(result.startsWith("John"));
        Assert.assertTrue(result.endsWith("Doe"));
    }

    @Test
    public void testSetCaseWithMultipleWordsAndSemicolon() {
        final String result = StringUtils.setCase("john;doe");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains(","));
        Assert.assertFalse(result.contains(";"));
    }

    @Test
    public void testSetCaseWithShortWord() {
        final String result = StringUtils.setCase("a b c");
        Assert.assertNotNull(result);
        Assert.assertEquals(result, "a b c");
    }

    @Test
    public void testSetCaseWithMixedCase() {
        final String result = StringUtils.setCase("JOHN DOE");
        Assert.assertNotNull(result);
        Assert.assertEquals(result, "John Doe");
        Assert.assertNotEquals(result, "JOHN DOE");
        Assert.assertNotEquals(result, "john doe");
    }

    @Test
    public void testSetCaseWithNumbers() {
        final String result = StringUtils.setCase("john123 doe456");
        Assert.assertNotNull(result);
        Assert.assertEquals(result, "John123 Doe456");
        Assert.assertTrue(result.contains("123"));
        Assert.assertTrue(result.contains("456"));
    }

    @Test
    public void testSetCaseWithLeadingAndTrailingSpaces() {
        final String result = StringUtils.setCase("  john doe  ");
        Assert.assertNotNull(result);
        Assert.assertEquals(result, "John Doe");
        Assert.assertFalse(result.startsWith(" "));
        Assert.assertFalse(result.endsWith(" "));
    }

    @Test
    public void testSetCaseWithMultipleSemicolons() {
        final String result = StringUtils.setCase("john;doe;smith");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains(","));
        Assert.assertFalse(result.contains(";"));
        Assert.assertTrue(result.length() > 0);
    }

    @Test
    public void testGenerateRandomTokenLength() {
        final String token = StringUtils.generateRandomToken(10);
        Assert.assertNotNull(token);
        Assert.assertEquals(token.length(), 10);
        Assert.assertNotEquals(token.length(), 11);
        Assert.assertNotEquals(token.length(), 9);
    }

    @Test
    public void testGenerateRandomTokenNotNull() {
        final String token = StringUtils.generateRandomToken(5);
        Assert.assertNotNull(token);
        Assert.assertTrue(token.length() > 0);
    }

    @Test
    public void testGenerateRandomTokenUniqueness() {
        final String token1 = StringUtils.generateRandomToken(20);
        final String token2 = StringUtils.generateRandomToken(20);
        Assert.assertNotNull(token1);
        Assert.assertNotNull(token2);
        Assert.assertNotEquals(token1, token2);
    }

    @Test
    public void testGenerateRandomTokenValidCharacters() {
        final String token = StringUtils.generateRandomToken(100);
        Assert.assertNotNull(token);
        for (char c : token.toCharArray()) {
            final int ascii = (int) c;
            Assert.assertTrue(ascii >= 33 && ascii <= 90,
                    "Character '" + c + "' with ASCII " + ascii + " is outside expected range [33-90]");
            Assert.assertFalse(ascii < 33, "ASCII value should not be less than 33");
            Assert.assertFalse(ascii > 90, "ASCII value should not be greater than 90");
        }
    }

    @Test
    public void testGenerateRandomTokenZeroLength() {
        final String token = StringUtils.generateRandomToken(0);
        Assert.assertNotNull(token);
        Assert.assertEquals(token.length(), 0);
    }

    @Test
    public void testGenerateRandomTokenLargeLength() {
        final String token = StringUtils.generateRandomToken(1000);
        Assert.assertNotNull(token);
        Assert.assertEquals(token.length(), 1000);
        Assert.assertNotEquals(token.length(), 999);
        Assert.assertNotEquals(token.length(), 1001);
    }
}

