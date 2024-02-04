package ru.plorum.reporter.component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.dto.UserDetailsDto;
import ru.plorum.reporter.model.License;
import ru.plorum.reporter.model.Privilege;
import ru.plorum.reporter.model.Role;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.repository.PrivilegeRepository;
import ru.plorum.reporter.repository.RoleRepository;
import ru.plorum.reporter.repository.UserRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetupDataLoader {

    private final PrivilegeRepository privilegeRepository;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AES256TextEncryptor jasyptEncryptor;

    private final HttpClient httpClient;

    private final String systemId;

    private final TariffInfo tariffInfo;

    @Getter
    private final ILicenseCache licenseCache;

    @Value("${api.user.details}")
    private String apiUrl;

    @PostConstruct
    protected void initialize() {
        if (!CollectionUtils.isEmpty(privilegeRepository.findAll())) return;
        final Map<String, String> privilegesMap = new HashMap<>() {{
            put("OWN_REPORT_CREATE_PRIVILEGE", "Создание собственных отчетов");
            put("OWN_REPORT_EDIT_PRIVILEGE", "Редактирование собственных отчетов");
            put("REPORT_CREATE_PRIVILEGE", "Создание отчетов");
            put("REPORT_EDIT_PRIVILEGE", "Редактирование отчетов");
        }};
        final Set<Privilege> adminPrivileges = privilegesMap.entrySet().stream()
                .map(this::createPrivilegeIfNotFound)
                .filter(p -> Arrays.asList("REPORT_CREATE_PRIVILEGE", "REPORT_EDIT_PRIVILEGE").contains(p.getName()))
                .collect(Collectors.toSet());
        createAdminRoleIfNotFound(adminPrivileges);
    }

    @Transactional
    public boolean createAdminIfNotFound(final String login, final String password) {
        final var admin = userRepository.findByLogin(login);
        final var activeLicense = licenseCache.getActive();
        if (admin.isPresent() && activeLicense.isPresent()) return true;
        try {
            final var mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final var requestUserDetailsDto = UserDetailsDto.builder()
                    .login(login)
                    .password(password)
                    .tariffId(tariffInfo.getTariffId())
                    .systemId(systemId)
                    .build();
            final var request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(jasyptEncryptor.encrypt(mapper.writeValueAsString(requestUserDetailsDto))))
                    .header("Content-Type", "text/plain")
                    .build();
            final var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final var responseUserDetailsDto = mapper.readValue(jasyptEncryptor.decrypt(response.body()), UserDetailsDto.class);
            if (CollectionUtils.isEmpty(responseUserDetailsDto.getLicenses())) return false;
            if (admin.isEmpty()) {
                final var user = new User();
                user.setId(UUID.randomUUID());
                user.setLogin(login);
                user.setName(login);
                user.setPassword(passwordEncoder.encode(password));
                Optional.ofNullable(roleRepository.findByName("ROLE_ADMIN")).map(Collections::singleton).ifPresent(user::setRoles);
                user.setCreatedOn(LocalDateTime.now());
                user.setActive(true);
                userRepository.save(user);
            }
            if (activeLicense.isEmpty()) {
                licenseCache.clear();
                final var licenses = responseUserDetailsDto.getLicenses().stream()
                        .map(l -> new License(UUID.randomUUID(), l.getStartDate(), l.getValidity(), responseUserDetailsDto.getTariffId(), false))
                        .toList();
                licenseCache.addAll(licenses);
            }

            return true;
        } catch (Exception e) {
            log.error("error creating admin user", e);
            return false;
        }
    }

    @Transactional
    public Role createAdminRoleIfNotFound(final Set<Privilege> privileges) {
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
    public Privilege createPrivilegeIfNotFound(final Map.Entry<String, String> data) {
        final Privilege privilege = privilegeRepository.findByName(data.getKey());
        if (Objects.nonNull(privilege)) return privilege;
        final Privilege privilegeToCreate = new Privilege();
        privilegeToCreate.setId(UUID.randomUUID());
        privilegeToCreate.setName(data.getKey());
        privilegeToCreate.setDescription(data.getValue());
        return privilegeRepository.save(privilegeToCreate);
    }

    public List<String> getRolesByLogin(final String login) {
        return userRepository.findByLogin(login)
                .map(user -> user.getRoles().stream().map(Role::getName).toList())
                .orElse(Collections.emptyList());
    }

}
