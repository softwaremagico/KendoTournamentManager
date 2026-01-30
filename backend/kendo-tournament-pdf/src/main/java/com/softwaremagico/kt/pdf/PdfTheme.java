package com.softwaremagico.kt.pdf;

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


import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;

public abstract class PdfTheme {
    public static final String LOGO_IMAGE = "kendo-tournament-manager-kendo-tournament-manager-logo.png";
    public static final String LINE_FONT_NAME = "DejaVuSansCondensed.ttf";
    public static final String LINE_FONT_ITALIC_NAME = "DejaVuSansCondensed-Oblique.ttf";
    public static final String TABLE_SUBTITLE_FONT_NAME = "DejaVuSansCondensed-Oblique.ttf";
    public static final String LINE_BOLD_FONT_NAME = "DejaVuSansCondensed-Bold.ttf";
    public static final String TITLE_FONT_NAME = "DejaVuSansCondensed-Bold.ttf";
    public static final String FILLED_TEXT_FONT_NAME = "Montserrat-Regular.ttf";

    public static final int FOOTER_FONT_SIZE = 8;

    public static final int FONT_SIZE = 12;
    public static final int SCORE_FONT_SIZE = 12;
    public static final int SCORE_LIST_SIZE = 9;

    public static final int HEADER_FONT_SIZE = FONT_SIZE + 15;

    public static final int ACCREDITATION_IDENTIFICATION_FONT_SIZE = 32;
    public static final int ACCREDITATION_NAME_FONT_SIZE = 12;
    public static final int ACCREDITATION_ROLE_FONT_SIZE = 10;
    public static final int ACCREDITATION_LASTNAME_FONT_SIZE = 18;
    public static final int ACCREDITATION_CLUBNAME_FONT_SIZE = 10;

    public static final int DIPLOMA_FONT_SIZE = 36;

    private static BaseFont footerFont;
    private static BaseFont lineFont;
    private static BaseFont lineItalicFont;
    private static BaseFont lineBoldFont;
    private static BaseFont titleFont;
    private static BaseFont tableSubtitleFont;
    private static BaseFont handwrittenFont;
    private static BaseFont scoreFont;

    private static BaseFont basicFont;

    private PdfTheme() {

    }

    public static BaseFont getFooterFont() {
        if (footerFont == null) {
            final Font font = FontFactory.getFont("/" + TITLE_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0.5f,
                    Font.NORMAL, BaseColor.BLACK);
            footerFont = font.getBaseFont();
        }
        return footerFont;
    }

    public static BaseFont getLineFont() {
        if (lineFont == null) {
            final Font font = FontFactory.getFont("/" + LINE_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0.8f,
                    Font.NORMAL, BaseColor.BLACK);
            lineFont = font.getBaseFont();
        }
        return lineFont;
    }

    public static BaseFont getLineItalicFont() {
        if (lineItalicFont == null) {
            final Font font = FontFactory.getFont("/" + LINE_FONT_ITALIC_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.ITALIC, BaseColor.BLACK);
            lineItalicFont = font.getBaseFont();
        }
        return lineItalicFont;
    }

    public static BaseFont getLineFontBold() {
        if (lineBoldFont == null) {
            final Font font = FontFactory.getFont("/" + LINE_BOLD_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.BOLD, BaseColor.BLACK);
            lineBoldFont = font.getBaseFont();
        }
        return lineBoldFont;
    }

    public static BaseFont getTitleFont() {
        if (titleFont == null) {
            final Font font = FontFactory.getFont("/" + TITLE_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 0.8f,
                    Font.NORMAL, BaseColor.BLACK);
            titleFont = font.getBaseFont();
        }
        return titleFont;
    }

    public static BaseFont getSubtitleFont() {
        if (tableSubtitleFont == null) {
            final Font font = FontFactory.getFont("/" + TABLE_SUBTITLE_FONT_NAME, BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED, 0.8f, Font.ITALIC, BaseColor.BLACK);
            tableSubtitleFont = font.getBaseFont();
        }
        return tableSubtitleFont;
    }

    public static BaseFont getHandwrittenFont() {
        if (handwrittenFont == null) {
            final Font font = FontFactory.getFont("/" + FILLED_TEXT_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.NORMAL, BaseColor.BLACK);
            handwrittenFont = font.getBaseFont();
        }
        return handwrittenFont;
    }

    public static BaseFont getScoreFont() {
        if (scoreFont == null) {
            final Font font = FontFactory.getFont("/" + FILLED_TEXT_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.BOLD, BaseColor.BLACK);
            scoreFont = font.getBaseFont();
        }
        return scoreFont;
    }

    public static BaseFont getBasicFont() {
        if (basicFont == null) {
            final Font font = FontFactory.getFont("/" + LINE_FONT_NAME, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    0.8f, Font.NORMAL, BaseColor.BLACK);
            basicFont = font.getBaseFont();
        }
        return basicFont;
    }

    public static int getHandWrittenFontSize(int originalSize) {
        return originalSize - 1;
    }

}
