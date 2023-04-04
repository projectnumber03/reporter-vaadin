package ru.plorum.reporter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "HISTORY")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class History {

    @Id
    @Column(length = 36)
    @EqualsAndHashCode.Include
    UUID id;

    @Column(name = "SQL_TEXT", nullable = false, length = 5000)
    String sqlText; // тело запроса

    @Column(name = "TABLE_NAME", nullable = false)
    String tableName; // имя таблицы с историей

    @Column
    LocalDateTime date;

    @Column(name = "SUB_REPORT", length = 250)
    String subReport;

    @Column(name = "QUERY_ID")
    UUID queryId;

    public History clone() {
        return History.builder()
                .sqlText(this.sqlText)
                .tableName(this.tableName)
                .date(this.date)
                .subReport(this.subReport)
                .queryId(this.queryId)
                .build();
    }

}
