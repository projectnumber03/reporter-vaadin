package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.component.ReportGroupTabContent;
import ru.plorum.reporter.component.SecurityTabContent;
import ru.plorum.reporter.model.ReportGroup;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.model.Visibility;
import ru.plorum.reporter.service.ReportGroupService;
import ru.plorum.reporter.service.UserGroupService;
import ru.plorum.reporter.service.UserService;

import java.util.*;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(REPORT_GROUP)
@Route(value = "report_groups/upsert", layout = MainView.class)
public class ReportGroupUpsertView extends AbstractView implements HasUrlParameter<String>, Validatable {

    private final UserService userService;

    private final UserGroupService userGroupService;

    private final ReportGroupService reportGroupService;

    private final TabSheet tabSheet = new TabSheet();

    private final Button saveButton = new Button(SAVE);

    private final Map<String, Component> content = new HashMap<>();

    public ReportGroupUpsertView(
            final UserService userService,
            final UserGroupService userGroupService,
            final ReportGroupService reportGroupService
    ) {
        this.userService = userService;
        this.userGroupService = userGroupService;
        this.reportGroupService = reportGroupService;
    }

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        vertical.add(createMenuTabs());
        vertical.add(createSaveButton());
        add(vertical);
    }

    private Component createSaveButton() {
        saveButton.addClickListener(e -> {
            if (!validate()) return;
            reportGroupService.saveFromContent(content);
            saveButton.getUI().ifPresent(ui -> ui.navigate("report_groups"));
        });
        return saveButton;
    }

    private TabSheet createMenuTabs() {
        tabSheet.setWidthFull();
        tabSheet.setHeightFull();

        final ReportGroupTabContent reportGroupTabContent = new ReportGroupTabContent();
        content.put(REPORT_GROUP, reportGroupTabContent);
        final SecurityTabContent securityTabContent = new SecurityTabContent(userService, userGroupService);
        content.put(SECURITY, securityTabContent);

        tabSheet.add(REPORT_GROUP, reportGroupTabContent);
        tabSheet.add(SECURITY, securityTabContent);

        return tabSheet;
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, @OptionalParameter final String s) {
        final var location = beforeEvent.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parametersMap = queryParameters.getParameters();
        final var id = parametersMap.getOrDefault("id", Collections.emptyList());
        final var securityTabContent = (SecurityTabContent) content.get(SECURITY);
        if (CollectionUtils.isEmpty(id)) {
            securityTabContent.getReportVisibilityRadioButtonGroup().setValue(Visibility.ME);
            return;
        }
        final var reportGroup = reportGroupService.findById(UUID.fromString(id.iterator().next()));
        if (Objects.isNull(reportGroup)) return;
        final var reportGroupTabContent = (ReportGroupTabContent) content.get(REPORT_GROUP);
        reportGroupTabContent.getNameField().setValue(reportGroup.getName());
        reportGroupTabContent.getDescriptionField().setValue(reportGroup.getDescription());
        securityTabContent.getReportVisibilityRadioButtonGroup().setValue(reportGroup.getVisibility());
        switch (securityTabContent.getReportVisibilityRadioButtonGroup().getValue()) {
            case GROUPS ->
                    securityTabContent.getGroupSelect().setValue(reportGroup.getPermittedUsers().stream().map(User::getGroup).distinct().toList());
            case USERS ->
                    securityTabContent.getUserSelect().setValue(reportGroup.getPermittedUsers().stream().distinct().toList());
        }
    }

    @Override
    public boolean validate() {
        final Binder<ReportGroup> binder = new BeanValidationBinder<>(ReportGroup.class);
        final var reportGroupTabContent = (ReportGroupTabContent) content.get(REPORT_GROUP);
        binder.forField(reportGroupTabContent.getNameField()).asRequired(REQUIRED_FIELD).bind(ReportGroup::getName, ReportGroup::setName);
        return binder.validate().isOk();
    }

}
