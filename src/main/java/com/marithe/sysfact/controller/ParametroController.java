package com.marithe.sysfact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import com.marithe.sysfact.model.Parametro;
import com.marithe.sysfact.service.ParametroService;

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
}