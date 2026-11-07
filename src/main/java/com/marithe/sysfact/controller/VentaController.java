package com.marithe.sysfact.controller;

import com.marithe.sysfact.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import com.marithe.sysfact.model.Venta;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestScope
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping()
    public ResponseEntity<List<Venta>> getAll() {
        List<Venta> lista = ventaService.getList(null);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> findById(@PathVariable Long id) {
        Venta venta = ventaService.getById(id);
        if (venta == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(venta);
    }

    @PostMapping()
    public ResponseEntity<Venta> insert(@RequestBody @Valid Venta venta) {
        Venta nuevaVenta = ventaService.insert(venta);
        return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
    }
}