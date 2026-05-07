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

import com.softwaremagico.kt.pdf.PdfTheme;
import com.lowagie.text.pdf.BaseFont;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "pdfTheme")
public class PdfThemeTests {

	@Test
	public void testFooterFontSizeConstant() {
		Assert.assertEquals(PdfTheme.FOOTER_FONT_SIZE, 8);
		Assert.assertNotEquals(PdfTheme.FOOTER_FONT_SIZE, 0);
		Assert.assertNotEquals(PdfTheme.FOOTER_FONT_SIZE, 12);
	}

	@Test
	public void testFontSizeConstant() {
		Assert.assertEquals(PdfTheme.FONT_SIZE, 12);
		Assert.assertNotEquals(PdfTheme.FONT_SIZE, 0);
		Assert.assertNotEquals(PdfTheme.FONT_SIZE, 10);
	}

	@Test
	public void testScoreFontSizeConstant() {
		Assert.assertEquals(PdfTheme.SCORE_FONT_SIZE, 12);
		Assert.assertEquals(PdfTheme.SCORE_FONT_SIZE, PdfTheme.FONT_SIZE);
	}

	@Test
	public void testScoreListSizeConstant() {
		Assert.assertEquals(PdfTheme.SCORE_LIST_SIZE, 9);
		Assert.assertNotEquals(PdfTheme.SCORE_LIST_SIZE, PdfTheme.FONT_SIZE);
		Assert.assertTrue(PdfTheme.SCORE_LIST_SIZE < PdfTheme.FONT_SIZE);
	}

	@Test
	public void testHeaderFontSizeConstant() {
		Assert.assertEquals(PdfTheme.HEADER_FONT_SIZE, PdfTheme.FONT_SIZE + 15);
		Assert.assertEquals(PdfTheme.HEADER_FONT_SIZE, 27);
		Assert.assertTrue(PdfTheme.HEADER_FONT_SIZE > PdfTheme.FONT_SIZE);
	}

	@Test
	public void testAccreditationIdentificationFontSize() {
		Assert.assertEquals(PdfTheme.ACCREDITATION_IDENTIFICATION_FONT_SIZE, 32);
		Assert.assertTrue(PdfTheme.ACCREDITATION_IDENTIFICATION_FONT_SIZE > 0);
	}

	@Test
	public void testAccreditationNameFontSize() {
		Assert.assertEquals(PdfTheme.ACCREDITATION_NAME_FONT_SIZE, 12);
		Assert.assertNotEquals(PdfTheme.ACCREDITATION_NAME_FONT_SIZE, 0);
	}

	@Test
	public void testAccreditationRoleFontSize() {
		Assert.assertEquals(PdfTheme.ACCREDITATION_ROLE_FONT_SIZE, 10);
		Assert.assertTrue(PdfTheme.ACCREDITATION_ROLE_FONT_SIZE > 0);
	}

	@Test
	public void testAccreditationLastnameFontSize() {
		Assert.assertEquals(PdfTheme.ACCREDITATION_LASTNAME_FONT_SIZE, 18);
		Assert.assertTrue(PdfTheme.ACCREDITATION_LASTNAME_FONT_SIZE > PdfTheme.ACCREDITATION_NAME_FONT_SIZE);
	}

	@Test
	public void testAccreditationClubnameFontSize() {
		Assert.assertEquals(PdfTheme.ACCREDITATION_CLUBNAME_FONT_SIZE, 10);
		Assert.assertEquals(PdfTheme.ACCREDITATION_CLUBNAME_FONT_SIZE, PdfTheme.ACCREDITATION_ROLE_FONT_SIZE);
	}

	@Test
	public void testDiplomaFontSize() {
		Assert.assertEquals(PdfTheme.DIPLOMA_FONT_SIZE, 36);
		Assert.assertTrue(PdfTheme.DIPLOMA_FONT_SIZE > PdfTheme.HEADER_FONT_SIZE);
	}

	@Test
	public void testLineFont() {
		final BaseFont font = PdfTheme.getLineFont();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testFooterFont() {
		final BaseFont font = PdfTheme.getFooterFont();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testLineItalicFont() {
		final BaseFont font = PdfTheme.getLineItalicFont();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testLineBoldFont() {
		final BaseFont font = PdfTheme.getLineFontBold();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testTitleFont() {
		final BaseFont font = PdfTheme.getTitleFont();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testSubtitleFont() {
		final BaseFont font = PdfTheme.getSubtitleFont();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testHandwrittenFont() {
		final BaseFont font = PdfTheme.getHandwrittenFont();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testScoreFont() {
		final BaseFont font = PdfTheme.getScoreFont();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testBasicFont() {
		final BaseFont font = PdfTheme.getBasicFont();
		Assert.assertNotNull(font);
		Assert.assertFalse(font.getPostscriptFontName().isEmpty());
	}

	@Test
	public void testFontLazyCaching() {
		final BaseFont font1 = PdfTheme.getLineFont();
		final BaseFont font2 = PdfTheme.getLineFont();
		Assert.assertNotNull(font1);
		Assert.assertNotNull(font2);
		Assert.assertEquals(font1, font2);
	}

	@Test
	public void testGetHandWrittenFontSize() {
		final int originalSize = 20;
		final int handWrittenSize = PdfTheme.getHandWrittenFontSize(originalSize);
		Assert.assertEquals(handWrittenSize, originalSize - 1);
		Assert.assertEquals(handWrittenSize, 19);
		Assert.assertNotEquals(handWrittenSize, originalSize);
		Assert.assertTrue(handWrittenSize < originalSize);
	}

	@Test
	public void testGetHandWrittenFontSizeWithSmallValue() {
		final int originalSize = 5;
		final int handWrittenSize = PdfTheme.getHandWrittenFontSize(originalSize);
		Assert.assertEquals(handWrittenSize, 4);
		Assert.assertTrue(handWrittenSize > 0);
	}

	@Test
	public void testFontConstantsNotEmpty() {
		Assert.assertFalse(PdfTheme.LOGO_IMAGE.isEmpty());
		Assert.assertFalse(PdfTheme.LINE_FONT_NAME.isEmpty());
		Assert.assertFalse(PdfTheme.LINE_FONT_ITALIC_NAME.isEmpty());
		Assert.assertFalse(PdfTheme.TABLE_SUBTITLE_FONT_NAME.isEmpty());
		Assert.assertFalse(PdfTheme.LINE_BOLD_FONT_NAME.isEmpty());
		Assert.assertFalse(PdfTheme.TITLE_FONT_NAME.isEmpty());
		Assert.assertFalse(PdfTheme.FILLED_TEXT_FONT_NAME.isEmpty());
	}

	@Test
	public void testFontNamesContainValidFileNames() {
		Assert.assertTrue(PdfTheme.LINE_FONT_NAME.endsWith(".ttf"));
		Assert.assertTrue(PdfTheme.LINE_FONT_ITALIC_NAME.endsWith(".ttf"));
		Assert.assertTrue(PdfTheme.LINE_BOLD_FONT_NAME.endsWith(".ttf"));
		Assert.assertTrue(PdfTheme.TITLE_FONT_NAME.endsWith(".ttf"));
		Assert.assertTrue(PdfTheme.FILLED_TEXT_FONT_NAME.endsWith(".ttf"));
	}

	@Test
	public void testAllFontSizeValuesArePositive() {
		Assert.assertTrue(PdfTheme.FOOTER_FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.SCORE_FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.SCORE_LIST_SIZE > 0);
		Assert.assertTrue(PdfTheme.HEADER_FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.ACCREDITATION_IDENTIFICATION_FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.ACCREDITATION_NAME_FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.ACCREDITATION_ROLE_FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.ACCREDITATION_LASTNAME_FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.ACCREDITATION_CLUBNAME_FONT_SIZE > 0);
		Assert.assertTrue(PdfTheme.DIPLOMA_FONT_SIZE > 0);
	}
}

