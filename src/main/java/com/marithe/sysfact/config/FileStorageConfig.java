package py.com.ventasjdbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Deprecated
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {
	private String uploadDir;
	private String reportDir;

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public String getReportDir() {
		return reportDir;
	}

	public void setReportDir(String reportDir) {
		this.reportDir = reportDir;
	}
}
