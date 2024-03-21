package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.logger.KendoTournamentLogger;
import io.github.simonscholz.qrcode.QrCodeApi;
import io.github.simonscholz.qrcode.QrCodeConfig;
import io.github.simonscholz.qrcode.QrCodeDotStyler;
import io.github.simonscholz.qrcode.QrCodeFactory;
import io.github.simonscholz.qrcode.QrPositionalSquaresConfig;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
public class QrProvider {

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
                final BufferedImage logoImage = readResource(resourceLogo, (float) size);
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

    private BufferedImage readResource(String resourceLogo, float size) throws IOException, TranscoderException {
        if (resourceLogo.endsWith(".svg")) {
            // Create a PNG transcoder.
            final Transcoder pngTranscoder = new PNGTranscoder();

            // Set the transcoding hints.
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, size / 2);
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, size / 2);
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceLogo)) {
                // Create the transcoder input.
                final TranscoderInput input = new TranscoderInput(inputStream);

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
        } else {
            final URL urlResource = QrProvider.class.getClassLoader().getResource(resourceLogo);
            return ImageIO.read(urlResource);
        }
    }


}
