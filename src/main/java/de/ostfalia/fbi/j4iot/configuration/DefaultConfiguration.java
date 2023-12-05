package de.ostfalia.fbi.j4iot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "j4iot.default")
public class DefaultConfiguration {

    private String username;
    private String password;
    private Boolean createExampleData;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getCreateExampleData() {
        return createExampleData;
    }
    public void setCreateExampleData(Boolean createExampleData) {
        this.createExampleData = createExampleData;
    }
}
