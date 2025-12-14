package py.com.ventasjdbc.dao.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:sql/dao/cliente/cliente_sql.properties")
public class ClienteSql {

	@Value("${cliente.getAll}")
	private String getAll;

	@Value("${cliente.findById}")
	private String findById;

	@Value("${cliente.insert}")
	private String insert;

	@Value("${cliente.update}")
	private String update;

	@Value("${cliente.delete}")
	private String delete;

	public String getGetAll() {
		return getAll;
	}

	public String getFindById() {
		return findById;
	}

	public String getInsert() {
		return insert;
	}

	public String getUpdate() {
		return update;
	}

	public String getDelete() {
		return delete;
	}

}
