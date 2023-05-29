package ru.plorum.reporter.view;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;

@PermitAll
@PageTitle("О программе")
@Route(value = "about", layout = MainView.class)
public class AboutView extends AbstractView {

    public AboutView(
            @Value("${spring.profiles.active}") final String profile,
            @Value("${amount.users:2147483647}") final Integer users,
            @Value("${amount.roles:2147483647}") final Integer roles,
            @Value("${amount.sources:2147483647}") final Integer sources,
            @Value("${amount.reports:2147483647}") final Integer reports,
            @Value("${amount.generations:2147483647}") final Integer generations,
            @Value("${own.storage:true}") final Boolean ownStorage
    ) {
        vertical.add(new Label("Профиль: " + profile));
        vertical.add(new Label("Количество пользователей: " + formatAmount(users)));
        vertical.add(new Label("Количество ролей безопасности: " + formatAmount(roles)));
        vertical.add(new Label("Количество источников данных: " + formatAmount(sources)));
        vertical.add(new Label("Количество отчётов: " + formatAmount(reports)));
        vertical.add(new Label("Количество генераций отчётов: " + formatAmount(generations)));
        vertical.add(new Label("Собственное хранилище данных: " + (ownStorage ? "Да" : "Нет")));
    }

    @Override
    @PostConstruct
    protected void initialize() {
        add(vertical);
    }

    private String formatAmount(@NonNull final Integer value) {
        if (value > 100) {
            return "без ограничений";
        }
        return String.valueOf(value);
    }

}
