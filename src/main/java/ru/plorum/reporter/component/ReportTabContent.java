package ru.plorum.reporter.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import ru.plorum.reporter.model.Parameter;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static ru.plorum.reporter.util.Constants.*;


@Getter
public class ReportTabContent extends VerticalLayout {

    private final Grid<Parameter> parameterTable = new Grid<>();

    private final List<Parameter> items = new ArrayList<>();

    private final Button detectButton = new Button("Определить");

    private final QueryTabContent queryTabContent;

    public ReportTabContent(final QueryTabContent queryTabContent, final DatePicker.DatePickerI18n i18n) {
        setHeightFull();
        this.queryTabContent = queryTabContent;
        add(createParameterTable(i18n));
        add(createDetectButton());
    }

    private Component createParameterTable(final DatePicker.DatePickerI18n i18n) {
        parameterTable.addColumn(Parameter::getName).setHeader(NAME);

        parameterTable.addComponentColumn(p -> {
            var textField = new TextField();
            textField.setWidthFull();
            Optional.ofNullable(p.getDescription()).ifPresent(textField::setValue);
            return textField;
        }).setHeader(DESCRIPTION);

        final var dateColumn = parameterTable.addComponentColumn(p -> {
            var datePicker = new DatePicker();
            datePicker.setI18n(i18n);
            if (Parameter.Type.DATE.equals(p.getType())) {
                datePicker.setValue(LocalDate.parse(p.getDefaultValue(), DATE_FORMATTER));
            }
            datePicker.setWidthFull();
            return datePicker;
        }).setHeader(DEFAULT_VALUE);

        final var integerColumn = parameterTable.addComponentColumn(p -> {
            var numberField = new NumberField();
            numberField.setWidthFull();
            return numberField;
        }).setHeader(DEFAULT_VALUE);

        final var stringColumn = parameterTable.addComponentColumn(p -> {
            var textField = new TextField();
            textField.setWidthFull();
            return textField;
        }).setHeader(DEFAULT_VALUE);

        Stream.of(dateColumn, integerColumn, stringColumn).forEach(c -> c.setVisible(false));
        parameterTable.addComponentColumn(p -> {
            final ComboBox<Parameter.Type> typeComboBox = new ComboBox<>();
            typeComboBox.setWidthFull();
            typeComboBox.setItems(Parameter.Type.values());
            typeComboBox.setItemLabelGenerator(Parameter.Type::getDescription);
            typeComboBox.addValueChangeListener(e -> {
                switch (e.getValue()) {
                    case DATE -> {
                        dateColumn.setVisible(true);
                        integerColumn.setVisible(false);
                        stringColumn.setVisible(false);
                    }
                    case INTEGER -> {
                        dateColumn.setVisible(false);
                        integerColumn.setVisible(true);
                        stringColumn.setVisible(false);
                    }
                    case STRING -> {
                        dateColumn.setVisible(false);
                        integerColumn.setVisible(false);
                        stringColumn.setVisible(true);
                    }
                }
            });
            return typeComboBox;
        }).setHeader("Тип");
        return parameterTable;
    }

    private Component createDetectButton() {
        detectButton.setIcon(VaadinIcon.SEARCH.create());
        detectButton.addClickListener(e -> detectParameters());
        return detectButton;
    }

    private void detectParameters() {
        final var parameters = queryTabContent.getItems().stream()
                .map(query -> NamedParameterUtils.buildSqlParameterList(NamedParameterUtils.parseSqlStatement(query.getSqlTextField().getValue()), new MapSqlParameterSource()))
                .flatMap(Collection::stream)
                .map(p -> new Parameter(UUID.randomUUID(), p.getName()))
                .toList();
        items.clear();
        items.addAll(parameters);
        parameterTable.setItems(parameters);
    }

}
