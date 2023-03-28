package ru.plorum.reporter.model.connection;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.plorum.reporter.model.User;

import java.util.UUID;

@Data
@Entity
@ToString
@NoArgsConstructor
@Table(name = "system_name")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Connection {

    @Id
    @Column(length = 36)
    @EqualsAndHashCode.Include
    UUID id;

    @Column(name = "type", nullable = false, length = 10)
    String type;

    @Column(name = "description")
    String description;

    @Column(name = "host", nullable = false, length = 50)
    String host;

    @Column(name = "port", nullable = false, length = 4)
    int port;

    @Column(name = "login", nullable = false)
    String login;

    @Column(name = "password", nullable = false)
    String password;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    User user;

    @Column(name = "name")
    String name;

    public abstract String getConnectionString();

    public abstract String getHibernateDialect();

    public abstract String getDriver();

    public static Connection getByDriver(final String driver) {
        switch (driver) {
            case "com.microsoft.sqlserver.jdbc.SQLServerDriver": return new MSSQLConnection();
            case "com.mysql.cj.jdbc.Driver": return new MYSQLConnection();
            case "oracle.jdbc.driver.OracleDriver": return new OracleConnection();
            case "org.postgresql.Driver": return new PostgresConnection();
            default: return new H2Connection();
        }
    }

}
