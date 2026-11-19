package com.marithe.sysfact.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import com.marithe.sysfact.exception.BusinessLogicException;

class ImagenUtilTest {

	private static byte[] codificar(BufferedImage img, String formato) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, formato, out);
		return out.toByteArray();
	}

	private static BufferedImage decodificar(byte[] bytes) throws IOException {
		return ImageIO.read(new ByteArrayInputStream(bytes));
	}

	/** 40×20 image: left half red, right half blue. */
	private static BufferedImage mitades() {
		BufferedImage img = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < 40; x++) {
			for (int y = 0; y < 20; y++) {
				img.setRGB(x, y, x < 20 ? Color.RED.getRGB() : Color.BLUE.getRGB());
			}
		}
		return img;
	}

	private static boolean rojizo(int rgb) {
		Color c = new Color(rgb);
		return c.getRed() > 150 && c.getBlue() < 100;
	}

	private static boolean azulado(int rgb) {
		Color c = new Color(rgb);
		return c.getBlue() > 150 && c.getRed() < 100;
	}

	/** Inserts an EXIF APP1 segment with the given orientation right after the JPEG SOI marker. */
	private static byte[] conOrientacion(byte[] jpeg, int orientacion) {
		byte[] app1 = {
				(byte) 0xFF, (byte) 0xE1, 0x00, 0x22, // APP1, length 34
				'E', 'x', 'i', 'f', 0, 0,
				'M', 'M', 0x00, 0x2A, 0x00, 0x00, 0x00, 0x08, // big-endian TIFF, IFD0 at 8
				0x00, 0x01, // one entry
				0x01, 0x12, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, (byte) orientacion, 0x00, 0x00, // Orientation
				0x00, 0x00, 0x00, 0x00 // no next IFD
		};
		byte[] out = new byte[jpeg.length + app1.length];
		System.arraycopy(jpeg, 0, out, 0, 2);
		System.arraycopy(app1, 0, out, 2, app1.length);
		System.arraycopy(jpeg, 2, out, 2 + app1.length, jpeg.length - 2);
		return out;
	}

	@Test
	void bigPhotoIsScaledToFitKeepingProportions() throws IOException {
		BufferedImage grande = new BufferedImage(3000, 1500, BufferedImage.TYPE_INT_RGB);
		BufferedImage r = decodificar(ImagenUtil.aJpegMediano(codificar(grande, "png"), 1000, 0.85f));
		assertEquals(1000, r.getWidth());
		assertEquals(500, r.getHeight());
	}

	@Test
	void smallPhotoIsNotEnlarged() throws IOException {
		BufferedImage r = decodificar(ImagenUtil.aJpegMediano(codificar(mitades(), "png"), 1000, 0.85f));
		assertEquals(40, r.getWidth());
		assertEquals(20, r.getHeight());
	}

	@Test
	void transparentPngBecomesWhite() throws IOException {
		BufferedImage png = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB); // fully transparent
		BufferedImage r = decodificar(ImagenUtil.aJpegMediano(codificar(png, "png"), 1000, 0.9f));
		Color c = new Color(r.getRGB(5, 5));
		assertTrue(c.getRed() > 245 && c.getGreen() > 245 && c.getBlue() > 245, c.toString());
	}

	@Test
	void notAnImageIsRejected() {
		BusinessLogicException e = assertThrows(BusinessLogicException.class,
				() -> ImagenUtil.aJpegMediano("hola, no soy una foto".getBytes(), 1000, 0.85f));
		assertTrue(e.getMessage().contains("no es una imagen"), e.getMessage());
	}

	@Test
	void readsExifOrientation() throws IOException {
		byte[] jpeg = codificar(mitades(), "jpg");
		assertEquals(1, ImagenUtil.orientacionExif(jpeg));
		assertEquals(6, ImagenUtil.orientacionExif(conOrientacion(jpeg, 6)));
		assertEquals(1, ImagenUtil.orientacionExif("no es jpeg".getBytes()));
	}

	@Test
	void phonePhotoTakenSidewaysIsRotated() throws IOException {
		// Orientation 6: the stored image must be turned 90° clockwise to be seen upright.
		byte[] foto = conOrientacion(codificar(mitades(), "jpg"), 6);
		BufferedImage r = decodificar(ImagenUtil.aJpegMediano(foto, 1000, 0.95f));
		assertEquals(20, r.getWidth());
		assertEquals(40, r.getHeight());
		// Turning clockwise brings the left (red) half to the top.
		assertTrue(rojizo(r.getRGB(10, 5)), "top should be red");
		assertTrue(azulado(r.getRGB(10, 34)), "bottom should be blue");
	}

	@Test
	void everyOrientationGivesTheExpectedSize() {
		BufferedImage img = mitades();
		for (int o = 1; o <= 8; o++) {
			BufferedImage r = ImagenUtil.orientar(img, o);
			boolean gira = o >= 5;
			assertEquals(gira ? 20 : 40, r.getWidth(), "orientation " + o);
			assertEquals(gira ? 40 : 20, r.getHeight(), "orientation " + o);
			// No orientation may leave an empty (white) corner: the whole image is drawn.
			Color esquina = new Color(r.getRGB(r.getWidth() - 1, r.getHeight() - 1));
			assertTrue(esquina.getRed() < 200 || esquina.getBlue() < 200, "orientation " + o + " corner " + esquina);
		}
	}
}
