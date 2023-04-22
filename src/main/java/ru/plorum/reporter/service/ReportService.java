package ru.plorum.reporter.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.component.QueryTabContent;
import ru.plorum.reporter.component.SourcesTabContent;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.model.connection.Connection;
import ru.plorum.reporter.repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.*;

import static ru.plorum.reporter.util.Constants.REPORT_QUERIES;
import static ru.plorum.reporter.util.Constants.SOURCES;

@Service
@AllArgsConstructor
public class ReportService {

    private final ReportRepository repository;

    private final UserService userService;

    private final ConnectionService connectionService;

    public void save(final Report report) {
        if (Objects.isNull(report)) return;
        repository.save(report);
    }

    public void saveFromContent(final Map<String, Component> content) {
        final QueryTabContent queryTabContent = (QueryTabContent) content.get(REPORT_QUERIES);
        final SourcesTabContent sourcesTabContent = (SourcesTabContent) content.get(SOURCES);
        final Connection connection = sourcesTabContent.getConnectionComboBox().getValue();
        if (!connectionService.test(connection)) {
            final Notification notification = Notification.show(String.format("Ошибка подключения к \"%s\"", connection.getDescription()));
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setPosition(Notification.Position.TOP_CENTER);
        }
        final Report report = new Report(UUID.randomUUID());
        final User authenticatedUser = userService.getAuthenticatedUser();
        report.setId(Optional.ofNullable(report.getId()).orElse(UUID.randomUUID()));
        report.setAuthor(authenticatedUser);
        report.setName(queryTabContent.getName().getValue());
        report.setDescription(queryTabContent.getDescription().getValue());
        report.setLastEditor(authenticatedUser);
        report.setDateReport(LocalDateTime.now());
        report.setConnection(connection);
        report.getQueries().addAll(queryTabContent.getItems().stream().peek(q -> q.setSqlText(q.getSqlTextField().getValue())).toList());
        report.setStatus("NEW");
        repository.save(report);
    }

    @Nullable
    public Report findById(final UUID id) {
        if (Objects.isNull(id)) return null;
        return repository.findById(id).orElse(null);
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

    }

    public void clone(final Report report) {
        Report clone = report.clone();
    }

}
