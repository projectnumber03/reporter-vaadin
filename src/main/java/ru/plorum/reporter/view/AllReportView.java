package ru.plorum.reporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

import static ru.plorum.reporter.util.Constants.ALL_REPORTS;

@PageTitle(ALL_REPORTS)
@Route(value = "all_reports", layout = MainView.class)
public class AllReportView extends AbstractView {

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        add(vertical);
    }

}
