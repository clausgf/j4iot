package de.ostfalia.fbi.j4iot.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "users",
    indexes = {
        @Index(columnList = "name", unique = true)
})
public class User extends AbstractEntity {
    @Column(length = 40, unique = true)
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+]*$", message = "Name must start with a letter or a number, the rest can also contain plus, minus or underscores.")
    @NotNull @NotEmpty private String name;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    @NotNull private Boolean isEnabled = true;

    @NotNull @NotEmpty @JsonIgnore private String encodedPassword;
    @NotNull private String firstName = "";
    @NotNull private String lastName = "";
    @NotNull @Email private String email = "";

    @NotNull private Instant expiresAt;
    private Instant lastLoginAt = null;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // ***********************************************************************

    public User() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public User(String name, Boolean isEnabled, String encodedPassword,
                String firstName, String lastName, String email,
                Instant expiresAt) {
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.isEnabled = isEnabled;
        this.encodedPassword = encodedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.expiresAt = expiresAt;
        this.lastLoginAt = null;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    @Override
    public String toString() {
        return name;
    }

    // ***********************************************************************

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }
    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }
    public void setEncodedPassword(String hashedPassword) {
        this.encodedPassword = hashedPassword;
    }

    public java.lang.String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    // ***********************************************************************

    public static final class UserBuilder {
        private Logger log = LoggerFactory.getLogger(UserBuilder.class);
        private String name;
        private Boolean isEnabled = true;
        private String encodedPassword;
        private String firstName = "";
        private String lastName = "";
        private String email = "";
        private Instant expiresAt = Instant.now().plus(50*365, ChronoUnit.DAYS);
        //private Set<Role> roles = new HashSet<>();
        private final PasswordEncoder passwordEncoder;

        public UserBuilder() {
            this.passwordEncoder = createDelegatingPasswordEncoder();
        }

        public PasswordEncoder createDelegatingPasswordEncoder() {
            String encodingId = "bcrypt";
            Map<String, PasswordEncoder> encoders = new HashMap<>();
            encoders.put(encodingId, new BCryptPasswordEncoder());
            return new DelegatingPasswordEncoder(encodingId, encoders);
        }

        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder enabled(Boolean isEnabled) { this.isEnabled = isEnabled; return this; }
        public UserBuilder password(String password) { this.encodedPassword = passwordEncoder.encode(password); return this; }
        public UserBuilder firstName(String firstName) { this.firstName = firstName; return this; }
        public UserBuilder lastName(String lastName) { this.lastName = lastName; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public User build() {
            return new User(name, isEnabled, encodedPassword,
                    firstName, lastName, email, expiresAt);
        }
    }
}
