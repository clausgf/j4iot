package de.ostfalia.fbi.j4iot.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        indexes = {
            @Index(columnList = "name", unique = true),
            @Index(columnList = "tags")
        })
public class Project extends AbstractEntity {

    // ***********************************************************************

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate // TODO https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#auditing.annotations
    private Instant updatedAt;

    @Column(length = 80, unique = true)
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+]*$", message = "Name must start with a letter or a number, the rest can also contain plus, minus or underscores.")
    @NotNull @NotEmpty
    private String name = "";

    @NotNull private String description = "";
    @NotNull private String tags = "";

    @NotNull private Integer defaultProvisioningTokenLength = 64;
    @NotNull private Integer defaultProvisioningTokenExpiresInSeconds = 365*24*60*60;
    @NotNull private Integer defaultDeviceTokenLength = 32;
    @NotNull private Integer defaultDeviceTokenExpiresInSeconds = 7*24*60*60;

    @NotNull private Boolean autocreateDevices = true;
    @NotNull private Boolean provisioningAutoapproval = true;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) @OrderBy("createdAt desc")
    private List<ProvisioningToken> provisioningTokens = new LinkedList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) @OrderBy("name")
    private List<Forwarding> forwardings = new LinkedList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL) @OrderBy("name")
    private List<Device> devices = new LinkedList<>();

    // ***********************************************************************

    public Project() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Project(String name, String description, String tags) {
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // ***********************************************************************

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

    public Integer getDefaultProvisioningTokenLength() {
        return defaultProvisioningTokenLength;
    }
    public void setDefaultProvisioningTokenLength(Integer provisioningTokenLength) {
        this.defaultProvisioningTokenLength = provisioningTokenLength;
    }

    public Integer getDefaultProvisioningTokenExpiresInSeconds() {
        return defaultProvisioningTokenExpiresInSeconds;
    }
    public void setDefaultProvisioningTokenExpiresInSeconds(Integer provisioningTokenExpiresInSeconds) {
        this.defaultProvisioningTokenExpiresInSeconds = provisioningTokenExpiresInSeconds;
    }

    public Integer getDefaultDeviceTokenLength() {
        return defaultDeviceTokenLength;
    }
    public void setDefaultDeviceTokenLength(Integer defaultAccessTokenLength) {
        this.defaultDeviceTokenLength = defaultAccessTokenLength;
    }

    public Integer getDefaultDeviceTokenExpiresInSeconds() {
        return defaultDeviceTokenExpiresInSeconds;
    }
    public void setDefaultDeviceTokenExpiresInSeconds(Integer defaultAccessTokenExpiresInSeconds) {
        this.defaultDeviceTokenExpiresInSeconds = defaultAccessTokenExpiresInSeconds;
    }

    public Boolean getAutocreateDevices() {
        return autocreateDevices;
    }
    public void setAutocreateDevices(Boolean autocreateDevices) {
        this.autocreateDevices = autocreateDevices;
    }

    public Boolean getProvisioningAutoapproval() {
        return provisioningAutoapproval;
    }
    public void setProvisioningAutoapproval(Boolean provisioningAutoapproval) {
        this.provisioningAutoapproval = provisioningAutoapproval;
    }

    public List<ProvisioningToken> getProvisioningTokens() {
        return provisioningTokens;
    }
    public void setProvisioningTokens(List<ProvisioningToken> provisioningTokens) {
        this.provisioningTokens = provisioningTokens;
    }

    public List<Forwarding> getForwardings() {
        return forwardings;
    }
    public void setForwardings(List<Forwarding> forwardings) {
        this.forwardings = forwardings;
    }

    public List<Device> getDevices() {
        return devices;
    }
    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    // ***********************************************************************

}
