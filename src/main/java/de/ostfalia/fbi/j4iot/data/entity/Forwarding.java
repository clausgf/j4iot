package de.ostfalia.fbi.j4iot.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        indexes = {
                @Index(columnList = "name"),
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "RedirectionUniqueNameAndProject", columnNames = { "name", "project_id" })
        })
public class Forwarding extends AbstractEntity {

    // ***********************************************************************

    @ManyToOne
    @JsonIgnoreProperties({"provisioningTokens", "devices", "forwardings"})
    @NotNull Project project;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(length = 80, unique = true)
    @NotNull @NotEmpty
    private String name = "";

    @NotNull @NotEmpty
    @Pattern(regexp = "^(http|https).*", message = "Please enter a valid http or https URL.")
    private String forwardToUrl = "https://";

    @NotNull private Boolean extendUrl = true;
    @NotNull private Boolean enableMethodGet = true;
    private Instant lastUseAt = null;


    // ***********************************************************************

    public Forwarding() {
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public Forwarding(Project project) {
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        this.project = project;
    }

    // ***********************************************************************

    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getForwardToUrl() {
        return forwardToUrl;
    }
    public void setForwardToUrl(String redirectUrl) {
        this.forwardToUrl = redirectUrl;
    }

    public Boolean getExtendUrl() {
        return extendUrl;
    }
    public void setExtendUrl(Boolean extendUrl) {
        this.extendUrl = extendUrl;
    }

    public Boolean getEnableMethodGet() {
        return enableMethodGet;
    }
    public void setEnableMethodGet(Boolean enableMethodGet) {
        this.enableMethodGet = enableMethodGet;
    }

    public Instant getLastUseAt() {
        return lastUseAt;
    }
    public void setLastUseAt(Instant lastUseAt) {
        this.lastUseAt = lastUseAt;
    }

    // ***********************************************************************

}
