package ru.plorum.reporter.util;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.ServletException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityUtils {

    public final String LOGOUT_SUCCESS_URL = "/";

    public final String LOGIN_SUCCESS_URL = "/";

    public boolean isAuthenticated() {
        final var request = VaadinServletRequest.getCurrent();
        return request != null && request.getUserPrincipal() != null;
    }

    public boolean authenticate(final String username, final String password) {
        final var request = VaadinServletRequest.getCurrent();
        if (request == null) return false;
        try {
            request.login(username, password);
            // change session ID to protect against session fixation
            request.getHttpServletRequest().changeSessionId();
            return true;
        } catch (ServletException e) {
            return false;
        }
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        VaadinSession.getCurrent().getSession().invalidate();
    }

}
