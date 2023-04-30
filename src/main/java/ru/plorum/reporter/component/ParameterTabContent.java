package ru.plorum.reporter.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import ru.plorum.reporter.model.Parameter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static ru.plorum.reporter.util.Constants.*;


@Getter
public class ParameterTabContent extends VerticalLayout {

    private final Grid<Parameter> parameterGrid = new Grid<>();

    private final List<Parameter> items = new ArrayList<>();

    private final Button detectButton = new Button("Определить");

    private final QueryTabContent queryTabContent;

    public ParameterTabContent(final QueryTabContent queryTabContent, final DatePicker.DatePickerI18n i18n) {
        setHeightFull();
        this.queryTabContent = queryTabContent;
        add(createParameterGrid(i18n));
        add(createDetectButton());
    }

    private Component createParameterGrid(final DatePicker.DatePickerI18n i18n) {
        final Function<Component, Component> wrappingFunction = c -> {
            final var layout = new VerticalLayout();
            layout.setWidthFull();
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.add(c);
            return layout;
        };

        parameterGrid.addColumn(Parameter::getName).setHeader(NAME);

        parameterGrid.addComponentColumn(p -> {
            final var descriptionField = p.getDescriptionField();
            descriptionField.setWidthFull();
            return wrappingFunction.apply(descriptionField);
        }).setHeader(DESCRIPTION);

        final var dateColumn = parameterGrid.addComponentColumn(p -> {
            var datePicker = p.getDateDefaultValue();
            datePicker.setI18n(i18n);
            datePicker.setWidthFull();
            return wrappingFunction.apply(datePicker);
        }).setHeader(DEFAULT_VALUE);

        final var integerColumn = parameterGrid.addComponentColumn(p -> {
            var numberField = p.getIntegerDefaultValue();
            numberField.setWidthFull();
            return wrappingFunction.apply(numberField);
        }).setHeader(DEFAULT_VALUE);

        final var stringColumn = parameterGrid.addComponentColumn(p -> {
            var textField = p.getStringDefaultValue();
            textField.setWidthFull();
            return wrappingFunction.apply(textField);
        }).setHeader(DEFAULT_VALUE);

        Stream.of(dateColumn, integerColumn, stringColumn).forEach(c -> c.setVisible(false));
        parameterGrid.addComponentColumn(p -> {
            final ComboBox<Parameter.Type> typeComboBox = p.getTypeComboBox();
            typeComboBox.setWidthFull();
            typeComboBox.setItemLabelGenerator(Parameter.Type::getDescription);
            typeComboBox.addValueChangeListener(e -> {
                final var value = e.getValue();
                if (Objects.isNull(value)) return;
                switch (value) {
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
            return wrappingFunction.apply(typeComboBox);
        }).setHeader("Тип");
        return parameterGrid;
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
        parameterGrid.setItems(items);
    }

}
