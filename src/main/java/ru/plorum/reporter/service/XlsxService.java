package ru.plorum.reporter.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.plorum.reporter.model.Query;
import ru.plorum.reporter.model.ReportOutputData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XlsxService extends ReportWriter {

    //Конкретный лист, с которым идет работа
    @NonFinal
    XSSFSheet currentSheet;

    //Книга excel
    @NonFinal
    XSSFWorkbook wb;

    @Override
    public void write(final String subReport) {
        nextSheet(subReport);
        var counter = new AtomicInteger();
        var row = currentSheet.createRow(counter.getAndIncrement());
        if (CollectionUtils.isEmpty(data)) return;
        final var firstRow = data.values().iterator().next();
        writeRow(row, firstRow.stream().map(ReportOutputData::getKey).collect(Collectors.toList()));
        for (var ro : data.values()) {
            row = currentSheet.createRow(counter.getAndIncrement());
            writeRow(row, ro.stream().map(ReportOutputData::getValue).collect(Collectors.toList()));
        }
        for (int i = 0; i < currentSheet.getRow(0).getPhysicalNumberOfCells(); i++) {
            currentSheet.autoSizeColumn(i);
        }
    }

    //Метод для переключения листа
    @Override
    void nextSheet(final String subReport) {
        currentSheet = StringUtils.isEmpty(subReport) ? wb.createSheet() : wb.createSheet(subReport);
    }

    //Метод записи строки из массива строк
    @Override
    void writeRow(final Object row, final List<String> data) {
        if (row instanceof XSSFRow) {
            IntStream.range(0, data.size()).forEach(i -> setCellValue(((XSSFRow) row).createCell(i), data.get(i)));
        }
    }

    private void setCellValue(final XSSFCell cell, final String value) {
        try {
            cell.setCellValue(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            cell.setCellValue(value);
        }
    }

    public InputStream generateReportXLSX(final Map<Query, List<ReportOutputData>> data) {
        try {
            wb = new XSSFWorkbook();
            data.forEach((q, ro) -> {
                setData(
                        ro.stream()
                                .sorted(Comparator.comparing(ReportOutputData::getRowNumber))
                                .collect(Collectors.groupingBy(ReportOutputData::getRowNumber))
                );
                write(q.getSubReport());
            });
            try (final var out = new ByteArrayOutputStream()) {
                wb.write(out);
                return new ByteArrayInputStream(out.toByteArray());
            } finally {
                wb.close();
            }
        } catch (Exception e) {
            log.error("Ошибка генерации отчета в xlsx");
            e.printStackTrace();
            return InputStream.nullInputStream();
        }
    }

}
