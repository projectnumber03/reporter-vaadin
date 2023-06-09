package ru.plorum.reporter.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.Parameter;

import java.util.*;
import java.util.function.Function;

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

        parameterGrid.addComponentColumn(p -> {
            switch (p.getType()) {
                case DATE -> {
                    var datePicker = p.getDateDefaultValue();
                    datePicker.setI18n(i18n);
                    datePicker.setWidthFull();
                    return wrappingFunction.apply(datePicker);
                }
                case INTEGER -> {
                    var numberField = p.getIntegerDefaultValue();
                    numberField.setWidthFull();
                    return wrappingFunction.apply(numberField);
                }
                default -> {
                    var textField = p.getStringDefaultValue();
                    textField.setWidthFull();
                    return wrappingFunction.apply(textField);
                }
            }
        }).setHeader(DEFAULT_VALUE);

        parameterGrid.addComponentColumn(p -> {
            final ComboBox<Parameter.Type> typeComboBox = p.getTypeComboBox();
            typeComboBox.setWidthFull();
            typeComboBox.setItemLabelGenerator(Parameter.Type::getDescription);
            typeComboBox.addValueChangeListener(e -> {
                final var value = e.getValue();
                if (Objects.isNull(value)) return;
                items.stream().filter(item -> item.equals(p)).findAny().ifPresent(v -> {
                    v.setType(e.getValue());
                    parameterGrid.setItems(items);
                });
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
        if (CollectionUtils.isEmpty(parameters)) {
            final var notification = Notification.show("Параметры не найдены");
            notification.setPosition(Notification.Position.TOP_CENTER);
        }
        items.clear();
        items.addAll(parameters);
        parameterGrid.setItems(items);
    }

}
