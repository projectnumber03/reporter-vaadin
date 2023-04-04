package ru.plorum.reporter.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.UserGroup;
import ru.plorum.reporter.service.UserGroupService;

import java.util.Collections;
import java.util.UUID;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(USER_GROUP)
@Route(value = "groups/upsert", layout = MainView.class)
public class UserGroupUpsertView extends AbstractView implements HasUrlParameter<String>, Validatable {

    private final TextField nameField = new TextField("Название группы");

    private final TextArea descriptionField = new TextArea("Описание группы");

    private final ComboBox<UserGroup> parentGroupField = new ComboBox<>("Вышестоящая группа");

    private final Button saveButton = new Button(SAVE);

    private Registration saveListener;

    private final UserGroupService userGroupService;

    public UserGroupUpsertView(final UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    @Override
    @PostConstruct
    protected void initialize() {
        horizontal.add(new H4(USER_GROUP));
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
    protected void saveUserGroup(final UserGroup group) {
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

}
