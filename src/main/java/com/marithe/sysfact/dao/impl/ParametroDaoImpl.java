package py.com.ventasjdbc.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import py.com.ventasjdbc.dao.ParametroDao;
import py.com.ventasjdbc.dao.mapper.ParametroMapper;
import py.com.ventasjdbc.dao.sql.ParametroSql;
import py.com.ventasjdbc.model.Parametro;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository
public class ParametroDaoImpl implements ParametroDao {

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    private ParametroSql daoSql;

    private Logger logger = Logger.getLogger(getClass().getName());

    public List<Parametro> getAll() {
        String sql = daoSql.getGetAll();
        logger.info("sql a ejecutar: " + sql);
        return jdbcTemplate.query(sql, new ArrayList<>().toArray(), new ParametroMapper());
    }

    public Parametro findByCodigo(String codigo) {
        String sql = daoSql.getFindByCodigo();
        return jdbcTemplate.queryForObject(sql, new Object[]{codigo}, new ParametroMapper());
    }
}
