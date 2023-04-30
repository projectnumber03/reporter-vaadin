package ru.plorum.reporter.component;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.service.ReportGroupService;
import ru.plorum.reporter.service.ReportService;

import java.util.List;
import java.util.Optional;

import static ru.plorum.reporter.util.Constants.*;

public abstract class ReportTableContextMenu extends GridContextMenu<Report> {

    public ReportTableContextMenu(final Grid<Report> targetTable, final ReportService reportService, final ReportGroupService reportGroupService) {
        super(targetTable);

        final var openMenuItem = addItem(OPEN);
        openMenuItem.addMenuItemClickListener(e -> e.getItem().ifPresent(report -> openMenuItem.getUI().ifPresent(ui -> ui.navigate("report/upsert/", getQueryParameters(report)))));

        final var makeMenuItem = addItem(MAKE);
        makeMenuItem.addComponentAsFirst(VaadinIcon.REFRESH.create());
        makeMenuItem.addMenuItemClickListener(event -> {
            final var report = event.getItem();
            if (report.isEmpty()) return;
            if (CollectionUtils.isEmpty(report.get().getParameters())) {
                reportService.generate(report.get());
            }
            makeMenuItem.getUI().ifPresent(ui -> ui.navigate(""));
        });

        final var reportOutputsMenuItem = addItem(REPORT_OUTPUTS);
        reportOutputsMenuItem.addMenuItemClickListener(e -> e.getItem().ifPresent(report -> reportOutputsMenuItem.getUI().ifPresent(ui -> ui.navigate("report_outputs/" + report.getId()))));

        addItem(COPY, e -> e.getItem().ifPresent(report -> {
            final Report reportToClone = reportService.findById(report.getId());
            Optional.ofNullable(reportToClone).ifPresent(r -> {
                reportService.clone(r);
                targetTable.setItems(getReports());
            });
        }));

        final var moveToGroupMenuItem = addItem("Переместить в группу");
        final var moveToGroupMenuItemSubMenu = moveToGroupMenuItem.getSubMenu();

        final var deleteMenuItem = addItem(DELETE);
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
            moveToGroupMenuItemSubMenu.removeAll();
            reportGroupService.findMy().forEach(group -> {
                final var menuItem = moveToGroupMenuItemSubMenu.addItem(group.getName(), e -> {
                    report.setGroup(group);
                    reportService.save(report);
                    targetTable.setItems(getReports());
                });
                menuItem.setCheckable(true);
                menuItem.setChecked(report.getGroup().equals(group));
            });
            return true;
        });
    }

    public abstract List<Report> getReports();

    public abstract QueryParameters getQueryParameters(final Report report);

}
