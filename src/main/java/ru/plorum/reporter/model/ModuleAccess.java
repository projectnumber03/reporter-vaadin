package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "MODULE")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ModuleAccess {

    @Id
    @Column(length = 36)
    @EqualsAndHashCode.Include
    UUID id;

    @NonNull
    @Enumerated(EnumType.STRING)
    Module module;

    @NonNull
    @Column(name = "STATUS")
    Boolean status;

    @NonNull
    @Column(name = "LICENSE")
    String license;

    @Transient
    String expirationDate;

    @Getter
    @AllArgsConstructor
    public enum Module {
        REPORTER("АИС «Отчетность»"),
        MEDICINE("АИС «Телемед-интеграции»");

        private final String description;
    }

}
