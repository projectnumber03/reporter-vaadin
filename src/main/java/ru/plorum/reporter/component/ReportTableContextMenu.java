package ru.plorum.reporter.component;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.service.ReportService;

import java.util.List;
import java.util.Optional;

import static ru.plorum.reporter.util.Constants.*;

public abstract class ReportTableContextMenu extends GridContextMenu<Report> {

    public ReportTableContextMenu(final Grid<Report> targetTable, final ReportService reportService) {
        super(targetTable);

        final GridMenuItem<Report> openMenuItem = addItem("Открыть");
        openMenuItem.addMenuItemClickListener(e -> e.getItem().ifPresent(report -> {
            openMenuItem.getUI().ifPresent(ui -> ui.navigate("reports/upsert/", getQueryParameters(report)));
        }));

        final GridMenuItem<Report> makeMenuItem = addItem(MAKE);
        makeMenuItem.addComponentAsFirst(VaadinIcon.REFRESH.create());
        makeMenuItem.addMenuItemClickListener(e -> e.getItem().ifPresent(reportService::generate));

        addItem(COPY, e -> e.getItem().ifPresent(report -> {
            final Report reportToClone = reportService.findById(report.getId());
            Optional.ofNullable(reportToClone).ifPresent(r -> {
                final Report clonedReport = r.clone();
                reportService.save(clonedReport);
                targetTable.setItems(getReports());
            });
        }));

        final GridMenuItem<Report> deleteMenuItem = addItem(DELETE);
        deleteMenuItem.addComponentAsFirst(VaadinIcon.TRASH.create());
        deleteMenuItem.addMenuItemClickListener(e -> e.getItem().ifPresent(report -> {
            final String message = String.format("Хотите удалить отчёт \"%s\"?", report.getName());
            final Runnable callback = () -> {
                reportService.delete(report);
                targetTable.setItems(getReports());
            };
            new ConfirmationDialog(message, callback).open();
        }));

        setDynamicContentHandler(report -> {
            if (report == null) {
                return false;
            }
            return true;
        });
    }

    public abstract List<Report> getReports();

    public abstract QueryParameters getQueryParameters(final Report report);

}
