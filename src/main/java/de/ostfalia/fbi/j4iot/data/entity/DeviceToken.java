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
        @Index(columnList = "token", unique = true) // token really has to be unique
})
public class DeviceToken extends AbstractEntity {
    @ManyToOne
    @JsonIgnoreProperties({"project", "tokens"})
    @NotNull Device device;

    @NotNull @NotEmpty
    private String token;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    @NotNull private Instant expiresAt;
    private Instant lastUseAt = null;


    public DeviceToken() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public DeviceToken(Device device, String token, Instant expiresAt) {
        this.device = device;
        this.token = token;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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
