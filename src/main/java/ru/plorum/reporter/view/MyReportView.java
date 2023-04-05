package ru.plorum.reporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

import static ru.plorum.reporter.util.Constants.MY_REPORTS;

@PageTitle(MY_REPORTS)
@Route(value = "my_reports", layout = MainView.class)
public class MyReportView extends AbstractView {

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        add(vertical);
    }

}
