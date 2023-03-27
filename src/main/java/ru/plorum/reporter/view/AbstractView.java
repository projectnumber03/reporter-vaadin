package ru.plorum.reporter.view;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractView extends VerticalLayout {

    protected final VerticalLayout vertical = new VerticalLayout();

    protected final HorizontalLayout horizontal = new HorizontalLayout();

    public AbstractView() {
        horizontal.setWidthFull();
        vertical.add(horizontal);
    }

    protected abstract void initialize();

}
