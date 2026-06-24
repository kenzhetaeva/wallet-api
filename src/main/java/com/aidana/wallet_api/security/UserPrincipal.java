package com.aidana.wallet_api.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {
    private final Long userId;
    private final String email;
    private final Date expirationDate;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String email, Date expirationDate, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.expirationDate = expirationDate;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
