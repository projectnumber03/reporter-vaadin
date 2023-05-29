package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.component.ConfirmationDialog;
import ru.plorum.reporter.component.NewButton;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.Role;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.service.IUserService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.plorum.reporter.util.Constants.DELETE;
import static ru.plorum.reporter.util.Constants.USERS;

@PageTitle(USERS)
@RolesAllowed(value = {"ROLE_ADMIN"})
@FieldDefaults(level = AccessLevel.PROTECTED)
@Route(value = "users", layout = MainView.class)
public class UserView extends AbstractView {

    final IUserService userService;
    final PaginatedGrid<User> userTable;

    public UserView(
            final IUserService userService
    ) {
        this.userService = userService;
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
        grid.addColumn(u -> u.getRoles().stream().map(Role::getName).collect(Collectors.joining(", "))).setHeader("Роли");
        grid.addColumn(user -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(user.getCreatedOn())).setHeader("Создан");
        grid.addColumn(new ComponentRenderer<>(u -> {
            if (CollectionUtils.isEmpty(u.getRoles())) {
                final Span pending = new Span("Новый");
                pending.getElement().getThemeList().add("badge");
                return pending;
            }
            if (u.isActive()) {
                final Span pending = new Span("Активен");
                pending.getElement().getThemeList().add("badge success");
                return pending;
            }
            final Span pending = new Span("Заблокирован");
            pending.getElement().getThemeList().add("badge error");
            return pending;
        })).setHeader("Статус");
        grid.addColumn(createActionRenderer()).setTextAlign(ColumnTextAlign.CENTER).setAutoWidth(true);
        final var paginatedGrid = new PaginatedGrid<>(grid);
        paginatedGrid.setItems(userService.findAllWithRoles());
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
                userService.delete(user);
                userTable.setItems(userService.findAll());
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
            userService.save(user);
            userTable.setItems(userService.findAll());
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
            userService.save(user);
            userTable.setItems(userService.findAll());
        };
        final var button = new Button();
        button.setIcon(VaadinIcon.UNLOCK.create());
        button.setText("Разблокировать");
        button.addClickListener(e -> new ConfirmationDialog(message, callback).open());
        return button;
    }

}
