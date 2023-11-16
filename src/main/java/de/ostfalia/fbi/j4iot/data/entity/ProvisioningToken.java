package de.ostfalia.fbi.j4iot.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(indexes = {
        @Index(columnList = "token", unique = true)
})
public class ProvisioningToken extends AbstractEntity {
    @ManyToOne
    @JsonIgnoreProperties({"provisioningTokens", "devices"})
    @NotNull Project project;

    @NotNull @NotEmpty @Column(unique = true)
    private String token; // TODO unique+index
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    @NotNull private Integer authTokenExpiresInSeconds = 7*24*60*60;
    @NotNull private Instant expiresAt;
    private Instant lastUseAt = null;


    public ProvisioningToken() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public ProvisioningToken(Project project, String token, Integer authTokenExpiresInSeconds, Instant expiresAt) {
        this.project = project;
        this.token = token;
        this.authTokenExpiresInSeconds = authTokenExpiresInSeconds;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Integer getAuthTokenExpiresInSeconds() {
        return authTokenExpiresInSeconds;
    }

    public void setAuthTokenExpiresInSeconds(Integer authTokenExpiresInSeconds) {
        this.authTokenExpiresInSeconds = authTokenExpiresInSeconds;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUseAt() {
        return lastUseAt;
    }

    public void setLastUseAt(Instant lastUseAt) {
        this.lastUseAt = lastUseAt;
    }

}
