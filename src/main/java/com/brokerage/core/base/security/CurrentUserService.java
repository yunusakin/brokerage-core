package com.brokerage.core.base.security;

import com.brokerage.core.api.customer.model.Customer;
import com.brokerage.core.api.customer.respository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final CustomerRepository customerRepository;

    public Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public String username() {
        var a = auth();
        return a == null ? null : a.getName();
    }

    public Set<String> roles() {
        var a = auth();
        if (a == null) return Set.of();
        return a.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    public boolean isAdmin() {
        return roles().contains("ROLE_ADMIN");
    }

    public UUID currentCustomerId() {
        String user = username();
        if (user == null) return null;
        Customer c = customerRepository.findByUsername(user).orElse(null);
        return c == null ? null : c.getId();
    }
}

