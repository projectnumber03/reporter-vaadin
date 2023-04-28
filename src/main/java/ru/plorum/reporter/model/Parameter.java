package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.*;
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
    UUID id;

    @Column(name = "NAME")
    String name;

    @Column(name = "DESCRIPTION")
    String description;

    @Column(name = "DEFAULT_VALUE")
    String defaultValue;

    @Enumerated(EnumType.STRING)
    Type type;

    @Getter
    @AllArgsConstructor
    public enum Type {
        STRING("Строка"),
        DATE("Дата"),
        INTEGER("Число");

        private final String description;

    }

    public Parameter(final UUID id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Parameter clone() {
        return Parameter.builder()
                .name(this.name)
                .defaultValue(this.defaultValue)
                .description(this.description)
                .type(this.type)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        return id.equals(parameter.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
