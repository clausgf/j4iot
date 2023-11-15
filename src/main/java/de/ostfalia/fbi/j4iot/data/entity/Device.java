package de.ostfalia.fbi.j4iot.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedList;
import java.util.List;

@Entity
public class Device extends AbstractEntity {

    // todo Standard-Sortierung alphabetisch!
    @NotEmpty @Column(length = 40) String name = ""; // TODO index
    @NotNull @Column(length = 240) String description = "";
    @NotNull @Column(length = 240) String location = "";
    @NotNull Boolean active = true;

    @NotNull Boolean provisioningApproved = false;
    // TODO last_provisioning_request_at: Mapped[Optional[datetime]] = mapped_column(DateTime, default=None)
    // TODO last_provisioned_at: Mapped[Optional[datetime]] = mapped_column(DateTime, default=None)

    // TODO created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    // TODO updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    // TODO last_seen_at: Mapped[Optional[datetime]] = mapped_column(DateTime, default=None)

    // TODO eager fetching might not be a good idea
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "devices")
    List<Tag> tags = new LinkedList<>();
    @ManyToOne
    @JsonIgnoreProperties({"tags", "provisioningTokens", "devices"})
    @NotNull Project project;

    public Device() {
    }

    public Device(Project project, String name, String description, String location) {
        this.project = project;
        this.name = name;
        this.description = description;
        this.location = location;
    }

    public Device(Project project, String name, String description, String location, List<Tag> tags) {
        this.project = project;
        this.name = name;
        this.description = description;
        this.location = location;
        this.tags = tags;
        for (Tag tag: tags) {
            tag.getDevices().add(this);
        }
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getProvisioningApproved() {
        return provisioningApproved;
    }

    public void setProvisioningApproved(Boolean provisioningApproved) {
        this.provisioningApproved = provisioningApproved;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getDevices().add(this);
    }
}
