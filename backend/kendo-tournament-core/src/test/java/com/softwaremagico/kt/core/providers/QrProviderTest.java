package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import io.github.simonscholz.qrcode.QrPositionalSquaresConfig;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {"qrProviderTests"})
public class QrProviderTest {

    private QrProvider provider = new QrProvider();

    // Tests for crateSquareConfig - covering null parameter paths
    @Test
    public void testCreateSquareConfigWithAllNullParameters() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(null, null, null, null, null, null);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithCircleShapedTrue() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(true, null, null, null, null, null);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithCircleShapedFalse() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(false, null, null, null, null, null);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithRelativeSquareBorderRound() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(null, 0.5d, null, null, null, null);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithCenterColor() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(null, null, Color.RED, null, null, null);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithInnerSquareColor() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(null, null, null, Color.GREEN, null, null);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithOuterSquareColor() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(null, null, null, null, Color.BLUE, null);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithOuterBorderColor() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(null, null, null, null, null, Color.BLACK);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithAllParameters() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(true, 0.5d, Color.RED, Color.GREEN, Color.BLUE, Color.BLACK);
        assertThat(config).isNotNull();
    }

    @Test
    public void testCreateSquareConfigWithMultipleParameters() {
        final QrPositionalSquaresConfig config = provider.crateSquareConfig(false, 0.3d, Color.WHITE, Color.YELLOW, null, Color.DARK_GRAY);
        assertThat(config).isNotNull();
    }

    // Tests for getQr methods - BufferedImage creation
    @Test
    public void testGetQrBasicContent() {
        final BufferedImage qrImage = provider.getQr("TestContent", 200, Color.BLACK, Color.WHITE);
        assertThat(qrImage).isNotNull();
    }

    @Test
    public void testGetQrWithDifferentSize() {
        final BufferedImage qrImage = provider.getQr("TestData", 400, Color.RED, Color.BLUE);
        assertThat(qrImage).isNotNull();
    }

    @Test
    public void testGetQrWithNullSize() {
        final BufferedImage qrImage = provider.getQr("TestData", null, Color.BLACK, Color.WHITE);
        assertThat(qrImage).isNotNull();
    }

    // Tests for getQrAsSvg methods - SVG Document creation
    @Test
    public void testGetQrAsSvgBasic() {
        final Document svgDoc = provider.getQrAsSvg("TestContent", 200, Color.BLACK, "test.png");
        assertThat(svgDoc).isNotNull();
    }

    @Test
    public void testGetQrAsSvgWithCircleShape() {
        final Document svgDoc = provider.getQrAsSvg("TestContent", 200, Color.BLACK, "test.png", true);
        assertThat(svgDoc).isNotNull();
    }

    @Test
    public void testGetQrAsSvgWithSquareShape() {
        final Document svgDoc = provider.getQrAsSvg("TestContent", 200, Color.BLACK, "test.png", false);
        assertThat(svgDoc).isNotNull();
    }

    @Test
    public void testGetQrAsSvgWithAllParameters() {
        final Document svgDoc = provider.getQrAsSvg("TestContent", 200, Color.BLACK, Color.WHITE, Color.GRAY, "test.png");
        assertThat(svgDoc).isNotNull();
    }

    @Test
    public void testGetQrAsSvgWithNullBackground() {
        final Document svgDoc = provider.getQrAsSvg("TestContent", 200, Color.BLACK, Color.WHITE, null, "test.png");
        assertThat(svgDoc).isNotNull();
    }

    // Test for different border colors
    @Test
    public void testGetQrWithBorderColor() {
        final BufferedImage qrImage = provider.getQr("TestData", 200, Color.BLUE, Color.BLACK, Color.WHITE, "test.png");
        assertThat(qrImage).isNotNull();
    }

    @Test
    public void testGetQrWithNullBorderColor() {
        final BufferedImage qrImage = provider.getQr("TestData", 200, null, Color.BLACK, Color.WHITE, "test.png");
        assertThat(qrImage).isNotNull();
    }

    @Test
    public void testGetQrWithNullInkColor() {
        final BufferedImage qrImage = provider.getQr("TestData", 200, Color.BLACK, null, Color.WHITE, "test.png");
        assertThat(qrImage).isNotNull();
    }

}

