package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "REPORT_OUTPUT")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportOutput {

    @Id
    @Column(length = 36)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "REPORT_ID")
    Report report;

    @Column(name = "CREATED_AT")
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "CREATED_BY")
    User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "REPORT_OUTPUT_REPORT_OUTPUT_DATA",
            joinColumns = @JoinColumn(name = "OUTPUT_ID"),
            inverseJoinColumns = @JoinColumn(name = "DATA_ID"))
    List<ReportOutputData> data = new ArrayList<>();

    public ReportOutput(final UUID id) {
        this.id = id;
    }

}
