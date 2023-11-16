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
public class AccessToken extends AbstractEntity {
    @ManyToOne
    @JsonIgnoreProperties({"project", "tokens"})
    @NotNull Device device;

    @NotNull @NotEmpty @Column(unique = true)
    private String token; // TODO unique+index
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    @NotNull private Instant expiresAt;
    private Instant lastUseAt = null;


    public AccessToken() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public AccessToken(Device device, String token, Instant expiresAt) {
        this.device = device;
        this.token = token;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.expiresAt = expiresAt;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
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
