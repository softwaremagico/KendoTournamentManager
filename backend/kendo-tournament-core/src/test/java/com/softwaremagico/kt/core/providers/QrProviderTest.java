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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = {"qrProviderTests"})
public class QrProviderTest {

    private final QrProvider provider = new QrProvider();

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

    @Test
    public void testGetQrWithMissingSvgLogoDoesNotFail() {
        final BufferedImage qrImage = provider.getQr("svg-missing", 200, Color.BLACK, Color.BLACK, Color.WHITE, "/missing-logo.svg");
        assertThat(qrImage).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReadSvgResourceCachesByBackgroundColor() throws Exception {
        final Field cacheField = QrProvider.class.getDeclaredField("qrLogoByBackground");
        cacheField.setAccessible(true);
        cacheField.set(null, new HashMap<Color, BufferedImage>());

        final Method readSvg = QrProvider.class.getDeclaredMethod("readSvgResource", String.class, float.class, Color.class, Color.class);
        readSvg.setAccessible(true);

        final BufferedImage first = (BufferedImage) readSvg.invoke(provider, "/qr/test-logo.svg", 200f, Color.BLACK, Color.WHITE);
        final BufferedImage second = (BufferedImage) readSvg.invoke(provider, "/qr/test-logo.svg", 200f, Color.BLACK, Color.WHITE);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull().isSameAs(first);

        final Map<Color, BufferedImage> cache = (Map<Color, BufferedImage>) cacheField.get(null);
        assertThat(cache).containsKey(Color.WHITE);
    }

    @Test
    public void testReadSvgResourceWithNullBackground() throws Exception {
        final Method readSvg = QrProvider.class.getDeclaredMethod("readSvgResource", String.class, float.class, Color.class, Color.class);
        readSvg.setAccessible(true);

        final BufferedImage image = (BufferedImage) readSvg.invoke(provider, "/qr/test-logo.svg", 200f, Color.BLACK, null);

        assertThat(image).isNotNull();
    }

    @Test
    public void testReadSvgResourceReturnsNullWhenResourceNotFound() throws Exception {
        final Field cacheField = QrProvider.class.getDeclaredField("qrLogoByBackground");
        cacheField.setAccessible(true);
        cacheField.set(null, new HashMap<Color, BufferedImage>());

        final Method readSvg = QrProvider.class.getDeclaredMethod("readSvgResource", String.class, float.class, Color.class, Color.class);
        readSvg.setAccessible(true);

        final BufferedImage image = (BufferedImage) readSvg.invoke(provider, "/not-found.svg", 200f, Color.BLACK, Color.MAGENTA);

        assertThat(image).isNull();
    }

    @Test
    public void testUpdateLogoColorWithNullColors() throws Exception {
        final String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M0 0L10 0\"/></svg>";
        final Method updateLogoColor = QrProvider.class.getDeclaredMethod("updateLogoColor", String.class, Color.class, Color.class);
        updateLogoColor.setAccessible(true);

        final Object svgDocument = updateLogoColor.invoke(provider, svg, null, null);

        assertThat(svgDocument).isNotNull();
    }

    @Test
    public void testDrawDotImageWithMissingResourceDoesNotFail() {
        final BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        provider.drawDotImage(0, 0, 10, graphics, "/missing-dot.png");
        graphics.dispose();

        assertThat(image).isNotNull();
    }

    @Test
    public void testDrawDotImageWithExistingResource() {
        final BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        provider.drawDotImage(0, 0, 10, graphics, "/qr/dot.png");
        graphics.dispose();

        assertThat(image).isNotNull();
    }

    @Test
    public void testGetQrWithExistingPngLogo() {
        final BufferedImage qrImage = provider.getQr("TestPngLogo", 200, Color.BLACK, Color.BLACK, Color.WHITE, "/qr/dot.png");
        assertThat(qrImage).isNotNull();
    }

}

