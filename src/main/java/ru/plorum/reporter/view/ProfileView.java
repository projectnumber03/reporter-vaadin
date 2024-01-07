package ru.plorum.reporter.view;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import lombok.AllArgsConstructor;
import ru.plorum.reporter.component.LicenseCache;

import static ru.plorum.reporter.util.Constants.PROFILE;

@PageTitle(PROFILE)
@AllArgsConstructor
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "profile", layout = MainView.class)
public class ProfileView extends AbstractView implements BeforeEnterObserver {

    private final LicenseCache licenseCache;

    @Override
    @PostConstruct
    protected void initialize() {
        super.initialize();
        add(vertical);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
        if (licenseCache.getActive().isEmpty()) {
            beforeEnterEvent.rerouteTo(IndexView.class);
        }
    }

}
