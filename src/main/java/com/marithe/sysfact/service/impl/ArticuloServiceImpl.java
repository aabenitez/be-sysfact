package com.marithe.sysfact.service.impl;

import com.marithe.sysfact.dao.ArticuloDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Articulo;
import com.marithe.sysfact.service.ArticuloService;
import com.marithe.sysfact.service.ImpuestoService;
import com.marithe.sysfact.service.ArticuloImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ArticuloServiceImpl implements ArticuloService {

    @Autowired
    private ArticuloDao articuloDao;

    @Autowired
    private ImpuestoService impuestoService;

    @Autowired
    private ArticuloImagenService imagenService;

    @Override
    public List<Articulo> getList(Articulo obj) {
        return articuloDao.getList(obj);
    }

    @Override
    public Articulo getById(Long id) {
        return articuloDao.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Articulo insert(Articulo obj) {
        if (obj.getTasaIva() == null) {
            obj.setTasaIva(impuestoService.tasaGeneral());
        }
        ImpuestoService.validarTasa(obj.getTasaIva());
        normalizar(obj);
        // The photo is uploaded separately (POST /articulos/{id}/imagen).
        obj.setNombreImagen(null);

        Articulo nuevo = articuloDao.insert(obj);
        // Toda venta descuenta stock, así que cada artículo necesita su fila (existencia inicial 0).
        articuloDao.insertStock(nuevo.getId());
        return nuevo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Articulo update(Articulo obj) {
        Articulo actual = articuloDao.getById(obj.getId());
        if (obj.getTasaIva() == null) {
            // Si no se envía, se conserva la tasa que ya tenía.
            obj.setTasaIva(actual != null ? actual.getTasaIva() : impuestoService.tasaGeneral());
        }
        // The photo only changes through its own endpoints; whatever the form sends is ignored.
        obj.setNombreImagen(actual != null ? actual.getNombreImagen() : null);
        ImpuestoService.validarTasa(obj.getTasaIva());
        normalizar(obj);
        return articuloDao.update(obj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        long ventas = articuloDao.countVentas(id);
        if (ventas > 0) {
            throw new BusinessLogicException("No se puede eliminar el artículo porque figura en "
                    + ventas + (ventas == 1 ? " venta registrada." : " ventas registradas."));
        }
        Articulo actual = articuloDao.getById(id);
        articuloDao.deleteStock(id);
        articuloDao.delete(id);
        if (actual != null) {
            imagenService.borrarAlConfirmar(actual.getNombreImagen());
        }
    }

    private static void normalizar(Articulo obj) {
        obj.setNombre(obj.getNombre().trim());
        if (obj.getDescripcion() != null) {
            String d = obj.getDescripcion().trim();
            obj.setDescripcion(d.isEmpty() ? null : d);
        }
        if (obj.getUnidadMedida().getId() == null) {
            throw new BusinessLogicException("Debe especificar una unidad de medida.");
        }
        if (obj.getProveedor().getId() == null) {
            throw new BusinessLogicException("Debe especificar un proveedor.");
        }
    }
}
