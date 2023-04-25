package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.model.connection.Connection;

@Service
@AllArgsConstructor
public class DataSourceService {

    private final AES256TextEncryptor connectionEncoder;

    public DriverManagerDataSource createDataSource(final Connection connection) {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(connection.getDriver());
        dataSource.setUrl(connection.getConnectionString());
        dataSource.setUsername(connection.getLogin());
        dataSource.setPassword(connectionEncoder.decrypt(connection.getPassword()));
        return dataSource;
    }

}
