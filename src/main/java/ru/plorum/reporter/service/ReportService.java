package ru.plorum.reporter.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.component.QueryTabContent;
import ru.plorum.reporter.component.SecurityTabContent;
import ru.plorum.reporter.component.SourcesTabContent;
import ru.plorum.reporter.model.*;
import ru.plorum.reporter.model.connection.Connection;
import ru.plorum.reporter.repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static ru.plorum.reporter.util.Constants.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository repository;

    private final UserService userService;

    private final ConnectionService connectionService;

    private final DataSourceService dataSourceService;

    private final ReportOutputService reportOutputService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Setter
    private UI ui;

    public void save(final Report report) {
        if (Objects.isNull(report)) return;
        repository.save(report);
    }

    public void saveFromContent(final Report report, final Map<String, Component> content) {
        final var queryTabContent = (QueryTabContent) content.get(REPORT_QUERIES);
        final var sourcesTabContent = (SourcesTabContent) content.get(SOURCES);
        final var securityTabContent = (SecurityTabContent) content.get(SECURITY);
        final var connection = sourcesTabContent.getConnectionComboBox().getValue();
        if (!connectionService.test(connection)) {
            final Notification notification = Notification.show(String.format("Ошибка подключения к \"%s\"", connection.getDescription()));
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
        report.setDateReport(LocalDateTime.now());
        report.setConnection(connection);
        report.getQueries().clear();
        final Consumer<Query> action = q -> {
            q.setSqlText(q.getSqlTextField().getValue());
            q.setReport(q.getGenerateReportCheckbox().getValue());
            q.setSubReport(q.getSubReportField().getValue());
        };
        report.getQueries().addAll(queryTabContent.getItems().stream().peek(action).toList());
        report.setStatus("NEW");
        switch (securityTabContent.getReportVisibilityRadioButtonGroup().getValue()) {
            case ME -> report.getPermittedUsers().add(authenticatedUser);
            case MY_GROUP -> report.getPermittedUsers().addAll(userService.findByGroup(authenticatedUser.getGroup()));
            case ALL -> report.getPermittedUsers().addAll(userService.findAll());
            case GROUPS ->
                    report.getPermittedUsers().addAll(securityTabContent.getGroupSelect().getSelectedItems().stream().map(userService::findByGroup).flatMap(Collection::stream).toList());
            case USERS -> report.getPermittedUsers().addAll(securityTabContent.getUserSelect().getSelectedItems());
        }
        repository.save(report);
    }

    @Nullable
    public Report findById(final UUID id) {
        if (Objects.isNull(id)) return null;
        return repository.findByIdWithPermittedUsers(id).orElse(null);
    }

    public List<Report> findMy() {
        final User user = userService.getAuthenticatedUser();
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

    public void generate(final Report report) {
        executorService.execute(() -> {
            try {
                final Connection connection = report.getConnection();
                if (Objects.isNull(connection)) return;
                final ReportOutput reportOutput = new ReportOutput(UUID.randomUUID());
                reportOutput.setReport(report);
                reportOutput.setUser(userService.getAuthenticatedUser());
                reportOutput.setCreatedAt(LocalDateTime.now());
                final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceService.createDataSource(connection));
                report.getQueries().forEach(q -> {
                    if (!q.isReport()) {
                        jdbcTemplate.execute(q.getSqlText());
                        return;
                    }
                    final List<Map<String, Object>> rawData = jdbcTemplate.queryForList(q.getSqlText());
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
                ui.access(() -> {
                    final Notification notification = Notification.show(SUCCESS);
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notification.setPosition(Notification.Position.TOP_CENTER);
                });
            } catch (Exception e) {
                ui.access(() -> {
                    final Notification notification = Notification.show("Ошибка формирования отчёта");
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setPosition(Notification.Position.TOP_CENTER);
                });
            }
        });
    }

    public void clone(final Report report) {
        final var clone = report.clone();
        save(clone);
    }

}
