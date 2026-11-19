package com.marithe.sysfact.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.marithe.sysfact.exception.BusinessLogicException;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

/**
 * Turns an uploaded photo (JPEG, PNG or WEBP) into a medium-size JPEG: rotated as the camera
 * intended (EXIF orientation), scaled down to fit a square of {@code ladoMaximo} pixels without
 * distortion, transparent areas painted white.
 */
public final class ImagenUtil {

	/** Refuse to decode anything bigger (a small file can expand to gigabytes in memory). */
	static final long MAX_PIXELES = 50_000_000L;

	private ImagenUtil() {}

	/** Reads, orients, scales and encodes the image. */
	public static byte[] aJpegMediano(byte[] original, int ladoMaximo, float calidad) {
		BufferedImage img = leer(original);
		img = orientar(img, orientacionExif(original));
		img = reducir(img, ladoMaximo);
		return jpeg(img, calidad);
	}

	/** Decodes the image, or throws a business error if the bytes are not a supported image. */
	public static BufferedImage leer(byte[] bytes) {
		try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			ImageReader reader = lector(in);
			if (reader == null) {
				throw new BusinessLogicException("El archivo no es una imagen válida. Use JPG, PNG o WEBP.");
			}
			try {
				reader.setInput(in, true, true);
				long pixeles = (long) reader.getWidth(0) * reader.getHeight(0);
				if (pixeles > MAX_PIXELES) {
					throw new BusinessLogicException("La imagen es demasiado grande (más de 50 megapíxeles).");
				}
				return reader.read(0);
			} finally {
				reader.dispose();
			}
		} catch (IOException | RuntimeException e) {
			if (e instanceof BusinessLogicException) {
				throw (BusinessLogicException) e;
			}
			throw new BusinessLogicException("No se pudo leer la imagen. Use JPG, PNG o WEBP.");
		}
	}

	/**
	 * ImageIO's own readers first; WEBP through TwelveMonkeys directly, so it works even when the
	 * application server did not register the plugin.
	 */
	private static ImageReader lector(ImageInputStream in) throws IOException {
		Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
		if (readers.hasNext()) {
			return readers.next();
		}
		ImageReaderSpi webp = new WebPImageReaderSpi();
		return webp.canDecodeInput(in) ? webp.createReaderInstance() : null;
	}

	/** Scales down (never up) so the longest side is at most {@code lado}, in good quality. */
	static BufferedImage reducir(BufferedImage img, int lado) {
		int w = img.getWidth();
		int h = img.getHeight();
		double escala = Math.min(1.0, (double) lado / Math.max(w, h));
		int destW = Math.max(1, (int) Math.round(w * escala));
		int destH = Math.max(1, (int) Math.round(h * escala));

		BufferedImage actual = aRgb(img);
		// Halving in steps keeps detail that a single bicubic pass from a big photo would lose.
		while (actual.getWidth() / 2 >= destW && actual.getHeight() / 2 >= destH) {
			actual = escalar(actual, actual.getWidth() / 2, actual.getHeight() / 2);
		}
		if (actual.getWidth() != destW || actual.getHeight() != destH) {
			actual = escalar(actual, destW, destH);
		}
		return actual;
	}

	private static BufferedImage escalar(BufferedImage src, int w, int h) {
		BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dst.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(src, 0, 0, w, h, null);
		} finally {
			g.dispose();
		}
		return dst;
	}

	/** Opaque RGB copy; transparent pixels become white (JPEG has no transparency). */
	private static BufferedImage aRgb(BufferedImage img) {
		if (img.getType() == BufferedImage.TYPE_INT_RGB) {
			return img;
		}
		BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = rgb.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());
			g.drawImage(img, 0, 0, null);
		} finally {
			g.dispose();
		}
		return rgb;
	}

	static byte[] jpeg(BufferedImage img, float calidad) {
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
			writer.setOutput(ios);
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(calidad);
			writer.write(null, new IIOImage(img, null, null), param);
		} catch (IOException e) {
			throw new IllegalStateException("No se pudo convertir la imagen a JPG", e);
		} finally {
			writer.dispose();
		}
		return out.toByteArray();
	}

	/** Applies an EXIF orientation (1–8). Phones store photos sideways and set this tag instead. */
	static BufferedImage orientar(BufferedImage img, int orientacion) {
		if (orientacion < 2 || orientacion > 8) {
			return img;
		}
		int w = img.getWidth();
		int h = img.getHeight();
		boolean gira = orientacion >= 5;
		AffineTransform t = new AffineTransform();
		switch (orientacion) {
		case 2: t.translate(w, 0); t.scale(-1, 1); break;
		case 3: t.translate(w, h); t.rotate(Math.PI); break;
		case 4: t.translate(0, h); t.scale(1, -1); break;
		case 5: t = new AffineTransform(0, 1, 1, 0, 0, 0); break; // transpose: (x, y) -> (y, x)
		case 6: t.translate(h, 0); t.rotate(Math.PI / 2); break;
		case 7: t = new AffineTransform(0, -1, -1, 0, h, w); break; // (x, y) -> (h - y, w - x)
		case 8: t.translate(0, w); t.rotate(3 * Math.PI / 2); break;
		default: break;
		}
		BufferedImage dst = new BufferedImage(gira ? h : w, gira ? w : h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dst.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, dst.getWidth(), dst.getHeight());
			g.drawImage(aRgb(img), t, null);
		} finally {
			g.dispose();
		}
		return dst;
	}

	/** EXIF orientation of a JPEG (1 when absent, unreadable or not a JPEG). */
	static int orientacionExif(byte[] b) {
		try {
			if (b.length < 4 || (b[0] & 0xFF) != 0xFF || (b[1] & 0xFF) != 0xD8) {
				return 1;
			}
			int i = 2;
			while (i + 4 <= b.length) {
				if ((b[i] & 0xFF) != 0xFF) {
					return 1;
				}
				int marker = b[i + 1] & 0xFF;
				int len = ((b[i + 2] & 0xFF) << 8) | (b[i + 3] & 0xFF);
				if (marker == 0xDA || marker == 0xD9) {
					return 1; // image data starts: no EXIF before it
				}
				if (marker == 0xE1 && len >= 8 && i + 4 + 6 <= b.length
						&& b[i + 4] == 'E' && b[i + 5] == 'x' && b[i + 6] == 'i' && b[i + 7] == 'f') {
					return orientacionTiff(b, i + 10, Math.min(b.length, i + 2 + len));
				}
				i += 2 + len;
			}
		} catch (RuntimeException e) {
			// Malformed metadata: keep the image as stored.
		}
		return 1;
	}

	private static int orientacionTiff(byte[] b, int tiff, int fin) {
		boolean little = b[tiff] == 'I' && b[tiff + 1] == 'I';
		int ifd = tiff + leer32(b, tiff + 4, little);
		int entradas = leer16(b, ifd, little);
		for (int k = 0; k < entradas; k++) {
			int e = ifd + 2 + k * 12;
			if (e + 12 > fin) {
				break;
			}
			if (leer16(b, e, little) == 0x0112) {
				int v = leer16(b, e + 8, little);
				return v >= 1 && v <= 8 ? v : 1;
			}
		}
		return 1;
	}

	private static int leer16(byte[] b, int i, boolean little) {
		return little ? (b[i] & 0xFF) | ((b[i + 1] & 0xFF) << 8) : ((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF);
	}

	private static int leer32(byte[] b, int i, boolean little) {
		return little
				? (b[i] & 0xFF) | ((b[i + 1] & 0xFF) << 8) | ((b[i + 2] & 0xFF) << 16) | ((b[i + 3] & 0xFF) << 24)
				: ((b[i] & 0xFF) << 24) | ((b[i + 1] & 0xFF) << 16) | ((b[i + 2] & 0xFF) << 8) | (b[i + 3] & 0xFF);
	}
}
