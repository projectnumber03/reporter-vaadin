package ru.plorum.reporter.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.SchedulerTask;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.util.Constants;

import java.util.Collection;

@Getter
public class SchedulerTabContent extends VerticalLayout {

    private final Checkbox enabledCheckbox = new Checkbox("Включен", false);

    private final IntegerField hourField = new IntegerField("Время начала:");

    private final IntegerField minuteField = new IntegerField();

    private final MultiSelectComboBox<User> sendToField = new MultiSelectComboBox<>("Отправлять:");

    private final RadioButtonGroup<SchedulerTask.Type> radioGroup = new RadioButtonGroup<>();

    private final MultiSelectComboBox<Constants.Day> daySelectField = new MultiSelectComboBox<>();

    private final IntegerField intervalSelectField = new IntegerField();

    private final QueryTabContent queryTabContent;

    public SchedulerTabContent(final QueryTabContent queryTabContent) {
        this.queryTabContent = queryTabContent;
        setHeightFull();
        add(createEnabledCheckbox(), createBeginAtField(), createModeSelectComponent(), createSendToField());
    }

    private Component createEnabledCheckbox() {
        enabledCheckbox.addValueChangeListener(e -> {
            if (!e.getValue()) return;
            final var parameters = queryTabContent.getItems().stream()
                    .map(query -> NamedParameterUtils.buildSqlParameterList(NamedParameterUtils.parseSqlStatement(query.getSqlTextField().getValue()), new MapSqlParameterSource()))
                    .flatMap(Collection::stream)
                    .toList();
            if (CollectionUtils.isEmpty(parameters)) return;
            final var notification = Notification.show("Невозможно активировать планировщик для отчёта, содержащего параметры");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setPosition(Notification.Position.TOP_CENTER);
            enabledCheckbox.setValue(e.getOldValue());
        });

        return enabledCheckbox;
    }

    private Component createBeginAtField() {
        final var layout = new HorizontalLayout();
        layout.setPadding(false);
        layout.setAlignItems(Alignment.END);

        hourField.setValue(0);
        hourField.setMin(0);
        hourField.setMax(23);
        hourField.setStepButtonsVisible(true);
        final var hourSuffix = new Div();
        hourSuffix.setText("ч.");
        hourField.setSuffixComponent(hourSuffix);
        hourField.setWidth(170, Unit.PIXELS);

        minuteField.setValue(0);
        minuteField.setMin(0);
        minuteField.setMax(59);
        minuteField.setStepButtonsVisible(true);
        final var minuteSuffix = new Div();
        minuteSuffix.setText("мин.");
        minuteField.setSuffixComponent(minuteSuffix);
        minuteField.setWidth(170, Unit.PIXELS);

        layout.add(hourField, minuteField);

        return layout;
    }

    private Component createSendToField() {
        sendToField.setItemLabelGenerator(User::getName);
        sendToField.setAllowCustomValue(false);

        return sendToField;
    }

    private Component createModeSelectComponent() {
        radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioGroup.setItems(SchedulerTask.Type.values());
        radioGroup.setValue(SchedulerTask.Type.DAY);

        daySelectField.setItemLabelGenerator(Constants.Day::getName);
        daySelectField.setItems(Constants.Day.values());
        daySelectField.getStyle().set("padding-left", "20px");

        intervalSelectField.setReadOnly(true);
        intervalSelectField.setStepButtonsVisible(true);
        intervalSelectField.setMin(1);

        radioGroup.setRenderer(new ComponentRenderer<Component, SchedulerTask.Type>(type -> {
            final var layout = new HorizontalLayout();
            layout.setAlignItems(Alignment.CENTER);
            layout.add(new Span(type.getName()));
            if (type.equals(SchedulerTask.Type.INTERVAL)) {
                layout.add(intervalSelectField);
            }
            if (type.equals(SchedulerTask.Type.DAY)) {
                layout.add(daySelectField);
            }
            return layout;
        }));

        radioGroup.addValueChangeListener(e -> {
            final var type = e.getValue();
            if (type.equals(SchedulerTask.Type.INTERVAL)) {
                daySelectField.deselectAll();
                daySelectField.setReadOnly(true);
                intervalSelectField.setReadOnly(false);
            }
            if (type.equals(SchedulerTask.Type.DAY)) {
                intervalSelectField.clear();
                daySelectField.setItems(Constants.Day.values());
                daySelectField.setReadOnly(false);
                intervalSelectField.setReadOnly(true);
            }
        });

        return radioGroup;
    }

}
