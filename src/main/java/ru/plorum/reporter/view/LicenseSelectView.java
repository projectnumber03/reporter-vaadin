package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Value;
import ru.plorum.reporter.component.LicenseCache;
import ru.plorum.reporter.model.License;


@AnonymousAllowed
@PageTitle("Панель администратора")
@Route("license-select")
public class LicenseSelectView extends VerticalLayout {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final LicenseCache licenseCache;

    private final RadioButtonGroup<License> licenseField = new RadioButtonGroup<>("Для активации продукта, выберите 1 из доступных лицензий");

    private final Button selectButton = new Button("Выбрать");

    public LicenseSelectView(final LicenseCache licenseCache) {
        this.licenseCache = licenseCache;
        setSizeFull();
        addClassName("license-select");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        licenseField.setWidth(300, Unit.PIXELS);
        licenseField.setRequired(true);
        licenseField.setRequiredIndicatorVisible(true);
        licenseField.setItems(licenseCache.getAll());
        licenseField.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        licenseField.addValueChangeListener(e -> {
            licenseCache.getAll().forEach(v -> v.setActive(v.equals(e.getValue())));
            selectButton.setEnabled(true);
        });
        final var titleLayout = new VerticalLayout(new H1("Панель администратора"));
        titleLayout.setAlignItems(Alignment.CENTER);
        titleLayout.setWidth("auto");
        add(titleLayout);
        final var layout = new VerticalLayout(new H2(String.format("Доступные лицензии для продукта Reporter (%s)", activeProfile.toUpperCase())), licenseField, createSelectButton(), createBackButton());
        layout.setWidth("auto");
        layout.setAlignItems(Alignment.START);
        add(layout);
    }

    private Component createSelectButton() {
        selectButton.setWidth(300, Unit.PIXELS);
        selectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectButton.setEnabled(false);
        selectButton.addClickListener(e -> selectButton.getUI().ifPresent(ui -> ui.navigate(IndexView.class)));
        return selectButton;
    }

    private Component createBackButton() {
        var button = new Button("Назад");
        button.setWidth(300, Unit.PIXELS);
        button.addClickListener(e -> button.getUI().ifPresent(ui -> ui.navigate(LoginView.class)));

        return button;
    }

}
