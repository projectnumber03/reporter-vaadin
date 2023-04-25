package ru.plorum.reporter.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.PostConstruct;
import ru.plorum.reporter.component.pagination.PaginatedGrid;
import ru.plorum.reporter.model.ReportOutputData;
import ru.plorum.reporter.service.PdfService;
import ru.plorum.reporter.service.ReportOutputService;
import ru.plorum.reporter.service.XlsxService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.plorum.reporter.util.Constants.REPORT_OUTPUT;

@PageTitle(REPORT_OUTPUT)
@Route(value = "report_output", layout = MainView.class)
public class ReportOutputDataView extends AbstractView implements HasUrlParameter<String> {

    private final ReportOutputService reportOutputService;

    private final PdfService pdfService;

    private final XlsxService xlsxService;

    private final TabSheet tabSheet = new TabSheet();

    private final Anchor pdfAnchor = new Anchor();

    private final Anchor xlsxAnchor = new Anchor();

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
        horizontal.add(title, pdfAnchor, xlsxAnchor);
        horizontal.setAlignItems(Alignment.CENTER);
        setHeightFull();
        setWidthFull();
        vertical.setHeightFull();
        tabSheet.setHeightFull();
        tabSheet.setWidthFull();
        vertical.add(tabSheet);
        add(vertical);
    }

    private PaginatedGrid<List<ReportOutputData>> createReportOutputDataTable(final List<ReportOutputData> items) {
        final Grid<List<ReportOutputData>> grid = new Grid<>();
        final var tableData = items.stream()
                .sorted(Comparator.comparing(ReportOutputData::getRowNumber))
                .collect(Collectors.groupingBy(ReportOutputData::getRowNumber));
        final var firstRow = tableData.values().iterator().next();
        firstRow.forEach(column -> grid.addColumn(ro -> ro.get(firstRow.indexOf(column)).getValue()).setHeader(column.getKey()));
        final var paginatedGrid = new PaginatedGrid<>(grid);
        paginatedGrid.setItems(tableData.values());

        return paginatedGrid;
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
        pdfAnchor.add(pdfButton);
        xlsxAnchor.setHref(new StreamResource(String.format("%s_%s.xlsx", reportOutput.get().getReport().getName(), System.currentTimeMillis()), () -> xlsxService.generateReportXLSX(reportOutputData)));
        xlsxAnchor.getElement().setAttribute("download", true);
        final var xlsxImg = new Image(new StreamResource("xls.png", () -> getClass().getResourceAsStream("/images/xls.png")), "Скачать в xlsx");
        xlsxImg.setWidth("32px");
        final var xlsxButton = new Button(xlsxImg);
        xlsxButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        xlsxAnchor.add(xlsxButton);
        final var counter = new AtomicInteger();
        reportOutputData.forEach((k, v) -> tabSheet.add(Optional.ofNullable(k.getSubReport()).orElse("Запрос " + counter.incrementAndGet()), createReportOutputDataTable(v)));
    }

}
