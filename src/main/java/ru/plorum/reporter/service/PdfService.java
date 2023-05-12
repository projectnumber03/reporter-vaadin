package ru.plorum.reporter.service;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.VerticalAlignment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Resources;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.Query;
import ru.plorum.reporter.model.ReportOutputData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfService extends ReportWriter {

    @NonFinal
    PDDocument document;

    @NonFinal
    PDPage currentPage;

    @Override
    @SneakyThrows
    public void write(final String subReport) {
        final var timesNewRomanFont = PDType0Font.load(document, Resources.getInputStream("Times_New_Roman.ttf"));
        try (final var cos = new PDPageContentStream(document, currentPage)) {
            final float margin = 30;
            final float yStartNewPage = currentPage.getMediaBox().getHeight() - (2 * margin);
            final float tableWidth = currentPage.getMediaBox().getWidth() - (2 * margin);
            final var table = new BaseTable(800, yStartNewPage, 70, tableWidth, margin, document, currentPage, true, true);
            final var headerRow = table.createRow(30);
            final var cell = headerRow.createCell(100, subReport);
            cell.setFont(timesNewRomanFont);
            cell.setFontSize(18);
            cell.setValign(VerticalAlignment.MIDDLE);
            cell.setAlign(HorizontalAlignment.CENTER);
            table.addHeaderRow(headerRow);
            writeHeader(table);
            data.values().forEach(r -> writeRow(table, r.stream().map(ReportOutputData::getValue).toList()));
            table.draw();
        }
    }

    @Override
    void nextSheet(final String subReport) {
        currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
    }

    @Override
    @SneakyThrows
    void writeRow(final Object table, final List<String> data) {
        final var timesNewRomanFont = PDType0Font.load(document, Resources.getInputStream("Times_New_Roman.ttf"));
        final var row = ((BaseTable) table).createRow(20);
        data.forEach(c -> {
            Cell<PDPage> cell = row.createCell(100F / data.size(), c);
            cell.setFontSize(12);
            cell.setFont(timesNewRomanFont);
        });
    }

    @SneakyThrows
    private void writeHeader(final BaseTable table) {
        final var timesNewRomanFont = PDType0Font.load(document, Resources.getInputStream("Times_New_Roman.ttf"));
        final var row = table.createRow(20);
        if (CollectionUtils.isEmpty(data)) return;
        final var firstRow = data.values().iterator().next();
        firstRow.forEach(c -> {
            Cell<PDPage> cell = row.createCell(100F / firstRow.size(), c.getKey());
            cell.setFontSize(12);
            cell.setFont(timesNewRomanFont);
        });
        table.addHeaderRow(row);
    }

    @SneakyThrows
    public InputStream generateReportPDF(final Map<Query, List<ReportOutputData>> data) {
        document = new PDDocument();
        data.forEach((q, ro) -> {
            nextSheet(q.getSubReport());
            setData(
                    ro.stream()
                            .sorted(Comparator.comparing(ReportOutputData::getRowNumber))
                            .collect(Collectors.groupingBy(ReportOutputData::getRowNumber))
            );
            write(q.getSubReport());
        });
        try (final var out = new ByteArrayOutputStream()) {
            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());
        } finally {
            document.close();
        }
    }

}
