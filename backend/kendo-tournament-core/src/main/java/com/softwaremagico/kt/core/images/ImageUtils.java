package com.softwaremagico.kt.core.images;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageUtils {

    public static byte[] decodeFromBase64(byte[] data) {
        return Base64.getDecoder().decode(data);
    }

    public void storeImageFromBase64(byte[] data, String path) throws IOException {
        final byte[] decodedImg = Base64.getDecoder()
                .decode(data);
        final Path destinationFile = Paths.get(path, "myImage.jpg");
        Files.write(destinationFile, decodedImg);
    }

    public BufferedImage cropImage(BufferedImage image, int startX, int startY, int endX, int endY) {
        final BufferedImage bufferedImage = image.getSubimage(startX, startY, endX, endY);
        final BufferedImage copyOfImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics graphics = copyOfImage.createGraphics();
        graphics.drawImage(bufferedImage, 0, 0, null);
        return copyOfImage;
    }
}
