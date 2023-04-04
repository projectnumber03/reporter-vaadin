package ru.plorum.reporter.component;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.timepicker.TimePicker;
import lombok.Getter;
import ru.plorum.reporter.model.User;

import java.time.Duration;

@Getter
public class SchedulerTabContent extends VerticalLayout {

    private final TimePicker timePicker = new TimePicker("Начало:");

    private final MultiSelectComboBox<User> sendTo = new MultiSelectComboBox<>("Отправлять:");

    public SchedulerTabContent() {
        setHeightFull();
        sendTo.setItemLabelGenerator(User::getName);
        timePicker.setStep(Duration.ofMinutes(30));
        add(timePicker, sendTo);
    }

}
