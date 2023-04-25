package ru.plorum.reporter.service;

import lombok.Setter;
import ru.plorum.reporter.model.ReportOutputData;

import java.util.List;
import java.util.Map;

public abstract class ReportWriter {

    //Данные, которые необходимо записать в лист
    @Setter
    Map<Integer, List<ReportOutputData>> data;

    public abstract void write(final String subReport);

    abstract void nextSheet(final String subReport);

    abstract void writeRow(final Object row, final List<String> data);

}
