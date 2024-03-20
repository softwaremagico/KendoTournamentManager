package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.logger.KendoTournamentLogger;
import io.github.simonscholz.qrcode.QrCodeApi;
import io.github.simonscholz.qrcode.QrCodeConfig;
import io.github.simonscholz.qrcode.QrCodeDotStyler;
import io.github.simonscholz.qrcode.QrCodeFactory;
import io.github.simonscholz.qrcode.QrPositionalSquaresConfig;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
        return getQr(content, size, color, color, null, resourceLogo,
                crateSquareConfig(false, null, color, null, color, null),
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
            final URL urlResource = QrProvider.class.getClassLoader().getResource(resourceLogo);
            try {
                final BufferedImage logoImage = ImageIO.read(urlResource);
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


}
