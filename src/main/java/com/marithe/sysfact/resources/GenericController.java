package com.marithe.sysfact.resources;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.marithe.sysfact.model.BaseEntity;

import javax.validation.Valid;
import java.util.List;

public abstract class GenericController<T extends BaseEntity> {
    private final GenericService<T> service;

    public GenericController(GenericService<T> service) {
        this.service = service;
    }

    @GetMapping()
    public ResponseEntity<List<T>> getAll(T obj) {
        return ResponseEntity.ok(service.getAll(obj));
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> findById(@PathVariable Long id) {
        T obj = service.findById(id);

        if (obj == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(obj);
    }

    @PostMapping
    public ResponseEntity<T> insert(@RequestBody @Valid T obj) {
        T createdObject = service.insert(obj);
        return new ResponseEntity<>(createdObject, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(@RequestBody @Valid T obj, @PathVariable Long id) {
        obj.setId(id);

        if (service.findById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        T updatedObject = service.update(obj);
        return new ResponseEntity<>(updatedObject, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
