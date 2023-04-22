package ru.plorum.reporter.service;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.ReportGroup;
import ru.plorum.reporter.repository.ReportGroupRepository;

import java.util.Collection;
import java.util.Objects;

@Service
@AllArgsConstructor
public class ReportGroupService {

    private final ReportGroupRepository repository;

    public void save(final ReportGroup reportGroup) {
        if (Objects.isNull(reportGroup)) return;
        repository.save(reportGroup);
    }

    @Nullable
    public ReportGroup findDistinctByReportsIn(final Collection<Report> reports) {
        if (CollectionUtils.isEmpty(reports)) return null;
        return repository.findDistinctByReportsIn(reports);
    }

}
