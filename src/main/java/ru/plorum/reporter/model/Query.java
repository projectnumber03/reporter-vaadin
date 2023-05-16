package ru.plorum.reporter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextField;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.plorum.reporter.component.TextAreaHighlighter;

import java.util.Optional;
import java.util.UUID;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "QUERY")
@RequiredArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class Query {

    @Id
    @Column(length = 36)
    @EqualsAndHashCode.Include
    UUID id;

    @Column
    boolean report; //запрос для формирования отчета или технический

    @NonNull
    @Column(name = "SQL_TEXT", nullable = false, length = 20000)
    String sqlText; // тело запроса

    @Column(name = "SUB_REPORT", length = 250)
    String subReport;

    @Transient
    @JsonIgnore
    TextAreaHighlighter sqlTextField = new TextAreaHighlighter();

    @Transient
    @JsonIgnore
    Checkbox generateReportCheckbox = new Checkbox("Формировать отчёт");

    @Transient
    @JsonIgnore
    TextField subReportField = new TextField("", "Название подотчёта");

    public Query(final UUID id) {
        this.id = id;
        subReportField.setEnabled(generateReportCheckbox.getValue());
        generateReportCheckbox.addValueChangeListener(e -> subReportField.setEnabled(e.getValue()));
    }

    public Query clone() {
        return Query.builder()
                .report(this.report)
                .sqlText(this.sqlText)
                .subReport(this.subReport)
                .build();
    }

    public void fillTransients() {
        sqlTextField.setValue(sqlText);
        generateReportCheckbox.setValue(report);
        Optional.ofNullable(subReport).ifPresent(subReportField::setValue);
    }

}
