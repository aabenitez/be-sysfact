package py.com.ventasjdbc.dao.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:sql/dao/rol/rol_sql.properties")
public class RolSql {

	@Value("${rol.getAll}")
	private String getAll;

	@Value("${rol.findById}")
	private String findById;

	@Value("${rol.insert}")
	private String insert;

	@Value("${rol.update}")
	private String update;

	@Value("${rol.delete}")
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
