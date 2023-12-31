package de.ostfalia.fbi.j4iot.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Entity()
@Table(
        indexes = {
                @Index(columnList = "name"),
                @Index(columnList = "tags"),
                @Index(columnList = "location"),
                @Index(columnList = "lastSeenAt")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "DeviceUniqueNameAndProject", columnNames = { "name", "project_id" })
        })
public class Device extends AbstractEntity {

    // ***********************************************************************

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    @ManyToOne
    @JsonIgnoreProperties({"provisioningTokens", "devices", "forwardings"})
    @NotNull
    Project project;

    @Column(length = 80)
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+]*$", message = "Name must start with a letter or a number, the rest can also contain plus, minus or underscores.")
    @NotNull @NotEmpty
    private String name = "";

    @NotNull private String description = "";
    @NotNull private String location = "";
    @NotNull private String tags = "";

    @NotNull private Boolean provisioningApproved = false;

    private Instant lastProvisioningRequestAt = null;
    private Instant lastProvisionedAt = null;
    private Instant lastSeenAt = null;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) @OrderBy("createdAt desc")
    private List<DeviceToken> deviceTokens = new LinkedList<>();

    // ***********************************************************************

    public Device() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Device(Project project, String name, Boolean provisioningApproved) {
        this.project = project;
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.provisioningApproved = provisioningApproved;
    }

    public Device(Project project, String name, String description, String location, String tags) {
        this.project = project;
        this.name = name;
        this.description = description;
        this.location = location;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.tags = tags;
    }

    // ***********************************************************************

    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
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

    public Boolean getProvisioningApproved() {
        return provisioningApproved;
    }
    public void setProvisioningApproved(Boolean provisioningApproved) {
        this.provisioningApproved = provisioningApproved;
    }

    public Instant getLastProvisioningRequestAt() {
        return lastProvisioningRequestAt;
    }
    public void setLastProvisioningRequestAt(Instant lastProvisioningRequestAt) {
        this.lastProvisioningRequestAt = lastProvisioningRequestAt;
    }

    public Instant getLastProvisionedAt() {
        return lastProvisionedAt;
    }
    public void setLastProvisionedAt(Instant lastProvisionedAt) {
        this.lastProvisionedAt = lastProvisionedAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }
    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public List<DeviceToken> getDeviceTokens() {
        return deviceTokens;
    }
    public void setDeviceTokens(List<DeviceToken> deviceTokens) {
        this.deviceTokens = deviceTokens;
    }

    // ***********************************************************************

}
