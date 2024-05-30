package de.ostfalia.fbi.j4iot.data.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.time.Instant;
import java.util.*;

public class User extends DefaultOidcUser implements UserDetails {

    //Keycloak Daten
    private String name;
    @NotNull
    private String firstName = "";
    @NotNull private String lastName = "";
    @NotNull @Email
    private String email = "";
    @NotNull private Instant expiresAt;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    private Instant lastLoginFailureAt;
    private Long loginFailures;
    private Set<String> projects;
    private Long id;



    private static Map<String, Object> claims = new HashMap<>();

    static{
        claims = new HashMap<>();
        claims.put("bla", new HashMap<String, Object>());
        claims.put("sub", new HashMap<String, Object>());
    }

    public User(String name, String firstName, String lastName,
                String email, Instant expiresAt){
        this(name, firstName, lastName, email, expiresAt, new ArrayList<>());
    }

    public User(String name, String firstName, String lastName,
                String email, Instant expiresAt, Collection<? extends GrantedAuthority> authorities){
        super(authorities, new OidcIdToken("lkasdhf", Instant.now(), Instant.now().plusMillis(5), claims));

        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.expiresAt = expiresAt;
        this.projects = new HashSet<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    public Instant getLastLoginFailureAt() {
        return lastLoginFailureAt;
    }
    public void setLastLoginFailureAt(Instant lastLoginFailureAt) {
        this.lastLoginFailureAt = lastLoginFailureAt;
    }

    public Long getLoginFailures() {
        return loginFailures;
    }
    public void setLoginFailures(Long loginFailures) {
        this.loginFailures = loginFailures;
    }

    public Set<String> getProjects() {
        return projects;
    }
    public void setProjects(Set<String> projects) {
        this.projects = projects;
    }

    @Override
    public String getPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode("fuehner");
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
