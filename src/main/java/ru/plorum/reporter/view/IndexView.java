package ru.plorum.reporter.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.AllArgsConstructor;
import ru.plorum.reporter.component.SetupDataLoader;


@PermitAll
@AllArgsConstructor
@PageTitle("Главная")
@Route(value = "", layout = MainView.class)
public class IndexView extends AbstractView implements BeforeEnterObserver {

    private final SetupDataLoader setupDataLoader;

    @Override
    @PostConstruct
    protected void initialize() {
        add(vertical);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        if (setupDataLoader.getLicenseCache().getActive().isEmpty()) {
            event.rerouteTo(LicenseSelectView.class);
        }
    }

}
