package com.marithe.sysfact.dao.sql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:sql/dao/unidadMedida/unidad_medida_sql.properties")
public class UnidadMedidaSql {

	@Value("${unidadMedida.getAll}")
	private String getAll;

	@Value("${unidadMedida.findById}")
	private String findById;

	@Value("${unidadMedida.insert}")
	private String insert;

	@Value("${unidadMedida.update}")
	private String update;

	@Value("${unidadMedida.delete}")
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
