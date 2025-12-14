package py.com.ventasjdbc.dao.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:sql/dao/usuario/usuario_sql.properties")
public class UsuarioDaoSql {

	@Value("${query.getAll}")
	private String getAll;
	@Value("${query.findById}")
	private String findById;
	@Value("${query.findByEmail}")
	private String findByEmail;
	@Value("${query.validate}")
	private String validate;
	@Value("${query.create}")
	private String create;
	@Value("${query.update}")
	private String update;
	@Value("${query.delete}")
	private String delete;
	@Value("${query.changePassword}")
	private String changePassword;
	@Value("${query.findByPasswordToken}")
	private String findByPasswordToken;
	@Value("${query.setPasswordToken}")
	private String setPasswordToken;

	public String getGetAll() {
		return getAll;
	}

	public String getFindById() {
		return findById;
	}

	public String getFindByEmail() {
		return findByEmail;
	}

	public String getValidate() {
		return validate;
	}

	public String getCreate() {
		return create;
	}

	public String getDelete() {
		return delete;
	}

	public String getUpdate() {
		return update;
	}

	public String getChangePassword() {
		return changePassword;
	}

	public String getFindByPasswordToken() {
		return findByPasswordToken;
	}

	public String getSetPasswordToken() {
		return setPasswordToken;
	}
}
