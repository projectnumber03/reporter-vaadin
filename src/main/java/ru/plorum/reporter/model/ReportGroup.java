package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "report_group")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ReportGroup {

    @Id
    @NonNull
    @Column(length = 36)
    @EqualsAndHashCode.Include
    UUID id;

    @NonNull
    @Column(name = "NAME", nullable = false, length = 100, unique = true)
    String name;

    @Column(name = "DESCRIPTION")
    String description;

    @Column(name = "LAST_REPORT_CREATION_DATE")
    LocalDateTime lastReportCreationDate;

    @Column(name = "ACCESS_TYPE", length = 1)
    int visible = 1;

    @NonNull
    @Enumerated(EnumType.STRING)
    ModuleAccess.Module module;

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "REPORT_GROUP_REPORTS",
            joinColumns = @JoinColumn(name = "REPORT_GROUP_ID"),
            inverseJoinColumns = @JoinColumn(name = "REPORT_ID"))
    Set<Report> reports = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "REPORT_GROUP_USERS",
            joinColumns = @JoinColumn(name = "REPORT_GROUP_ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    Set<User> permittedUsers = new LinkedHashSet<>();

}
