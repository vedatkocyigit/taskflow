package com.taskflow.backend.security;

import com.taskflow.backend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

public class AppUserPrincipal implements org.springframework.security.core.userdetails.UserDetails {

    private final User user;

    public AppUserPrincipal(User user) { this.user = user; }

    public Long getId() { return user.getId(); }
    public String getEmail() { return user.getEmail(); }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null) return java.util.List.of();
        return user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                .toList();
    }

    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
