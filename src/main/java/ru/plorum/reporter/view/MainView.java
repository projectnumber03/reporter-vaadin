package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import lombok.Getter;

import java.util.Optional;

public class MainView extends AppLayout {

    @Getter
    private final H3 viewTitle = new H3();

    public MainView() {
        final DrawerToggle toggle = new DrawerToggle();
        final H1 title = new H1("Reporter");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");
        final H6 mainMenuHeader = new H6("Главное меню");
        mainMenuHeader.getStyle().set("padding", "10px 25px 10px 15px");
        addToDrawer(mainMenuHeader);
        addToDrawer(getTabs());
        addToDrawer(createAdminMenu());
        final H6 reporterMenuHeader = new H6("Отчетность");
        reporterMenuHeader.getStyle().set("padding", "10px 25px 10px 15px");
        addToDrawer(reporterMenuHeader);
        addToDrawer(createReportMenu());
        addToNavbar(toggle, title);
    }

    private Tabs getTabs() {
        final Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.USER, "Профиль", null)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        return tabs;
    }

    private Component createAdminMenu() {
        final Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.USER, "Пользователи", UsersView.class),
                createTab(VaadinIcon.USERS, "Группы", null),
                createTab(VaadinIcon.UNLINK, "Подключения", null),
                createTab(VaadinIcon.CUBES, "Модули", null),
                createTab(VaadinIcon.TABLE, "Таблицы системы", null),
                createTab(VaadinIcon.WRENCH, "Настройки системы", null)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        final VerticalLayout content = new VerticalLayout(tabs);
        content.setSpacing(false);
        content.setPadding(false);

        final Details details = new Details("Администрирование", content);
        details.setOpened(true);

        return details;
    }

    private Component createReportMenu() {
        final Tabs tabs = new Tabs();
        tabs.add(
                createTab(VaadinIcon.PLUS_SQUARE_O, "Создать отчёт", null),
                createTab(VaadinIcon.FILE_TEXT_O, "Мои отчёты", null),
                createTab(VaadinIcon.COPY_O, "Все отчёты", null),
                createTab(VaadinIcon.COPY_O, "Группы отчётов", null)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        return tabs;
    }

    private Tab createTab(final VaadinIcon viewIcon, final String viewName, final Class<? extends Component> navigationTarget) {
        final Icon icon = viewIcon.create();
        icon.getStyle().set("box-sizing", "border-box")
                .set("margin-inline-end", "var(--lumo-space-m)")
                .set("margin-inline-start", "var(--lumo-space-xs)")
                .set("padding", "var(--lumo-space-xs)");
        final RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setTabIndex(-1);
        Optional.ofNullable(navigationTarget).ifPresent(link::setRoute);

        return new Tab(link);
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return getContent().getClass().getAnnotation(PageTitle.class).value();
    }

}
