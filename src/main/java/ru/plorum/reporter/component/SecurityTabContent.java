package ru.plorum.reporter.component;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.Getter;
import ru.plorum.reporter.model.ReportVisibility;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.model.UserGroup;
import ru.plorum.reporter.service.UserGroupService;
import ru.plorum.reporter.service.UserService;

@Getter
public class SecurityTabContent extends VerticalLayout {

    private final RadioButtonGroup<ReportVisibility> reportVisibilityRadioButtonGroup = new RadioButtonGroup<>("Видимость отчёта");

    private final MultiSelectComboBox<UserGroup> groupSelect = new MultiSelectComboBox<>();

    private final MultiSelectComboBox<User> userSelect = new MultiSelectComboBox<>();

    public SecurityTabContent(final UserService userService, final UserGroupService userGroupService) {
        groupSelect.setItems(userGroupService.findAll());
        groupSelect.setItemLabelGenerator(UserGroup::getName);
        groupSelect.setVisible(false);
        userSelect.setItems(userService.findAll());
        userSelect.setItemLabelGenerator(User::getName);
        userSelect.setVisible(false);
        reportVisibilityRadioButtonGroup.setItems(ReportVisibility.values());
        reportVisibilityRadioButtonGroup.setItemLabelGenerator(ReportVisibility::getDescription);
        reportVisibilityRadioButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        reportVisibilityRadioButtonGroup.setRenderer(new ComponentRenderer<>(rv -> {
            final VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);
            if (rv.equals(ReportVisibility.GROUP)) {
                layout.add(new Text(rv.getDescription()), groupSelect);
                return layout;
            }
            if (rv.equals(ReportVisibility.USERS)) {
                layout.add(new Text(rv.getDescription()), userSelect);
                return layout;
            }
            layout.add(new Text(rv.getDescription()));
            return layout;
        }));
        reportVisibilityRadioButtonGroup.addValueChangeListener(e -> {
            if (e.getValue().equals(ReportVisibility.GROUP)) {
                groupSelect.setVisible(true);
                userSelect.setVisible(false);
                return;
            }
            if (e.getValue().equals(ReportVisibility.USERS)) {
                groupSelect.setVisible(false);
                userSelect.setVisible(true);
                return;
            }
            groupSelect.setVisible(false);
            userSelect.setVisible(false);
        });
        add(reportVisibilityRadioButtonGroup);
    }

}
