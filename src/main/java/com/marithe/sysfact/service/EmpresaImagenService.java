package com.marithe.sysfact.service;

import com.marithe.sysfact.constants.ApplicationConstants;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.exception.FileStorageException;
import com.marithe.sysfact.model.Parametro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Company images: the logo shown in the application header and the favicon shown in the
 * browser tab.
 *
 * <p>Each image's location is kept in a parameter ({@code empresa.logo}, {@code empresa.favicon}).
 * Its value is the file path: either absolute, or relative to the {@code uploads.path} directory.
 * Images uploaded through the API are stored in a subdirectory of {@code uploads.path}.</p>
 */
@Service
public class EmpresaImagenService {

    /** The images the company can customize, with the rules for each one. */
    public enum Imagen {
        LOGO(ApplicationConstants.EMPRESA_LOGO, "logos", "logo-empresa", "el logo",
                "Logo de la empresa (ruta del archivo)", 2L * 1024 * 1024, "2 MB",
                "PNG, JPG, WEBP, GIF o SVG", "png", "jpg", "jpeg", "webp", "gif", "svg"),
        FAVICON(ApplicationConstants.EMPRESA_FAVICON, "favicons", "favicon", "el favicon",
                "Ícono de la pestaña del navegador (ruta del archivo)", 512L * 1024, "512 KB",
                "ICO, PNG o SVG", "ico", "png", "svg");

        final String codigo;
        final String dir;
        final String filePrefix;
        final String nombre;
        final String descripcion;
        final long maxBytes;
        final String maxLabel;
        final String formatsLabel;
        final Set<String> extensions;

        Imagen(String codigo, String dir, String filePrefix, String nombre, String descripcion,
               long maxBytes, String maxLabel, String formatsLabel, String... extensions) {
            this.codigo = codigo;
            this.dir = dir;
            this.filePrefix = filePrefix;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.maxBytes = maxBytes;
            this.maxLabel = maxLabel;
            this.formatsLabel = formatsLabel;
            this.extensions = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(extensions)));
        }
    }

    /** Content type each allowed extension is served with. */
    private static final Map<String, String> CONTENT_TYPES;

    static {
        Map<String, String> types = new HashMap<>();
        types.put("png", "image/png");
        types.put("jpg", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("webp", "image/webp");
        types.put("gif", "image/gif");
        types.put("svg", "image/svg+xml");
        types.put("ico", "image/x-icon");
        CONTENT_TYPES = Collections.unmodifiableMap(types);
    }

    private final Logger LOGGER = Logger.getLogger(getClass().getName());

    @Autowired
    private ParametroService parametroService;

    /** The current image file, if its parameter is active and the file exists. */
    public Optional<Path> get(Imagen imagen) {
        return findParametro(imagen)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()) || p.getActivo() == null)
                .map(Parametro::getValor)
                .filter(StringUtils::hasText)
                .map(this::resolve)
                .filter(Files::isReadable);
    }

    public String getContentType(Path file) {
        return CONTENT_TYPES.getOrDefault(extension(file.getFileName().toString()), "application/octet-stream");
    }

    /** Saves the uploaded image and points its parameter at it. Returns the stored path. */
    public String store(Imagen imagen, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessLogicException("Debe seleccionar una imagen.");
        }
        if (file.getSize() > imagen.maxBytes) {
            throw new BusinessLogicException("La imagen no puede superar los " + imagen.maxLabel + ".");
        }
        String ext = extension(StringUtils.cleanPath(String.valueOf(file.getOriginalFilename())));
        if (!imagen.extensions.contains(ext)) {
            throw new BusinessLogicException("Formato no permitido. Use " + imagen.formatsLabel + ".");
        }

        Path dir = uploadsDir().resolve(imagen.dir);
        Path target = dir.resolve(imagen.filePrefix + "-" + System.currentTimeMillis() + "." + ext);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo guardar " + imagen.nombre + " en " + dir
                    + ". Verifique que el directorio exista y que el servidor tenga permiso de escritura.", ex);
        }

        Optional<Parametro> current = findParametro(imagen);
        current.map(Parametro::getValor).ifPresent(v -> deleteIfUploaded(imagen, v));
        save(imagen, current, target.toString(), true);
        return target.toString();
    }

    /** Stops using the custom image, so the application falls back to its default one. */
    public void reset(Imagen imagen) {
        Optional<Parametro> current = findParametro(imagen);
        if (!current.isPresent()) {
            return;
        }
        deleteIfUploaded(imagen, current.get().getValor());
        save(imagen, current, current.get().getValor(), false);
    }

    private void save(Imagen imagen, Optional<Parametro> current, String valor, boolean activo) {
        Parametro p = current.orElseGet(Parametro::new);
        p.setCodigo(imagen.codigo);
        p.setDescripcion(StringUtils.hasText(p.getDescripcion()) ? p.getDescripcion() : imagen.descripcion);
        p.setValor(valor);
        p.setActivo(activo);
        if (p.getId() == null) {
            parametroService.insert(p);
        } else {
            parametroService.update(p);
        }
    }

    /** Looks the parameter up whether it is active or not (findByCodigo only returns active ones). */
    private Optional<Parametro> findParametro(Imagen imagen) {
        return parametroService.getAll().stream()
                .filter(p -> imagen.codigo.equals(p.getCodigo()))
                .findFirst();
    }

    /** Only files this service uploaded are deleted; a path set by hand is left alone. */
    private void deleteIfUploaded(Imagen imagen, String valor) {
        if (!StringUtils.hasText(valor)) {
            return;
        }
        Path file = resolve(valor);
        if (file.startsWith(uploadsDir().resolve(imagen.dir))) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "No se pudo borrar la imagen anterior " + file, ex);
            }
        }
    }

    private Path resolve(String valor) {
        Path path = Paths.get(valor.trim());
        return (path.isAbsolute() ? path : uploadsDir().resolve(path)).normalize();
    }

    private Path uploadsDir() {
        Parametro uploads = parametroService.findByCodigo(ApplicationConstants.UPLOADS_PATH);
        return Paths.get(uploads.getValor()).toAbsolutePath().normalize();
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
