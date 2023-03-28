package ru.plorum.reporter.model.connection;

import jakarta.persistence.Entity;

@Entity
public final class PostgresConnection extends Connection {

    @Override
    public String getConnectionString() {
        return String.format("jdbc:postgresql://%s:%s/%s", getHost(), getPort(), getName());
    }

    public static String getConnectionType() {
        return "POSTGRESQL";
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.PostgreSQLDialect";
    }

    @Override
    public String getDriver() {
        return "org.postgresql.Driver";
    }

}
