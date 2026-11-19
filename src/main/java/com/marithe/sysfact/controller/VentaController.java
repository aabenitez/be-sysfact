package com.marithe.sysfact.controller;

import com.marithe.sysfact.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import com.marithe.sysfact.model.Auditoria;
import com.marithe.sysfact.model.Venta;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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

    @PutMapping("/{id}")
    public ResponseEntity<Venta> update(@PathVariable Long id, @RequestBody @Valid Venta venta) {
        Venta actualizada = ventaService.update(id, venta);
        return actualizada == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : ResponseEntity.ok(actualizada);
    }

    /** Body: {"motivo": "..."}. Sales are cancelled, never deleted. */
    @PostMapping("/{id}/anular")
    public ResponseEntity<Venta> anular(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Venta anulada = ventaService.anular(id, body == null ? null : body.get("motivo"));
        return anulada == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : ResponseEntity.ok(anulada);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<Auditoria>> historial(@PathVariable Long id) {
        if (ventaService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(ventaService.historial(id));
    }
}
