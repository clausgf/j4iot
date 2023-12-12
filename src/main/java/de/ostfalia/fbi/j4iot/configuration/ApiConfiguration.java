package de.ostfalia.fbi.j4iot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "j4iot.api")
public class ApiConfiguration {

    private int port;
    private String path;
    private Boolean logging;

    @Value("${server.servlet.context-path}")
    private String servletContextPath;

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getApiPathPrefix() {
        String pathPrefix = servletContextPath;
        if (pathPrefix.endsWith("/")) {
            pathPrefix = pathPrefix.substring(0, pathPrefix.length() - 1);
        }
        String path = getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return pathPrefix + "/" + path;
    }

    public Boolean getLogging() {
        return logging;
    }
    public void setLogging(Boolean logging) {
        this.logging = logging;
    }

    public String getServletContextPath() {
        return servletContextPath;
    }
    public void setServletContextPath(String servletContextPath) {
        this.servletContextPath = servletContextPath;
    }
}
