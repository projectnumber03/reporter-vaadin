package ru.plorum.reporter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "REPORT_STATUS")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ReportStatus {

    @Id
    @NonNull
    @Column(length = 36)
    @EqualsAndHashCode.Include
    UUID id;

    @Column
    String status;

    @Column
    LocalDateTime date;

}
