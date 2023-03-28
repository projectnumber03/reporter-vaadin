package ru.plorum.reporter.model.connection;


import jakarta.persistence.Entity;

@Entity
public final class H2Connection extends Connection {

    @Override
    public String getConnectionString() {
        return String.format("jdbc:h2:file:./%s", getName());
    }

    public static String getConnectionType() {
        return "H2";
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.H2Dialect";
    }

    @Override
    public String getDriver() {
        return "org.h2.Driver";
    }

}
