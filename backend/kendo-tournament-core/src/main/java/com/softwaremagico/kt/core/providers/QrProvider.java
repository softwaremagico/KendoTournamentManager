package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import com.softwaremagico.kt.logger.KendoTournamentLogger;
import io.github.simonscholz.qrcode.QrCodeApi;
import io.github.simonscholz.qrcode.QrCodeConfig;
import io.github.simonscholz.qrcode.QrCodeDotStyler;
import io.github.simonscholz.qrcode.QrCodeFactory;
import io.github.simonscholz.qrcode.QrPositionalSquaresConfig;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
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
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class QrProvider {

    private static final float DEFAULT_SVG_SIZE = 200F;

    public QrPositionalSquaresConfig crateSquareConfig(Boolean circleShaped, Double relativeSquareBorderRound,
                                                       Color center, Color innerSquare, Color outerSquare, Color outerBorder) {
        QrPositionalSquaresConfig.Builder builder = new QrPositionalSquaresConfig.Builder();

        if (circleShaped != null) {
            builder = builder.circleShaped(circleShaped);
        }

        if (relativeSquareBorderRound != null) {
            builder = builder.relativeSquareBorderRound(relativeSquareBorderRound);
        }

        if (center != null) {
            builder = builder.centerColor(center);
        }

        if (innerSquare != null) {
            builder = builder.innerSquareColor(innerSquare);
        }

        if (outerSquare != null) {
            builder = builder.outerSquareColor(outerSquare);
        }

        if (outerBorder != null) {
            builder = builder.outerBorderColor(outerBorder);
        }

        return builder.build();
    }

    public BufferedImage getQr(String content, Integer size, Color color) {
        return getQr(content, size, color, color, null, null,
                crateSquareConfig(false, null, color, null, color, null),
                null);
    }

    public BufferedImage getQr(String content, Integer size, Color color, String resourceLogo) {
        return getQr(content, size, color, resourceLogo, false);
    }

    public BufferedImage getQr(String content, Integer size, Color color, String resourceLogo, boolean circleShaped) {
        return getQr(content, size, color, color, null, resourceLogo,
                crateSquareConfig(circleShaped, null, color, null, color, null),
                null);
    }


    public BufferedImage getQr(String content, Integer size, Color borderColor, Color ink, Color background, String resourceLogo,
                               QrPositionalSquaresConfig qrPositionalSquaresConfig, QrCodeDotStyler qrCodeDotStyler) {
        final QrCodeApi qrCodeApi = QrCodeFactory.createQrCodeApi();

        QrCodeConfig.Builder builder = new QrCodeConfig.Builder(content);

        if (size != null) {
            builder = builder.qrCodeSize(size);
        }


        if (borderColor != null) {
            builder = builder.qrBorderConfig(borderColor);
        }

        if (ink != null) {
            builder = builder.qrCodeColorConfig(background != null ? background : new Color(0, 0, 0, 0), ink);
        }

        if (qrPositionalSquaresConfig != null) {
            builder = builder.qrPositionalSquaresConfig(qrPositionalSquaresConfig);
        }

        if (qrCodeDotStyler != null) {
            builder = builder.qrCodeDotStyler(qrCodeDotStyler);
        }

        if (resourceLogo != null) {
            try {
                final BufferedImage logoImage;
                if (resourceLogo.endsWith(".svg")) {
                    logoImage = readSvgResource(resourceLogo, size != null ? (float) size : DEFAULT_SVG_SIZE, ink);
                } else {
                    logoImage = readImageResource(resourceLogo);
                }
                builder = builder.qrLogoConfig(logoImage);
            } catch (Exception e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
            }
        }
        return qrCodeApi.createQrCodeImage(builder.build());
    }

    public void drawDotImage(final int x, final int y, final int dotSize, final Graphics2D graphics, final String resourceImage) {
        final URL resource = QrProvider.class.getClassLoader().getResource(resourceImage);
        if (resource != null) {
            try {
                final BufferedImage imageDot = ImageIO.read(resource);
                graphics.drawImage(imageDot, x, y, dotSize, dotSize, null);
            } catch (final IOException e) {
                KendoTournamentLogger.errorMessage(this.getClass(), e);
            }
        }
    }

    private BufferedImage readImageResource(String resourceLogo) throws IOException {
        final URL urlResource = QrProvider.class.getClassLoader().getResource(resourceLogo);
        return ImageIO.read(urlResource);
    }

    private BufferedImage readSvgResource(String resourceLogo, float size, Color ink) throws IOException, TranscoderException, URISyntaxException {
        //Change SVG Color.
        final String svgText = Files.readString(Paths.get(getClass().getClassLoader().getResource(resourceLogo).toURI()));
        final SVGDocument svgDocument = updateLogoColor(svgText, ink);

        //Convert to PNG.
        final Transcoder pngTranscoder = new PNGTranscoder();

        // Set the transcoding hints.
        pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, size / 2);
        pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, size / 2);
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
        return ImageIO.read(new ByteArrayInputStream(imgData));
    }

    private SVGDocument updateLogoColor(String svgText, Color color) throws IOException {
        final StringReader reader = new StringReader(svgText);
        final String uri = "file:svgImage";
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        final SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        final SVGDocument svgDocument = f.createSVGDocument(uri, reader);
        if (color != null) {
            final NodeList styleList = svgDocument.getElementsByTagName("path");
            for (int i = 0; i < styleList.getLength(); i++) {
                // To search only "style" desired children
                final Node defsChild = styleList.item(i);
                if (defsChild.getNodeType() == Node.ELEMENT_NODE) {
                    final Element element = (Element) defsChild;
                    element.setAttributeNS(null, "fill", "#"
                            + Integer.toHexString(color.getRGB()).substring(2));
                }
            }
        }
        return svgDocument;
    }


}
