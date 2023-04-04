package ru.plorum.reporter.component;

import com.vaadin.flow.component.Unit;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;

public class TextAreaHighlighter extends AceEditor {

    public TextAreaHighlighter() {
        setHeight(100, Unit.PIXELS);
        setTheme(AceTheme.chrome);
        setMode(AceMode.sql);
    }

}
