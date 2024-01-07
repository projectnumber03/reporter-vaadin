package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import ru.plorum.reporter.component.LicenseCache;
import ru.plorum.reporter.component.TariffInfo;
import ru.plorum.reporter.model.License;

import java.util.Optional;

import static ru.plorum.reporter.util.Constants.DATE_FORMATTER;


@PermitAll
@Route("license-select")
@PageTitle("Панель администратора")
public class LicenseSelectView extends VerticalLayout {

    private final LicenseCache licenseCache;

    private final RadioButtonGroup<License> licenseField = new RadioButtonGroup<>("Для активации продукта, выберите 1 из доступных лицензий");

    private final Button selectButton = new Button("Выбрать");

    public LicenseSelectView(
            final LicenseCache licenseCache,
            final TariffInfo tariffInfo
    ) {
        this.licenseCache = licenseCache;
        setSizeFull();
        addClassName("license-select");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        licenseField.getStyle().set("padding", "0");
        licenseField.setWidth("auto");
        licenseField.setRequired(true);
        licenseField.setRequiredIndicatorVisible(true);
        licenseField.setItems(licenseCache.getAll());
        licenseField.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        licenseField.setItemLabelGenerator(l -> String.format("Лицензия %s", l.getValidity() == Integer.MAX_VALUE ? "бессрочная" : String.format("%s - %s", DATE_FORMATTER.format(l.getStartDate()), DATE_FORMATTER.format(l.getFinishDate()))));
        licenseField.addValueChangeListener(e -> selectButton.setEnabled(true));
        final var titleLayout = new VerticalLayout(new H1("Панель администратора"));
        titleLayout.setWidth("auto");
        titleLayout.setAlignItems(Alignment.START);
        add(titleLayout);
        final var tariffName = Optional.ofNullable(tariffInfo).map(TariffInfo::getTariffName).map(v -> String.format("(%s)", v.toUpperCase())).orElse("");
        final var layout = new VerticalLayout(new H2(String.format("Доступные лицензии для продукта Reporter %s", tariffName)));
        final var line = new Div();
        line.getStyle().set("width", "100%").set("border-top", "1px solid #DADEDF");
        layout.add(line);
        layout.add(licenseField);
        layout.setWidth("auto");
        layout.setAlignItems(Alignment.START);
        final var layoutStyle = layout.getStyle();
        layoutStyle.set("background", "#F8F8F8");
        layoutStyle.set("border-radius", "10px");
        add(layout);
        final var buttonLayout = new VerticalLayout(createSelectButton());
        buttonLayout.setPadding(false);
        buttonLayout.setSpacing(false);
        buttonLayout.setAlignItems(Alignment.END);
        layout.add(buttonLayout);
    }

    private Component createSelectButton() {
        selectButton.setWidth(200, Unit.PIXELS);
        selectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectButton.setEnabled(false);
        selectButton.addClickListener(e -> {
            licenseCache.getAll().forEach(v -> v.setActive(v.equals(licenseField.getValue())));
            selectButton.getUI().ifPresent(ui -> ui.navigate(IndexView.class));
        });
        return selectButton;
    }

}
