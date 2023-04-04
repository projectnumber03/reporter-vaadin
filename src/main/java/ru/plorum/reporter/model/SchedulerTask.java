package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "SCHEDULER_TASKS")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class SchedulerTask {

    @Id
    @NonNull
    @Column(length = 36)
    @EqualsAndHashCode.Include
    UUID id;

    @Column
    String time;

    @ElementCollection
    List<String> days;

    @Column
    long duration;

    @Column(name = "EMAIL")
    String users;

}
