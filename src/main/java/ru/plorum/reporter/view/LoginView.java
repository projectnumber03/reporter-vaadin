package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import ru.plorum.reporter.component.SetupDataLoader;
import ru.plorum.reporter.service.LicenseService;

@Route("login")
@PageTitle("Вход | Reporter")
public class LoginView extends VerticalLayout implements BeforeEnterObserver/*, ComponentEventListener<AbstractLogin.LoginEvent>*/ {

    private final LicenseService licenseService;

    private final SetupDataLoader setupDataLoader;

    private final LoginForm loginForm = new LoginForm();

    public LoginView(
            final LicenseService licenseService,
            final SetupDataLoader setupDataLoader
    ) {
        this.licenseService = licenseService;
        this.setupDataLoader = setupDataLoader;
        loginForm.setI18n(createI18n());
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);
        setSizeFull();
        addClassName("login-view");
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        add(new H1("Reporter"), loginForm, createSignupButton());
    }

    @PostConstruct
    protected void initialize() {
        licenseService.setUi(UI.getCurrent());
    }

    private LoginI18n createI18n() {
        final var i18n = LoginI18n.createDefault();
        final var i18nForm = i18n.getForm();
        i18nForm.setTitle("Вход");
        i18nForm.setUsername("Логин");
        i18nForm.setPassword("Пароль");
        i18nForm.setSubmit("Войти в систему");
        i18nForm.setForgotPassword("Забыли пароль?");
        i18n.setForm(i18nForm);
        final var i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Неверное имя пользователя или пароль");
        i18nErrorMessage.setMessage("Проверьте правильность имени пользователя и пароля и повторите попытку");
        i18n.setErrorMessage(i18nErrorMessage);

        return i18n;
    }

    private Component createSignupButton() {
        var button = new Button("Регистрация");
        button.addClickListener(e -> button.getUI().ifPresent(ui -> ui.navigate("register")));
        return button;
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent beforeEnterEvent) {
        final var error = beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error");
        if (!error) return;
        loginForm.setError(true);
    }

   /* @Override
    public void onComponentEvent(final AbstractLogin.LoginEvent loginEvent) {
        final var authenticated = SecurityUtils.authenticate(loginEvent.getUsername(), loginEvent.getPassword());
        if (!authenticated) {
            loginForm.setError(true);
            return;
        }
        if (setupDataLoader.getLicenseCache().getActive().isEmpty()) {
            UI.getCurrent().navigate(LicenseSelectView.class);
            return;
        }
        UI.getCurrent().getPage().setLocation(SecurityUtils.LOGIN_SUCCESS_URL);
    }*/

}
