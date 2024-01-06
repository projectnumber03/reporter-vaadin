package ru.plorum.reporter.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class License {

    @NonNull
    UUID id;

    LocalDate startDate;

    Integer validity;

    String tariffId;

    boolean active = false;

    public License(@NonNull final UUID id) {
        this.id = id;
    }

    public LocalDate getFinishDate() {
        return getStartDate().plusMonths(getValidity()).plusDays(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        License license = (License) o;
        return Objects.equals(id, license.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
