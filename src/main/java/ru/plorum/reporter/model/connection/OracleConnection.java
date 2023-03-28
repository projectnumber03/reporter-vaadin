package ru.plorum.reporter.model.connection;

import jakarta.persistence.Entity;

@Entity
public final class OracleConnection extends Connection {

    @Override
    public String getConnectionString() {
        return String.format("jdbc:oracle:thin:@%s:%s:%s", getHost(), getPort(), getName());
    }

    public static String getConnectionType() {
        return "ORACLE";
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.OracleDialect";
    }

    @Override
    public String getDriver() {
        return "oracle.jdbc.driver.OracleDriver";
    }

}
