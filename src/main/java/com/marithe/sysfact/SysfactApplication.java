package com.marithe.sysfact;

import com.marithe.sysfact.config.FileStorageConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ FileStorageConfig.class })
public class SysfactApplication {

	public static void main(String[] args) {
		SpringApplication.run(SysfactApplication.class, args);
	}

}
