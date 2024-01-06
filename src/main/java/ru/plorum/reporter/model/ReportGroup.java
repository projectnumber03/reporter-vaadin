package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Table(name = "report_group")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ReportGroup {

    @Id
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
    int visible = 0;

    @NonNull
    @Enumerated(EnumType.STRING)
    ModuleAccess.Module module;

    @ManyToMany
    @JoinTable(name = "REPORT_GROUP_USERS",
            joinColumns = @JoinColumn(name = "REPORT_GROUP_ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    Set<User> permittedUsers = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportGroup that = (ReportGroup) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Visibility getVisibility() {
        return Visibility.values()[this.visible];
    }

}
