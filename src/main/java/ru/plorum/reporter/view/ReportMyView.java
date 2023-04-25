package ru.plorum.reporter.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.plorum.reporter.component.ReportTableContextMenu;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.ReportGroup;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.service.ReportGroupService;
import ru.plorum.reporter.service.ReportService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.plorum.reporter.util.Constants.*;

@Slf4j
@PageTitle(MY_REPORTS)
@Route(value = "my_reports", layout = MainView.class)
public class ReportMyView extends AbstractView {

    @Getter
    private final ReportService reportService;

    private final ReportGroupService reportGroupService;

    private final PaginatedGrid<Report> reportTable;

    public ReportMyView(final ReportService reportService, final ReportGroupService reportGroupService) {
        this.reportService = reportService;
        this.reportGroupService = reportGroupService;
        this.reportTable = createReportTable();
    }

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        reportService.setUi(UI.getCurrent());
        setHeightFull();
        vertical.setHeightFull();
        vertical.add(reportTable);
        add(vertical);
    }

    private PaginatedGrid<Report> createReportTable() {
        final Grid<Report> grid = new Grid<>();
        grid.addColumn(createEditButtonRenderer()).setHeader(NAME);
        grid.addColumn(r -> Optional.ofNullable(r.getGroup()).map(ReportGroup::getName).orElse(NA)).setHeader("Группа отчета");
        grid.addColumn(r -> Optional.ofNullable(r.getDateReport()).map(DATE_TIME_FORMATTER::format).orElse(NA)).setHeader("Дата формирования отчета");
        grid.addColumn(r -> r.getAuthor().getName()).setHeader("Автор");
        grid.addColumn(Report::getDescription).setHeader(DESCRIPTION);
        grid.addColumn(r -> Optional.ofNullable(r.getLastEditor()).map(User::getName).orElse(NA)).setHeader("Последний редактировавший");
        grid.addColumn(Report::getStatus).setHeader("Статус");
        new ReportTableContextMenu(grid, reportService, reportGroupService) {

            @Override
            public List<Report> getReports() {
                return reportService.findMy();
            }

            @Override
            public QueryParameters getQueryParameters(final Report report) {
                return ReportMyView.this.getQueryParameters(report);
            }

        };

        final var paginatedGrid = new PaginatedGrid<>(grid);
        paginatedGrid.setItems(getReports());

        return paginatedGrid;
    }

    private ComponentRenderer<Button, Report> createEditButtonRenderer() {
        final SerializableBiConsumer<Button, Report> editButtonProcessor = (button, report) -> {
            button.setThemeName("tertiary");
            button.setText(report.getName());
            button.addClickListener(e -> button.getUI().ifPresent(ui -> ui.navigate("reports/upsert/", getQueryParameters(report))));
        };
        return new ComponentRenderer<>(Button::new, editButtonProcessor);
    }

    private QueryParameters getQueryParameters(final Report report) {
        return QueryParameters.simple(Map.of("id", report.getId().toString()));
    }

    protected List<Report> getReports() {
        return reportService.findMy();
    }

}
