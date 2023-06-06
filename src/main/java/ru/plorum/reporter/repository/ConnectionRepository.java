package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.plorum.reporter.model.connection.Connection;

import java.util.UUID;

public interface ConnectionRepository extends JpaRepository<Connection, UUID> {
    Connection findConnectionById(final UUID id);

    @Query("select count(c) from Connection as c")
    Long countAll();

}
