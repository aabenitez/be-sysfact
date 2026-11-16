package com.marithe.sysfact.service;

import com.marithe.sysfact.dao.ProveedorDao;
import com.marithe.sysfact.exception.BusinessLogicException;
import com.marithe.sysfact.model.Proveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorDao dao;

    public List<Proveedor> getAll() {
        return dao.getAll();
    }

    public Proveedor getById(Long id) {
        return dao.getById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Proveedor insert(Proveedor p) {
        normalizar(p);
        return dao.insert(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public Proveedor update(Proveedor p) {
        normalizar(p);
        return dao.update(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        long articulos = dao.countArticulos(id);
        if (articulos > 0) {
            throw new BusinessLogicException("No se puede eliminar el proveedor porque tiene "
                    + articulos + (articulos == 1 ? " artículo asociado." : " artículos asociados.")
                    + " Asigná esos artículos a otro proveedor primero.");
        }
        dao.delete(id);
    }

    /** Trims text and stores empty optional fields as null. */
    private static void normalizar(Proveedor p) {
        p.setNombre(p.getNombre().trim());
        p.setContacto(vacioANull(p.getContacto()));
        p.setTelefono(vacioANull(p.getTelefono()));
        p.setDireccion(vacioANull(p.getDireccion()));
    }

    private static String vacioANull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
