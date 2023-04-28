package ru.plorum.reporter.view;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.component.*;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.service.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(REPORT)
@Route(value = "reports/upsert", layout = MainView.class)
public class ReportUpsertView extends AbstractView implements HasUrlParameter<String> {

    private final UserService userService;

    private final ConnectionService connectionService;

    private final UserGroupService userGroupService;

    private final ReportService reportService;

    private final ReportGroupService reportGroupService;

    private final TabSheet tabSheet = new TabSheet();

    private final Button saveButton = new Button(SAVE);

    private final Map<String, Component> content = new HashMap<>();

    private final AtomicReference<Report> currentReport = new AtomicReference<>(new Report());

    private final DatePicker.DatePickerI18n i18n;

    public ReportUpsertView(
            final UserService userService,
            final ConnectionService connectionService,
            final UserGroupService userGroupService,
            final ReportService reportService,
            final ReportGroupService reportGroupService,
            final DatePicker.DatePickerI18n i18n
    ) {
        this.userService = userService;
        this.connectionService = connectionService;
        this.userGroupService = userGroupService;
        this.reportService = reportService;
        this.reportGroupService = reportGroupService;
        this.i18n = i18n;
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
        final var queriesTabContent = createQueriesTabContent();
        content.put(REPORT_QUERIES, queriesTabContent);
        final var parametersTabContent = createParametersTabContent((QueryTabContent) queriesTabContent);
        content.put(REPORT_PARAMETERS, parametersTabContent);
        final var schedulerTabContent = createSchedulerTabContent();
        content.put(SCHEDULER, schedulerTabContent);
        final var sourcesTabContent = createSourcesTabContent();
        content.put(SOURCES, sourcesTabContent);
        final var importExportTabContent = createImportExportTabContent();
        content.put(IMPORT_EXPORT, importExportTabContent);
        final var securityTabContent = createSecurityTabContent();
        content.put(SECURITY, securityTabContent);
        tabSheet.add(REPORT_QUERIES, queriesTabContent);
        tabSheet.add(REPORT_PARAMETERS, parametersTabContent);
        tabSheet.add(SCHEDULER, schedulerTabContent);
        tabSheet.add(SOURCES, sourcesTabContent);
        tabSheet.add(IMPORT_EXPORT, importExportTabContent);
        tabSheet.add(SECURITY, securityTabContent);
        return tabSheet;
    }

    private Component createSaveButton() {
        saveButton.addClickListener(e -> {
            reportService.saveFromContent(currentReport.get(), content);
            saveButton.getUI().ifPresent(ui -> ui.navigate("my_reports"));
        });
        return saveButton;
    }

    private Component createQueriesTabContent() {
        return new QueryTabContent(reportGroupService);
    }

    private Component createParametersTabContent(final QueryTabContent queriesTabContent) {
        return new ReportTabContent(queriesTabContent, i18n);
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
    public void setParameter(final BeforeEvent beforeEvent, @OptionalParameter final String s) {
        final var location = beforeEvent.getLocation();
        final var queryParameters = location.getQueryParameters();
        final var parametersMap = queryParameters.getParameters();
        final var id = parametersMap.getOrDefault("id", Collections.emptyList());
        if (CollectionUtils.isEmpty(id)) return;
        final var report = reportService.findById(UUID.fromString(id.iterator().next()));
        if (Objects.isNull(report)) return;
        currentReport.set(report);
        final var queryTabContent = (QueryTabContent) content.get(REPORT_QUERIES);
        queryTabContent.getName().setValue(report.getName());
        queryTabContent.getDescription().setId(report.getDescription());
        Optional.ofNullable(report.getGroup()).ifPresent(queryTabContent.getReportGroup()::setValue);
        final var queries = report.getQueriesWithTransients();
        queryTabContent.getItems().clear();
        queryTabContent.getItems().addAll(queries);
        queryTabContent.getQueryGrid().setItems(queries);
        final var sourcesTabContent = (SourcesTabContent) content.get(SOURCES);
        sourcesTabContent.getConnectionComboBox().setValue(report.getConnection());
        final var securityTabContent = (SecurityTabContent) content.get(SECURITY);
        securityTabContent.getReportVisibilityRadioButtonGroup().setValue(report.getVisibility());
        switch (securityTabContent.getReportVisibilityRadioButtonGroup().getValue()) {
            case GROUPS ->
                    securityTabContent.getGroupSelect().setValue(report.getPermittedUsers().stream().map(User::getGroup).distinct().collect(Collectors.toList()));
            case USERS ->
                    securityTabContent.getUserSelect().setValue(report.getPermittedUsers().stream().distinct().collect(Collectors.toList()));
        }
    }

}
