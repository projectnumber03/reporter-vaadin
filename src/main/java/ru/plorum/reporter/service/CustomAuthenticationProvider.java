package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.component.SetupDataLoader;

@Service
@AllArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final SetupDataLoader setupDataLoader;

    @Nullable
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final var username = authentication.getName();
        final var password = authentication.getCredentials().toString();
        final var isAdminCreated = setupDataLoader.createAdminIfNotFound(username, password);
        if (!isAdminCreated) throw new BadCredentialsException("admin wasn't created");
        final var grantedAuthorities = setupDataLoader.getRolesByLogin(username).stream().map(SimpleGrantedAuthority::new).toList();
        return new UsernamePasswordAuthenticationToken(username, password, grantedAuthorities);
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
