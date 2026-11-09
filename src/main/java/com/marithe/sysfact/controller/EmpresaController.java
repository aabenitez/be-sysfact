package com.marithe.sysfact.controller;

import com.marithe.sysfact.service.EmpresaLogoService;
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
    private EmpresaLogoService logoService;

    /**
     * Public (the login page shows it too). Answers 404 when no custom logo is configured, so the
     * frontend falls back to its default image.
     */
    @GetMapping("/logo")
    public ResponseEntity<Resource> getLogo() {
        Optional<Path> logo = logoService.getLogo();
        if (!logo.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Path file = logo.get();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(logoService.getContentType(file)))
                .cacheControl(CacheControl.noCache())
                .header("X-Content-Type-Options", "nosniff")
                // An uploaded SVG opened on its own must not be able to run scripts.
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; img-src data:")
                .body(new FileSystemResource(file));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadLogo(@RequestParam("archivo") MultipartFile archivo) {
        String path = logoService.store(archivo);
        return new ResponseEntity<>(Collections.singletonMap("valor", path), HttpStatus.CREATED);
    }

    @DeleteMapping("/logo")
    public ResponseEntity<Void> resetLogo() {
        logoService.reset();
        return ResponseEntity.noContent().build();
    }
}
