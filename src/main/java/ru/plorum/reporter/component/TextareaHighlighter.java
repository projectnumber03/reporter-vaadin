package ru.plorum.reporter.component;

import com.vaadin.flow.component.ClickNotifier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.dom.Element;

@Tag("div")
@StyleSheet("//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/default.min.css")
@JavaScript("//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/highlight.min.js")
public class TextareaHighlighter extends Component implements ClickNotifier<TextareaHighlighter> {

    private final Element pre = new Element("pre");
    private final Element code = new Element("code");

    public TextareaHighlighter() {
        code.setAttribute("class", "language-sql");
        pre.appendChild(code);
        getElement().appendChild(pre);
    }

    public void setValue(final String value) {
        code.setText(value);
        getElement().executeJs("hljs.highlightAll()");
    }

}
