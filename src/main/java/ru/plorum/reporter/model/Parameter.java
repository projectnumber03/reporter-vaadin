package ru.plorum.reporter.model;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ru.plorum.reporter.util.Constants.*;

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

    @Setter(AccessLevel.NONE)
    @Column(name = "DEFAULT_VALUE")
    String defaultValue;

    @Enumerated(EnumType.STRING)
    Type type = Type.STRING;

    @Transient
    TextField descriptionField = new TextField();

    @Transient
    DatePicker dateDefaultValue = new DatePicker();

    @Transient
    NumberField integerDefaultValue = new NumberField();

    @Transient
    TextField stringDefaultValue = new TextField();

    @Transient
    ComboBox<Type> typeComboBox = new ComboBox<>(Strings.EMPTY, Parameter.Type.values());

    public void setDefaultValue() {
        switch (typeComboBox.getValue()) {
            case DATE -> this.defaultValue = Optional.ofNullable(dateDefaultValue.getValue()).map(DATE_FORMATTER::format).orElse(Strings.EMPTY);
            case INTEGER -> this.defaultValue = Objects.toString(integerDefaultValue.getValue(), Strings.EMPTY);
            case STRING -> this.defaultValue = Optional.ofNullable(stringDefaultValue.getValue()).orElse(Strings.EMPTY);
        }
    }

    public void fillTransients() {
        Optional.ofNullable(description).ifPresent(descriptionField::setValue);
        Optional.ofNullable(type).ifPresent(typeComboBox::setValue);
        if (Objects.isNull(type)) return;
        switch (type) {
            case DATE -> Optional.ofNullable(defaultValue).filter(StringUtils::hasText).map(v -> LocalDate.parse(v, DATE_FORMATTER)).ifPresent(dateDefaultValue::setValue);
            case INTEGER -> Optional.ofNullable(defaultValue).filter(StringUtils::hasText).map(Double::valueOf).ifPresent(integerDefaultValue::setValue);
            case STRING -> Optional.ofNullable(defaultValue).filter(StringUtils::hasText).ifPresent(stringDefaultValue::setValue);
        }
    }

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
