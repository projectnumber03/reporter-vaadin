package ru.plorum.reporter.service;

import com.vaadin.flow.component.Component;
import jakarta.annotation.PostConstruct;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.component.SchedulerTabContent;
import ru.plorum.reporter.model.*;
import ru.plorum.reporter.repository.SchedulerTaskRepository;
import ru.plorum.reporter.util.Constants;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.plorum.reporter.util.Constants.SCHEDULER;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ReportSchedulerService {

    @NonNull
    ReportService reportService;

    @NonNull
    XlsxService xlsxService;

    @NonNull
    ReportOutputService reportOutputService;

    @NonNull
    MailService mailSender;

    @NonNull
    SchedulerTaskRepository schedulerTaskRepository;

    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    Map<UUID, ScheduledFuture<?>> taskCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(10);
        scheduler.initialize();
        final var allReportsWithSchedulerTaskIsNotNull = reportService.findAllBySchedulerTaskIsNotNull();
        allReportsWithSchedulerTaskIsNotNull.forEach(r -> taskCache.put(r.getId(), scheduler.schedule(getTask(r, r.getSchedulerTask().getUserEmails()), new CronTrigger(r.getSchedulerTask().getCronExpression()))));
        log.info("there are {} tasks in cache", taskCache.size());
    }

    public void saveFromContent(final Report report, final Map<String, Component> content) {
        cancel(report.getId());
        final var schedulerTabContent = (SchedulerTabContent) content.get(SCHEDULER);
        if (!schedulerTabContent.getEnabledCheckbox().getValue()) return;
        final var beginAt = LocalTime.of(schedulerTabContent.getHourField().getValue(), schedulerTabContent.getMinuteField().getValue());
        final var days = schedulerTabContent.getDaySelectField().getSelectedItems();
        final var interval = schedulerTabContent.getIntervalSelectField().getValue();
        final var users = schedulerTabContent.getSendToField().getSelectedItems();
        final var task = submit(report, beginAt, days, interval, users);
        report.setSchedulerTask(task);
        reportService.save(report);
    }

    private SchedulerTask submit(final Report report, final LocalTime beginAt, final Collection<Constants.Day> days, final Integer interval, final Collection<User> users) {
        final var cronExpression = new SchedulerCronExpression(beginAt, days, interval).toString();
        final var userEmails = users.stream().map(User::getEmail).collect(Collectors.joining(","));
        final var task = SchedulerTask.builder().id(UUID.randomUUID()).cronExpression(cronExpression).userEmails(userEmails).build();
        taskCache.put(report.getId(), scheduler.schedule(getTask(report, userEmails), new CronTrigger(cronExpression)));
        return schedulerTaskRepository.saveAndFlush(task);
    }

    private void cancel(final UUID reportId) {
        if (!taskCache.containsKey(reportId)) return;
        taskCache.remove(reportId).cancel(true);
        final var report = reportService.findById(reportId);
        Stream.of(report).filter(Objects::nonNull).map(Report::getSchedulerTask).forEach(st -> {
            report.setSchedulerTask(null);
            reportService.save(report);
            schedulerTaskRepository.delete(st);
        });
    }

    private Runnable getTask(final Report report, final String userEmails) {
        return () -> {
            reportService.generate(report, Collections.emptyMap(), false);
            final var reportOutput = reportOutputService.findFirstByReport(report);
            if (reportOutput.isEmpty()) return;
            final var reportOutputData = reportOutput.get().getData().stream().collect(Collectors.groupingBy(ReportOutputData::getQuery));
            try (final InputStream inputStream = xlsxService.generateReportXLSX(reportOutputData)) {
                log.info("sending report {} to {}", report.getName(), userEmails);
                mailSender.sendMail(userEmails, report.getDescription(), report.getName(), new Attachment(String.format("%s_%s.xlsx", report.getName(), UUID.randomUUID()), new ByteArrayDataSource(inputStream, "application/xlsx")));
            } catch (Exception e) {
                log.error("error sending report {} to {}", report.getName(), userEmails, e);
            }
        };
    }

}
