package com.marithe.sysfact.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.marithe.sysfact.constants.ApplicationConstants;
import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.exception.FileStorageException;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.util.ImagenUtil;

/**
 * One photo per article, all in {@code <uploads.path>/articulos}. Every upload is converted to a
 * medium-size JPEG (at most {@value #LADO_MAXIMO} px per side); the list shows the same file small,
 * so there are no separate thumbnails. The file name includes the upload time, which makes each new
 * photo a new URL (browsers may cache them for good) and lets the old file be deleted.
 */
@Service
public class ArticuloImagenService {

	public static final String DIRECTORIO = "articulos";
	public static final int LADO_MAXIMO = 1000;
	static final float CALIDAD_JPEG = 0.85f;
	static final long MAX_BYTES = 8L * 1024 * 1024;

	/** Only names this service creates are ever resolved to a path. */
	private static final Pattern NOMBRE = Pattern.compile("articulo-\\d+-\\d+\\.jpg");

	private static final Logger LOGGER = Logger.getLogger(ArticuloImagenService.class.getName());

	@Autowired
	private ArticuloDao articuloDao;

	@Autowired
	private ParametroService parametroService;

	/** The photo of an article, if it has one and the file exists. */
	public Optional<Path> get(Articulo articulo) {
		return archivo(articulo.getNombreImagen()).filter(Files::isReadable);
	}

	/** Stores a new photo for the article (replacing the previous one) and returns the article. */
	@Transactional(rollbackFor = Exception.class)
	public Articulo guardar(Long articuloId, MultipartFile file) {
		Articulo articulo = existente(articuloId);
		if (file == null || file.isEmpty()) {
			throw new BusinessLogicException("Elegí una imagen.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new BusinessLogicException("La imagen no puede superar los 8 MB.");
		}
		byte[] jpeg;
		try {
			jpeg = ImagenUtil.aJpegMediano(file.getBytes(), LADO_MAXIMO, CALIDAD_JPEG);
		} catch (IOException e) {
			throw new BusinessLogicException("No se pudo leer la imagen subida.");
		}

		Path dir = directorio();
		String nombre = "articulo-" + articuloId + "-" + System.currentTimeMillis() + ".jpg";
		Path destino = dir.resolve(nombre);
		try {
			Files.createDirectories(dir);
			// Written aside and moved, so a half-written file is never served.
			Path tmp = Files.createTempFile(dir, ".subiendo-", ".tmp");
			Files.write(tmp, jpeg);
			Files.move(tmp, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException ex) {
			throw new FileStorageException("No se pudo guardar la imagen en " + dir
					+ ". Verifique que el directorio exista y que el servidor tenga permiso de escritura.", ex);
		}

		String anterior = articulo.getNombreImagen();
		articuloDao.updateImagen(articuloId, nombre);
		borrarAlConfirmar(anterior);
		articulo.setNombreImagen(nombre);
		return articulo;
	}

	/** Removes the article's photo, if any. */
	@Transactional(rollbackFor = Exception.class)
	public Articulo quitar(Long articuloId) {
		Articulo articulo = existente(articuloId);
		String anterior = articulo.getNombreImagen();
		articuloDao.updateImagen(articuloId, null);
		borrarAlConfirmar(anterior);
		articulo.setNombreImagen(null);
		return articulo;
	}

	/**
	 * Deletes the file once the current transaction commits (right away without one), so a rolled
	 * back change never leaves the database pointing to a deleted file.
	 */
	public void borrarAlConfirmar(String nombre) {
		if (nombre == null) {
			return;
		}
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					borrar(nombre);
				}
			});
		} else {
			borrar(nombre);
		}
	}

	/** Deletes a stored photo file; does nothing for names this service did not create. */
	public void borrar(String nombre) {
		archivo(nombre).ifPresent(f -> {
			try {
				Files.deleteIfExists(f);
			} catch (IOException ex) {
				LOGGER.log(Level.WARNING, "No se pudo borrar la imagen " + f, ex);
			}
		});
	}

	private Articulo existente(Long id) {
		Articulo a = articuloDao.getById(id);
		if (a == null) {
			throw new BusinessLogicException("El artículo " + id + " no existe.");
		}
		return a;
	}

	private Optional<Path> archivo(String nombre) {
		if (nombre == null || !NOMBRE.matcher(nombre).matches()) {
			return Optional.empty();
		}
		return Optional.of(directorio().resolve(nombre));
	}

	private Path directorio() {
		String uploads = parametroService.findByCodigo(ApplicationConstants.UPLOADS_PATH).getValor();
		return Paths.get(uploads.trim()).toAbsolutePath().normalize().resolve(DIRECTORIO);
	}
}
