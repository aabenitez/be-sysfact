package py.com.ventasjdbc.service;

import org.springframework.stereotype.Service;
import py.com.ventasjdbc.dao.ClienteDao;
import py.com.ventasjdbc.dao.impl.GenericServiceImpl;
import py.com.ventasjdbc.model.Cliente;

@Service
public class ClienteService extends GenericServiceImpl<Cliente> {

    public ClienteService(ClienteDao dao) {
        super(dao);
    }
}