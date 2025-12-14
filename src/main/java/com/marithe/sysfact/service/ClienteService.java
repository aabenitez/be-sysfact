package com.marithe.sysfact.service;

import org.springframework.stereotype.Service;
import com.marithe.sysfact.dao.ClienteDao;
import com.marithe.sysfact.dao.impl.GenericServiceImpl;
import com.marithe.sysfact.model.Cliente;

@Service
public class ClienteService extends GenericServiceImpl<Cliente> {

    public ClienteService(ClienteDao dao) {
        super(dao);
    }
}