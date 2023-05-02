package ru.plorum.reporter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Entity
@Table(name = "INN")
@NoArgsConstructor
@AllArgsConstructor
public final class Inn {

    @Id
    @NonNull
    private String inn;

}
