package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.plorum.reporter.model.connection.Connection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "REPORT")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class Report {

    @Id
    @Column(length = 36)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "AUTHOR", nullable = false)
    User author;

    @Column(name = "NAME", nullable = false, length = 50)
    String name;

    @Column(name = "DESCRIPTION")
    String description;

    @ManyToOne
    @JoinColumn(name = "LAST_EDITOR")
    User lastEditor;

    @Column(name = "CREATED_AT")
    LocalDateTime dateReport;

    @ManyToOne
    @JoinColumn(name = "CONNECTION_ID")
    Connection connection;

    @ManyToOne
    @JoinColumn(name = "GROUP_ID")
    ReportGroup group;

    @Enumerated(EnumType.STRING)
    ModuleAccess.Module module;

    @Column(name = "ACCESS_TYPE", length = 1)
    int accessType;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "REPORT_STATUSES",
            joinColumns = @JoinColumn(name = "REPORT_ID"),
            inverseJoinColumns = @JoinColumn(name = "STATUS_ID"))
    List<ReportStatus> statuses = new ArrayList<>();

    @Column
    boolean locked = true;

    @Column
    boolean history; //требуется формирование истории

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "REPORT_QUERIES",
            joinColumns = @JoinColumn(name = "REPORT_ID"),
            inverseJoinColumns = @JoinColumn(name = "QUERY_ID"))
    List<Query> queries = new ArrayList<>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "REPORT_HISTORIES",
            joinColumns = @JoinColumn(name = "REPORT_ID"),
            inverseJoinColumns = @JoinColumn(name = "HISTORY_ID"))
    List<History> histories = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "REPORT_USERS",
            joinColumns = @JoinColumn(name = "REPORT_ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    Set<User> permittedUsers = new HashSet<>();

    @NonNull
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "REPORT_PARAMETERS",
            joinColumns = @JoinColumn(name = "REPORT_ID"),
            inverseJoinColumns = @JoinColumn(name = "PARAMETER_ID"))
    List<Parameter> parameters = new ArrayList<>();

    @OneToOne(cascade = CascadeType.DETACH, orphanRemoval = true)
    @JoinColumn(name = "SCHEDULER_TASK_ID", referencedColumnName = "id")
    SchedulerTask schedulerTask;

    public Report(final UUID id) {
        this.id = id;
    }

    public Report clone() {
        return Report.builder()
                .id(null)
                .author(this.author)
                .name(formatNameReport(this.name))
                .description(this.description)
                .group(this.group)
                .history(this.history)
                .lastEditor(this.lastEditor)
                .dateReport(this.dateReport)
                .connection(this.connection)
                .module(this.module)
                .accessType(this.accessType)
                .statuses(new ArrayList<>(Collections.singletonList(ReportStatus.builder().status("NEW").date(LocalDateTime.now()).build())))
                .locked(this.locked)
                .queries(new ArrayList<>(queries.stream().map(Query::clone).collect(Collectors.toList())))
                .permittedUsers(new HashSet<>(permittedUsers))
                .parameters(new ArrayList<>(parameters.stream().map(Parameter::clone).collect(Collectors.toList())))
                .build();
    }

    private String formatNameReport(final String nameReport) {
        return String.format("Копия {%s} %s", DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDate.now()), nameReport);
    }

    public void unlock(final String value) {
        this.locked = !"on".equals(value);
    }

    public String getStatus() {
        return statuses.stream()
                .max(Comparator.comparing(ReportStatus::getDate))
                .map(ReportStatus::getStatus)
                .orElse("");
    }

    public void setStatus(final String status) {
        this.statuses.add(ReportStatus.builder().id(UUID.randomUUID()).status(String.format("%d. %s", statuses.size() + 1, status)).date(LocalDateTime.now()).build());
    }

    public List<Query> getQueriesWithTransients() {
        queries.forEach(Query::fillTransients);
        return queries;
    }

    public List<Parameter> getParametersWithTransients() {
        parameters.forEach(Parameter::fillTransients);
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Report report = (Report) o;

        return id != null ? id.equals(report.id) : report.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Visibility getVisibility() {
        return Visibility.values()[this.accessType];
    }

}
