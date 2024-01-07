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
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.apache.logging.log4j.util.Strings;
import ru.plorum.reporter.component.ConfirmationDialog;
import ru.plorum.reporter.component.LicenseCache;
import ru.plorum.reporter.component.NewButton;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.UserGroup;
import ru.plorum.reporter.service.UserGroupService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(USER_GROUPS)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "groups", layout = MainView.class)
public class UserGroupView extends AbstractView implements BeforeEnterObserver {

    private final UserGroupService userGroupService;

    private final LicenseCache licenseCache;

    private final PaginatedGrid<UserGroup> userGroupTable;

    public UserGroupView(
            final UserGroupService userGroupService,
            final LicenseCache licenseCache
    ) {
        this.userGroupService = userGroupService;
        this.licenseCache = licenseCache;
        this.userGroupTable = createUserGroupTable();
    }

    @Override
    @PostConstruct
    protected void initialize() {
        final var layout = new VerticalLayout(new H4(USER_GROUPS), createNewButton());
        layout.setPadding(false);
        horizontal.add(layout);
        vertical.add(userGroupTable);
        add(vertical);
    }

    private Component createNewButton() {
        return new NewButton("Новая группа", "groups/upsert");
    }

    private PaginatedGrid<UserGroup> createUserGroupTable() {
        final Grid<UserGroup> grid = new Grid<>();
        grid.addColumn(createEditButtonRenderer()).setHeader(NAME);
        grid.addColumn(UserGroup::getDescription).setHeader(DESCRIPTION);
        grid.addColumn(g -> Optional.ofNullable(g.getParentUserGroup()).map(UserGroup::getName).orElse(Strings.EMPTY)).setHeader("Вышестоящая группа");
        grid.addColumn(createActionRenderer()).setTextAlign(ColumnTextAlign.CENTER).setAutoWidth(true);
        final var paginatedGrid = new PaginatedGrid<>(grid);
        paginatedGrid.setItems(userGroupService.findAll());
        return paginatedGrid;
    }

    private ComponentRenderer<Button, UserGroup> createEditButtonRenderer() {
        final SerializableBiConsumer<Button, UserGroup> editButtonProcessor = (button, group) -> {
            button.setThemeName("tertiary");
            button.setText(group.getName());
            button.addClickListener(e -> button.getUI().ifPresent(ui -> ui.navigate("groups/upsert/", getQueryParameters(group))));
        };
        return new ComponentRenderer<>(Button::new, editButtonProcessor);
    }

    private QueryParameters getQueryParameters(final UserGroup group) {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("id", group.getId().toString());
        parameters.put("name", group.getName());
        return QueryParameters.simple(parameters);
    }

    private ComponentRenderer<HorizontalLayout, UserGroup> createActionRenderer() {
        final SerializableBiConsumer<HorizontalLayout, UserGroup> actionProcessor = (layout, group) -> {
            final String message = String.format("Хотите удалить группу \"%s\"?", group.getName());
            final Runnable callback = () -> {
                userGroupService.delete(group);
                userGroupTable.setItems(userGroupService.findAll());
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

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
        if (licenseCache.getActive().isEmpty()) {
            beforeEnterEvent.rerouteTo(IndexView.class);
        }
    }

}
