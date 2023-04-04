package ru.plorum.reporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;


@PageTitle("Главная")
@Route(value = "", layout = MainView.class)
public class IndexView extends AbstractView {

    @Override
    @PostConstruct
    protected void initialize() {
        add(vertical);
    }

}
