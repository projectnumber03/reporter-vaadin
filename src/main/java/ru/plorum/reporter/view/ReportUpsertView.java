package ru.plorum.reporter.view;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import ru.plorum.reporter.component.*;
import ru.plorum.reporter.service.ConnectionService;
import ru.plorum.reporter.service.ReportService;
import ru.plorum.reporter.service.UserGroupService;
import ru.plorum.reporter.service.UserService;

import java.util.HashMap;
import java.util.Map;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(REPORT)
@Route(value = "reports/upsert", layout = MainView.class)
public class ReportUpsertView extends AbstractView implements HasUrlParameter<String> {

    private final UserService userService;

    private final ConnectionService connectionService;

    private final UserGroupService userGroupService;

    private final ReportService reportService;

    private final TabSheet tabSheet = new TabSheet();

    private final Button saveButton = new Button(SAVE);

    private final Map<String, Component> content = new HashMap<>();

    public ReportUpsertView(
            final UserService userService,
            final ConnectionService connectionService,
            final UserGroupService userGroupService,
            final ReportService reportService
    ) {
        this.userService = userService;
        this.connectionService = connectionService;
        this.userGroupService = userGroupService;
        this.reportService = reportService;
    }

    @Override
    @PostConstruct
    protected void initialize() {
        setWidthFull();
        setHeightFull();
        vertical.setWidthFull();
        vertical.setHeightFull();
        vertical.add(createMenuTabs());
        vertical.add(createSaveButton());
        add(vertical);
    }

    private TabSheet createMenuTabs() {
        tabSheet.setWidthFull();
        tabSheet.setHeightFull();
        final Component queriesTabContent = createQueriesTabContent();
        content.put(REPORT_QUERIES, queriesTabContent);
        final Component schedulerTabContent = createSchedulerTabContent();
        content.put(SCHEDULER, schedulerTabContent);
        final Component sourcesTabContent = createSourcesTabContent();
        content.put(SOURCES, sourcesTabContent);
        final Component importExportTabContent = createImportExportTabContent();
        content.put(IMPORT_EXPORT, importExportTabContent);
        final Component securityTabContent = createSecurityTabContent();
        content.put(SECURITY, securityTabContent);
        tabSheet.add(REPORT_QUERIES, queriesTabContent);
        tabSheet.add(SCHEDULER, schedulerTabContent);
        tabSheet.add(SOURCES, sourcesTabContent);
        tabSheet.add(IMPORT_EXPORT, importExportTabContent);
        tabSheet.add(SECURITY, securityTabContent);
        return tabSheet;
    }

    private Component createSaveButton() {
        saveButton.addClickListener(e -> {
            reportService.saveFromContent(content);
            saveButton.getUI().ifPresent(ui -> ui.navigate("my_reports"));
        });
        return saveButton;
    }

    private Component createQueriesTabContent() {
        return new QueryTabContent();
    }

    private Component createSchedulerTabContent() {
        var schedulerTabContent = new SchedulerTabContent();
        schedulerTabContent.getSendTo().setItems(userService.findAll());
        return schedulerTabContent;
    }

    private Component createSourcesTabContent() {
        return new SourcesTabContent(connectionService);
    }

    private Component createImportExportTabContent() {
        return new ImportExportTabContent();
    }

    private Component createSecurityTabContent() {
        return new SecurityTabContent(userService, userGroupService);
    }

    @Override
    public void setParameter(final BeforeEvent event, @OptionalParameter final String s) {

    }

}
