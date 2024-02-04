package ru.plorum.reporter.view;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.component.ILicenseCache;
import ru.plorum.reporter.model.Parameter;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.service.ReportService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.plorum.reporter.util.Constants.REPORT_PARAMETERS;

@PageTitle(REPORT_PARAMETERS)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "report/parameters", layout = MainView.class)
public class ReportParameterView extends AbstractView implements HasUrlParameter<String>, BeforeEnterObserver {

    private final ReportService reportService;

    private final ILicenseCache licenseCache;

    private final Button processButton = new Button("Продолжить");

    private final Map<String, DatePicker> dateParameterFields = new HashMap<>();

    private final Map<String, NumberField> integerParameterFields = new HashMap<>();

    private final Map<String, TextField> stringParameterFields = new HashMap<>();

    private final DatePicker.DatePickerI18n i18n;

    private Report report;

    public ReportParameterView(
            final ReportService reportService,
            final ILicenseCache licenseCache,
            final DatePicker.DatePickerI18n i18n
    ) {
        this.reportService = reportService;
        this.licenseCache = licenseCache;
        this.i18n = i18n;
    }

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        setHeightFull();
        vertical.setHeightFull();
        add(vertical);
    }

    private Component createProcessButton() {
        processButton.addClickListener(e -> {
            final Map<String, Object> parameters = Stream.of(dateParameterFields, integerParameterFields, stringParameterFields)
                    .map(Map::entrySet)
                    .flatMap(Collection::stream)
                    .filter(entry -> Objects.nonNull(entry.getValue().getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getValue()));
            Optional.ofNullable(report).ifPresent(r -> reportService.generateInThread(r, parameters, true));
            processButton.getUI().ifPresent(ui -> ui.navigate("my_reports"));
        });

        return processButton;
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
        this.report = report;
        final var parameters = this.report.getParameters();
        if (CollectionUtils.isEmpty(parameters)) return;
        parameters.forEach(this::createParameterField);
        vertical.add(createProcessButton());
    }

    private void createParameterField(final Parameter parameter) {
        switch (parameter.getType()) {
            case DATE -> {
                final var dateParameterField = new DatePicker(parameter.getDescription());
                dateParameterField.setI18n(i18n);
                dateParameterFields.put(parameter.getName(), dateParameterField);
                vertical.add(dateParameterField);
            }
            case INTEGER -> {
                final var integerParameterField = new NumberField(parameter.getDescription());
                integerParameterFields.put(parameter.getName(), integerParameterField);
                vertical.add(integerParameterField);
            }
            case STRING -> {
                final var stringParameterField = new TextField(parameter.getDescription());
                stringParameterFields.put(parameter.getName(), stringParameterField);
                vertical.add(stringParameterField);
            }
        }
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
        if (licenseCache.getActive().isEmpty()) {
            beforeEnterEvent.rerouteTo(IndexView.class);
        }
    }

}
