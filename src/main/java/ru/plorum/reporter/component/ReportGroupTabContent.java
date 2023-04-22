package ru.plorum.reporter.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

import static ru.plorum.reporter.util.Constants.DESCRIPTION;
import static ru.plorum.reporter.util.Constants.NAME;

@Getter
public class ReportGroupTabContent extends VerticalLayout {

    private final TextField nameField = new TextField(NAME);

    private final TextArea descriptionField = new TextArea(DESCRIPTION);

    public ReportGroupTabContent() {
        setHeightFull();
        add(nameField, descriptionField);
    }

}
