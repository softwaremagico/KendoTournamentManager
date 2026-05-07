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
        Assert.assertNull(StringUtils.setCase(null));
    }

    @Test
    public void testSetCaseWithSingleWord() {
        Assert.assertEquals(StringUtils.setCase("john"), "John");
    }

    @Test
    public void testSetCaseWithMultipleWords() {
        Assert.assertEquals(StringUtils.setCase("john doe"), "John Doe");
    }

    @Test
    public void testSetCaseWithMultipleWordsAndSemicolon() {
        String result = StringUtils.setCase("john;doe");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains(","));
    }

    @Test
    public void testSetCaseWithShortWord() {
        Assert.assertEquals(StringUtils.setCase("a b c"), "a b c");
    }

    @Test
    public void testSetCaseWithMixedCase() {
        Assert.assertEquals(StringUtils.setCase("JOHN DOE"), "John Doe");
    }

    @Test
    public void testSetCaseWithNumbers() {
        Assert.assertEquals(StringUtils.setCase("john123 doe456"), "John123 Doe456");
    }

    @Test
    public void testSetCaseWithLeadingAndTrailingSpaces() {
        Assert.assertEquals(StringUtils.setCase("  john doe  "), "John Doe");
    }

    @Test
    public void testSetCaseWithMultipleSemicolons() {
        String result = StringUtils.setCase("john;doe;smith");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains(","));
    }

    @Test
    public void testGenerateRandomTokenLength() {
        String token = StringUtils.generateRandomToken(10);
        Assert.assertEquals(token.length(), 10);
    }

    @Test
    public void testGenerateRandomTokenNotNull() {
        String token = StringUtils.generateRandomToken(5);
        Assert.assertNotNull(token);
    }

    @Test
    public void testGenerateRandomTokenUniqueness() {
        String token1 = StringUtils.generateRandomToken(20);
        String token2 = StringUtils.generateRandomToken(20);
        Assert.assertNotEquals(token1, token2);
    }

    @Test
    public void testGenerateRandomTokenValidCharacters() {
        String token = StringUtils.generateRandomToken(100);
        for (char c : token.toCharArray()) {
            int ascii = (int) c;
            Assert.assertTrue(ascii >= 33 && ascii <= 90,
                    "Character '" + c + "' with ASCII " + ascii + " is outside expected range [33-90]");
        }
    }

    @Test
    public void testGenerateRandomTokenZeroLength() {
        String token = StringUtils.generateRandomToken(0);
        Assert.assertEquals(token.length(), 0);
    }

    @Test
    public void testGenerateRandomTokenLargeLength() {
        String token = StringUtils.generateRandomToken(1000);
        Assert.assertEquals(token.length(), 1000);
    }
}

