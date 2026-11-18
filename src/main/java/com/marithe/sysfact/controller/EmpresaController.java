package com.marithe.sysfact.controller;

import com.marithe.sysfact.service.EmpresaImagenService;
import com.marithe.sysfact.service.EmpresaImagenService.Imagen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/empresa")
public class EmpresaController {

    @Autowired
    private EmpresaImagenService imagenService;

    /**
     * Public (the login page shows it too). Answers 404 when no custom logo is configured, so the
     * frontend falls back to its default image.
     */
    @GetMapping("/logo")
    public ResponseEntity<Resource> getLogo() {
        return serve(Imagen.LOGO);
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadLogo(@RequestParam("archivo") MultipartFile archivo) {
        return upload(Imagen.LOGO, archivo);
    }

    @DeleteMapping("/logo")
    public ResponseEntity<Void> resetLogo() {
        imagenService.reset(Imagen.LOGO);
        return ResponseEntity.noContent().build();
    }

    /** Public, like the logo. Answers 404 when none is configured (the default favicon.ico is used). */
    @GetMapping("/favicon")
    public ResponseEntity<Resource> getFavicon() {
        return serve(Imagen.FAVICON);
    }

    @PostMapping(value = "/favicon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFavicon(@RequestParam("archivo") MultipartFile archivo) {
        return upload(Imagen.FAVICON, archivo);
    }

    @DeleteMapping("/favicon")
    public ResponseEntity<Void> resetFavicon() {
        imagenService.reset(Imagen.FAVICON);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Resource> serve(Imagen imagen) {
        Optional<Path> found = imagenService.get(imagen);
        if (!found.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Path file = found.get();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagenService.getContentType(file)))
                .cacheControl(CacheControl.noCache())
                .header("X-Content-Type-Options", "nosniff")
                // An uploaded SVG opened on its own must not be able to run scripts.
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; img-src data:")
                .body(new FileSystemResource(file));
    }

    private ResponseEntity<Map<String, String>> upload(Imagen imagen, MultipartFile archivo) {
        String path = imagenService.store(imagen, archivo);
        return new ResponseEntity<>(Collections.singletonMap("valor", path), HttpStatus.CREATED);
    }
}
