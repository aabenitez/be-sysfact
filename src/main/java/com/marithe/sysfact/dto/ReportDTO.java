package py.com.ventasjdbc.dto;

import org.springframework.core.io.Resource;

public class ReportDTO {
    private Resource resource;
    private String contentType;

    public ReportDTO(Resource resource, String contentType) {
        this.resource = resource;
        this.contentType = contentType;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
