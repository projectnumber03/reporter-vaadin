package ru.plorum.reporter.view;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import ru.plorum.reporter.component.QueryTabContent;
import ru.plorum.reporter.component.SchedulerTabContent;
import ru.plorum.reporter.component.SecurityTabContent;
import ru.plorum.reporter.component.SourcesTabContent;
import ru.plorum.reporter.service.ConnectionService;
import ru.plorum.reporter.service.UserGroupService;
import ru.plorum.reporter.service.UserService;

import static ru.plorum.reporter.util.Constants.REPORT;
import static ru.plorum.reporter.util.Constants.SAVE;

@PageTitle(REPORT)
@Route(value = "reports/upsert", layout = MainView.class)
public class ReportUpsertView extends AbstractView {

    private final UserService userService;

    private final ConnectionService connectionService;

    private final UserGroupService userGroupService;

    private final TabSheet tabSheet = new TabSheet();

    private final Button saveButton = new Button(SAVE);

    public ReportUpsertView(final UserService userService, final ConnectionService connectionService, final UserGroupService userGroupService) {
        this.userService = userService;
        this.connectionService = connectionService;
        this.userGroupService = userGroupService;
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
        tabSheet.add("Запросы отчёта", createQueriesTabContent());
        tabSheet.add("Планировщик", createSchedulerTabContent());
        tabSheet.add("Источники", createSourcesTabContent());
        tabSheet.add("Импорт/экспорт", createImportExportTabContent());
        tabSheet.add("Безопасность", createSecurityTabContent());
        return tabSheet;
    }

    private Component createSaveButton() {
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
        return new QueryTabContent(); //todo
    }

    private Component createSecurityTabContent() {
        return new SecurityTabContent(userService, userGroupService);
    }

}
