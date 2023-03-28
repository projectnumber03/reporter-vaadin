package ru.plorum.reporter.model.connection;


import jakarta.persistence.Entity;

@Entity
public final class MYSQLConnection extends Connection {

    @Override
    public String getConnectionString() {
        return String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", getHost(), getPort(), getName());
    }

    public static String getConnectionType() {
        return "MYSQL";
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.MySQLDialect";
    }

    @Override
    public String getDriver() {
        return "com.mysql.cj.jdbc.Driver";
    }

}
