package ru.plorum.reporter.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import ru.plorum.reporter.model.connection.Connection;
import ru.plorum.reporter.service.ConnectionService;

@Getter
public class SourcesTabContent extends VerticalLayout {

    private final ConnectionService connectionService;

    private final ComboBox<Connection> connectionComboBox = new ComboBox<>("Источники для выборки данных");

    public SourcesTabContent(final ConnectionService connectionService) {
        this.connectionService = connectionService;
        setHeightFull();
        connectionComboBox.setItems(connectionService.find());
        add(connectionComboBox);
    }

}
