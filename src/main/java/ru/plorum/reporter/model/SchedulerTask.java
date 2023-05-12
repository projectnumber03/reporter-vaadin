package ru.plorum.reporter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.UUID;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "SCHEDULER_TASKS")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class SchedulerTask {

    @Id
    @Column(length = 36)
    UUID id;

    @Column(name = "CRON_EXPRESSION")
    String cronExpression;

    @Column(name = "EMAIL")
    String userEmails;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (SchedulerTask) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Getter
    @AllArgsConstructor
    public enum Type {

        DAY("День недели"),
        INTERVAL("Интервал, мин.");

        private final String name;

    }

}
