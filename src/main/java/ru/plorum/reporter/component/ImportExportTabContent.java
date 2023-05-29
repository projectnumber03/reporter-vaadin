package ru.plorum.reporter.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.*;
import ru.plorum.reporter.model.connection.Connection;
import ru.plorum.reporter.service.ConnectionService;
import ru.plorum.reporter.service.IUserService;
import ru.plorum.reporter.service.ReportGroupService;
import ru.plorum.reporter.service.UserGroupService;
import ru.plorum.reporter.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static ru.plorum.reporter.util.Constants.DATE_FORMATTER;

@Slf4j
@Getter
public class ImportExportTabContent extends VerticalLayout {

    private final QueryTabContent queryTabContent;

    private final ParameterTabContent parameterTabContent;

    private final SchedulerTabContent schedulerTabContent;

    private final SourceTabContent sourceTabContent;

    private final SecurityTabContent securityTabContent;

    private final ReportGroupService reportGroupService;

    private final IUserService userService;

    private final ConnectionService connectionService;

    private final UserGroupService userGroupService;

    private final Button exportButton = new Button("Экспортировать конфигурацию отчёта");

    private final Upload upload = new Upload();

    public ImportExportTabContent(
            final QueryTabContent queryTabContent,
            final ParameterTabContent parameterTabContent,
            final SchedulerTabContent schedulerTabContent,
            final SourceTabContent sourceTabContent,
            final SecurityTabContent securityTabContent,
            final ReportGroupService reportGroupService,
            final IUserService userService,
            final ConnectionService connectionService,
            final UserGroupService userGroupService
    ) {
        this.queryTabContent = queryTabContent;
        this.parameterTabContent = parameterTabContent;
        this.schedulerTabContent = schedulerTabContent;
        this.sourceTabContent = sourceTabContent;
        this.securityTabContent = securityTabContent;
        this.reportGroupService = reportGroupService;
        this.userService = userService;
        this.connectionService = connectionService;
        this.userGroupService = userGroupService;
        add(createImportField());
        add(createExportButton());
    }

    private Component createExportButton() {
        final var exportAnchor = new Anchor();
        exportAnchor.setWidth(315, Unit.PIXELS);
        exportAnchor.setHref(new StreamResource("report.conf", () -> {
            final var result = new HashMap<>() {{
                put("queryTabContent", getQueryTabContentData());
                put("parameterTabContent", getParameterTabContentData());
                put("schedulerTabContent", getSchedulerTabContentData());
                put("sourceTabContent", getSourceTabContentData());
                put("securityTabContent", getSecurityTabContentData());
            }};
            try {
                return new ByteArrayInputStream(new ObjectMapper().writeValueAsBytes(result));
            } catch (Exception e) {
                log.error("error exporting report conf", e);
                return InputStream.nullInputStream();
            }
        }));
        exportAnchor.getElement().setAttribute("download", true);
        exportAnchor.add(exportButton);

        return exportAnchor;
    }

    private Map<String, Object> getQueryTabContentData() {
        return new HashMap<>() {{
            put("name", queryTabContent.getName().getValue());
            put("description", queryTabContent.getDescription().getValue());
            put("reportGroup", Optional.ofNullable(queryTabContent.getReportGroup().getValue()).map(ReportGroup::getId).orElse(null));
            put("queries", queryTabContent.getItems());
        }};
    }

    private Map<String, Object> getParameterTabContentData() {
        return Map.of("parameters", parameterTabContent.getItems());
    }

    private Map<String, Object> getSchedulerTabContentData() {
        return new HashMap<>() {{
            put("enabled", schedulerTabContent.getEnabledCheckbox().getValue());
            put("hour", schedulerTabContent.getHourField().getValue());
            put("minute", schedulerTabContent.getMinuteField().getValue());
            put("sendTo", schedulerTabContent.getSendToField().getValue().stream().map(User::getEmail).toList());
            put("type", schedulerTabContent.getRadioGroup().getValue());
            put("days", schedulerTabContent.getDaySelectField().getSelectedItems());
            put("interval", schedulerTabContent.getIntervalSelectField().getValue());
        }};
    }

    private Map<String, Object> getSourceTabContentData() {
        return new HashMap<>() {{
            put("connection", Optional.ofNullable(sourceTabContent.getConnectionComboBox().getValue()).map(Connection::getId).orElse(null));
        }};
    }

    private Map<String, Object> getSecurityTabContentData() {
        return new HashMap<>() {{
            put("visibility", securityTabContent.getReportVisibilityRadioButtonGroup().getValue());
            put("groups", securityTabContent.getGroupSelect().getSelectedItems().stream().map(UserGroup::getId).toList());
            put("users", securityTabContent.getUserSelect().getSelectedItems().stream().map(User::getId).toList());
        }};
    }

    private Component createImportField() {
        final var buffer = new MemoryBuffer();
        upload.setReceiver(buffer);
        upload.addSucceededListener(event -> {
            try (final var inputStream = buffer.getInputStream()) {
                final Map<String, Object> map = new ObjectMapper().readValue(inputStream, new TypeReference<>() {
                });
                setQueryTabContent(map);
                setParameterTabContent(map);
                setSchedulerTabContent(map);
                setSourceTabContent(map);
                setSecurityTabContent(map);
                final var notification = Notification.show("Конфигурация загружена");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setPosition(Notification.Position.TOP_CENTER);
            } catch (Exception e) {
                log.error("error importing report conf", e);
                final var notification = Notification.show("Ошибка импорта конфигурации отчёта");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setPosition(Notification.Position.TOP_CENTER);
            } finally {
                upload.getElement().executeJs("this.files=[]");
            }
        });
        upload.setI18n(getLocalization());
        upload.setAcceptedFileTypes("application/conf", ".conf");

        return upload;
    }

    private UploadI18N getLocalization() {
        final var i18n = new UploadI18N();
        i18n
                .setDropFiles(
                        new UploadI18N.DropFiles()
                                .setOne("Перетащите файл сюда...")
                                .setMany("Перетащите файлы сюда...")

                )
                .setAddFiles(
                        new UploadI18N.AddFiles()
                                .setOne("Импортировать конфигурацию отчёта")
                                .setMany("Добавить файлы")
                )
                .setError(
                        new UploadI18N.Error()
                                .setFileIsTooBig("Слишком большой файл.")
                                .setTooManyFiles("Слишком много файлов.")
                                .setIncorrectFileType("Некорректный тип файла.")
                )
                .setUploading(
                        new UploadI18N.Uploading()
                                .setStatus(
                                        new UploadI18N.Uploading.Status()
                                                .setConnecting("Соединение...")
                                                .setStalled("Загрузка застопорилась.")
                                                .setProcessing("Обработка файла...")
                                )
                                .setRemainingTime(
                                        new UploadI18N.Uploading.RemainingTime()
                                                .setPrefix("оставшееся время: ")
                                                .setUnknown("оставшееся время неизвестно")
                                )
                                .setError(
                                        new UploadI18N.Uploading.Error()
                                                .setServerUnavailable("Сервер недоступен")
                                                .setUnexpectedServerError("Неожиданная ошибка сервера")
                                                .setForbidden("Загрузка запрещена")
                                )
                )
                .setUnits(new UploadI18N.Units().setSize(Arrays.asList("Б", "Кбайт", "Мбайт", "Гбайт", "Тбайт", "Пбайт", "Эбайт", "Збайт", "Ибайт")));

        return i18n;
    }

    private void setQueryTabContent(final Map<String, Object> map) {
        final var queryTabContent = (LinkedHashMap<String, Object>) map.get("queryTabContent");
        if (queryTabContent.containsKey("name")) {
            this.queryTabContent.getName().setValue((String) queryTabContent.get("name"));
        }
        if (queryTabContent.containsKey("description")) {
            this.queryTabContent.getDescription().setValue((String) queryTabContent.get("description"));
        }
        if (queryTabContent.containsKey("reportGroup")) {
            final var reportGroupId = (String) Objects.toString(queryTabContent.get("reportGroup"), null);
            final var reportGroup = reportGroupService.findById(Optional.ofNullable(reportGroupId).map(UUID::fromString).orElse(null));
            Optional.ofNullable(reportGroup).ifPresent(this.queryTabContent.getReportGroup()::setValue);
        }
        if (queryTabContent.containsKey("queries")) {
            final var queries = (ArrayList<LinkedHashMap<String, Object>>) queryTabContent.get("queries");
            final var queryList = queries.stream().map(q -> {
                final var query = new Query();
                Optional.ofNullable(q.getOrDefault("id", null)).map(Objects::toString).map(UUID::fromString).ifPresent(query::setId);
                Optional.ofNullable(q.getOrDefault("report", null)).map(Objects::toString).map(Boolean::parseBoolean).ifPresent(report -> {
                    query.getGenerateReportCheckbox().setValue(report);
                    query.getGenerateReportCheckbox().setReadOnly(false);
                    query.setReport(report);
                });
                Optional.ofNullable(q.getOrDefault("sqlText", null)).map(Objects::toString).ifPresent(sqlText -> {
                    query.setSqlText(sqlText);
                    query.getSqlTextField().setValue(sqlText);
                });
                Optional.ofNullable(q.getOrDefault("subReport", null)).map(Objects::toString).ifPresent(subReport -> {
                    query.getSubReportField().setValue(subReport);
                    query.getSubReportField().setReadOnly(false);
                    query.setSubReport(subReport);
                });

                return query;
            }).toList();
            this.queryTabContent.getItems().clear();
            this.queryTabContent.getItems().addAll(queryList);
            this.queryTabContent.setItems();
        }
    }

    private void setParameterTabContent(final Map<String, Object> map) {
        final var parameterTabContent = (LinkedHashMap<String, Object>) map.get("parameterTabContent");
        if (!parameterTabContent.containsKey("parameters")) return;
        final var parameters = (ArrayList<LinkedHashMap<String, Object>>) parameterTabContent.get("parameters");
        final var parameterList = parameters.stream().map(p -> {
            final var parameter = new Parameter();
            Optional.ofNullable(p.getOrDefault("id", null)).map(Objects::toString).map(UUID::fromString).ifPresent(parameter::setId);
            Optional.ofNullable(p.getOrDefault("name", null)).map(Objects::toString).ifPresent(parameter::setName);
            Optional.ofNullable(p.getOrDefault("description", null)).map(Objects::toString).ifPresent(description -> {
                parameter.getDescriptionField().setValue(description);
                parameter.setDescription(description);
            });
            Optional.ofNullable(p.getOrDefault("type", null)).map(Objects::toString).map(Parameter.Type::valueOf).ifPresent(value -> {
                parameter.setType(value);
                parameter.getTypeComboBox().setValue(value);
            });
            Optional.ofNullable(p.getOrDefault("defaultValue", null)).map(Objects::toString).ifPresent(v -> {
                switch (parameter.getTypeComboBox().getValue()) {
                    case DATE -> parameter.getDateDefaultValue().setValue(LocalDate.parse(v, DATE_FORMATTER));
                    case INTEGER -> parameter.getIntegerDefaultValue().setValue(Double.valueOf(v));
                    case STRING -> parameter.getStringDefaultValue().setValue(v);
                }
                parameter.setDefaultValue();
            });

            return parameter;
        }).toList();
        this.parameterTabContent.getItems().clear();
        this.parameterTabContent.getItems().addAll(parameterList);
        this.parameterTabContent.getParameterGrid().setItems(parameterList);
    }

    private void setSchedulerTabContent(final Map<String, Object> map) {
        final var schedulerTabContent = (LinkedHashMap<String, Object>) map.get("schedulerTabContent");
        Optional.ofNullable(schedulerTabContent.getOrDefault("enabled", null))
                .map(Objects::toString)
                .map(Boolean::parseBoolean)
                .ifPresent(this.schedulerTabContent.getEnabledCheckbox()::setValue);
        Optional.ofNullable(schedulerTabContent.getOrDefault("hour", null))
                .map(Objects::toString)
                .map(Integer::parseInt)
                .ifPresent(this.schedulerTabContent.getHourField()::setValue);
        Optional.ofNullable(schedulerTabContent.getOrDefault("minute", null))
                .map(Objects::toString)
                .map(Integer::parseInt)
                .ifPresent(this.schedulerTabContent.getMinuteField()::setValue);
        Optional.ofNullable(schedulerTabContent.getOrDefault("sendTo", null))
                .filter(c -> c instanceof ArrayList<?>)
                .map(c -> (ArrayList<?>) c)
                .filter(not(CollectionUtils::isEmpty))
                .map(c -> c.stream().map(Objects::toString).toList())
                .map(userService::findActiveByEmails)
                .ifPresent(this.schedulerTabContent.getSendToField()::select);
        Optional.ofNullable(schedulerTabContent.getOrDefault("type", null))
                .map(Objects::toString)
                .map(SchedulerTask.Type::valueOf)
                .ifPresent(this.schedulerTabContent.getRadioGroup()::setValue);
        Optional.ofNullable(schedulerTabContent.getOrDefault("days", null))
                .filter(c -> c instanceof ArrayList<?>)
                .map(c -> (ArrayList<?>) c)
                .filter(not(CollectionUtils::isEmpty))
                .map(c -> c.stream().map(Objects::toString))
                .map(s -> s.map(Constants.Day::valueOf))
                .map(Stream::toList)
                .ifPresent(this.schedulerTabContent.getDaySelectField()::select);
        Optional.ofNullable(schedulerTabContent.getOrDefault("interval", null))
                .map(Objects::toString)
                .map(Integer::parseInt)
                .ifPresent(this.schedulerTabContent.getIntervalSelectField()::setValue);
    }

    private void setSourceTabContent(final Map<String, Object> map) {
        final var sourceTabContent = (LinkedHashMap<String, Object>) map.get("sourceTabContent");
        Optional.ofNullable(sourceTabContent.getOrDefault("connection", null))
                .map(Objects::toString)
                .map(UUID::fromString)
                .map(connectionService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .ifPresent(this.sourceTabContent.getConnectionComboBox()::setValue);
    }

    private void setSecurityTabContent(final Map<String, Object> map) {
        final var securityTabContent = (LinkedHashMap<String, Object>) map.get("securityTabContent");
        Optional.ofNullable(securityTabContent.getOrDefault("visibility", null))
                .map(Objects::toString)
                .map(Visibility::valueOf)
                .ifPresent(this.securityTabContent.getReportVisibilityRadioButtonGroup()::setValue);
        Optional.ofNullable(securityTabContent.getOrDefault("groups", null))
                .filter(c -> c instanceof ArrayList<?>)
                .map(c -> (ArrayList<?>) c)
                .filter(not(CollectionUtils::isEmpty))
                .map(c -> c.stream().map(Objects::toString).map(UUID::fromString).toList())
                .map(userGroupService::findAllById)
                .ifPresent(this.securityTabContent.getGroupSelect()::select);
        Optional.ofNullable(securityTabContent.getOrDefault("users", null))
                .filter(c -> c instanceof ArrayList<?>)
                .map(c -> (ArrayList<?>) c)
                .filter(not(CollectionUtils::isEmpty))
                .map(c -> c.stream().map(Objects::toString).map(UUID::fromString).toList())
                .map(userService::findAllById)
                .ifPresent(this.securityTabContent.getUserSelect()::select);
    }

}
