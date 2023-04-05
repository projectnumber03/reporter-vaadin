package ru.plorum.reporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

import static ru.plorum.reporter.util.Constants.SYSTEM_TABLES;

@PageTitle(SYSTEM_TABLES)
@Route(value = "historical_tables", layout = MainView.class)
public class SystemTableView extends AbstractView {

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        add(vertical);
    }

}
