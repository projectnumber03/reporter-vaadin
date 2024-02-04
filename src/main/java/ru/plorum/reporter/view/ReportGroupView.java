package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import ru.plorum.reporter.component.ILicenseCache;
import ru.plorum.reporter.component.NewButton;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.ReportGroup;
import ru.plorum.reporter.service.ReportGroupService;

import java.util.Map;
import java.util.Optional;

import static ru.plorum.reporter.util.Constants.*;

@PageTitle(REPORT_GROUPS)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "report_groups", layout = MainView.class)
public class ReportGroupView extends AbstractView implements BeforeEnterObserver {

    private final ReportGroupService reportGroupService;

    private final ILicenseCache licenseCache;

    private final PaginatedGrid<ReportGroup> reportGroupTable;

    public ReportGroupView(
            final ReportGroupService reportGroupService,
            final ILicenseCache licenseCache
    ) {
        this.reportGroupService = reportGroupService;
        this.licenseCache = licenseCache;
        this.reportGroupTable = createReportGroupTable();
    }

    @Override
    @PostConstruct
    protected void initialize() {
        setHeightFull();
        final VerticalLayout layout = new VerticalLayout(new H4(REPORT_GROUPS), createNewButton());
        layout.setPadding(false);
        horizontal.add(layout);
        vertical.setHeightFull();
        vertical.add(reportGroupTable);
        add(vertical);
    }

    private Component createNewButton() {
        return new NewButton("Новая группа", "report_groups/upsert");
    }

    private PaginatedGrid<ReportGroup> createReportGroupTable() {
        final Grid<ReportGroup> grid = new Grid<>();
        grid.addColumn(createEditButtonRenderer()).setHeader(NAME);
        grid.addColumn(ReportGroup::getDescription).setHeader(DESCRIPTION);
        grid.addColumn(rg -> Optional.ofNullable(rg.getLastReportCreationDate()).map(DATE_TIME_FORMATTER::format).orElse(NA)).setHeader("Дата последнего формирования отчёта");
        final PaginatedGrid<ReportGroup> paginatedGrid = new PaginatedGrid<>(grid);
        paginatedGrid.setItems(reportGroupService.findMy());

        return paginatedGrid;
    }

    private ComponentRenderer<Button, ReportGroup> createEditButtonRenderer() {
        final SerializableBiConsumer<Button, ReportGroup> editButtonProcessor = (button, reportGroup) -> {
            button.setThemeName("tertiary");
            button.setText(reportGroup.getName());
            button.addClickListener(e -> button.getUI().ifPresent(ui -> ui.navigate("report_groups/upsert/", getQueryParameters(reportGroup))));
        };
        return new ComponentRenderer<>(Button::new, editButtonProcessor);
    }

    private QueryParameters getQueryParameters(final ReportGroup reportGroup) {
        return QueryParameters.simple(Map.of("id", reportGroup.getId().toString()));
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
        if (licenseCache.getActive().isEmpty()) {
            beforeEnterEvent.rerouteTo(IndexView.class);
        }
    }

}
