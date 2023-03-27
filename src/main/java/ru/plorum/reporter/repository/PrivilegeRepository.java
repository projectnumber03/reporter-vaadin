package ru.plorum.reporter.repository;

import ru.plorum.reporter.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {
    Privilege findByName(final String name);
}
