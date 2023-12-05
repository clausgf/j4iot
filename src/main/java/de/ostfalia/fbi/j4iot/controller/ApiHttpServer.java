package de.ostfalia.fbi.j4iot.controller;

import de.ostfalia.fbi.j4iot.configuration.ApiConfiguration;
import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ApiHttpServer {

    private final Logger log = LoggerFactory.getLogger(ApiHttpServer.class);
    private final ApiConfiguration apiConfiguration;

    public ApiHttpServer(ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

        int apiPort = apiConfiguration.getPort();
        if (apiPort > 0) {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setPort(apiPort);
            tomcat.addAdditionalTomcatConnectors(connector);
            log.info("Listening on port {} for path {}", apiPort, apiConfiguration.getPath());
        }

        return tomcat;
    }
}
