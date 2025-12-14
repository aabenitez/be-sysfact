package py.com.ventasjdbc.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import py.com.ventasjdbc.config.FileStorageConfig;
import py.com.ventasjdbc.constants.ApplicationConstants;
import py.com.ventasjdbc.exception.FileNotFoundException;
import py.com.ventasjdbc.exception.FileStorageException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@Service
public class FileStorageService {
	protected Path fileStorageLocation;
	
	private Logger LOGGER = Logger.getLogger(getClass().getName());

	@Autowired
	private ParametroService parametroService;

	@PostConstruct
	public void initialize() {
		String uploadDir = parametroService.findByCodigo(ApplicationConstants.UPLOADS_PATH).getValor();
		this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
		createDirectory();
	}

	public FileStorageService() {}

	public String storeFile(MultipartFile file) {
		// Normalize file name
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());

		try {
			// Check if the file's name contains invalid characters
			if (fileName.contains("..")) {
				throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
			}

			// Copy file to the target location (Replacing existing file with the same name)
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return fileName;
		} catch (IOException ex) {
			throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
		}
	}

	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			
			LOGGER.log(Level.FINE, "File path:", filePath);
			LOGGER.log(Level.FINE, "File resource:", resource);
			
			if (resource.exists()) {
				return resource;
			} else {
				throw new FileNotFoundException("File not found " + fileName);
			}
		} catch (MalformedURLException ex) {
			throw new FileNotFoundException("File not found " + fileName, ex);
		}
	}

	public String getContentTypeFromRequest(HttpServletRequest request, Resource resource) {
		// Fallback to the default content type if type could not be determined
		String contentType = "application/octet-stream";
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			LOGGER.info("Could not determine file type.");
		}

		return contentType;
	}

	protected void createDirectory() {
		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
					ex);
		}
	}
}

