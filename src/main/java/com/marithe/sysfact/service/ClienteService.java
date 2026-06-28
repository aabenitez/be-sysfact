package com.marithe.sysfact.service;

import org.springframework.stereotype.Service;
import com.marithe.sysfact.dao.ClienteDao;
import com.marithe.sysfact.model.Cliente;

import java.util.List;

public interface ClienteService {

    List<Cliente> getAll(Cliente obj);

    Cliente findById(Long id); // Necesario para validar antes de actualizar

    Cliente insert(Cliente obj);
    void update(Cliente obj);
    void delete(Long id);
}