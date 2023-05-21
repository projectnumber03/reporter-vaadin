package ru.plorum.reporter.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.component.ChartFlow;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.ReportOutputData;
import ru.plorum.reporter.service.PdfService;
import ru.plorum.reporter.service.ReportOutputService;
import ru.plorum.reporter.service.XlsxService;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ru.plorum.reporter.util.Constants.REPORT_OUTPUT;
import static ru.plorum.reporter.util.Constants.SUCCESS;

@PageTitle(REPORT_OUTPUT)
@RolesAllowed(value = {"ROLE_ADMIN"})
@Route(value = "report_output", layout = MainView.class)
public class ReportOutputDataView extends AbstractView implements HasUrlParameter<String> {

    private final ReportOutputService reportOutputService;

    private final PdfService pdfService;

    private final XlsxService xlsxService;

    private final TabSheet tabSheet = new TabSheet();

    private final Map<String, List<ReportOutputData>> tabContent = new HashMap<>();

    private final Map<String, Map<String, Checkbox>> dataTableHeaderContent = new HashMap<>();

    private final Anchor pdfAnchor = new Anchor();

    private final Anchor xlsxAnchor = new Anchor();

    private final MenuBar menuBar = new MenuBar();

    private final H4 title = new H4();

    public ReportOutputDataView(
            final ReportOutputService reportOutputService,
            final PdfService pdfService,
            final XlsxService xlsxService
    ) {
        this.reportOutputService = reportOutputService;
        this.pdfService = pdfService;
        this.xlsxService = xlsxService;
    }

    @Override
    @PostConstruct
    protected void initialize() {
        horizontal.add(title, pdfAnchor, xlsxAnchor, createChartButton());
        horizontal.setAlignItems(Alignment.CENTER);
        setHeightFull();
        setWidthFull();
        vertical.setHeightFull();
        tabSheet.setHeightFull();
        tabSheet.setWidthFull();
        tabSheet.addSelectedChangeListener(e -> {
            dataTableHeaderContent.values().stream()
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .forEach(checkbox -> {
                        checkbox.setValue(false);
                        checkbox.setReadOnly(false);
                    });
        });
        vertical.add(tabSheet);
        add(vertical);
    }

    private PaginatedGrid<List<ReportOutputData>> createReportOutputDataTable(final String tabText, final List<ReportOutputData> items) {
        final Grid<List<ReportOutputData>> grid = new Grid<>();
        final var tableData = items.stream()
                .sorted(Comparator.comparing(ReportOutputData::getRowNumber))
                .collect(Collectors.groupingBy(ReportOutputData::getRowNumber));
        final var firstRow = tableData.values().iterator().next();
        final HashMap<String, Checkbox> headerContent = new HashMap<>();
        firstRow.forEach(column -> {
            final var checkbox = new Checkbox();
            checkbox.addValueChangeListener(e -> {
                final var isTwoSelectedHeaders = dataTableHeaderContent.values().stream().map(Map::values).flatMap(Collection::stream).filter(Checkbox::getValue).count() == 2;
                if (e.getValue() && isTwoSelectedHeaders) {
                    dataTableHeaderContent.values().stream()
                            .map(Map::values)
                            .flatMap(Collection::stream)
                            .filter(Predicate.not(Checkbox::getValue))
                            .forEach(cb -> cb.setReadOnly(true));
                    menuBar.setVisible(true);
                    return;
                }
                dataTableHeaderContent.values().stream()
                        .map(Map::values)
                        .flatMap(Collection::stream)
                        .filter(Predicate.not(Checkbox::getValue))
                        .forEach(cb -> cb.setReadOnly(false));
                menuBar.setVisible(false);
            });
            final var header = new HorizontalLayout(checkbox, new Span(column.getKey()));
            headerContent.put(column.getKey(), checkbox);
            header.setAlignItems(Alignment.CENTER);
            grid.addColumn(ro -> ro.get(firstRow.indexOf(column)).getValue()).setHeader(header);
        });
        dataTableHeaderContent.put(tabText, headerContent);
        final var paginatedGrid = new PaginatedGrid<>(grid);
        paginatedGrid.setItems(tableData.values());

        return paginatedGrid;
    }

    private Component createChartButton() {
        menuBar.setVisible(false);
        final var chartImg = new Image(new StreamResource("diagram.png", () -> getClass().getResourceAsStream("/images/diagram.png")), "Построить диаграмму");
        chartImg.setWidth("32px");
        final Button chartButton = new Button();
        chartButton.setIcon(chartImg);
        chartButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        chartButton.setTooltipText("Построить диаграмму");
        final var menuItem = menuBar.addItem(chartButton);
        final var subMenu = menuItem.getSubMenu();
        Arrays.stream(ChartFlow.Type.values()).forEach(t -> subMenu.addItem(t.getDescription(), e -> buildChart(t)));

        return menuBar;
    }

    public void buildChart(final ChartFlow.Type type) {
        try {
            final var selectedTab = tabSheet.getSelectedTab();
            final var selectedTabContent = tabContent.get(selectedTab.getLabel());
            if (CollectionUtils.isEmpty(selectedTabContent)) return;
            final var selectedHeaders = dataTableHeaderContent.get(selectedTab.getLabel()).entrySet().stream()
                    .filter(e -> e.getValue().getValue())
                    .map(Map.Entry::getKey)
                    .toList();
            if (CollectionUtils.isEmpty(selectedHeaders) || selectedHeaders.size() < 2) return;
            final var tableData = selectedTabContent.stream()
                    .sorted(Comparator.comparing(ReportOutputData::getRowNumber))
                    .collect(Collectors.groupingBy(ReportOutputData::getRowNumber));
            if (CollectionUtils.isEmpty(tableData)) return;
            final var firstRow = tableData.values().iterator().next().stream().collect(Collectors.toMap(ReportOutputData::getKey, rd -> rd));
            final Supplier<Optional<String>> valueHeaderSupplier = () -> {
                for (final String h : selectedHeaders) {
                    try {
                        Integer.parseInt(firstRow.get(h).getValue());
                        return Optional.of(h);
                    } catch (Exception e) {
                    }
                }
                return Optional.empty();
            };
            final var valueHeader = valueHeaderSupplier.get();
            if (valueHeader.isEmpty()) return;
            final var labelHeader = selectedHeaders.stream().filter(v -> !v.equals(valueHeader.get())).findAny();
            if (labelHeader.isEmpty()) return;
            final ArrayList<String> values = new ArrayList<>();
            final ArrayList<String> labels = new ArrayList<>();
            tableData.values().forEach(td -> {
                final Map<String, ReportOutputData> data = td.stream().collect(Collectors.toMap(ReportOutputData::getKey, rd -> rd));
                labels.add(data.get(labelHeader.get()).getValue());
                values.add(data.get(valueHeader.get()).getValue());
            });
            final var chartLabel = "Диаграмма " + selectedTab.getLabel();
            final var chartFlow = new ChartFlow(UUID.randomUUID().toString(), type, labels, values, valueHeader.get());
            tabSheet.add(chartLabel, chartFlow);
            final var notification = Notification.show(SUCCESS);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setPosition(Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            final var notification = Notification.show("Ошибка");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.setPosition(Notification.Position.TOP_CENTER);
        }
    }

    @Override
    public void setParameter(final BeforeEvent beforeEvent, final String s) {
        final var reportOutput = reportOutputService.findById(UUID.fromString(s));
        if (reportOutput.isEmpty()) return;
        title.setText(String.format("%s \"%s\"", getClass().getAnnotation(PageTitle.class).value(), reportOutput.get().getReport().getName()));
        final var reportOutputData = reportOutput.get().getData().stream().collect(Collectors.groupingBy(ReportOutputData::getQuery));
        pdfAnchor.setHref(new StreamResource(String.format("%s_%s.pdf", reportOutput.get().getReport().getName(), System.currentTimeMillis()), () -> pdfService.generateReportPDF(reportOutputData)));
        pdfAnchor.getElement().setAttribute("download", true);
        final var pdfImg = new Image(new StreamResource("pdf.png", () -> getClass().getResourceAsStream("/images/pdf.png")), "Скачать в pdf");
        pdfImg.setWidth("32px");
        final var pdfButton = new Button(pdfImg);
        pdfButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        pdfButton.setTooltipText("Скачать в pdf");
        pdfAnchor.add(pdfButton);
        xlsxAnchor.setHref(new StreamResource(String.format("%s_%s.xlsx", reportOutput.get().getReport().getName(), System.currentTimeMillis()), () -> xlsxService.generateReportXLSX(reportOutputData)));
        xlsxAnchor.getElement().setAttribute("download", true);
        final var xlsxImg = new Image(new StreamResource("xls.png", () -> getClass().getResourceAsStream("/images/xls.png")), "Скачать в xlsx");
        xlsxImg.setWidth("32px");
        final var xlsxButton = new Button(xlsxImg);
        xlsxButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        xlsxButton.setTooltipText("Скачать в xlsx");
        xlsxAnchor.add(xlsxButton);
        final var counter = new AtomicInteger();
        reportOutputData.forEach((k, v) -> {
            final var tabText = Optional.ofNullable(Strings.trimToNull(k.getSubReport())).orElse("Запрос " + counter.incrementAndGet());
            tabContent.put(tabText, v);
            tabSheet.add(tabText, createReportOutputDataTable(tabText, v));
        });
    }

}
