package ru.plorum.reporter.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.component.ILicenseCache;
import ru.plorum.reporter.model.UserGroup;
import ru.plorum.reporter.service.UserGroupService;

import java.util.Collections;
import java.util.UUID;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(USER_GROUP)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "groups/upsert", layout = MainView.class)
public class UserGroupUpsertView extends AbstractView implements HasUrlParameter<String>, Validatable, BeforeEnterObserver {

    private final TextField nameField = new TextField(NAME);

    private final TextArea descriptionField = new TextArea(DESCRIPTION);

    private final ComboBox<UserGroup> parentGroupField = new ComboBox<>("Вышестоящая группа");

    private final Button saveButton = new Button(SAVE);

    private Registration saveListener;

    private final UserGroupService userGroupService;

    private final ILicenseCache licenseCache;

    public UserGroupUpsertView(
            final UserGroupService userGroupService,
            final ILicenseCache licenseCache
    ) {
        this.userGroupService = userGroupService;
        this.licenseCache = licenseCache;
    }

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        vertical.add(nameField);
        vertical.add(descriptionField);
        parentGroupField.setItemLabelGenerator(UserGroup::getName);
        parentGroupField.setItems(userGroupService.findAll());
        vertical.add(parentGroupField);
        vertical.add(createSaveButton());
        add(vertical);
    }

    private Button createSaveButton() {
        saveListener = saveButton.addClickListener(e -> {
            if (!validate()) return;
            saveUserGroup(new UserGroup(UUID.randomUUID()));
            saveButton.getUI().ifPresent(ui -> ui.navigate("groups"));
        });
        return saveButton;
    }

    @Transactional
    public void saveUserGroup(final UserGroup group) {
        group.setName(nameField.getValue());
        group.setDescription(descriptionField.getValue());
        group.setParentUserGroup(parentGroupField.getValue());
        userGroupService.save(group);
    }

    @Override
    public void setParameter(final BeforeEvent event, @OptionalParameter final String s) {
        final var location = event.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parametersMap = queryParameters.getParameters();
        final var id = parametersMap.getOrDefault("id", Collections.emptyList());
        if (CollectionUtils.isEmpty(id)) return;
        final var group = userGroupService.findById(UUID.fromString(id.iterator().next()));
        if (group.isEmpty()) return;
        nameField.setValue(group.get().getName());
        descriptionField.setValue(group.get().getDescription());
        parentGroupField.setValue(group.get().getParentUserGroup());
        saveListener.remove();
        saveListener = saveButton.addClickListener(e -> {
            if (!validate()) return;
            saveUserGroup(group.get());
            saveButton.getUI().ifPresent(ui -> ui.navigate("groups"));
        });
    }

    @Override
    public boolean validate() {
        final Binder<UserGroup> binder = new BeanValidationBinder<>(UserGroup.class);
        binder.forField(nameField).asRequired(REQUIRED_FIELD).bind(UserGroup::getName, UserGroup::setName);
        return binder.validate().isOk();
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
        if (licenseCache.getActive().isEmpty()) {
            beforeEnterEvent.rerouteTo(IndexView.class);
        }
    }

}
