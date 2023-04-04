package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "PARAMETER")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class Parameter {

    @Id
    @Column(length = 36)
    @EqualsAndHashCode.Include
    UUID id;

    @Column(name = "NAME")
    String name;

    @Column(name = "ASK_USER")
    boolean askUser;

    @Column(name = "DEFAULT_VALUE")
    String defaultValue;

    @Column(name = "DISPLAYED_NAME")
    String displayedName;

    @Enumerated(EnumType.STRING)
    Type type;

    public Parameter(final String param) {
        final String[] parsedParam = param.split(";");
        this.name = parsedParam[0];
        this.defaultValue = parsedParam[1];
        this.askUser = "true".equals(parsedParam[2]);
        this.displayedName = parsedParam[3];
        this.type = Type.valueOf(parsedParam[4]);
    }

    public enum Type {
        STRING, DATE
    }

    public Parameter clone() {
        return Parameter.builder()
                .name(this.name)
                .askUser(this.askUser)
                .defaultValue(this.defaultValue)
                .displayedName(this.displayedName)
                .type(this.type)
                .build();
    }

}
