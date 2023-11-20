package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.Role;
import de.ostfalia.fbi.j4iot.data.entity.User;
import de.ostfalia.fbi.j4iot.data.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    Logger log = LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        passwordEncoder = createDelegatingPasswordEncoder();
    }

    public PasswordEncoder createDelegatingPasswordEncoder() {
        String encodingId = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(encodingId, new BCryptPasswordEncoder());
        encoders.put("scrypt@SpringSecurity_v5_8", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("argon2@SpringSecurity_v5_8", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        return new DelegatingPasswordEncoder(encodingId, encoders);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findOneByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getEncodedPassword(),
                user.getEnabled(),
                user.getExpiresAt().isAfter(Instant.now()),
                true, true,
                getAuthorities(user.getRoles()) );
    }

    private Set<GrantedAuthority> getAuthorities(Set<Role> roles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }
        return authorities;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findByName(String name) {
        return userRepository.findOneByName(name);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void createUser(User user, String password) {
        user.setEncodedPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(String username) {
        User user = userRepository.findOneByName(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findOneByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username/password incorrect: " + username));
        changePassword(user, oldPassword, newPassword);
        userRepository.save(user);
    }

    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getEncodedPassword())) {
            throw new UsernameNotFoundException("Username/password incorrect: " + user.getName());
        }
        user.setEncodedPassword(passwordEncoder.encode(newPassword));
    }

    public boolean userExists(String username) {
        return userRepository.findOneByName(username).isPresent();
    }

    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent successEvent) {
        log.info("Authentication success: {}", successEvent.toString());
        String username = successEvent.getAuthentication().getName();
        findByName(username).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
        });
    }

    @EventListener
    public void onLoginFailure(AbstractAuthenticationFailureEvent failureEvent) {
        log.info("Authentication failure: {}", failureEvent.toString());
    }
}
