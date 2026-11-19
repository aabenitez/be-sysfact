package com.marithe.sysfact.controller;

import com.marithe.sysfact.service.ArticuloImagenService;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.service.ArticuloService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestScope
@RequestMapping("/articulos")
public class ArticuloController {

    @Autowired
    private ArticuloService service;

    @Autowired
    private ArticuloImagenService imagenService;

    @GetMapping()
    public ResponseEntity<List<Articulo>> listarArticulos(Articulo obj) {
        List<Articulo> list = service.getList(obj);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Articulo> listarPorId(@PathVariable Long id) {
        Articulo obj = service.getById(id);
        if (obj == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.ok(obj);
        }
    }

    @PostMapping()
    public ResponseEntity<Articulo> insert(@RequestBody @Valid Articulo obj) {
        Articulo objInsert = service.insert(obj);
        return new ResponseEntity<>(objInsert, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Articulo> update(@RequestBody @Valid Articulo obj, @PathVariable Long id) {
        Articulo objCurrent = service.getById(id);
        if (objCurrent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        obj.setId(id);
        service.update(obj);
        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Articulo obj = service.getById(id);
        if (obj == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            service.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    /**
     * The article's photo (a medium-size JPEG). Public, like the company logo, because an <img> tag
     * cannot send the session token. The URL the frontend uses carries the file name, so each new
     * photo is a new URL and the browser may cache it for good.
     */
    @GetMapping("/{id}/imagen")
    public ResponseEntity<Resource> imagen(@PathVariable Long id) {
        Articulo obj = service.getById(id);
        if (obj == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return imagenService.get(obj)
                .<ResponseEntity<Resource>>map((Path file) -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                        .body(new PathResource(file)))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** Uploads (or replaces) the photo. Multipart field: archivo. */
    @PostMapping("/{id}/imagen")
    public ResponseEntity<Articulo> subirImagen(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        if (service.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(imagenService.guardar(id, archivo));
    }

    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Articulo> quitarImagen(@PathVariable Long id) {
        if (service.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(imagenService.quitar(id));
    }
}