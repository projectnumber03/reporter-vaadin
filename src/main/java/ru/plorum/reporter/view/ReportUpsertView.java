package ru.plorum.reporter.view;


import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

import static ru.plorum.reporter.util.Constants.REPORT;

@PageTitle(REPORT)
@Route(value = "reports/upsert", layout = MainView.class)
public class ReportUpsertView extends AbstractView {

    final MenuBar menuBar = new MenuBar();

    @Override
    @PostConstruct
    protected void initialize() {
        vertical.add(createMenuBar());
        add(vertical);
    }

    private MenuBar createMenuBar() {
        menuBar.addItem("Запросы отчёта");
        menuBar.addItem("Планировщик");
        menuBar.addItem("Источники");
        menuBar.addItem("Импорт/экспорт");
        menuBar.addItem("Безопасность");
        return menuBar;
    }

}
