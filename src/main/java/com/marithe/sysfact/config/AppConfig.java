package com.marithe.sysfact.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.marithe.sysfact.constants.ApplicationConstants;
import com.marithe.sysfact.service.ParametroService;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.TimeZone;

@Configuration
public class AppConfig {

	@Autowired
	ParametroService parametroService;

	@Primary
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.setResultsMapCaseInsensitive(true);
		return jdbcTemplate;
	}

	/*
	 * @Bean public WebMvcConfigurer corsConfigurer() { return new
	 * WebMvcConfigurer() {
	 * 
	 * @Override public void addCorsMappings(CorsRegistry registry) {
	 * registry.addMapping("/**").allowedMethods("PUT", "DELETE", "POST", "GET"); }
	 * }; }
	 */

	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	}

	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

		String emailHost = parametroService.findByCodigo(ApplicationConstants.SMTP_HOST).getValor();
		String emailPort = parametroService.findByCodigo(ApplicationConstants.SMTP_PORT).getValor();
		String emailUser = parametroService.findByCodigo(ApplicationConstants.EMAIL_USER).getValor();
		String emailPassword = parametroService.findByCodigo(ApplicationConstants.EMAIL_PASSWORD).getValor();

		javaMailSender.setHost(emailHost);
		javaMailSender.setPort(Integer.parseInt(emailPort));
		javaMailSender.setUsername(emailUser);
		javaMailSender.setPassword(emailPassword);
		javaMailSender.setProtocol("smtp");

		javaMailSender.setJavaMailProperties(getMailProperties());

		return javaMailSender;
	}

	private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.put("spring.mail.protocol", "smtp");
        properties.put("mail.smtps.starttls.enable", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        return properties;
	}
}
