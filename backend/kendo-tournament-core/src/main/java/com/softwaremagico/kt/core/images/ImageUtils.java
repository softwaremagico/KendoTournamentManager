package com.softwaremagico.kt.core.images;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public final class ImageUtils {
    private static final double DNI_PROPORTIONS = 26d / 32d;
    private static final int DEFAULT_WIDTH = 680;
    private static final int DEFAULT_HEIGHT = 480;

    private ImageUtils() {

    }

    public static byte[] decodeFromBase64(byte[] data) {
        return Base64.getDecoder().decode(data);
    }

    public static void storeImageFromBase64(byte[] data, String path) throws IOException {
        final byte[] decodedImg = Base64.getDecoder()
                .decode(data);
        final Path destinationFile = Paths.get(path, "myImage.jpg");
        Files.write(destinationFile, decodedImg);
    }

    public static BufferedImage getImage(byte[] data) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            return ImageIO.read(inputStream);
        }
    }

    public static byte[] getBytes(BufferedImage bufferedImage) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static byte[] resizeImage(byte[] data) throws IOException {
        return getBytes(resizeImage(getImage(data)));
    }

    public static BufferedImage resizeImage(BufferedImage bufferedImage) {
        return resizeImage(bufferedImage, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static byte[] cropImage(byte[] data) throws IOException {
        return getBytes(cropImage(getImage(data)));
    }

    /**
     * Converts to DNI format.
     *
     * @param image the image to crop.
     * @return image cropped.
     */
    public static BufferedImage cropImage(BufferedImage image) {
        final double width = image.getHeight() * DNI_PROPORTIONS;
        if (image.getWidth() > width) {
            return cropImage(image, (int) ((image.getWidth() - width) / 2), 0,
                    (int) width, image.getHeight() - 1);
        }
        return image;
    }

    public static BufferedImage cropImage(BufferedImage image, int startX, int startY, int endX, int endY) {
        final BufferedImage bufferedImage = image.getSubimage(startX, startY, endX, endY);
        final BufferedImage copyOfImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics graphics = copyOfImage.createGraphics();
        graphics.drawImage(bufferedImage, 0, 0, null);
        return copyOfImage;
    }

    public static BufferedImage resizeImage(BufferedImage inputImage, int maxWidth, int maxHeight) {
        final int sourceWidth = inputImage.getWidth();
        final int sourceHeight = inputImage.getHeight();
        final int targetWidth;
        final int targetHeight;
        if (sourceWidth <= maxWidth && sourceHeight <= maxHeight) {
            return inputImage;
        } else if (sourceWidth > sourceHeight) {
            targetWidth = maxWidth;
            targetHeight = targetWidth * sourceHeight / (sourceWidth > 0 ? sourceWidth : 1);
        } else {
            targetHeight = maxHeight;
            targetWidth = targetHeight * sourceWidth / (sourceHeight > 0 ? sourceHeight : 1);
        }
        final Image scaledImage = inputImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        final BufferedImage targetImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        targetImage.getGraphics().drawImage(scaledImage, 0, 0, null);
        return targetImage;
    }
}
