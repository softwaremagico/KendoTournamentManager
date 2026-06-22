package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.core.images.ImageUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.awt.image.BufferedImage;
import java.util.Base64;

@Test(groups = "imageUtils")
public class ImageUtilsTests {

	private static final int TEST_IMAGE_WIDTH = 100;
	private static final int TEST_IMAGE_HEIGHT = 80;
	private static final int DEFAULT_WIDTH = 680;
	private static final int DEFAULT_HEIGHT = 480;

	private BufferedImage createTestImage(int width, int height) {
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, 0xFF0000); // Red color
			}
		}
		return image;
	}

	@Test
	public void testDecodeFromBase64WithValidData() throws Exception {
		final String testData = "SGVsbG8gV29ybGQ="; // "Hello World" in base64
		final byte[] encoded = testData.getBytes();
		final byte[] decoded = ImageUtils.decodeFromBase64(encoded);

		Assert.assertNotNull(decoded);
		Assert.assertTrue(decoded.length > 0);
		Assert.assertEquals(new String(decoded), "Hello World");
		Assert.assertNotEquals(new String(decoded), "");
	}

	@Test
	public void testDecodeFromBase64WithEmptyData() {
		final byte[] encoded = "".getBytes();
		final byte[] decoded = ImageUtils.decodeFromBase64(encoded);

		Assert.assertNotNull(decoded);
		Assert.assertEquals(decoded.length, 0);
	}

	@Test
	public void testGetImageWithValidData() throws Exception {
		final BufferedImage originalImage = this.createTestImage(TEST_IMAGE_WIDTH, TEST_IMAGE_HEIGHT);
		final byte[] imageBytes = ImageUtils.getBytes(originalImage);
		final BufferedImage retrievedImage = ImageUtils.getImage(imageBytes);

		Assert.assertNotNull(retrievedImage);
		Assert.assertEquals(retrievedImage.getWidth(), TEST_IMAGE_WIDTH);
		Assert.assertEquals(retrievedImage.getHeight(), TEST_IMAGE_HEIGHT);
		Assert.assertNotEquals(retrievedImage.getWidth(), 0);
		Assert.assertNotEquals(retrievedImage.getHeight(), 0);
	}

	@Test
	public void testGetBytesWithValidImage() throws Exception {
		final BufferedImage image = this.createTestImage(TEST_IMAGE_WIDTH, TEST_IMAGE_HEIGHT);
		final byte[] bytes = ImageUtils.getBytes(image);

		Assert.assertNotNull(bytes);
		Assert.assertTrue(bytes.length > 0);
		Assert.assertNotEquals(bytes.length, 0);
	}

	@Test
	public void testResizeImageWhenSmallerThanDefaults() throws Exception {
		final BufferedImage originalImage = this.createTestImage(TEST_IMAGE_WIDTH, TEST_IMAGE_HEIGHT);
		final BufferedImage resizedImage = ImageUtils.resizeImage(originalImage);

		Assert.assertNotNull(resizedImage);
		// Should not resize if smaller than defaults
		Assert.assertEquals(resizedImage.getWidth(), TEST_IMAGE_WIDTH);
		Assert.assertEquals(resizedImage.getHeight(), TEST_IMAGE_HEIGHT);
	}

	@Test
	public void testResizeImageWhenLargerThanDefaults() throws Exception {
		final BufferedImage originalImage = this.createTestImage(1000, 800);
		final BufferedImage resizedImage = ImageUtils.resizeImage(originalImage);

		Assert.assertNotNull(resizedImage);
		// Should be resized - at least one dimension should be smaller or equal to
		// defaults
		Assert.assertTrue(resizedImage.getWidth() <= DEFAULT_WIDTH || resizedImage.getHeight() <= DEFAULT_HEIGHT);
		Assert.assertFalse(resizedImage.getWidth() == 1000 && resizedImage.getHeight() == 800);
	}

	@Test
	public void testResizeImageWithSpecificDimensions() throws Exception {
		final BufferedImage originalImage = this.createTestImage(1000, 800);
		final BufferedImage resizedImage = ImageUtils.resizeImage(originalImage, 500, 400);

		Assert.assertNotNull(resizedImage);
		Assert.assertTrue(resizedImage.getWidth() <= 500);
		Assert.assertTrue(resizedImage.getHeight() <= 400);
		Assert.assertNotEquals(resizedImage.getWidth(), 0);
		Assert.assertNotEquals(resizedImage.getHeight(), 0);
	}

	@Test
	public void testCropImageNormalize() throws Exception {
		// Create image with DNI proportions (26/32)
		final BufferedImage originalImage = createTestImage(400, 200);
		final BufferedImage croppedImage = ImageUtils.cropImage(originalImage);

		Assert.assertNotNull(croppedImage);
		Assert.assertTrue(croppedImage.getWidth() > 0);
		Assert.assertTrue(croppedImage.getHeight() > 0);
		// Should preserve height or crop width
		Assert.assertTrue(croppedImage.getHeight() <= 200);
		Assert.assertTrue(croppedImage.getWidth() > 0);
	}

	@Test
	public void testCropImageWithSpecificCoordinates() throws Exception {
		final BufferedImage originalImage = this.createTestImage(200, 200);
		final BufferedImage croppedImage = ImageUtils.cropImage(originalImage, 10, 10, 100, 100);

		Assert.assertNotNull(croppedImage);
		Assert.assertEquals(croppedImage.getWidth(), 100);
		Assert.assertEquals(croppedImage.getHeight(), 100);
		Assert.assertNotEquals(croppedImage.getWidth(), 0);
		Assert.assertNotEquals(croppedImage.getHeight(), 0);
	}

	@Test
	public void testResizeImageBytesRoundTrip() throws Exception {
		final BufferedImage originalImage = this.createTestImage(1000, 800);
		final byte[] originalBytes = ImageUtils.getBytes(originalImage);
		final byte[] resizedBytes = ImageUtils.resizeImage(originalBytes);

		Assert.assertNotNull(resizedBytes);
		Assert.assertTrue(resizedBytes.length > 0);
		Assert.assertNotEquals(resizedBytes.length, 0);
		// Resized should typically be smaller
		Assert.assertTrue(resizedBytes.length <= originalBytes.length);
	}

	@Test
	public void testCropImageBytesRoundTrip() throws Exception {
		final BufferedImage originalImage = this.createTestImage(400, 200);
		final byte[] originalBytes = ImageUtils.getBytes(originalImage);
		final byte[] croppedBytes = ImageUtils.cropImage(originalBytes);

		Assert.assertNotNull(croppedBytes);
		Assert.assertTrue(croppedBytes.length > 0);
		Assert.assertNotEquals(croppedBytes.length, 0);
	}

	@Test
	public void testDecodeEncodeBase64Roundtrip() {
		final String originalData = "Test Data 12345";
		final byte[] encoded = Base64.getEncoder().encode(originalData.getBytes());
		final byte[] decoded = ImageUtils.decodeFromBase64(encoded);

		Assert.assertNotNull(decoded);
		Assert.assertEquals(new String(decoded), originalData);
		Assert.assertNotEquals(new String(decoded), "");
		Assert.assertTrue(decoded.length > 0);
	}

	@Test
	public void testResizeImagePreservesAspectRatio() throws Exception {
		// Wide image: 1000x500
		final BufferedImage wideImage = this.createTestImage(1000, 500);
		final BufferedImage resizedWideImage = ImageUtils.resizeImage(wideImage, 680, 480);

		Assert.assertNotNull(resizedWideImage);
		// Should fit within bounds
		Assert.assertTrue(resizedWideImage.getWidth() <= 680);
		Assert.assertTrue(resizedWideImage.getHeight() <= 480);
		// Aspect ratio should be preserved (1000:500 = 2:1)
		final double originalRatio = 1000.0 / 500.0;
		final double resizedRatio = (double) resizedWideImage.getWidth() / resizedWideImage.getHeight();
		Assert.assertEquals(resizedRatio, originalRatio, 0.01);
	}

	@Test
	public void testResizeImageTallImage() throws Exception {
		// Tall image: 500x1000
		final BufferedImage tallImage = this.createTestImage(500, 1000);
		final BufferedImage resizedTallImage = ImageUtils.resizeImage(tallImage, 680, 480);

		Assert.assertNotNull(resizedTallImage);
		// Should fit within bounds
		Assert.assertTrue(resizedTallImage.getWidth() <= 680);
		Assert.assertTrue(resizedTallImage.getHeight() <= 480);
		// Height should be the limiting factor
		Assert.assertTrue(resizedTallImage.getHeight() == 480 || resizedTallImage.getHeight() < 480);
	}
}

