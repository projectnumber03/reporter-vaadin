package ru.plorum.reporter.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import ru.plorum.reporter.component.ILicenseCache;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.ReportOutput;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.service.ReportOutputService;
import ru.plorum.reporter.service.ReportService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ru.plorum.reporter.util.Constants.*;


@PageTitle(REPORT_OUTPUTS)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "report_outputs", layout = MainView.class)
public class ReportOutputView extends AbstractView implements HasUrlParameter<String>, BeforeEnterObserver {

    private final ReportOutputService reportOutputService;

    private final ReportService reportService;

    private final ILicenseCache licenseCache;

    private final PaginatedGrid<ReportOutput> reportOutputTable;

    public ReportOutputView(
            final ReportOutputService reportOutputService,
            final ReportService reportService,
            final ILicenseCache licenseCache
    ) {
        this.reportOutputService = reportOutputService;
        this.reportService = reportService;
        this.licenseCache = licenseCache;
        this.reportOutputTable = createReportOutputTable();
    }

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        setHeightFull();
        vertical.setHeightFull();
        vertical.add(reportOutputTable);
        add(vertical);
    }

    private PaginatedGrid<ReportOutput> createReportOutputTable() {
        final Grid<ReportOutput> grid = new Grid<>();
        grid.addColumn(ro -> Optional.ofNullable(ro.getReport()).map(Report::getName).orElse(NA)).setHeader(REPORT);
        grid.addColumn(ro -> Optional.ofNullable(ro.getCreatedAt()).map(DATE_TIME_FORMATTER::format).orElse(NA)).setHeader("Дата");
        grid.addColumn(ro -> Optional.ofNullable(ro.getUser()).map(User::getName).orElse(NA)).setHeader(USER);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addItemDoubleClickListener(event -> UI.getCurrent().navigate("report_output/" + event.getItem().getId()));

        return new PaginatedGrid<>(grid);
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String s) {
        final var report = reportService.findById(UUID.fromString(s));
        if (Objects.isNull(report)) return;
        final var reportOutputs = reportOutputService.findByReport(report);
        reportOutputTable.setItems(reportOutputs);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
        if (licenseCache.getActive().isEmpty()) {
            beforeEnterEvent.rerouteTo(IndexView.class);
        }
    }

}
