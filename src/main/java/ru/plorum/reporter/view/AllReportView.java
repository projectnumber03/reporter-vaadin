package ru.plorum.reporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.service.ReportService;

import java.util.List;

import static ru.plorum.reporter.util.Constants.ALL_REPORTS;

@PageTitle(ALL_REPORTS)
@Route(value = "all_reports", layout = MainView.class)
public class AllReportView extends MyReportView {

    public AllReportView(final ReportService reportService) {
        super(reportService);
    }

    @Override
    protected List<Report> getReports() {
        return getReportService().findAll();
    }

}
