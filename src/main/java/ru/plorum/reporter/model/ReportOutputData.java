package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "REPORT_OUTPUT_DATA")
public class ReportOutputData {

    @Id
    @Column(length = 36)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "QUERY_ID")
    Query query;

    @Column(name = "ROW_NUMBER")
    Integer rowNumber;

    @Column(name = "DATA_KEY")
    String key;

    @Column(name = "DATA_VALUE")
    String value;

}
