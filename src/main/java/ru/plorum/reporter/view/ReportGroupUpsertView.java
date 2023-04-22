package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import ru.plorum.reporter.component.ReportGroupTabContent;
import ru.plorum.reporter.component.SecurityTabContent;
import ru.plorum.reporter.service.ReportGroupService;
import ru.plorum.reporter.service.UserGroupService;
import ru.plorum.reporter.service.UserService;

import java.util.HashMap;
import java.util.Map;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(REPORT_GROUP)
@Route(value = "report_groups/upsert", layout = MainView.class)
public class ReportGroupUpsertView extends AbstractView implements HasUrlParameter<String> {

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
            reportGroupService.saveFromContent(content);
            saveButton.getUI().ifPresent(ui -> ui.navigate("report_groups"));
        });
        return saveButton;
    }

    private TabSheet createMenuTabs() {
        tabSheet.setWidthFull();
        tabSheet.setHeightFull();

        content.put(REPORT_GROUP, new ReportGroupTabContent());
        content.put(SECURITY, new SecurityTabContent(userService, userGroupService));

        return tabSheet;
    }

    @Override
    public void setParameter(final BeforeEvent event, @OptionalParameter final String s) {

    }

}
