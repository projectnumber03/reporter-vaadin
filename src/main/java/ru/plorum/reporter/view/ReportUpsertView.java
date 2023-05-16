package ru.plorum.reporter.view;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.component.*;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.SchedulerCronExpression;
import ru.plorum.reporter.model.SchedulerTask;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.service.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static ru.plorum.reporter.util.Constants.*;

@Slf4j
@PageTitle(REPORT)
@Route(value = "report/upsert", layout = MainView.class)
public class ReportUpsertView extends AbstractView implements HasUrlParameter<String> {

    private final UserService userService;

    private final ConnectionService connectionService;

    private final UserGroupService userGroupService;

    private final ReportService reportService;

    private final ReportGroupService reportGroupService;

    private final ReportSchedulerService reportSchedulerService;

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
            final ReportSchedulerService reportSchedulerService,
            final DatePicker.DatePickerI18n i18n
    ) {
        this.userService = userService;
        this.connectionService = connectionService;
        this.userGroupService = userGroupService;
        this.reportService = reportService;
        this.reportGroupService = reportGroupService;
        this.reportSchedulerService = reportSchedulerService;
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
        final var schedulerTabContent = createSchedulerTabContent((QueryTabContent) queriesTabContent);
        content.put(SCHEDULER, schedulerTabContent);
        final var sourcesTabContent = createSourcesTabContent();
        content.put(SOURCES, sourcesTabContent);
        final var securityTabContent = createSecurityTabContent();
        content.put(SECURITY, securityTabContent);
        final var importExportTabContent = createImportExportTabContent(
                (QueryTabContent) queriesTabContent,
                (ParameterTabContent) parametersTabContent,
                (SchedulerTabContent) schedulerTabContent,
                (SourceTabContent) sourcesTabContent,
                (SecurityTabContent) securityTabContent,
                reportGroupService,
                userService,
                connectionService,
                userGroupService
        );
        content.put(IMPORT_EXPORT, importExportTabContent);
        tabSheet.add(REPORT_QUERIES, queriesTabContent);
        tabSheet.add(REPORT_PARAMETERS, parametersTabContent);
        tabSheet.add(SCHEDULER, schedulerTabContent);
        tabSheet.add(SOURCES, sourcesTabContent);
        tabSheet.add(IMPORT_EXPORT, importExportTabContent);
        tabSheet.add(SECURITY, securityTabContent);
        return tabSheet;
    }

    private Component createSaveButton() {
        saveButton.addClickListener(event -> {
            try {
                final var report = reportService.saveFromContent(currentReport.get(), content);
                reportSchedulerService.saveFromContent(report, content);
                saveButton.getUI().ifPresent(ui -> ui.navigate("my_reports"));
            } catch (Exception e) {
                log.error("error saving report", e);
                final var notification = Notification.show("Ошибка сохранения отчёта: " + e);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setPosition(Notification.Position.TOP_CENTER);
            }
        });
        return saveButton;
    }

    private Component createQueriesTabContent() {
        return new QueryTabContent(reportGroupService);
    }

    private Component createParametersTabContent(final QueryTabContent queriesTabContent) {
        return new ParameterTabContent(queriesTabContent, i18n);
    }

    private Component createSchedulerTabContent(final QueryTabContent queriesTabContent) {
        var schedulerTabContent = new SchedulerTabContent(queriesTabContent);
        schedulerTabContent.getSendToField().setItems(userService.findAll());
        return schedulerTabContent;
    }

    private Component createSourcesTabContent() {
        return new SourceTabContent(connectionService);
    }

    private Component createImportExportTabContent(
            final QueryTabContent queriesTabContent,
            final ParameterTabContent parameterTabContent,
            final SchedulerTabContent schedulerTabContent,
            final SourceTabContent sourceTabContent,
            final SecurityTabContent securityTabContent,
            final ReportGroupService reportGroupService,
            final UserService userService,
            final ConnectionService connectionService,
            final UserGroupService userGroupService
    ) {
        return new ImportExportTabContent(
                queriesTabContent,
                parameterTabContent,
                schedulerTabContent,
                sourceTabContent,
                securityTabContent,
                reportGroupService,
                userService,
                connectionService,
                userGroupService
        );
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
        final var parameterTabContent = (ParameterTabContent) content.get(REPORT_PARAMETERS);
        final var parameters = report.getParametersWithTransients();
        parameterTabContent.getItems().clear();
        parameterTabContent.getItems().addAll(parameters);
        parameterTabContent.getParameterGrid().setItems(parameters);
        final var sourcesTabContent = (SourceTabContent) content.get(SOURCES);
        sourcesTabContent.getConnectionComboBox().setValue(report.getConnection());
        final var securityTabContent = (SecurityTabContent) content.get(SECURITY);
        securityTabContent.getReportVisibilityRadioButtonGroup().setValue(report.getVisibility());
        switch (securityTabContent.getReportVisibilityRadioButtonGroup().getValue()) {
            case GROUPS ->
                    securityTabContent.getGroupSelect().setValue(report.getPermittedUsers().stream().map(User::getGroup).distinct().toList());
            case USERS ->
                    securityTabContent.getUserSelect().setValue(report.getPermittedUsers().stream().distinct().toList());
        }
        final var schedulerTabContent = (SchedulerTabContent) content.get(SCHEDULER);
        final var task = report.getSchedulerTask();
        if (Objects.isNull(task)) {
            schedulerTabContent.getEnabledCheckbox().setValue(false);
            return;
        }
        final var cronExpression = SchedulerCronExpression.parse(task.getCronExpression());
        if (cronExpression.isEmpty()) {
            schedulerTabContent.getEnabledCheckbox().setValue(false);
            return;
        }
        schedulerTabContent.getEnabledCheckbox().setValue(true);
        final var beginAt = cronExpression.get().beginAt();
        schedulerTabContent.getHourField().setValue(beginAt.getHour());
        schedulerTabContent.getMinuteField().setValue(beginAt.getMinute());
        final var days = cronExpression.get().days();
        schedulerTabContent.getDaySelectField().setItems(days);
        final var interval = cronExpression.get().interval();
        schedulerTabContent.getIntervalSelectField().setValue(interval);
        final var emails = Arrays.stream(task.getUserEmails().split(",")).toList();
        schedulerTabContent.getSendToField().select(userService.findActiveByEmails(emails));
        if (CollectionUtils.isEmpty(days) && Objects.nonNull(interval)) {
            schedulerTabContent.getRadioGroup().setValue(SchedulerTask.Type.INTERVAL);
            schedulerTabContent.getIntervalSelectField().setReadOnly(false);
            schedulerTabContent.getDaySelectField().deselectAll();
            schedulerTabContent.getDaySelectField().setReadOnly(true);
            return;
        }
        if (!CollectionUtils.isEmpty(days) && Objects.isNull(interval)) {
            schedulerTabContent.getRadioGroup().setValue(SchedulerTask.Type.DAY);
            schedulerTabContent.getDaySelectField().setReadOnly(false);
            schedulerTabContent.getIntervalSelectField().clear();
            schedulerTabContent.getIntervalSelectField().setReadOnly(true);
        }
    }

}
