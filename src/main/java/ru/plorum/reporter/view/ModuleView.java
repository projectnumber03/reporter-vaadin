package ru.plorum.reporter.view;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

import static ru.plorum.reporter.util.Constants.MODULES;

@PageTitle(MODULES)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "modules", layout = MainView.class)
public class ModuleView extends AbstractView {

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        add(vertical);
    }

}
