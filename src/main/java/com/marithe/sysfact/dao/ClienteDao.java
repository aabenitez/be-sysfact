package com.marithe.sysfact.dao;

import com.marithe.sysfact.model.Cliente;

import java.util.List;

public interface ClienteDao {
    List<Cliente> getAll(Cliente obj);

    Cliente findById(Long id);

    Cliente insert(Cliente obj);
    Cliente update(Cliente obj);
    void delete(Long id);
}
