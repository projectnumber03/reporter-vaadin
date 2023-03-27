package ru.plorum.reporter.view;

import ru.plorum.reporter.component.TextareaHighlighter;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;


@PageTitle("Главная")
@Route(value = "", layout = MainView.class)
public class IndexView extends AbstractView {

    private final TextArea textArea = new TextArea();

    @Override
    @PostConstruct
    protected void initialize() {
        final TextareaHighlighter textareaHighlighter = new TextareaHighlighter();
        textareaHighlighter.addClickListener(e -> {
            textArea.setVisible(true);
            textareaHighlighter.setVisible(false);
            textArea.focus();
        });
        textArea.addBlurListener(e -> {
            textareaHighlighter.setValue(textArea.getValue());
            textArea.setVisible(false);
            textareaHighlighter.setVisible(true);
        });
        textArea.addValueChangeListener(e -> {
            textareaHighlighter.setValue(e.getValue());
            textArea.setVisible(false);
            textareaHighlighter.setVisible(true);
        });
        vertical.add(textArea);
        vertical.add(textareaHighlighter);
        add(vertical);
    }

}
