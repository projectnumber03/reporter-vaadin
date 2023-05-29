package ru.plorum.reporter.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import ru.plorum.reporter.view.LoginView;

@Configuration
@EnableWebSecurity
@Profile({"business", "corporative"})
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.authorizeHttpRequests().requestMatchers("/images/*.png").permitAll();
        super.configure(http);
        setLoginView(http, LoginView.class);
    }

}
