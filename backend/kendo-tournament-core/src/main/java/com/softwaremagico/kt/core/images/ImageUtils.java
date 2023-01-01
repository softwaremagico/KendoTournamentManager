package com.softwaremagico.kt.core.images;

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
}
