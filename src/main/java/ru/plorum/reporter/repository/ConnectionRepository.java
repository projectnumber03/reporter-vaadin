package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.connection.Connection;

import java.util.UUID;

public interface ConnectionRepository extends JpaRepository<Connection, UUID> {
    Connection findConnectionById(final UUID id);
}
