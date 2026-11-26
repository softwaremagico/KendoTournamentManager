package com.softwaremagico.kt.pdf.tests;

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

import com.softwaremagico.kt.pdf.BaseColor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.awt.Color;

@Test(groups = "baseColor")
public class BaseColorTests {

	@Test
	public void testBlackColorIsNotNull() {
		Assert.assertNotNull(BaseColor.BLACK);
	}

	@Test
	public void testWhiteColorIsNotNull() {
		Assert.assertNotNull(BaseColor.WHITE);
	}

	@Test
	public void testLightGrayColorIsNotNull() {
		Assert.assertNotNull(BaseColor.LIGHT_GRAY);
	}

	@Test
	public void testBlackColorValues() {
		final Color black = BaseColor.BLACK;
		Assert.assertNotNull(black);
		Assert.assertEquals(black.getRed(), 0);
		Assert.assertEquals(black.getGreen(), 0);
		Assert.assertEquals(black.getBlue(), 0);
		Assert.assertNotEquals(black.getRed(), 255);
	}

	@Test
	public void testWhiteColorValues() {
		final Color white = BaseColor.WHITE;
		Assert.assertNotNull(white);
		Assert.assertEquals(white.getRed(), 255);
		Assert.assertEquals(white.getGreen(), 255);
		Assert.assertEquals(white.getBlue(), 255);
		Assert.assertNotEquals(white.getRed(), 0);
	}

	@Test
	public void testLightGrayColorValues() {
		final Color lightGray = BaseColor.LIGHT_GRAY;
		Assert.assertNotNull(lightGray);
		Assert.assertEquals(lightGray.getRed(), 230);
		Assert.assertEquals(lightGray.getGreen(), 230);
		Assert.assertEquals(lightGray.getBlue(), 230);
		Assert.assertNotEquals(lightGray.getRed(), 255);
		Assert.assertNotEquals(lightGray.getRed(), 0);
	}

	@Test
	public void testBlackAndWhiteAreDifferent() {
		Assert.assertNotNull(BaseColor.BLACK);
		Assert.assertNotNull(BaseColor.WHITE);
		Assert.assertNotEquals(BaseColor.BLACK.getRed(), BaseColor.WHITE.getRed());
		Assert.assertFalse(BaseColor.BLACK.equals(BaseColor.WHITE));
	}

	@Test
	public void testBlackAndLightGrayAreDifferent() {
		Assert.assertNotNull(BaseColor.BLACK);
		Assert.assertNotNull(BaseColor.LIGHT_GRAY);
		Assert.assertNotEquals(BaseColor.BLACK.getRed(), BaseColor.LIGHT_GRAY.getRed());
		Assert.assertFalse(BaseColor.BLACK.equals(BaseColor.LIGHT_GRAY));
	}

	@Test
	public void testWhiteAndLightGrayAreDifferent() {
		Assert.assertNotNull(BaseColor.WHITE);
		Assert.assertNotNull(BaseColor.LIGHT_GRAY);
		Assert.assertNotEquals(BaseColor.WHITE.getRed(), BaseColor.LIGHT_GRAY.getRed());
		Assert.assertFalse(BaseColor.WHITE.equals(BaseColor.LIGHT_GRAY));
	}

	@Test
	public void testColorComponentRanges() {
		Assert.assertTrue(BaseColor.BLACK.getRed() >= 0 && BaseColor.BLACK.getRed() <= 255);
		Assert.assertTrue(BaseColor.WHITE.getGreen() >= 0 && BaseColor.WHITE.getGreen() <= 255);
		Assert.assertTrue(BaseColor.LIGHT_GRAY.getBlue() >= 0 && BaseColor.LIGHT_GRAY.getBlue() <= 255);
	}

	@Test
	public void testColorConsistency() {
		// Verify that multiple accesses return consistent values
		final Color black1 = BaseColor.BLACK;
		final Color black2 = BaseColor.BLACK;
		Assert.assertEquals(black1.getRed(), black2.getRed());
		Assert.assertEquals(black1.getGreen(), black2.getGreen());
		Assert.assertEquals(black1.getBlue(), black2.getBlue());
	}

	@Test
	public void testLightGrayIsActuallyLight() {
		final Color lightGray = BaseColor.LIGHT_GRAY;
		Assert.assertTrue(lightGray.getRed() > 200, "Light gray should have high red component");
		Assert.assertTrue(lightGray.getGreen() > 200, "Light gray should have high green component");
		Assert.assertTrue(lightGray.getBlue() > 200, "Light gray should have high blue component");
	}

	@Test
	public void testColorsAreInstancesOfColor() {
		Assert.assertTrue(BaseColor.BLACK instanceof Color);
		Assert.assertTrue(BaseColor.WHITE instanceof Color);
		Assert.assertTrue(BaseColor.LIGHT_GRAY instanceof Color);
	}
}

