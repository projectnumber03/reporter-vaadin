package ru.plorum.reporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

import static ru.plorum.reporter.util.Constants.PROFILE;

@PageTitle(PROFILE)
@Route(value = "profile", layout = MainView.class)
public class ProfileView extends AbstractView {

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        add(vertical);
    }

}
