package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
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
@Profile({"free", "professional"})
@Transactional
@AllArgsConstructor
public class UserServiceLight implements IUserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllActive() {
        if (isManager()) return userRepository.findByActiveTrue();
        return Collections.singletonList(getAuthenticatedUser());
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findAllWithRoles() {
        return userRepository.findAllWithRoles();
    }

    @Override
    public Optional<User> findById(final UUID id) {
        if (Objects.isNull(id)) return Optional.empty();
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAllById(final Collection<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        return userRepository.findAllById(ids);
    }

    @Override
    public Optional<User> findByLogin(final String login) {
        if (!StringUtils.hasText(login)) return Optional.empty();
        return userRepository.findByLogin(login);
    }

    @Override
    public List<User> findByLoginLike(final String login) {
        if (!StringUtils.hasText(login)) return Collections.emptyList();
        return userRepository.findByLoginLike(login);
    }

    @Override
    public List<User> findByGroup(final UserGroup userGroup) {
        return userRepository.findAllByGroup(userGroup);
    }

    @Override
    public List<User> findActiveByEmails(final List<String> emails) {
        if (CollectionUtils.isEmpty(emails)) return Collections.emptyList();
        return userRepository.findActiveByEmail(emails);
    }

    @Override
    public void delete(final User user) {
        if (Objects.isNull(user)) return;
        userRepository.delete(user);
    }

    @Override
    public void save(final User user) {
        if (Objects.isNull(user)) return;
        userRepository.save(user);
    }

    @Override
    public User getAuthenticatedUser() {
        return userRepository.findByLogin("admin").orElse(null);
    }

    @Override
    public boolean isManager() {
        return getAuthenticatedUser().getRoles().stream()
                .map(Role::getPrivileges)
                .flatMap(Collection::stream)
                .map(Privilege::getName)
                .anyMatch(Arrays.asList("REPORT_CREATE_PRIVILEGE", "REPORT_EDIT_PRIVILEGE")::contains);
    }

    @Override
    public Long countAll() {
        return userRepository.countAll();
    }

}
