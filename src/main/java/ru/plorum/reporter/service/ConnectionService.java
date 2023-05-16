package ru.plorum.reporter.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.plorum.reporter.model.connection.Connection;
import ru.plorum.reporter.repository.ConnectionRepository;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectionService {

    ConnectionRepository connectionRepository;

    @Qualifier("connectionEncoder")
    AES256TextEncryptor encryptor;

    public List<Connection> findById() {
        return connectionRepository.findAll();
    }

    public Optional<Connection> findById(final UUID id) {
        if (Objects.isNull(id)) return Optional.empty();
        return connectionRepository.findById(id);
    }

    public void save(final Connection connection) {
        if (!StringUtils.hasText(connection.getLogin()) || !StringUtils.hasText(connection.getPassword())) return;
        connection.setPassword(encrypt(connection));
        connectionRepository.saveAndFlush(connection);
    }

    public void delete(final Connection connection) {
        connectionRepository.delete(connection);
    }

    private String encrypt(final Connection connection) {
        return encryptor.encrypt(connection.getPassword());
    }

    public String decrypt(final Connection connection) {
        return encryptor.decrypt(connection.getPassword());
    }

    public String decrypt(final String password) {
        return encryptor.decrypt(password);
    }

    public boolean test(final Connection connection) {
        DriverManager.setLoginTimeout(3);
        try (java.sql.Connection sqlConnection = DriverManager.getConnection(connection.getConnectionString(), connection.getLogin(), connection.getPassword())) {
            return sqlConnection != null && sqlConnection.isValid(0);
        } catch (SQLException e) {
            try (java.sql.Connection sqlConnection = DriverManager.getConnection(connection.getConnectionString(), connection.getLogin(), decrypt(connection.getPassword()))) {
                return sqlConnection != null && sqlConnection.isValid(0);
            } catch (Exception ex) {
                log.info("Connection test error: ", e);
                return false;
            }
        }
    }

    public boolean test(final java.sql.Connection connection) {
        DriverManager.setLoginTimeout(3);
        try (connection) {
            boolean isValid = connection != null && connection.isValid(0);
            if (isValid) connection.close();
            return isValid;
        } catch (Exception e) {
            log.info("Connection test error: ", e);
            return false;
        }
    }

}
