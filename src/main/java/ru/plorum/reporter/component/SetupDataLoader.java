package ru.plorum.reporter.component;

import ru.plorum.reporter.model.Privilege;
import ru.plorum.reporter.model.Role;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.repository.PrivilegeRepository;
import ru.plorum.reporter.repository.RoleRepository;
import ru.plorum.reporter.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SetupDataLoader {

    private final PrivilegeRepository privilegeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    @Value("${administrator.login}")
    private String username;

    @PostConstruct
    protected void initialize() {
        if (!CollectionUtils.isEmpty(privilegeRepository.findAll())) return;
        final Map<String, String> privilegesMap = new HashMap<>() {{
            put("OWN_ISSUE_CREATE_PRIVILEGE", "Создание собственных заявок");
            put("OWN_ISSUE_EDIT_PRIVILEGE", "Редактирование собственных заявок");
            put("ISSUE_CREATE_PRIVILEGE", "Создание заявок");
            put("ISSUE_EDIT_PRIVILEGE", "Редактирование заявок");
        }};
        final Set<Privilege> adminPrivileges = privilegesMap.entrySet().stream()
                .map(this::createPrivilegeIfNotFound)
                .filter(p -> Arrays.asList("ISSUE_CREATE_PRIVILEGE", "ISSUE_EDIT_PRIVILEGE").contains(p.getName()))
                .collect(Collectors.toSet());
        createAdminIfNotFound(adminPrivileges);
    }

    @Transactional
    protected void createAdminIfNotFound(final Set<Privilege> privileges) {
        final List<User> users = userRepository.findByNameLike(username);
        final User user = CollectionUtils.isEmpty(users) ? new User() : users.iterator().next();
        user.setId(UUID.randomUUID());
        user.setRoles(Collections.singleton(createAdminRoleIfNotFound(privileges)));
        user.setCreatedOn(LocalDateTime.now());
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    protected Role createAdminRoleIfNotFound(final Set<Privilege> privileges) {
        final String name = "ROLE_ADMIN";
        final Role role = roleRepository.findByName(name);
        if (Objects.nonNull(role)) return role;
        final Role roleToCreate = new Role(UUID.randomUUID());
        roleToCreate.setName(name);
        roleToCreate.setDescription("Администратор");
        roleToCreate.setPrivileges(privileges);
        return roleRepository.save(roleToCreate);
    }

    @Transactional
    protected Privilege createPrivilegeIfNotFound(final Map.Entry<String, String> data) {
        final Privilege privilege = privilegeRepository.findByName(data.getKey());
        if (Objects.nonNull(privilege)) return privilege;
        final Privilege privilegeToCreate = new Privilege();
        privilegeToCreate.setId(UUID.randomUUID());
        privilegeToCreate.setName(data.getKey());
        privilegeToCreate.setDescription(data.getValue());
        return privilegeRepository.save(privilegeToCreate);
    }

}
