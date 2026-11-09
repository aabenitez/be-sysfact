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
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Company logo shown in the application header.
 *
 * <p>The logo location is kept in the {@code empresa.logo} parameter. Its value is the file path:
 * either absolute, or relative to the {@code uploads.path} directory. Logos uploaded through the
 * API are stored in {@code <uploads.path>/logos}.</p>
 */
@Service
public class EmpresaLogoService {

    public static final long MAX_SIZE_BYTES = 2L * 1024 * 1024;

    /** Allowed extensions and the content type each one is served with. */
    private static final Map<String, String> CONTENT_TYPES;

    static {
        Map<String, String> types = new HashMap<>();
        types.put("png", "image/png");
        types.put("jpg", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("webp", "image/webp");
        types.put("gif", "image/gif");
        types.put("svg", "image/svg+xml");
        CONTENT_TYPES = Collections.unmodifiableMap(types);
    }

    private static final String LOGOS_DIR = "logos";
    private static final String DESCRIPCION = "Logo de la empresa (ruta del archivo)";

    private final Logger LOGGER = Logger.getLogger(getClass().getName());

    @Autowired
    private ParametroService parametroService;

    /** The current logo file, if the parameter is active and the file exists. */
    public Optional<Path> getLogo() {
        return findParametro()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()) || p.getActivo() == null)
                .map(Parametro::getValor)
                .filter(StringUtils::hasText)
                .map(this::resolve)
                .filter(Files::isReadable);
    }

    public String getContentType(Path file) {
        return CONTENT_TYPES.getOrDefault(extension(file.getFileName().toString()), "application/octet-stream");
    }

    /** Saves the uploaded image and points {@code empresa.logo} at it. Returns the stored path. */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessLogicException("Debe seleccionar una imagen.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessLogicException("La imagen no puede superar los 2 MB.");
        }
        String ext = extension(StringUtils.cleanPath(String.valueOf(file.getOriginalFilename())));
        if (!CONTENT_TYPES.containsKey(ext)) {
            throw new BusinessLogicException("Formato no permitido. Use PNG, JPG, WEBP, GIF o SVG.");
        }

        Path dir = uploadsDir().resolve(LOGOS_DIR);
        Path target = dir.resolve("logo-empresa-" + System.currentTimeMillis() + "." + ext);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo guardar el logo en " + dir
                    + ". Verifique que el directorio exista y que el servidor tenga permiso de escritura.", ex);
        }

        Optional<Parametro> current = findParametro();
        current.map(Parametro::getValor).ifPresent(this::deleteIfUploaded);
        save(current, target.toString(), true);
        return target.toString();
    }

    /** Stops using the custom logo, so the application falls back to its default one. */
    public void reset() {
        Optional<Parametro> current = findParametro();
        if (!current.isPresent()) {
            return;
        }
        deleteIfUploaded(current.get().getValor());
        save(current, current.get().getValor(), false);
    }

    private void save(Optional<Parametro> current, String valor, boolean activo) {
        Parametro p = current.orElseGet(Parametro::new);
        p.setCodigo(ApplicationConstants.EMPRESA_LOGO);
        p.setDescripcion(StringUtils.hasText(p.getDescripcion()) ? p.getDescripcion() : DESCRIPCION);
        p.setValor(valor);
        p.setActivo(activo);
        if (p.getId() == null) {
            parametroService.insert(p);
        } else {
            parametroService.update(p);
        }
    }

    /** Looks the parameter up whether it is active or not (findByCodigo only returns active ones). */
    private Optional<Parametro> findParametro() {
        return parametroService.getAll().stream()
                .filter(p -> ApplicationConstants.EMPRESA_LOGO.equals(p.getCodigo()))
                .findFirst();
    }

    /** Only files this service uploaded are deleted; a path set by hand is left alone. */
    private void deleteIfUploaded(String valor) {
        if (!StringUtils.hasText(valor)) {
            return;
        }
        Path file = resolve(valor);
        Path logosDir = uploadsDir().resolve(LOGOS_DIR);
        if (file.startsWith(logosDir)) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "No se pudo borrar el logo anterior " + file, ex);
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
