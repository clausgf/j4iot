package de.ostfalia.fbi.j4iot.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedList;
import java.util.List;

@Entity
public class Tag extends AbstractEntity {

    @NotEmpty @Column(length = 40) String name = "";
    @NotNull @Column(length = 40) String color = "blue";

    @ManyToOne
    @JsonIgnoreProperties({"tags", "provisioningTokens", "devices"})
    @NotNull Project project;

    @ManyToMany List<Device> devices = new LinkedList<>();

    public Tag() {
    }

    public Tag(Project project, String name, String color) {
        this.project = project;
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }
}
