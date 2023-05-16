package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.jasypt.util.text.AES256TextEncryptor;
import ru.plorum.reporter.VaadinApplication;
import ru.plorum.reporter.model.Inn;
import ru.plorum.reporter.repository.InnRepository;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(SETTINGS)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "settings", layout = MainView.class)
public class SettingsView extends AbstractView {

    private final String systemId;

    private final InnRepository innRepository;

    private final AES256TextEncryptor encryptor;

    private final TextField systemIdField = new TextField("Уникальный идентификатор системы");

    private final TextField inn = new TextField("ИНН");

    private final Button saveButton = new Button(SAVE);

    private final Button rebootButton = new Button("Перезагрузить приложение");

    private final Button shutdownButton = new Button("Остановить приложение");

    public SettingsView(
            final String systemId,
            final InnRepository innRepository,
            final AES256TextEncryptor encryptor
    ) {
        this.systemId = systemId;
        this.innRepository = innRepository;
        this.encryptor = encryptor;
    }

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        innRepository.findAll().stream().findFirst().map(Inn::getInn).ifPresent(inn::setValue);
        vertical.add(createSystemIdField());
        final var layout = new HorizontalLayout(inn, createSaveButton());
        layout.setAlignItems(Alignment.END);
        vertical.add(layout);
        vertical.add(createGenerateUniqueKeyButton());
        vertical.add(createRebootButton());
        vertical.add(createShutdownButton());
        add(vertical);
    }

    private Component createSaveButton() {
        saveButton.addClickListener(event -> {
            try {
                innRepository.findAll().stream().findFirst().ifPresentOrElse(t -> {
                    t.setInn(inn.getValue());
                    innRepository.save(t);
                }, () -> innRepository.save(new Inn(inn.getValue())));
                final Notification notification = Notification.show(SUCCESS);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setPosition(Notification.Position.TOP_CENTER);
            } catch (Exception e) {
                final Notification notification = Notification.show("Ошибка");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setPosition(Notification.Position.TOP_CENTER);
            }
        });
        return saveButton;
    }

    private Component createShutdownButton() {
        shutdownButton.addClickListener(e -> System.exit(0));
        shutdownButton.setIcon(VaadinIcon.POWER_OFF.create());
        shutdownButton.setWidth(315, Unit.PIXELS);
        shutdownButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return shutdownButton;
    }

    private Component createRebootButton() {
        rebootButton.addClickListener(e -> VaadinApplication.reboot());
        rebootButton.setIcon(VaadinIcon.REFRESH.create());
        rebootButton.setWidth(315, Unit.PIXELS);
        rebootButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        return rebootButton;
    }

    private Component createGenerateUniqueKeyButton() {
        final var generateUniqueKeyAnchor = new Anchor();
        generateUniqueKeyAnchor.setWidth(315, Unit.PIXELS);
        generateUniqueKeyAnchor.setHref(new StreamResource("key", () -> {
            final String key = String.join(";", systemId, inn.getValue());
            return new ByteArrayInputStream(encryptor.encrypt(key).getBytes());
        }));
        generateUniqueKeyAnchor.getElement().setAttribute("download", true);
        final var generateUniqueKeyButton = new Button("Сформировать уникальный ключ");
        generateUniqueKeyButton.setWidth(315, Unit.PIXELS);
        generateUniqueKeyAnchor.add(generateUniqueKeyButton);
        return generateUniqueKeyAnchor;
    }

    private Component createSystemIdField() {
        Optional.ofNullable(systemId).ifPresent(systemIdField::setValue);
        systemIdField.setReadOnly(true);
        systemIdField.setWidth(315, Unit.PIXELS);

        return systemIdField;
    }

}
