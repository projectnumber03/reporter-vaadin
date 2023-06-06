package ru.plorum.reporter.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LicenseService {

    public static final int delay = 30;

    @Value("${amount.users}")
    private Integer userAmount;

    @Value("${amount.roles}")
    private Integer roleAmount;

    @Value("${amount.sources}")
    private Integer sourceAmount;

    @Value("${amount.reports}")
    private Integer reportAmount;

    @Value("${amount.generations}")
    private Integer generationAmount;

    @NonNull
    private final IUserService userService;

    @NonNull
    private final RoleService roleService;

    @NonNull
    private final ConnectionService connectionService;

    @NonNull
    private final ReportService reportService;

    @NonNull
    private final ReportOutputService reportOutputService;

    @Setter
    private UI ui;

    private boolean validState = true;

    @Scheduled(fixedDelay = delay, timeUnit = TimeUnit.SECONDS)
    public void schedule() {
        if (Objects.isNull(ui)) return;
        if (!validState) {
            System.exit(0);
        }
        if (!check()) {
            validState = check();
            ui.access(() -> {
                final var notification = Notification.show(String.format("Нарушение условий лицензии! Система будет остановлена через %d секунд", delay));
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setPosition(Notification.Position.TOP_CENTER);
            });
        }
    }

    public boolean check() {
        return Stream.of(
                userService.countAll() > userAmount,
                roleService.countAll() > roleAmount,
                connectionService.countAll() > sourceAmount,
                reportService.countAll() > reportAmount,
                reportOutputService.countAll() > generationAmount
        ).noneMatch(Boolean::booleanValue);
    }

}
