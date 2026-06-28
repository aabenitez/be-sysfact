package com.marithe.sysfact.controller;

import com.marithe.sysfact.model.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import com.marithe.sysfact.model.Parametro;
import com.marithe.sysfact.service.ParametroService;

import javax.validation.Valid;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestScope
@RequestMapping("/parametros")
public class ParametroController {

    @Autowired
    private ParametroService parametroService;

    private Logger LOGGER = Logger.getLogger(getClass().getName());

    @GetMapping()
    public ResponseEntity<List<Parametro>> getAll() {
        try {
            List<Parametro> list = parametroService.getAll();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener los registros de la tabla parametro", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Parametro> findByCodigo(
            @PathVariable String codigo) {

        try {
            Parametro parametro = parametroService.findByCodigo(codigo);

            if (parametro == null) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return ResponseEntity.ok(parametro);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar el registro del parametro por codigo: " + codigo, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping()
    public ResponseEntity<Parametro> insert(@RequestBody @Validated(BaseEntity.OnCreate.class) Parametro obj) {
        try {
            Parametro nuevo = parametroService.insert(obj);
            return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}") // Esta anotación habilita el método PUT para esta ruta
    public ResponseEntity<Parametro> update(@RequestBody @Valid Parametro obj, @PathVariable Long id) {
        // Lógica de actualización
        Parametro objCurrent = parametroService.findById(id);
        if (objCurrent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        obj.setId(id);
        parametroService.update(obj);
        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            parametroService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}