package ru.plorum.reporter.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "USER_GROUP")
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class UserGroup {

    @Id
    @NonNull
    @Column(length = 36)
    UUID id;

    @Column(name = "NAME_GROUP", nullable = false, length = 100)
    String nameGroup;

    @Column(name = "DESCRIPTION")
    String description;

    @ManyToOne
    @JoinColumn(name="PARENT_GROUP_ID")
    UserGroup parentUserGroup;

    @OneToMany (mappedBy = "parentUserGroup")
    @LazyCollection(LazyCollectionOption.FALSE)
    Collection<UserGroup> childUserGroups = new HashSet<>();

}
