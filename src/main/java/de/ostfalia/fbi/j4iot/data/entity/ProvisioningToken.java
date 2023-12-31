package de.ostfalia.fbi.j4iot.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table( indexes = { @Index(columnList = "token", unique = true) })
public class ProvisioningToken extends AbstractEntity {

    // ***********************************************************************

    @ManyToOne
    @JsonIgnoreProperties({"provisioningTokens", "devices", "forwardings"})
    @NotNull Project project;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    // tokens are unique to enable finding the device by token
    // this could be realized by encoding the device id into the token
    @NotNull @NotEmpty @Column(length = 160, unique = true)
    private String token;

    @NotNull private Instant expiresAt;
    private Instant lastUseAt = null;

    // ***********************************************************************

    public ProvisioningToken() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public ProvisioningToken(Project project, String token, Instant expiresAt) {
        this.project = project;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // ***********************************************************************

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
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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

    // ***********************************************************************

}
