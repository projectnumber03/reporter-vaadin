package ru.plorum.reporter.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.connection.*;
import ru.plorum.reporter.service.ConnectionService;
import ru.plorum.reporter.service.UserService;

import java.util.*;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(CONNECTION)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "connections/upsert", layout = MainView.class)
public class ConnectionUpsertView extends AbstractView implements HasUrlParameter<String>, Validatable {

    private static final Map<String, Connection> connectionMapping = new HashMap<>(){{
        put("MSSQL", new MSSQLConnection());
        put("ORACLE", new OracleConnection());
        put("MYSQL", new MYSQLConnection());
        put("POSTGRESQL", new PostgresConnection());
        put("H2", new H2Connection());
    }};

    private final ConnectionService connectionService;

    private final UserService userService;

    private final ComboBox<String> typeField = new ComboBox<>("Тип БД", Arrays.asList("MSSQL", "ORACLE", "MYSQL", "POSTGRESQL", "H2"));

    private final TextArea descriptionField = new TextArea(DESCRIPTION);

    private final TextField hostField = new TextField("IP адрес (host) БД");

    private final TextField portField = new TextField("Порт БД");

    private final TextField nameField = new TextField("Название БД");

    private final TextField loginField = new TextField("Пользователь БД");

    private final PasswordField passwordField = new PasswordField("Пароль к БД");

    private final Button testButton = new Button("Тест");

    private final Button saveButton = new Button(SAVE);

    private Registration saveListener;

    public ConnectionUpsertView(final ConnectionService connectionService, final UserService userService) {
        this.connectionService = connectionService;
        this.userService = userService;
    }

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        vertical.add(typeField, descriptionField, hostField, nameField, portField, loginField, passwordField);
        final var buttonLayout = new HorizontalLayout(createTestButton(), createSaveButton());
        buttonLayout.setPadding(false);
        vertical.add(buttonLayout);
        add(vertical);
    }

    private Button createSaveButton() {
        saveListener = saveButton.addClickListener(e -> {
            saveConnection(UUID.randomUUID());
            saveButton.getUI().ifPresent(ui -> ui.navigate("connections"));
        });
        return saveButton;
    }

    private Button createTestButton() {
        testButton.addClickListener(e -> {
            final var connection = connectionMapping.get(typeField.getValue());
            connection.setType(typeField.getValue());
            connection.setDescription(descriptionField.getValue());
            connection.setHost(hostField.getValue());
            try {
                connection.setPort(Integer.parseInt(portField.getValue()));
            } catch (Exception ignored) {
            }
            connection.setLogin(loginField.getValue());
            connection.setPassword(passwordField.getValue());
            connection.setUser(userService.getAuthenticatedUser());
            connection.setName(nameField.getValue());
            if (connectionService.test(connection)) {
                final Notification notification = Notification.show(SUCCESS);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setPosition(Notification.Position.TOP_CENTER);
                return;
            }
            final Notification notification = Notification.show("Ошибка");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setPosition(Notification.Position.TOP_CENTER);
        });
        return testButton;
    }

    private void saveConnection(final UUID id) {
        final var connection = connectionMapping.get(typeField.getValue());
        connection.setId(id);
        connection.setType(typeField.getValue());
        connection.setDescription(descriptionField.getValue());
        connection.setHost(hostField.getValue());
        try {
            connection.setPort(Integer.parseInt(portField.getValue()));
        } catch (Exception ignored) {
        }
        connection.setLogin(loginField.getValue());
        connection.setPassword(passwordField.getValue());
        connection.setUser(userService.getAuthenticatedUser());
        connection.setName(nameField.getValue());
        connectionService.save(connection);
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, @OptionalParameter final String s) {
        final var location = beforeEvent.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parametersMap = queryParameters.getParameters();
        final var id = parametersMap.getOrDefault("id", Collections.emptyList());
        if (CollectionUtils.isEmpty(id)) return;
        final var connection = connectionService.findById(UUID.fromString(id.iterator().next()));
        if (connection.isEmpty()) return;
        typeField.setValue(connection.get().getType());
        descriptionField.setValue(connection.get().getDescription());
        hostField.setValue(connection.get().getHost());
        portField.setValue(connection.get().getPort() + "");
        nameField.setValue(connection.get().getName());
        loginField.setValue(connection.get().getLogin());
        passwordField.setValue(connection.get().getPassword());
        saveListener.remove();
        saveListener = saveButton.addClickListener(e -> {
            if (!validate()) return;
            saveConnection(connection.get().getId());
            saveButton.getUI().ifPresent(ui -> ui.navigate("connections"));
        });
    }

    @Override
    public boolean validate() {
        return true;
    }

}
