package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import ru.plorum.reporter.component.ConfirmationDialog;
import ru.plorum.reporter.component.NewButton;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.model.connection.Connection;
import ru.plorum.reporter.service.ConnectionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(CONNECTIONS)
@Route(value = "connections", layout = MainView.class)
public class ConnectionView extends AbstractView {

    private final PaginatedGrid<Connection> connectionTable;

    private final ConnectionService connectionService;

    public ConnectionView(final ConnectionService connectionService) {
        this.connectionService = connectionService;
        this.connectionTable = createConnectionTable();
    }

    @Override
    @PostConstruct
    protected void initialize() {
        final VerticalLayout layout = new VerticalLayout(new H4(CONNECTIONS), createNewButton());
        layout.setPadding(false);
        horizontal.add(layout);
        vertical.add(connectionTable);
        add(vertical);
    }

    private Component createNewButton() {
        return new NewButton("Новое подключение", "connections/upsert");
    }

    private PaginatedGrid<Connection> createConnectionTable() {
        final Grid<Connection> grid = new Grid<>();
        grid.addColumn(createEditButtonRenderer()).setHeader("Название подключения");
        grid.addColumn(Connection::getType).setHeader("Тип БД");
        grid.addColumn(Connection::getName).setHeader("Название БД");
        grid.addColumn(c -> Optional.ofNullable(c.getUser()).map(User::getName).orElse(NA)).setHeader("Автор");
        grid.addColumn(createActionRenderer()).setTextAlign(ColumnTextAlign.CENTER);
        final PaginatedGrid<Connection> paginatedGrid = new PaginatedGrid<>(grid);
        paginatedGrid.setItems(connectionService.find());
        return paginatedGrid;
    }

    private ComponentRenderer<Button, Connection> createEditButtonRenderer() {
        final SerializableBiConsumer<Button, Connection> editButtonProcessor = (button, connection) -> {
            button.setThemeName("tertiary");
            button.setText(connection.getDescription());
            button.addClickListener(e -> button.getUI().ifPresent(ui -> ui.navigate("connections/upsert/", getQueryParameters(connection))));
        };
        return new ComponentRenderer<>(Button::new, editButtonProcessor);
    }

    private ComponentRenderer<HorizontalLayout, Connection> createActionRenderer() {
        final SerializableBiConsumer<HorizontalLayout, Connection> actionProcessor = (layout, connection) -> {
            final String message = String.format("Хотите удалить подключение \"%s\"?", connection.getDescription());
            final Runnable callback = () -> {
                connectionService.delete(connection);
                connectionTable.setItems(connectionService.find());
            };
            final Button button = new Button();
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            button.setIcon(VaadinIcon.TRASH.create());
            button.setText(DELETE);
            button.addClickListener(e -> new ConfirmationDialog(message, callback).open());
            layout.add(button);
        };
        return new ComponentRenderer<>(HorizontalLayout::new, actionProcessor);
    }

    private QueryParameters getQueryParameters(final Connection connection) {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("id", connection.getId().toString());
        parameters.put("name", connection.getDescription());
        return QueryParameters.simple(parameters);
    }

}
