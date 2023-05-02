package ru.plorum.reporter;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Push
@SpringBootApplication
public class VaadinApplication implements AppShellConfigurator {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(VaadinApplication.class, args);
    }

    public static void reboot() {
        final ApplicationArguments args = context.getBean(ApplicationArguments.class);
        final Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(VaadinApplication.class, args.getSourceArgs());
        });
        thread.setDaemon(false);
        thread.start();
    }

}
