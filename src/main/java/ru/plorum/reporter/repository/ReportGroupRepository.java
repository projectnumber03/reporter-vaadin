package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.ReportGroup;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ReportGroupRepository extends JpaRepository<ReportGroup, UUID> {

    ReportGroup findReportGroupById(final UUID id);

    ReportGroup findDistinctByReportsIn(final Collection<Report> reports);

    Optional<ReportGroup> findReportGroupByName(final String name);

}
