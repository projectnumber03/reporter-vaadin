package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.UserGroup;
import ru.plorum.reporter.repository.UserGroupRepository;

import java.util.*;

@Service
@AllArgsConstructor
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;

    public Optional<UserGroup> findById(final UUID id) {
        if (Objects.isNull(id)) return Optional.empty();
        return userGroupRepository.findById(id);
    }

    public List<UserGroup> findAllById(final Collection<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();
        return userGroupRepository.findAllById(ids);
    }

    public List<UserGroup> findAll() {
        return userGroupRepository.findAll();
    }

    public void delete(final UserGroup group) {
        if (Objects.isNull(group)) return;
        userGroupRepository.delete(group);
    }

    public void save(final UserGroup group) {
        if (Objects.isNull(group)) return;
        userGroupRepository.save(group);
    }

}
