package ru.plorum.reporter.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import org.apache.logging.log4j.util.Strings;
import ru.plorum.reporter.model.Query;
import ru.plorum.reporter.model.ReportGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.plorum.reporter.util.Constants.DESCRIPTION;
import static ru.plorum.reporter.util.Constants.NAME;

@Getter
public class QueryTabContent extends VerticalLayout {

    private final TextField name = new TextField(NAME);

    private final TextArea description = new TextArea(DESCRIPTION);

    private final ComboBox<ReportGroup> reportGroup = new ComboBox<>("Группа");

    private final Grid<Query> queryGrid = new Grid<>();

    private final Button addQueryButton = new Button("Добавить запрос");

    private Query draggedItem;

    private final List<Query> items = new ArrayList<>();

    private Registration dropListener;

    public QueryTabContent() {
        setHeightFull();
        add(name, description, reportGroup, createQueryGrid(), createAddQueryButton());
    }

    private Component createQueryGrid() {
        queryGrid.setSelectionMode(Grid.SelectionMode.NONE);
        queryGrid.addColumn(new ComponentRenderer<>(q -> {
            final VerticalLayout layout = new VerticalLayout();
            layout.setWidthFull();
            layout.add(q.getSqlTextField());
            layout.add(q.getGenerateReportCheckbox());
            layout.add(q.getSubReportField());
            final Details details = new Details(Strings.EMPTY, layout);
            details.setOpened(true);
            details.addThemeVariants(DetailsVariant.SMALL);
            details.setWidthFull();
            return details;
        })).setHeader("Запросы отчета");

        queryGrid.setWidthFull();
        queryGrid.setDropMode(GridDropMode.BETWEEN);
        queryGrid.setRowsDraggable(true);
        queryGrid.addDragStartListener(e -> draggedItem = e.getDraggedItems().get(0));
        queryGrid.addDragEndListener(e -> draggedItem = null);
        queryGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        return queryGrid;
    }

    private void setItems() {
        Optional.ofNullable(dropListener).ifPresent(Registration::remove);
        final GridListDataView<Query> dataView = queryGrid.setItems(items);
        dropListener = queryGrid.addDropListener(e -> {
            final Query targetQuery = e.getDropTargetItem().orElse(null);
            final GridDropLocation dropLocation = e.getDropLocation();
            boolean personWasDroppedOntoItself = draggedItem.equals(targetQuery);
            if (targetQuery == null || personWasDroppedOntoItself) {
                return;
            }
            dataView.removeItem(draggedItem);
            if (dropLocation == GridDropLocation.BELOW) {
                dataView.addItemAfter(draggedItem, targetQuery);
                return;
            }
            dataView.addItemBefore(draggedItem, targetQuery);
        });
    }

    private Component createAddQueryButton() {
        addQueryButton.addClickListener(e -> {
            final UUID id = UUID.randomUUID();
            items.add(new Query(id));
            setItems();
        });
        return addQueryButton;
    }

}
