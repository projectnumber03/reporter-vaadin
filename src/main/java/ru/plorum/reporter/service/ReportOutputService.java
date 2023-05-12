package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.ReportOutput;
import ru.plorum.reporter.repository.ReportOutputRepository;

import java.util.*;

@Service
@AllArgsConstructor
public class ReportOutputService {

    private final ReportOutputRepository reportOutputRepository;

    public void save(final ReportOutput reportOutput) {
        if (Objects.isNull(reportOutput)) return;
        reportOutputRepository.save(reportOutput);
    }

    public Optional<ReportOutput> findById(final UUID id) {
        if (Objects.isNull(id)) return Optional.empty();
        return reportOutputRepository.findByIdWithData(id);
    }

    public List<ReportOutput> findByReport(final Report report) {
        if (Objects.isNull(report)) return Collections.emptyList();
        return reportOutputRepository.findAllByReportOrderByCreatedAtDesc(report);
    }

    public Optional<ReportOutput> findFirstByReport(final Report report) {
        if (Objects.isNull(report)) return Optional.empty();
        return Optional.ofNullable(reportOutputRepository.findFirstByReportOrderByCreatedAtDesc(report))
                .map(ReportOutput::getId)
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

}
