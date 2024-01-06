package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.model.connection.Connection;

@Service
@AllArgsConstructor
public class DataSourceService {

    @Qualifier("jasyptEncryptor")
    private final AES256TextEncryptor encryptor;

    public DriverManagerDataSource createDataSource(final Connection connection) {
        final var dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(connection.getDriver());
        dataSource.setUrl(connection.getConnectionString());
        dataSource.setUsername(connection.getLogin());
        dataSource.setPassword(encryptor.decrypt(connection.getPassword()));
        return dataSource;
    }

}
