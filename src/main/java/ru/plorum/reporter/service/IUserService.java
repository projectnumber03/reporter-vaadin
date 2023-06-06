package ru.plorum.reporter.service;

import ru.plorum.reporter.model.User;
import ru.plorum.reporter.model.UserGroup;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserService {
    List<User> getAllActive();

    List<User> findAll();

    List<User> findAllWithRoles();

    Optional<User> findById(UUID id);

    List<User> findAllById(Collection<UUID> ids);

    Optional<User> findByLogin(String login);

    List<User> findByLoginLike(String login);

    List<User> findByGroup(UserGroup userGroup);

    List<User> findActiveByEmails(List<String> emails);

    void delete(User user);

    void save(User user);

    User getAuthenticatedUser();

    boolean isManager();

    Long countAll();

}
