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
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.plorum.reporter.component.ConfirmationDialog;
import ru.plorum.reporter.component.NewButton;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static ru.plorum.reporter.util.Constants.DELETE;
import static ru.plorum.reporter.util.Constants.USERS;

@PageTitle(USERS)
@FieldDefaults(level = AccessLevel.PROTECTED)
@Route(value = "users", layout = MainView.class)
public class UsersView extends AbstractView {

    final UserRepository userRepository;
    final PaginatedGrid<User> userTable;

    public UsersView(
            final UserRepository userRepository
    ) {
        this.userRepository = userRepository;
        this.userTable = createUserTable();
    }

    @Override
    @PostConstruct
    protected void initialize() {
        final var layout = new VerticalLayout(new H4(USERS), createNewButton());
        layout.setPadding(false);
        horizontal.add(layout);
        vertical.add(userTable);
        add(vertical);
    }

    private Component createNewButton() {
        return new NewButton("Новый пользователь", "users/upsert");
    }

    private PaginatedGrid<User> createUserTable() {
        final Grid<User> grid = new Grid<>();
        grid.addColumn(createEditButtonRenderer()).setHeader("Пользователь");
        grid.addColumn(User::getName).setHeader("ФИО");
        grid.addColumn(User::getEmail).setHeader("Email");
        grid.addColumn(user -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(user.getCreatedOn())).setHeader("Создан");
        grid.addColumn(createActionRenderer()).setTextAlign(ColumnTextAlign.CENTER).setAutoWidth(true);
        final var paginatedGrid = new PaginatedGrid<>(grid);
        paginatedGrid.setItems(userRepository.findAll());
        return paginatedGrid;
    }

    private ComponentRenderer<Button, User> createEditButtonRenderer() {
        final SerializableBiConsumer<Button, User> editButtonProcessor = (button, user) -> {
            button.setThemeName("tertiary");
            button.setText(user.getLogin());
            button.addClickListener(e -> button.getUI().ifPresent(ui -> ui.navigate("users/upsert/", getQueryParameters(user))));
        };
        return new ComponentRenderer<>(Button::new, editButtonProcessor);
    }

    private QueryParameters getQueryParameters(final User user) {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("id", user.getId().toString());
        parameters.put("name", user.getLogin());
        return QueryParameters.simple(parameters);
    }

    private ComponentRenderer<HorizontalLayout, User> createActionRenderer() {
        final SerializableBiConsumer<HorizontalLayout, User> actionProcessor = (layout, user) -> {
            if (user.isActive()) {
                layout.add(createBlockButton(user));
            }
            if (!user.isActive()) {
                layout.add(createUnblockButton(user));
            }
            final String message = String.format("Хотите удалить пользователя \"%s\"?", user.getName());
            final Runnable callback = () -> {
                userRepository.delete(user);
                userTable.setItems(userRepository.findAll());
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

    private Button createBlockButton(final User user) {
        final var message = String.format("Хотите заблокировать пользователя \"%s\"?", user.getName());
        final Runnable callback = () -> {
            user.setActive(false);
            userRepository.save(user);
            userTable.setItems(userRepository.findAll());
        };
        final var button = new Button();
        button.setIcon(VaadinIcon.LOCK.create());
        button.setText("Заблокировать");
        button.addClickListener(e -> new ConfirmationDialog(message, callback).open());
        return button;
    }

    private Button createUnblockButton(final User user) {
        final var message = String.format("Хотите разблокировать пользователя \"%s\"?", user.getName());
        final Runnable callback = () -> {
            user.setActive(true);
            userRepository.save(user);
            userTable.setItems(userRepository.findAll());
        };
        final var button = new Button();
        button.setIcon(VaadinIcon.UNLOCK.create());
        button.setText("Разблокировать");
        button.addClickListener(e -> new ConfirmationDialog(message, callback).open());
        return button;
    }

}
