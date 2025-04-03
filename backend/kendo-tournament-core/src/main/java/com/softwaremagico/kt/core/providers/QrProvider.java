package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import com.softwaremagico.kt.core.controller.TournamentImageController;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import io.github.simonscholz.qrcode.QrCodeApi;
import io.github.simonscholz.qrcode.QrCodeConfig;
import io.github.simonscholz.qrcode.QrCodeDotStyler;
import io.github.simonscholz.qrcode.QrCodeFactory;
import io.github.simonscholz.qrcode.QrPositionalSquaresConfig;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Service
public class QrProvider {

    private static final float DEFAULT_SVG_SIZE = 200F;
    private static final double BORDER_RELATIVE_SIZE = 0.02d;
    private static final double BORDER_RADIUS = 0.03d;
    private static Map<Color, BufferedImage> qrLogoByBackground = new HashMap<>();

    public QrPositionalSquaresConfig crateSquareConfig(Boolean circleShaped, Double relativeSquareBorderRound,
                                                       Color center, Color outerSquare, Color background) {
        final QrPositionalSquaresConfig.Builder builder = new QrPositionalSquaresConfig.Builder();

        if (circleShaped != null) {
            builder.circleShaped(circleShaped);
        }

        if (relativeSquareBorderRound != null) {
            builder.relativeSquareBorderRound(relativeSquareBorderRound);
        }

        if (center != null) {
            builder.centerColor(center);
        }

        if (background != null) {
            builder.innerSquareColor(background);
        }

        if (outerSquare != null) {
            builder.outerSquareColor(outerSquare);
        }

        if (background != null) {
            builder.outerBorderColor(background);
        }

        return builder.build();
    }

    public BufferedImage getQr(String content, Integer size, Color color, Color background) {
        return getQr(content, size, color, color, null, null,
                crateSquareConfig(false, null, color, color, background),
                null);
    }

    public BufferedImage getQr(String content, Integer size, Color color, String resourceLogo, Color background) {
        return getQr(content, size, color, resourceLogo, true, background);
    }

    public BufferedImage getQr(String content, Integer size, Color color, String resourceLogo, boolean circleShaped, Color background) {
        return getQr(content, size, color, color, background, resourceLogo,
                crateSquareConfig(circleShaped, null, color, color, background),
                null);
    }


    public BufferedImage getQr(String content, Integer size, Color borderColor, Color ink, Color background, String resourceLogo,
                               QrPositionalSquaresConfig qrPositionalSquaresConfig, QrCodeDotStyler qrCodeDotStyler) {
        final QrCodeApi qrCodeApi = QrCodeFactory.createQrCodeApi();

        QrCodeConfig.Builder builder = new QrCodeConfig.Builder(content);

        if (size != null) {
            builder.qrCodeSize(size);
        }


        if (borderColor != null) {
            builder.qrBorderConfig(borderColor, BORDER_RELATIVE_SIZE, BORDER_RADIUS);
        }

        if (ink != null) {
            builder.qrCodeColorConfig(background != null ? background : new Color(0, 0, 0, 0), ink);
        }

        if (qrPositionalSquaresConfig != null) {
            builder.qrPositionalSquaresConfig(qrPositionalSquaresConfig);
        }

        if (qrCodeDotStyler != null) {
            builder.qrCodeDotStyler(qrCodeDotStyler);
        }

        if (resourceLogo != null) {
            try {
                final BufferedImage logoImage;
                if (resourceLogo.endsWith(".svg")) {
                    logoImage = readSvgResource(resourceLogo, size != null ? (float) size : DEFAULT_SVG_SIZE, ink, background);
                } else {
                    logoImage = readImageResource(resourceLogo);
                }
                if (logoImage != null) {
                    builder = builder.qrLogoConfig(logoImage);
                }
            } catch (Exception e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
            }
        }
        return qrCodeApi.createQrCodeImage(builder.build());
    }

    public void drawDotImage(final int x, final int y, final int dotSize, final Graphics2D graphics, final String resourceImage) {
        try (InputStream inputStream = TournamentImageController.class.getResourceAsStream(resourceImage)) {
            if (inputStream != null) {
                final BufferedImage imageDot = ImageIO.read(inputStream);
                graphics.drawImage(imageDot, x, y, dotSize, dotSize, null);
            }
        } catch (NullPointerException | IOException ex) {
            KendoTournamentLogger.errorMessage(this.getClass(), ex);
        }
    }

    private BufferedImage readImageResource(String resourceLogo) {
        try (InputStream inputStream = TournamentImageController.class.getResourceAsStream(resourceLogo)) {
            if (inputStream != null) {
                return ImageIO.read(inputStream);
            }
        } catch (NullPointerException | IOException ex) {
            KendoTournamentLogger.severe(TournamentImageController.class.getName(), "No image '" + resourceLogo + "' found!");
        }
        return null;
    }

    private BufferedImage readSvgResource(String resourceLogo, float size, Color ink, Color background) throws IOException, TranscoderException {
        if (qrLogoByBackground.get(background) == null) {
            //Get SVG File.
            final InputStream inputStream = QrProvider.class.getResourceAsStream(resourceLogo);
            if (inputStream == null) {
                return null;
            }
            final String svgText = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            final SVGDocument svgDocument = updateLogoColor(svgText, ink, background);

            //Convert to PNG.
            final Transcoder pngTranscoder = new PNGTranscoder();

            //Background.
            if (background != null) {
                pngTranscoder.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, background);
            }

            // Set the transcoding hints.
            pngTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, size / 2);
            pngTranscoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, size / 2);
            // Create the transcoder input.
            final TranscoderInput input = new TranscoderInput(svgDocument);

            // Create the transcoder output.
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final TranscoderOutput output = new TranscoderOutput(outputStream);

            // Save the image.
            pngTranscoder.transcode(input, output);

            // Flush and close the stream.
            outputStream.flush();
            outputStream.close();

            // Convert the byte stream into an image.
            final byte[] imgData = outputStream.toByteArray();
            qrLogoByBackground.put(background, ImageIO.read(new ByteArrayInputStream(imgData)));
        }
        return qrLogoByBackground.get(background);
    }

    private SVGDocument updateLogoColor(String svgText, Color ink, Color background) throws IOException {
        final StringReader reader = new StringReader(svgText);
        final String uri = "file:svgImage";
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        final SVGDocument svgDocument = f.createSVGDocument(uri, reader);
        if (ink != null) {
            final NodeList styleList = svgDocument.getElementsByTagName("path");
            for (int i = 0; i < styleList.getLength(); i++) {
                // To search only "style" desired children
                final Node defsChild = styleList.item(i);
                if (defsChild.getNodeType() == Node.ELEMENT_NODE) {
                    final Element element = (Element) defsChild;
                    element.setAttributeNS(null, "fill", "#"
                            + Integer.toHexString(ink.getRGB()).substring(2));
                }
            }
        }
        if (background != null) {
            final NodeList styleList = svgDocument.getElementsByTagName("circle");
            for (int i = 0; i < styleList.getLength(); i++) {
                // To search only "style" desired children
                final Node defsChild = styleList.item(i);
                if (defsChild.getNodeType() == Node.ELEMENT_NODE) {
                    final Element element = (Element) defsChild;
                    element.setAttributeNS(null, "fill", "#"
                            + Integer.toHexString(background.getRGB()).substring(2));
                }
            }
        }
        return svgDocument;
    }


}
