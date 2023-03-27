package ru.plorum.reporter.component;

import ru.plorum.reporter.view.MainView;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@UIScope
@Component
@NoArgsConstructor
public class MainViewBus {

    private MainView mainView;

}
