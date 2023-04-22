package ru.plorum.reporter.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

@Getter
public class ImportExportTabContent extends VerticalLayout {

    private final Button importButton = new Button("Импортировать конфигурацию отчёта");

    private final Button exportButton = new Button("Экспортировать конфигурацию отчёта");

    public ImportExportTabContent() {
        add(new HorizontalLayout(createImportButton(), createExportButton()));
    }

    private Component createImportButton() {

        return importButton;
    }

    private Component createExportButton() {

        return exportButton;
    }

}
