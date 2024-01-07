package ru.plorum.reporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ru.plorum.reporter.component.LicenseCache;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.service.ReportGroupService;
import ru.plorum.reporter.service.ReportOutputService;
import ru.plorum.reporter.service.ReportService;

import java.util.List;

import static ru.plorum.reporter.util.Constants.ALL_REPORTS;

@PageTitle(ALL_REPORTS)
@Route(value = "all_reports", layout = MainView.class)
public class ReportAllView extends ReportMyView {

    public ReportAllView(
            final ReportService reportService,
            final ReportOutputService reportOutputService,
            final ReportGroupService reportGroupService,
            final LicenseCache licenseCache
    ) {
        super(reportService, reportOutputService, reportGroupService, licenseCache);
    }

    @Override
    protected List<Report> getReports() {
        return getReportService().findAll();
    }

}
