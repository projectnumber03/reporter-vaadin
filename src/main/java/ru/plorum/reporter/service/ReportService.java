package ru.plorum.reporter.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.component.ParameterTabContent;
import ru.plorum.reporter.component.QueryTabContent;
import ru.plorum.reporter.component.SecurityTabContent;
import ru.plorum.reporter.component.SourceTabContent;
import ru.plorum.reporter.model.*;
import ru.plorum.reporter.repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static ru.plorum.reporter.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository repository;

    private final IUserService userService;

    private final ConnectionService connectionService;

    private final DataSourceService dataSourceService;

    private final ReportOutputService reportOutputService;

    private final ReportGroupService reportGroupService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Setter
    private UI ui;

    public void save(final Report report) {
        if (Objects.isNull(report)) return;
        repository.save(report);
    }

    public Report saveFromContent(final Report report, final Map<String, Component> content) {
        final var queryTabContent = (QueryTabContent) content.get(REPORT_QUERIES);
        final var parameterTabContent = (ParameterTabContent) content.get(REPORT_PARAMETERS);
        final var sourcesTabContent = (SourceTabContent) content.get(SOURCES);
        final var securityTabContent = (SecurityTabContent) content.get(SECURITY);
        final var connection = sourcesTabContent.getConnectionComboBox().getValue();
        if (!connectionService.test(connection)) {
            final var notification = Notification.show(String.format("Ошибка подключения к \"%s\"", connection.getDescription()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setPosition(Notification.Position.TOP_CENTER);
        }
        final var authenticatedUser = userService.getAuthenticatedUser();
        report.setId(Optional.ofNullable(report.getId()).orElse(UUID.randomUUID()));
        report.setAuthor(authenticatedUser);
        report.setName(queryTabContent.getName().getValue());
        report.setDescription(queryTabContent.getDescription().getValue());
        report.setGroup(queryTabContent.getReportGroup().getValue());
        report.setLastEditor(authenticatedUser);
        report.setCreatedAt(LocalDateTime.now());
        report.setConnection(connection);
        report.getQueries().clear();
        final Consumer<Query> queryAction = q -> {
            q.setId(UUID.randomUUID());
            q.setSqlText(q.getSqlTextField().getValue());
            q.setReport(q.getGenerateReportCheckbox().getValue());
            q.setSubReport(q.getSubReportField().getValue());
        };
        report.getQueries().addAll(queryTabContent.getItems().stream().peek(queryAction).toList());
        final Consumer<Parameter> parameterAction = p -> {
            p.setDescription(p.getDescriptionField().getValue());
            p.setType(p.getTypeComboBox().getValue());
            p.setDefaultValue();
        };
        report.getParameters().clear();
        report.getParameters().addAll(parameterTabContent.getItems().stream().peek(parameterAction).toList());
        switch (securityTabContent.getReportVisibilityRadioButtonGroup().getValue()) {
            case ME -> report.getPermittedUsers().add(authenticatedUser);
            case MY_GROUP -> report.getPermittedUsers().addAll(userService.findByGroup(authenticatedUser.getGroup()));
            case ALL -> report.getPermittedUsers().addAll(userService.findAll());
            case GROUPS -> report.getPermittedUsers().addAll(securityTabContent.getGroupSelect().getSelectedItems().stream().map(userService::findByGroup).flatMap(Collection::stream).toList());
            case USERS -> report.getPermittedUsers().addAll(securityTabContent.getUserSelect().getSelectedItems());
        }
        return repository.saveAndFlush(report);
    }

    @Nullable
    public Report findById(final UUID id) {
        if (Objects.isNull(id)) return null;
        return repository.findByIdWithPermittedUsers(id).orElse(null);
    }

    public List<Report> findMy() {
        final var user = userService.getAuthenticatedUser();
        if (Objects.isNull(user)) return Collections.emptyList();
        return repository.findAllByAuthorOrLastEditorOrPermittedUsers_Id(user, user, user.getId());
    }

    public List<Report> findAll() {
        return repository.findAll();
    }

    public void delete(final Report report) {
        if (Objects.isNull(report)) return;
        repository.delete(report);
    }

    public List<Report> findAllBySchedulerTaskIsNotNull() {
        return repository.findAllBySchedulerTaskIsNotNull();
    }

    public void generateInThread(final Report report, final Map<String, Object> parameters, final boolean showNotification) {
        final var user = userService.getAuthenticatedUser();
        executorService.execute(() -> generate(report, parameters, user, showNotification));
    }

    public void generate(final Report report, final Map<String, Object> parameters, final boolean showNotification) {
        generate(report, parameters, null, showNotification);
    }

    public void generate(final Report report, final Map<String, Object> parameters, final User user, final boolean showNotification) {
        try {
            final var connection = report.getConnection();
            if (Objects.isNull(connection)) return;
            final var reportOutput = new ReportOutput(UUID.randomUUID());
            reportOutput.setReport(report);
            final Supplier<User> userSupplier = () -> {
                if (Objects.isNull(user)) return userService.getAuthenticatedUser();
                return user;
            };
            Optional.ofNullable(userSupplier.get()).ifPresent(reportOutput::setUser);
            reportOutput.setCreatedAt(LocalDateTime.now());
            final var template = new NamedParameterJdbcTemplate(dataSourceService.createDataSource(connection));
            report.getQueries().forEach(q -> {
                if (!q.isReport()) {
                    template.update(q.getSqlText(), parameters);
                    return;
                }
                final List<Map<String, Object>> rawData = template.queryForList(q.getSqlText(), parameters);
                final Function<Map<String, Object>, Stream<ReportOutputData>> mapper = row -> row.entrySet().stream()
                        .map(e -> {
                            final ReportOutputData outputData = new ReportOutputData();
                            outputData.setId(UUID.randomUUID());
                            outputData.setQuery(q);
                            outputData.setRowNumber(rawData.indexOf(row));
                            outputData.setKey(e.getKey());
                            outputData.setValue(Objects.toString(e.getValue(), Strings.EMPTY));
                            return outputData;
                        });
                reportOutput.getData().addAll(rawData.stream().flatMap(mapper).toList());
            });
            reportOutputService.save(reportOutput);
            Optional.ofNullable(report.getGroup()).ifPresent(rg -> {
                rg.setLastReportCreationDate(reportOutput.getCreatedAt());
                reportGroupService.save(rg);
            });
            if (!showNotification) return;
            ui.access(() -> {
                final var notification = Notification.show(SUCCESS);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setPosition(Notification.Position.TOP_CENTER);
            });
        } catch (Exception e) {
            log.error("error generating report", e);
            if (!showNotification) return;
            ui.access(() -> {
                final var notification = Notification.show("Ошибка формирования отчёта");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setPosition(Notification.Position.TOP_CENTER);
            });
        }
    }

    public void clone(final Report report) {
        final var clone = report.clone();
        save(clone);
    }

}
