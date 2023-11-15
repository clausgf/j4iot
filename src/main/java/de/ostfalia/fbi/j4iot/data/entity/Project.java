package de.ostfalia.fbi.j4iot.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedList;
import java.util.List;

@Entity
public class Project extends AbstractEntity {
    @NotEmpty @Column(length = 40) String name = "";
    @NotNull @Column(length = 240) String description = "";
    @NotNull Boolean active = true;
    // created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    // updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

    @NotNull Boolean autocreateDevices = true;
    @NotNull Boolean provisioningAutoapproval = true;

    @OneToMany(mappedBy = "project") List<Tag> tags = new LinkedList<>();
    @OneToMany(mappedBy = "project") List<ProvisioningToken> provisioningTokens = new LinkedList<>();
    @OneToMany(mappedBy = "project") List<Device> devices = new LinkedList<>();

    public Project() {
    }

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<ProvisioningToken> getProvisioningTokens() {
        return provisioningTokens;
    }

    public void setProvisioningTokens(List<ProvisioningToken> provisioningTokens) {
        this.provisioningTokens = provisioningTokens;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }
}
