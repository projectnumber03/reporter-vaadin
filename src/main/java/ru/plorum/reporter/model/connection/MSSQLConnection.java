package ru.plorum.reporter.model.connection;

import jakarta.persistence.Entity;
import org.springframework.util.StringUtils;


@Entity
public final class MSSQLConnection extends Connection {

    @Override
    public String getConnectionString() {
        //Если несколько инстансов
        if (StringUtils.hasText(getHost()) && getHost().matches(".+\\\\.+")) {
            return String.format("jdbc:sqlserver://%s;database=%s", getHost(), getName());
        }
        //Если один
        return String.format("jdbc:sqlserver://%s:%s;database=%s", getHost(), getPort(), getName());
    }

    public static String getConnectionType() {
        return "MSSQL";
    }

    @Override
    public String getHibernateDialect() {
        return "org.hibernate.dialect.SQLServer2012Dialect";
    }

    @Override
    public String getDriver() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

}
