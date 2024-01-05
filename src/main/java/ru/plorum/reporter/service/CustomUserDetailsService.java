package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.model.UserPrincipal;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserService userService;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final var user = userService.findByLogin(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return new UserPrincipal(user.get());
    }

}
