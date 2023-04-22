package ru.plorum.reporter.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.AccessLevel;
import lombok.Getter;
import ru.plorum.reporter.model.connection.Connection;
import ru.plorum.reporter.service.ConnectionService;

@Getter
public class SourcesTabContent extends VerticalLayout {

    @Getter(AccessLevel.NONE)
    private final ConnectionService connectionService;

    private final ComboBox<Connection> connectionComboBox = new ComboBox<>("Источники для выборки данных");

    public SourcesTabContent(final ConnectionService connectionService) {
        this.connectionService = connectionService;
        setHeightFull();
        connectionComboBox.setItemLabelGenerator(Connection::getDescription);
        connectionComboBox.setItems(connectionService.find());
        add(connectionComboBox);
    }

}
