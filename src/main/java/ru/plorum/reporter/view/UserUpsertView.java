package ru.plorum.reporter.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.plorum.reporter.model.Role;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.model.UserGroup;
import ru.plorum.reporter.service.RoleService;
import ru.plorum.reporter.service.UserGroupService;
import ru.plorum.reporter.service.UserService;
import ru.plorum.reporter.util.LoginGenerator;
import ru.plorum.reporter.util.PasswordGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(USER)
@RequiredArgsConstructor
@Route(value = "users/upsert", layout = MainView.class)
public class UserUpsertView extends AbstractView implements HasUrlParameter<String>, Validatable {

    private final TextField fioField = new TextField("ФИО");

    private final TextField emailField = new TextField("Email");

    private final TextField loginField = new TextField("Логин");

    private final Button generateLoginButton = new Button("Создать логин");

    private final PasswordField passwordField = new PasswordField("Пароль");

    private final Button generatePasswordButton = new Button("Создать пароль");

    private final MultiSelectComboBox<Role> rolesField = new MultiSelectComboBox<>("Роли");

    private final ComboBox<UserGroup> groupField = new ComboBox<>("Группа");

    private final Checkbox blockField = new Checkbox("Заблокировать");

    private final Button saveButton = new Button(SAVE);

    private Registration saveListener;

    private final RoleService roleService;

    private final UserService userService;

    private final UserGroupService userGroupService;

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        generateLoginButton.addClickListener(e -> {
            if (StringUtils.hasText(fioField.getValue())) {
                loginField.setValue(generateLogin(fioField.getValue()));
                return;
            }
            final var notification = Notification.show("Введите ФИО");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setPosition(Notification.Position.TOP_CENTER);
        });
        generatePasswordButton.addClickListener(e -> passwordField.setValue(PasswordGenerator.generate()));
        vertical.add(fioField);
        vertical.add(emailField);
        final var loginLayout = new HorizontalLayout(loginField, generateLoginButton);
        loginLayout.setAlignItems(Alignment.END);
        vertical.add(loginLayout);
        final var passwordLayout = new HorizontalLayout(passwordField, generatePasswordButton);
        passwordLayout.setAlignItems(Alignment.END);
        vertical.add(passwordLayout);
        rolesField.setItems(roleService.findAll());
        rolesField.setItemLabelGenerator(Role::getName);
        vertical.add(rolesField);
        groupField.setItems(userGroupService.findAll());
        groupField.setItemLabelGenerator(UserGroup::getName);
        vertical.add(groupField);
        vertical.add(blockField);
        vertical.add(createSaveButton());
        add(vertical);
    }

    private Button createSaveButton() {
        saveListener = saveButton.addClickListener(e -> {
            if (!validate()) return;
            saveUser(new User(UUID.randomUUID()));
            saveButton.getUI().ifPresent(ui -> ui.navigate("groups"));
        });
        return saveButton;
    }

    @Transactional
    protected void saveUser(final User user) {
        user.setActive(!blockField.getValue());
        user.setName(fioField.getValue());
        user.setGroup(groupField.getValue());
        user.setLogin(loginField.getValue());
        user.setPassword(passwordField.getValue());
        user.setRoles(rolesField.getSelectedItems());
        user.setCreatedOn(Optional.ofNullable(user.getCreatedOn()).orElse(LocalDateTime.now()));
        user.setEmail(emailField.getValue());
        userService.save(user);
    }

    @Override
    public void setParameter(final BeforeEvent event, @OptionalParameter final String s) {
        final var location = event.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parametersMap = queryParameters.getParameters();
        final var id = parametersMap.getOrDefault("id", Collections.emptyList());
        if (CollectionUtils.isEmpty(id)) return;
        final var user = userService.findById(UUID.fromString(id.iterator().next()));
        if (user.isEmpty()) return;
        fioField.setValue(user.get().getName());
        Optional.ofNullable(user.get().getEmail()).ifPresent(emailField::setValue);
        loginField.setValue(user.get().getLogin());
        passwordField.setValue(user.get().getPassword());
        rolesField.select(user.get().getRoles());
        Optional.ofNullable(user.get().getGroup()).ifPresent(groupField::setValue);
        blockField.setValue(!user.get().isActive());
        saveListener.remove();
        saveListener = saveButton.addClickListener(e -> {
            if (!validate()) return;
            saveUser(user.get());
            saveButton.getUI().ifPresent(ui -> ui.navigate("users"));
        });
    }

    @Override
    public boolean validate() {
        final Binder<User> binder = new BeanValidationBinder<>(User.class);
        binder.forField(fioField).asRequired(REQUIRED_FIELD).bind(User::getName, User::setName);
        binder.forField(loginField).asRequired(REQUIRED_FIELD).bind(User::getName, User::setName);
        binder.forField(passwordField).asRequired(REQUIRED_FIELD).bind(User::getName, User::setName);
        binder.forField(blockField).withValidator(bf -> !bf || !loginField.getValue().equals(Optional.ofNullable(userService.getAuthenticatedUser()).map(User::getLogin).orElse(null)), "Нельзя заблокировать самого себя");
        return binder.validate().isOk();
    }

    private String generateLogin(final String fio) {
        final var login = LoginGenerator.generate(fio);
        if (CollectionUtils.isEmpty(userService.findByLogin(login))) return login;
        if (login.matches("\\D+_\\d+")) {
            return login.split("_")[0] + "_" + (Long.parseLong(login.split("_")[1]) + 1);
        }
        return login + "_1";
    }

}
