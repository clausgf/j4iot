package de.ostfalia.fbi.j4iot.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles",
        indexes = {
                @Index(columnList = "name", unique = true)
        })
public class Role extends AbstractEntity {
    @NotNull @NotEmpty
    @Column(length = 40, unique = true)
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+]*$", message = "Name must start with a letter or a number, the rest can also contain plus, minus or underscores.")
    @NotEmpty private String name; // TODO index unique

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }
}
