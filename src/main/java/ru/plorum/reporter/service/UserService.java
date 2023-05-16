package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.plorum.reporter.model.Privilege;
import ru.plorum.reporter.model.Role;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.model.UserGroup;
import ru.plorum.reporter.repository.UserRepository;

import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getAllActive() {
        if (isManager()) return userRepository.findByActiveTrue();
        return Collections.singletonList(getAuthenticatedUser());
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAllWithRoles() {
        return userRepository.findAllWithRoles();
    }

    public Optional<User> findById(final UUID id) {
        if (Objects.isNull(id)) return Optional.empty();
        return userRepository.findById(id);
    }

    public List<User> findAllById(final Collection<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        return userRepository.findAllById(ids);
    }

    public Optional<User> findByLogin(final String login) {
        if (!StringUtils.hasText(login)) return Optional.empty();
        return userRepository.findByLogin(login);
    }

    public List<User> findByLoginLike(final String login) {
        if (!StringUtils.hasText(login)) return Collections.emptyList();
        return userRepository.findByLoginLike(login);
    }

    public List<User> findByGroup(final UserGroup userGroup) {
        return userRepository.findAllByGroup(userGroup);
    }

    public List<User> findActiveByEmails(final List<String> emails) {
        if (CollectionUtils.isEmpty(emails)) return Collections.emptyList();
        return userRepository.findActiveByEmail(emails);
    }

    public void delete(final User user) {
        if (Objects.isNull(user)) return;
        userRepository.delete(user);
    }

    public void save(final User user) {
        if (Objects.isNull(user)) return;
        userRepository.save(user);
    }

    public User getAuthenticatedUser() {
        final var login = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!StringUtils.hasText(login)) return null;
        return userRepository.findByLogin(login).orElse(null);
    }

    public boolean isManager() {
        return getAuthenticatedUser().getRoles().stream()
                .map(Role::getPrivileges)
                .flatMap(Collection::stream)
                .map(Privilege::getName)
                .anyMatch(Arrays.asList("REPORT_CREATE_PRIVILEGE", "REPORT_EDIT_PRIVILEGE")::contains);
    }

}
